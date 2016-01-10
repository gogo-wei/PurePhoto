package com.gogowei.utils;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore.Images.Thumbnails;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

/**
 * 图片异步加载工具类
 * @author Final
 *
 */
public class ImageLoader {

	private static ImageLoader mInstance;
	/**
	 * 图片缓存
	 */
	private LruCache<String, Bitmap> mLruCache;
	/**
	 * 线程池
	 */
	private ExecutorService mThreadPool;
	/**
	 * 默认线程数
	 */
	private static final int DEFAULT_THREAD_COUNT = 3;
	/**
	 * 任务列表
	 */
	private LinkedList<Runnable> mTaskQuene;
	/**
	 * 轮询线程
	 */
	private Thread mLoopThread;
	private Handler mLoopHandler;
	
	private Handler mUIHandler;
	/**
	 * 调度方式
	 */
	private Type mType = Type.LIFO;
	
	private Semaphore mSemaphoreThreadPool;
	private Semaphore mSemaphoreLoopHandler = new Semaphore(0);
	private final int ADD_NEW_TASK = 0X01;
	public enum Type{
		FIFO, LIFO ; 
	}
	private ImageLoader(){}
	private ImageLoader(int threadCount, Type type){
		init(threadCount, type);
	}
	/**
	 * 初始化实例对象
	 * @param threadCount 最大线程数
	 * @param type 调度方式
	 */
	private void init(int threadCount, Type type) {
		mType = type;
		mThreadPool = Executors.newFixedThreadPool(threadCount);
		mSemaphoreThreadPool = new Semaphore(threadCount);
		mTaskQuene = new LinkedList<Runnable>();
		initLruCache();
		initLooper();
		initUIHandler();
	}
	/**
	 * 初始化图片缓存
	 */
	private void initLruCache() {
		//获取应用最大支持内存
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int cacheMemory = maxMemory/8;
		mLruCache = new LruCache<String, Bitmap>(cacheMemory){
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight();
			}
		};
		
	}
	
	/**
	 * 初始化轮询机制
	 */
	private void initLooper() {
		mLoopThread = new Thread(){
			@Override
			public void run() {
				Looper.prepare();
				mLoopHandler = new Handler(){
					@Override
					public void handleMessage(Message msg) {
						//取出一个任务执行
						mThreadPool.execute(getTask());
						try {
							mSemaphoreThreadPool.acquire();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				};
				mSemaphoreLoopHandler.release();
				Looper.loop();
			}
		};
		mLoopThread.start();
	}
	/**
	 * 初始化UI通知内容
	 */
	private void initUIHandler() {
		if(mUIHandler == null){
			mUIHandler = new Handler(){
				@Override
				public void handleMessage(Message msg) {
					//获取图片，为imageView 设置图片
					ImageHolder holder = (ImageHolder) msg.obj;
					ImageView imageView = holder.imageView;
					String path = holder.path;
					Bitmap bitmap = holder.bitmap;
					
					//比较tag和path,避免因复用造成的图片错乱问题
					if(imageView.getTag().toString().equals(path)){
						imageView.setImageBitmap(bitmap);
					}
					
				}
			};
		}
	}
	/**
	 * 获取实例对象
	 * @return
	 */
	public static ImageLoader getInstance(int threadCount, Type type){
		if(mInstance == null){
			synchronized (ImageLoader.class) {
				if(mInstance == null){
					mInstance = new ImageLoader(threadCount, type);
				}
			}
		}
		return mInstance;
	}
	
	/**
	 * 获取实例对象
	 * @return
	 */
	public static ImageLoader getInstance(){
		if(mInstance == null){
			synchronized (ImageLoader.class) {
				if(mInstance == null){
					mInstance = new ImageLoader(DEFAULT_THREAD_COUNT, Type.LIFO);
				}
			}
		}
		return mInstance;
	}
	/**
	 * 根据path,为imageView 设置图片
	 * @param path
	 * @param imageView
	 */
	public void loadImage(final String path, final ImageView imageView){
		imageView.setTag(path);

		Bitmap bitmap = getBitmapFromLruCache(path);
		if(bitmap != null){
			refreshBitmap(path,imageView,bitmap);
		}else{
			addTask(new Runnable() {
				@Override
				public void run() {
				//获取图片需要显示的大小
				ImageSize imageSize	= getImageViewSize(imageView);
				//压缩图片
				Bitmap bm = decodeSampleBitmapFromPath(path, imageSize.width, imageSize.height);
				//将图片加入缓存
				addBitmapToLruCache(path, bm);
				refreshBitmap(path,imageView,bm);
				mSemaphoreThreadPool.release();
				}
			});
		}
	}
	
	/**
	 * 根据video的path,为imageView 设置video的第一帧图片
	 * @param path
	 * @param imageView
	 */
	public void loadVideoImage(final String path, final ImageView imageView){
		imageView.setTag(path);

		Bitmap bitmap = getBitmapFromLruCache(path);
		if(bitmap != null){
			refreshBitmap(path,imageView,bitmap);
		}else{
			addTask(new Runnable() {
				@Override
				public void run() {
				//获取图片需要显示的大小
				ImageSize imageSize	= getImageViewSize(imageView);
				//获取视频第一帧图片，压缩图片
				Bitmap bm = getVideoThumbnail(path, imageSize.width, imageSize.height);
				//将图片加入缓存
				addBitmapToLruCache(path, bm);
				refreshBitmap(path,imageView,bm);
				mSemaphoreThreadPool.release();
				}
			});
		}
	}
	/**
	 * 将图片加入缓存
	 * @param path
	 * @param bm
	 */
	protected void addBitmapToLruCache(String path, Bitmap bm) {
		if(getBitmapFromLruCache(path) == null){
			if(bm != null){
				mLruCache.put(path, bm);
			}
		}
	}
	/**
	 * 通知UI线程设置图片
	 * @param path
	 * @param imageView
	 * @param bitmap
	 */
	private void refreshBitmap(String path, ImageView imageView, Bitmap bitmap) {
		Message msg = Message.obtain();
		ImageHolder holder = new ImageHolder();
		holder.path = path;
		holder.imageView = imageView;
		holder.bitmap = bitmap;
		msg.obj = holder;
		mUIHandler.sendMessage(msg);
	}
	/**
	 * 根据path在缓存中获取bitmap
	 * @param path
	 */
	private Bitmap getBitmapFromLruCache(String path) {
		return mLruCache.get(path);
	}
	/**
	 *向任务队列中添加任务
	 * @param runnable
	 */
	private void addTask(Runnable runnable) {
		mTaskQuene.add(runnable);
		try {
			if(mLoopHandler == null){
				mSemaphoreLoopHandler.acquire();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mLoopHandler.sendEmptyMessage(ADD_NEW_TASK);
	}
	/**
	 * 从任务队列取出一个任务
	 * @return
	 */
	private Runnable getTask() {
		if(mType == Type.FIFO){
			return mTaskQuene.removeFirst();
		}else if(mType == Type.LIFO){
			return mTaskQuene.removeLast();
		}
		return null;
	}
	/**
	 * 根据imageView 获取需要压缩的宽和高
	 * @param imageView
	 * @return
	 */
	private ImageSize getImageViewSize(ImageView imageView) {
		ImageSize imageSize = new ImageSize();
		LayoutParams params = imageView.getLayoutParams();
		int width = imageView.getWidth();
		int height = imageView.getHeight();
		
		if(width <= 0){
			width = params.width;
		}
		if(width <= 0){
//			width = imageView.getMaxWidth(); API16以上
			width = getImageViewFieldValue(imageView, "mMaxWidth");
		}
		if(width <= 0){
			DisplayMetrics metrics = imageView.getContext().getResources().getDisplayMetrics();
			width = metrics.widthPixels;
		}
		
		if(height <= 0){
			height = params.height;
		}
		if(height <= 0){
//			height = imageView.getMaxHeight(); API16以上
			height = getImageViewFieldValue(imageView, "mMaxHeight");
		}
		if(height <= 0){
			DisplayMetrics metrics = imageView.getContext().getResources().getDisplayMetrics();
			height = metrics.heightPixels;
		}
		
		imageSize.width = width;
		imageSize.height = height;
		return imageSize;
	}
	/**
	 * 通过反射获取imageView给定的某个属性值
	 * @param imageView
	 * @param string
	 * @return
	 */
	private int getImageViewFieldValue(Object object, String fieldName) {
		int value = 0;
			try {
				Field field = object.getClass().getDeclaredField(fieldName);
				field.setAccessible(true);
				int fieldValue = field.getInt(object);
				if(fieldValue > 0 && fieldValue < Integer.MAX_VALUE){
					value = fieldValue;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		return value;
	}
	/**
	 * 根据图片需要显示的宽和高压缩图片
	 * @param path
	 * @param width
	 * @param height
	 * @return
	 */
	private Bitmap decodeSampleBitmapFromPath(String path, int reqWidth, int reqHeight) {
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		options.inSampleSize = caculateInSampleSize(options, reqWidth, reqHeight);
		
		options.inJustDecodeBounds = false;
		Bitmap bm = BitmapFactory.decodeFile(path, options);
		return bm;
	}
	/**
	 * 根据图品需要的宽和高和实际的宽和高计算sampleSize
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	private int caculateInSampleSize(Options options, int reqWidth, int reqHeight) {
		int width = options.outWidth;
		int height = options.outHeight;
		int sampleSize = 1;
		
		if(width > reqWidth || height > reqHeight){
			int widthRadio = Math.round(width/reqWidth);
			int heightRadio = Math.round(height/reqHeight);
			
			sampleSize = Math.max(widthRadio, heightRadio);
		}
		return sampleSize;
	}

	private class ImageHolder {
		String path;
		ImageView imageView;
		Bitmap bitmap;
	}
	private class ImageSize{
		int width;
		int height;
	}
	
	/**
	* 获取视频的第一帧图片
	* @param videoPath 视频的路径
	* @param reqWidth 指定输出视频图片的宽度
	* @param reqHeight 指定输出视频图片的高度
	* @return 指定大小的图片
	*/
	private Bitmap getVideoThumbnail(String videoPath, int reqWidth, int reqHeight) {
	Bitmap bitmap = null;
	// 获取视频的缩略图
//	bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, Thumbnails.MICRO_KIND);
	MediaMetadataRetriever media = new MediaMetadataRetriever();
	media.setDataSource(videoPath);
	bitmap = media.getFrameAtTime();
	bitmap = ThumbnailUtils.extractThumbnail(bitmap, reqWidth, reqHeight, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
	return bitmap;
	}
}
