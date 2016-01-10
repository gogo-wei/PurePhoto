package com.gogowei.activity;


import java.util.List;

import com.gogowei.fragment.DisplayPhotoFragment;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class DisplayPhotoActivity extends BaseActivity{

	private ActionBar actionBar;
	private int mcurrentPosition;
	private List<String> mPhotoPaths;
	private static DisplayPhotoFragment mDisplayPhotoFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_display_photo);
		
		mcurrentPosition = getIntent().getIntExtra("position", 0);
		mPhotoPaths = getIntent().getStringArrayListExtra("paths");
		mDisplayPhotoFragment = DisplayPhotoFragment.getInstance(mPhotoPaths, mcurrentPosition);
		FragmentManager fm = getFragmentManager();
		fm.beginTransaction().replace(R.id.fragment_display, mDisplayPhotoFragment).commit();
	}

	public static void removeView(String path){
	mDisplayPhotoFragment.removeDisplayView(path);	
	}
	
	public void restoreActionBar() {
		actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setTitle(mPhotoPaths.get(mcurrentPosition).substring(mPhotoPaths.get(mcurrentPosition).lastIndexOf("/")+1));
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#9f000000")));
		actionBar.hide();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
			restoreActionBar();
			return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
