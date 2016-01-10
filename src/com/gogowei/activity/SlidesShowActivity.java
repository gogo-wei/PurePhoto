package com.gogowei.activity;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.gogowei.view.SlidesView;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.ViewGroup.LayoutParams;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
/**
 * 幻灯片播放界面
 * @author Final
 *
 */
public class SlidesShowActivity extends BaseActivity implements AnimatorListener {

	private FrameLayout mContainer;
	private ImageView mView;
	private int mIndex = 0;
	private String mFolderPath;
	private List<String> mPhotos = new ArrayList<String>();
	private Random mRandom;
	private PowerManager mPowerManager;
	private WakeLock mWakeLock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//设置屏幕常亮
		mPowerManager = (PowerManager) getSystemService(Activity.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "keep screen on");
		//设置全屏,隐藏actionBar
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getActionBar().hide();
		//获取需要播放的照片数据
		initDatas();
	}

	private void setContentView() {
		mContainer = new FrameLayout(this);
		mContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		mView = createNewView();
		mContainer.addView(mView);
		mRandom = new Random();
		setContentView(mContainer);
	}
	/**
	 * 返回一个准备好的图片组件
	 * @return
	 */
	private ImageView createNewView() {
		SlidesView ret = new SlidesView(this);
		ret.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		ret.setBackgroundColor(0xff000000);
		ret.setImageURI(Uri.parse(mPhotos.get(mIndex)));
		ret.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mIndex = (mIndex + 1 < mPhotos.size()) ? mIndex + 1 : 0;

		return ret;
	}

	/**
	 * 获取需要播放的照片数据
	 */
	private void initDatas() {
		//获取当前播放照片所在文件夹路径
		mFolderPath = getIntent().getStringExtra("folderPath");
		new Thread() {
			public void run() {
				mPhotos.clear();
				getPhotoFromFolder();
				runOnUiThread(new Runnable() {
					public void run() {
						//设置布局界面
						setContentView();
						//开始播放动画
						nextAnimation();
					}
				});
			};
		}.start();
	}

	/**
	 * 从指定文件夹中获取所有图片
	 */
	private void getPhotoFromFolder() {
		File file = new File(mFolderPath);
		if (file.exists()) {
			File[] photoFiles = file.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String filename) {
					if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png")) {
						return true;
					}
					return false;
				}
			});

			for (File photoFile : photoFiles) {
				String path = photoFile.getAbsolutePath();
				mPhotos.add(path);
			}
		}
	}
	/**
	 * 开始播放动画
	 */
	private void nextAnimation() {
        AnimatorSet anim = new AnimatorSet();
        final int index = mRandom.nextInt(2);

        switch (index) {
        case 0:
            anim.playTogether(
                    ObjectAnimator.ofFloat(mView, "scaleX", 1.5f, 1f),
                    ObjectAnimator.ofFloat(mView, "scaleY", 1.5f, 1f));
            break;

        case 1:
            anim.playTogether(ObjectAnimator.ofFloat(mView, "scaleX", 1, 1.5f),
                    ObjectAnimator.ofFloat(mView, "scaleY", 1, 1.5f));
            break;

        case 2:
            mView.setScaleX(1.5f);
            mView.setScaleY(1.5f);
            anim.playTogether(ObjectAnimator.ofFloat(mView, "translationY",
                    80f, 0f));
            break;

        case 3:
        default:
//            AnimatorProxy.wrap(mView).setScaleX(1.5f);
//            AnimatorProxy.wrap(mView).setScaleY(1.5f);
        	 mView.setScaleX(1.5f);
             mView.setScaleY(1.5f);
            anim.playTogether(ObjectAnimator.ofFloat(mView, "translationX", 0f,
                    40f));
            break;
        }

        anim.setDuration(3000);
        anim.addListener(this);
        anim.start();
    }

	@Override
	public void onAnimationStart(Animator animation) {
		// TODO Auto-generated method stub
		
	}

	@Override
    public void onAnimationEnd(Animator animator) {
		//每个照片播放完后设置播放下一张照片
        mContainer.removeView(mView);
        mView = createNewView();
        mContainer.addView(mView);
        nextAnimation();
    }

	@Override
	public void onAnimationCancel(Animator animation) {
		
	}

	@Override
	public void onAnimationRepeat(Animator animation) {
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mWakeLock.acquire();//activity显示时设置屏幕常亮
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mWakeLock.release();//activity暂停时取消屏幕常亮设置
	}
}
