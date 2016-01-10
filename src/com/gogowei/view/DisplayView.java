package com.gogowei.view;

import java.io.Serializable;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;

/**
 * 图片展示控件，支持双击缩放，手势缩放、平移等操作
 * 
 * @author Final
 *
 */
public class DisplayView extends ImageView
		implements OnGlobalLayoutListener, OnScaleGestureListener, OnTouchListener, Serializable {
	private boolean mOnce;
	private float mInitScale;
	private float mMidScale;
	private float mMaxScale;
	private Matrix mMatrix;
	/**
	 * 捕获用户多点触控时缩放比例
	 */
	private ScaleGestureDetector mScaleGestureDetector;

	// 上一次操作的触点数和中心点坐标
	private int mLastPointerCount;
	private float mLastX;
	private float mLastY;
	/**
	 * 触发位移的最小距离
	 */
	private int mTouchSlop;
	private boolean mIfCanDrag;

	private GestureDetector mGestureDetector;
	/**
	 * 是否正在缩放
	 */
	private boolean isAutoScaling;
	private OnSingleTapListener mSingleTapListener;

	public interface OnSingleTapListener {
		void onSingleTapConfirm();
	};

	public void setOnSingleTapListener(OnSingleTapListener singleTapListener) {
		this.mSingleTapListener = singleTapListener;
	}

	public DisplayView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DisplayView(Context context) {
		this(context, null);
	}

	public DisplayView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		super.setScaleType(ScaleType.MATRIX);
		mMatrix = new Matrix();
		mScaleGestureDetector = new ScaleGestureDetector(context, this);
		this.setOnTouchListener(this);
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

			@Override
			public boolean onDoubleTap(MotionEvent e) {
				if (isAutoScaling)
					return true;// 如果正在缩放则不允许双击缩放操作
				float x = e.getX();
				float y = e.getY();
				if (getScale() < mMidScale) {
					// 开始放大
					postDelayed(new AutoScaleRunnable(mMidScale, x, y), 16);
					isAutoScaling = true;
				} else {
					// 开始缩小
					postDelayed(new AutoScaleRunnable(mInitScale, x, y), 16);
					isAutoScaling = true;
				}
				return true;
			}

			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if (mSingleTapListener != null) {
					mSingleTapListener.onSingleTapConfirm();
				}
				return true;
			}

		});
	}

	private class AutoScaleRunnable implements Runnable {
		private float targetScale;// 缩放的目标值
		// 缩放中心点坐标
		private float x;
		private float y;
		// 每次缩放的比例
		private final float BIGGER = 1.07f;
		private final float SMALLER = 0.93f;
		private float tmpScale;

		public AutoScaleRunnable(float targetScale, float x, float y) {
			this.targetScale = targetScale;
			this.x = x;
			this.y = y;
			if (getScale() < targetScale) {
				tmpScale = BIGGER;
			}
			if (getScale() > targetScale) {
				tmpScale = SMALLER;
			}
		}

		public void run() {
			mMatrix.postScale(tmpScale, tmpScale, x, y);
			checkBorderAndCenterWhenScale();
			setImageMatrix(mMatrix);
			float currentScale = getScale();
			if ((tmpScale > 1.0f && currentScale < targetScale) || (tmpScale < 1.0f && currentScale > targetScale)) {
				// 若未达到目标值每隔16毫秒缩放一次
				postDelayed(this, 16);
			} else {
				float scale = targetScale / currentScale;
				mMatrix.postScale(scale, scale, x, y);
				checkBorderAndCenterWhenScale();
				setImageMatrix(mMatrix);
				isAutoScaling = false;
			}
		}
	};

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		// 注册global监听
		getViewTreeObserver().addOnGlobalLayoutListener(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		getViewTreeObserver().removeGlobalOnLayoutListener(this);
	}

	/**
	 * 获取imageview加载完成的图片
	 */
	@Override
	public void onGlobalLayout() {
		// 全局布局完成后调用
		if (!mOnce) {
			mOnce = true;
			// 得到控件宽高
			int width = getWidth();
			int height = getHeight();

			// 得到图片以及宽和高
			Drawable drawable = getDrawable();
			if (drawable == null) {
				return;
			}
			int dw = drawable.getIntrinsicWidth();
			int dh = drawable.getIntrinsicHeight();

			float scale = 1.0f;
			// 初始时保证图片不超出控件边界
			if (dw > width && dh < height) {
				scale = width * 1.0f / dw;
			}
			if (dw < width && dh > height) {
				scale = height * 1.0f / dh;
			}
			if ((dw > width && dh > height) || (dw < width && dh < height)) {
				scale = Math.min(width * 1.0f / dw, height * 1.0f / dh);
			}

			// 得到初始化时缩放的比例
			mInitScale = scale;
			mMidScale = mInitScale * 2;
			mMaxScale = mInitScale * 4;

			// 将图片移动至控件的中心位置
			float dx = (width - dw) * 1.0f / 2;
			float dy = (height - dh) * 1.0f / 2;

			mMatrix.postScale(mInitScale, mInitScale, dw / 2, dh / 2);
			mMatrix.postTranslate(dx, dy);

			setImageMatrix(mMatrix);
		}
	}

	/**
	 * 获取当前图片的缩放值
	 * 
	 * @return
	 */
	private float getScale() {
		float[] values = new float[9];
		mMatrix.getValues(values);
		return values[Matrix.MSCALE_X];
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		float scale = getScale();
		float scaleFactor = detector.getScaleFactor();
		if (getDrawable() == null) {
			return true;
		}
		if ((scale < mMaxScale && scaleFactor > 1.0f) || (scale > mInitScale && scaleFactor < 1.0f)) {

			if (scale * scaleFactor < mInitScale) {
				scaleFactor = mInitScale / scale;
			}
			if (scale * scaleFactor > mMaxScale) {
				scaleFactor = mMaxScale / scale;
			}
			mMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
			checkBorderAndCenterWhenScale();
			setImageMatrix(mMatrix);
		}
		return true;
	}

	/**
	 * 缩放时控制边界以及位置，保证图片不脱离控件边界
	 */
	private void checkBorderAndCenterWhenScale() {
		RectF rectF = getMatrixRectF();
		float offsetX = 0;
		float offsetY = 0;
		// 图片宽度或高度大于控件时，可能需要平移的距离
		if (rectF.width() >= getWidth()) {
			if (rectF.left > 0) {
				offsetX = -rectF.left;
			}
			if (rectF.right < getWidth()) {
				offsetX = getWidth() - rectF.right;
			}
		}
		if (rectF.height() >= getHeight()) {
			if (rectF.top > 0) {
				offsetY = -rectF.top;
			}
			if (rectF.bottom < getHeight()) {
				offsetY = getHeight() - rectF.bottom;
			}
		}
		if (rectF.width() < getWidth()) {
			offsetX = getWidth() / 2 - rectF.width() / 2 - rectF.left;
		}
		if (rectF.height() < getHeight()) {
			offsetY = getHeight() / 2 - rectF.height() / 2 - rectF.top;
		}
		mMatrix.postTranslate(offsetX, offsetY);
	}

	/**
	 * 获取图片缩放后的范围
	 * 
	 * @return
	 */
	private RectF getMatrixRectF() {
		Matrix matrix = mMatrix;
		Drawable drawable = getDrawable();
		RectF rectF = new RectF();
		if (drawable != null) {
			rectF.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			matrix.mapRect(rectF);
		}
		return rectF;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		// 返回true
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (mGestureDetector.onTouchEvent(event)) {
			// 双击时避免触发移动操作
			return true;
		}
		mScaleGestureDetector.onTouchEvent(event);
		// 触控点个数
		int pointCount = event.getPointerCount();
		float x = 0;
		float y = 0;
		for (int i = 0; i < pointCount; i++) {
			x += event.getX(i);
			y += event.getY(i);
		}
		// 触控中心点得X,Y坐标
		x /= pointCount;
		y /= pointCount;

		if (mLastPointerCount != pointCount) {
			mIfCanDrag = false;
			mLastX = x;
			mLastY = y;
			mLastPointerCount = pointCount;
		}

		RectF rectF = getMatrixRectF();
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			if (rectF.width() > getWidth() || rectF.height() > getHeight()) {
				if (getParent() instanceof ViewPager)
					getParent().requestDisallowInterceptTouchEvent(true);
			}

			float dx = x - mLastX;
			float dy = y - mLastY;

			if (!mIfCanDrag) {
				mIfCanDrag = isMoveAction(dx, dy);
			}

			if (mIfCanDrag) {
				if (getDrawable() != null) {
					// 如果图片宽度小于控件宽度，不允许横向移动
					if (rectF.width() <= getWidth()) {
						dx = 0;
					}
					// 如果图片高度小于控件高度，不允许纵向移动
					if (rectF.height() <= getHeight()) {
						dy = 0;
					}
					mMatrix.postTranslate(dx, dy);
					checkBorderAndCenterWhenTranslate();
					setImageMatrix(mMatrix);
				}
			}
			mLastX = x;
			mLastY = y;
			break;

		case MotionEvent.ACTION_DOWN:
			if (rectF.width() > getWidth() || rectF.height() > getHeight()) {
				if (getParent() instanceof ViewPager)
					getParent().requestDisallowInterceptTouchEvent(true);
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			mLastPointerCount = 0;
			break;
		}

		// 返回true
		return true;
	}

	/**
	 * 移动时控制边界以及位置
	 */
	private void checkBorderAndCenterWhenTranslate() {
		RectF rectF = getMatrixRectF();
		float offsetX = 0;
		float offsetY = 0;

		// 图片宽度或高度大于控件时，可能需要平移的距离
		if (rectF.width() > getWidth()) {
			if (rectF.left > 0) {
				offsetX = -rectF.left;
			}
			if (rectF.right < getWidth()) {
				offsetX = getWidth() - rectF.right;
			}
		}
		if (rectF.height() > getHeight()) {
			if (rectF.top > 0) {
				offsetY = -rectF.top;
			}
			if (rectF.bottom < getHeight()) {
				offsetY = getHeight() - rectF.bottom;
			}
		}

		mMatrix.postTranslate(offsetX, offsetY);
	}

	/**
	 * 判断是否足以触发移动
	 * 
	 * @param dx
	 * @param dy
	 * @return
	 */
	private boolean isMoveAction(float dx, float dy) {
		return Math.sqrt(dx * dx + dy * dy) > mTouchSlop;
	}
	/**
	 * 将图片顺时针旋转90度
	 */
	public void rotateImg() {
		mMatrix.postRotate(90, getWidth() / 2, getHeight() / 2);
		mMatrix.postScale(1.0f/mInitScale, 1.0f/mInitScale, getWidth()/2, getHeight()/2);
		setImageMatrix(mMatrix);
		initImg();
	}

	public void initImg() {
		// 得到控件宽高
		int width = getWidth();
		int height = getHeight();

		// 得到图片以及宽和高
		Drawable drawable = getDrawable();
		if (drawable == null) {
			return;
		}
		int dw = drawable.getIntrinsicWidth();
		int dh = drawable.getIntrinsicHeight();

		float scale = 1.0f;
		// 初始时保证图片不超出控件边界
		if (dh > width && dw < height) {
			scale = width * 1.0f / dh;
		}
		if (dh < width && dw > height) {
			scale = height * 1.0f / dw;
		}
		if ((dh > width && dw > height) || (dh < width && dw < height)) {
			scale = Math.min(width * 1.0f / dh, height * 1.0f / dw);
		}

		// 将图片移动至控件的中心位置
		float dx = (width - dh) * 1.0f / 2;
		float dy = (height - dw) * 1.0f / 2;
		mInitScale = scale;
		mMatrix.postScale(scale, scale, width / 2, height / 2);
//		mMatrix.postTranslate(dx, dy);

		setImageMatrix(mMatrix);
		
		Log.e("tag", "new图宽"+getDrawable().getIntrinsicWidth());
	}
}
