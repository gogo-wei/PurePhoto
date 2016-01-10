package com.gogowei.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
/**
 * 播放幻灯片时展示图片的组件
 * 使图片居中显示，且缩放图片使图片边界不超过控件宽和高
 * @author Final
 *
 */
public class SlidesView extends ImageView implements OnGlobalLayoutListener {
	
	private boolean mOnce;
	private Matrix mMatrix;
	
	public SlidesView(Context context) {
		super(context);
		super.setScaleType(ScaleType.MATRIX);
		mMatrix = new Matrix();
	}

	
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
			//初始时保证图片不超出控件边界
			if (dw > width && dh < height) {
				scale = width * 1.0f / dw;
			}
			if (dw < width && dh > height) {
				scale = height * 1.0f / dh;
			}
			if ((dw > width && dh > height) || (dw < width && dh < height)) {
				scale = Math.min(width * 1.0f / dw, height * 1.0f / dh);
			}
			
			
			// 将图片移动至控件的中心位置
			float dx = (width - dw) * 1.0f/ 2;
			float dy = (height - dh) * 1.0f/ 2;

			mMatrix.postScale(scale, scale, dw / 2, dh / 2);
			mMatrix.postTranslate(dx, dy);

			setImageMatrix(mMatrix);
		}
	}
}
