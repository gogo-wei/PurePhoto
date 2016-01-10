package com.gogowei.fragment;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gogowei.activity.FolderActivity;
import com.gogowei.activity.R;
import com.gogowei.adapter.MainPhotoAdapter;
import com.gogowei.adapter.MainVideoAdapter;
import com.gogowei.bean.FolderBean;
import com.gogowei.utils.MyToast;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

public class MainVideoFragment extends PlaceholderFragment {

	private GridView mPhotoList;
	private List<FolderBean> mDatas = new ArrayList<FolderBean>();
	private MainVideoAdapter mAdapter;
	private ProgressDialog mProgressDialog;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_main, container, false);
		
		initDatas();
		mPhotoList = (GridView) view.findViewById(R.id.gv_photo);
		mAdapter = new MainVideoAdapter(getActivity(), mDatas);
		mPhotoList.setAdapter(mAdapter);
		
		mPhotoList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				FolderBean bean = mDatas.get(position);
				Intent intent = new Intent(getActivity(),FolderActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("videoPath", bean.getDir());
				startActivity(intent);
			}
		});
		return view;
	}
	/**
	 * 扫描获取所有视频数据
	 */
	private void initDatas() {
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			MyToast.showShort(getActivity(), "当前存储卡不可用！");
			return;
		}
		mProgressDialog = ProgressDialog.show(getActivity(), null, "正在加载...");
		new Thread(){
			@Override
			public void run() {
				getAllPhotos();
				mProgressDialog.dismiss();
				getActivity().runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						if(mAdapter!=null){
							mAdapter.notifyDataSetChanged();
						}
					}
				});
			}
		}.start();
	}
	/**
	 * 获取所有视频数据
	 */
	private void getAllPhotos() {
		Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		ContentResolver cr = getActivity().getContentResolver();
		Cursor cursor = cr.query(uri, null, 
			MediaStore.Video.Media.MIME_TYPE+"=? or " + MediaStore.Video.Media.MIME_TYPE+"=?",
			new String[]{"video/mp4", "video/3gpp"}, MediaStore.Images.Media.DATE_MODIFIED);
	
		Set<String> parentDirs = new HashSet<String>();
		while(cursor.moveToNext()){
			String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
			File parentFile = new File(path).getParentFile();
			if(parentFile == null){
				continue;
			}
			String parentDir = parentFile.getAbsolutePath();
			
			if(parentDirs.contains(parentDir)){
				continue;
			}
			parentDirs.add(parentDir);
			
			FolderBean bean = new FolderBean();
			bean.setDir(parentDir);
			bean.setFirstImgPath(path);
			
			if(parentFile.list() == null){
				continue;
			}
			
			int size = parentFile.list(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String filename) {
					if(filename.endsWith(".mp4") || filename.endsWith(".3gp")){
						return true;
					}
					return false;
				}
			}).length;
			
			bean.setCount(size);
			mDatas.add(bean);
		}
		cursor.close();
	}
}
