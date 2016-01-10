package com.gogowei.fragment;

import java.util.List;

import com.gogowei.activity.R;
import com.gogowei.animation.DepthPageTransformer;
import com.gogowei.view.DisplayView;
import com.gogowei.view.DisplayView.OnSingleTapListener;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.Notification.Action;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DisplayPhotoFragment extends Fragment{
	public static int mCurrentPosition;
	public static List<String> mPhotoPaths;
	private DisplayView[] mDisplayViews;
	private ViewPager mPhotoPager;
	private PagerAdapter mAdapter;
	private static DisplayPhotoFragment mInstance;
	private ActionFragment mActionFragment;
	private ActionBar mActionBar;
	
	public static DisplayPhotoFragment getInstance(List<String> photoPahs, int position) {
		if(mInstance == null){
			mInstance = new DisplayPhotoFragment();
		}
		mInstance.mCurrentPosition = position;
		mInstance.mPhotoPaths = photoPahs;
		mInstance.mDisplayViews = new DisplayView[mPhotoPaths.size()];
		return mInstance;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_display_photo, container, false);
		final FragmentManager fm = getFragmentManager();
		mActionFragment = (ActionFragment) fm.findFragmentById(R.id.fragment_action);
		fm.beginTransaction().hide(mActionFragment).commit();
		mActionBar = getActivity().getActionBar(); 
		
		
		mPhotoPager = (ViewPager) view.findViewById(R.id.vp_photoPager);
		mAdapter = new PagerAdapter() {
			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				DisplayView displayView = new DisplayView(getActivity());
				displayView.setImageURI(Uri.parse(mPhotoPaths.get(position)));
				displayView.setOnSingleTapListener(new OnSingleTapListener() {
					
					@Override
					public void onSingleTapConfirm() {
						if(mActionFragment.isHidden()){
							fm.beginTransaction().show(mActionFragment).commit();
						}else{
							fm.beginTransaction().hide(mActionFragment).commit();
						}
						if(mActionBar.isShowing()){
							mActionBar.hide();
						}else{
							mActionBar.show();
						}
					}
				});
				
				
				container.addView(displayView);
				mDisplayViews[position] = displayView;
				if(position == mCurrentPosition){
					mActionFragment.setCurrentView(mPhotoPaths.get(mCurrentPosition), mDisplayViews[mCurrentPosition]);
				}
				return displayView;
			}
			@Override
			public void destroyItem(ViewGroup container, int position, Object object) {
				container.removeView(mDisplayViews[position]);
			}
			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}
			
			@Override
			public int getCount() {
				return mPhotoPaths.size();
			}
		};
		
		mPhotoPager.setAdapter(mAdapter);
		mPhotoPager.setCurrentItem(mCurrentPosition, true);
		mPhotoPager.setPageTransformer(true, new DepthPageTransformer());
//		mActionFragment.setView(mDisplayViews[mCurrentPosition]);
		mPhotoPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				mCurrentPosition = arg0;
				mActionBar.setTitle(mPhotoPaths.get(mCurrentPosition).substring(mPhotoPaths.get(mCurrentPosition).lastIndexOf("/")+1));
				mActionFragment.setCurrentView(mPhotoPaths.get(mCurrentPosition), mDisplayViews[mCurrentPosition]);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		return view;
	}
	
	public void removeDisplayView(String path){
		mPhotoPaths.remove(path);
		mAdapter.notifyDataSetChanged();
		
	}
}
