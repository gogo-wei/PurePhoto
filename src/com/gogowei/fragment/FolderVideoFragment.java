package com.gogowei.fragment;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gogowei.activity.FolderActivity;
import com.gogowei.activity.R;
import com.gogowei.adapter.FolderPhotoAdapter;
import com.gogowei.adapter.MainPhotoAdapter;
import com.gogowei.bean.FolderBean;
import com.gogowei.bean.PhotoBean;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaDataSource;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

public class FolderVideoFragment extends Fragment {

	private GridView mPhotoList;
	private List<PhotoBean> mDatas = new ArrayList<PhotoBean>();
	private FolderPhotoAdapter mAdapter;
	private ProgressDialog mProgressDialog;
	private String mPath = "";
	private String mTitle;
	private static FolderVideoFragment mFragment;
	
	public static FolderVideoFragment getInstance(String path) {
		if(mFragment == null){
			mFragment = new FolderVideoFragment();
		}
		mFragment.mPath = path;
		mFragment.mTitle = path.substring(path.lastIndexOf("/")+1);
		return mFragment;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((FolderActivity) activity).onSectionAttached(mTitle);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_folder_photo, container, false);
		
		initDatas();
		mPhotoList = (GridView) view.findViewById(R.id.gv_photo);
		mAdapter = new FolderPhotoAdapter(getActivity(), mDatas);
		mPhotoList.setAdapter(mAdapter);

		mPhotoList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String path = mDatas.get(position).getPath();
				Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.parse(path),"video/mp4");
				startActivity(intent);
			}
		});
		return view;
	}
	/**
	 * 扫描指定路径下所有视频
	 */
	private void initDatas() {
		mProgressDialog = ProgressDialog.show(getActivity(), null, "加载中...");
		new Thread(){
			public void run() {
				mFragment.mDatas.clear();
				getPhotoFromFolder();
				mProgressDialog.dismiss();
				
				getActivity().runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						if(mAdapter!=null){
							mAdapter.notifyDataSetChanged();
						}
					}
				});
			};
		}.start();
	}
	
	/**
	 * 从指定文件夹中获取所有图片
	 */
	private void getPhotoFromFolder() {
		File file = new File(mPath);
		if(file.exists()){
			File[] photoFiles = file.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String filename) {
					if(filename.endsWith(".mp4") || filename.endsWith(".3gp")){
						return true;
					}
					return false;
				}
			});
			
			for(File photoFile : photoFiles){
				PhotoBean bean = new PhotoBean();
				bean.setPath(photoFile.getAbsolutePath());
//				bean.setSize(photoFile.getTotalSpace());
//				bean.setLastModifyTime(photoFile.get);
				mDatas.add(bean);
			}
		}
	}
	
}
