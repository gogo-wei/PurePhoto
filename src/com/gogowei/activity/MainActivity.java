package com.gogowei.activity;

import com.gogowei.fragment.NavigationDrawerFragment;
import com.gogowei.fragment.PlaceholderFragment;
import com.gogowei.utils.MyToast;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;

public class MainActivity extends BaseActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.container, PlaceholderFragment.getInstance(position + 1))
		.commit();
	}

	public void onSectionAttached(CharSequence title) {
			mTitle = title;
	}

	public void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items iin the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_camera:
			Intent intent = null;
			if(mTitle.equals(getString(R.string.title_video))){
				intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
			}else if(mTitle.equals(getString(R.string.title_photo))){
				intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			}
			startActivity(intent);
			break;
		
		}
		return super.onOptionsItemSelected(item);
	}

	private long lastTime = 0;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			long currentTime = System.currentTimeMillis();
			if(currentTime - lastTime > 1000){
				lastTime = currentTime;
				MyToast.showShort(MainActivity.this, "再按一次退出应用");
			}else{
				MainActivity.this.finish();
			}
		}
		return false;
	}
}
