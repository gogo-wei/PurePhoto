package com.gogowei.fragment;

import com.gogowei.activity.MainActivity;
import com.gogowei.activity.R;

import android.app.Activity;
import android.app.Fragment;

public class PlaceholderFragment extends Fragment {

	public static final int TYPE_PHOTO = 0X01;
	public static final int TYPE_VIDEO = 0X02;
	private int sectionType;
	private static PlaceholderFragment mFragment;
	public static PlaceholderFragment getInstance(int sectionType) {
		switch (sectionType) {
		case TYPE_PHOTO:
			mFragment = new MainPhotoFragment();
			break;
			
		case TYPE_VIDEO:
			mFragment = new MainVideoFragment();
			break;
		}
		mFragment.sectionType = sectionType;
		return mFragment;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		switch (sectionType) {
		case TYPE_PHOTO:
			((MainActivity) activity).onSectionAttached(getString(R.string.title_photo));
			break;

		case TYPE_VIDEO:
			((MainActivity) activity).onSectionAttached(getString(R.string.title_video));
			break;
		}
	}

}
