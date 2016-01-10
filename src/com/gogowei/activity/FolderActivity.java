package com.gogowei.activity;


import com.gogowei.fragment.FolderPhotoFragment;
import com.gogowei.fragment.FolderVideoFragment;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class FolderActivity extends BaseActivity{

	
	private CharSequence mTitle;
	private String mPhotoPath;
	private String mVideoPath;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_folder);
		
		mPhotoPath = getIntent().getStringExtra("photoPath");
		mVideoPath = getIntent().getStringExtra("videoPath");
		FragmentManager fm = getFragmentManager();
		
		if(mPhotoPath!=null){
			fm.beginTransaction().replace(R.id.container, FolderPhotoFragment.getInstance(mPhotoPath)).commit();
		}else if(mVideoPath!=null){
			fm.beginTransaction().replace(R.id.container, FolderVideoFragment.getInstance(mVideoPath)).commit();
		}
	}

	public void onSectionAttached(CharSequence title) {
			mTitle = title;
	}

	public void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
			restoreActionBar();
			return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
			
		case R.id.action_slides_show:
			Intent intent = new Intent(FolderActivity.this, SlidesShowActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("folderPath", mPhotoPath);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

}
