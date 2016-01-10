package com.gogowei.fragment;

import java.io.File;

import com.gogowei.activity.CropPhotoActivity;
import com.gogowei.activity.DisplayPhotoActivity;
import com.gogowei.activity.R;
import com.gogowei.view.DisplayView;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * 图片操作选项区域
 * 
 * @author Final
 *
 */
public class ActionFragment extends Fragment implements OnClickListener {

	private LinearLayout mEditView;
	private LinearLayout mCutView;
	private LinearLayout mRotateView;
	private LinearLayout mShareView;
	private LinearLayout mDeleteView;
	private DisplayMetrics mDisplayMetrics;
	private DisplayView mView;
	private String mPath;
	
	public void setCurrentView(String path, DisplayView view){
		this.mPath = path;
		this.mView = view;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_action, container, false);
		mEditView = (LinearLayout) view.findViewById(R.id.ll_edit);
		mCutView = (LinearLayout) view.findViewById(R.id.ll_cut);
		mRotateView = (LinearLayout) view.findViewById(R.id.ll_rotate);
		mShareView = (LinearLayout) view.findViewById(R.id.ll_share);
		mDeleteView = (LinearLayout) view.findViewById(R.id.ll_delete);
		
		mDisplayMetrics = getActivity().getResources().getDisplayMetrics();
		
		mEditView.setOnClickListener(this);
		mCutView.setOnClickListener(this);
		mRotateView.setOnClickListener(this);
		mShareView.setOnClickListener(this);
		mDeleteView.setOnClickListener(this);
		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_edit:

			break;
		case R.id.ll_cut:
			Intent intent = new Intent(getActivity(), CropPhotoActivity.class);
			intent.putExtra("path", mPath);
			startActivity(intent);
			break;
		case R.id.ll_rotate:
			mView.rotateImg();
			break;
		case R.id.ll_share:
			Intent shareIntent = new Intent();
	        shareIntent.setAction(Intent.ACTION_SEND);
	        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(mPath));
	        shareIntent.setType("image/*");
	        startActivity(Intent.createChooser(shareIntent, "分享到"));
			break;
		case R.id.ll_delete:
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("提示").setMessage("确认要删除这张图片吗？").setPositiveButton("确认", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					deleteFile(mPath);
				}
				
			}).setNegativeButton("取消", null).show();
			break;
		}
	}
	/**
	 * 删除指定路径的文件
	 * @param filePath
	 */
	public void deleteFile(String filePath){
		File file = new File(filePath);
		if(file.exists() && file.isFile()){
			file.delete();
			((DisplayPhotoActivity)getActivity()).removeView(mPath);
		}
	}
}
