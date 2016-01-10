package com.gogowei.activity;

import java.io.File;
import java.io.FileOutputStream;

import com.gogowei.utils.MyToast;
import com.gogowei.view.CropImageView;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;

public class CropPhotoActivity extends Activity implements OnClickListener {

	private ImageView mSaveBtn;
	private ImageView mCancelBtn;
	private CropImageView mCropView;
	private String mImgPath;
	public static final String SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SmartPhoto/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().hide();
		setContentView(R.layout.activity_crop_photo);

		mImgPath = getIntent().getStringExtra("path");
		findView();
		initEvent();
	}

	private void initEvent() {
		mSaveBtn.setOnClickListener(this);
		mCancelBtn.setOnClickListener(this);
	}

	private void findView() {
		mSaveBtn = (ImageView) findViewById(R.id.ib_save);
		mCancelBtn = (ImageView) findViewById(R.id.ib_cancel);
		mCropView = (CropImageView) findViewById(R.id.ci_cropView);

		Drawable drawable = Drawable.createFromPath(mImgPath);
		mCropView.setDrawable(drawable, 300, 300);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ib_cancel:
			finish();
			break;

		case R.id.ib_save:
			Bitmap bitmap = mCropView.getCropImage();
			String picName = System.currentTimeMillis() + ".png";
			saveBitmap(bitmap, picName);
			finish();
			MyToast.showShort(CropPhotoActivity.this, "已保存");
			break;
		}
	}

	/** 保存方法 */
	public void saveBitmap(Bitmap bm, String picName) {
		File destDir = new File(SAVE_PATH);
		  if (!destDir.exists()) {
		   destDir.mkdirs();
		  }
		  
		File f = new File(SAVE_PATH, picName);
		try {
			f.createNewFile();
			FileOutputStream out = new FileOutputStream(f);
			bm.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.flush();
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
