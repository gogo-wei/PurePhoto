package com.gogowei.adapter;

import java.util.List;

import com.gogowei.activity.MainActivity;
import com.gogowei.activity.R;
import com.gogowei.bean.FolderBean;
import com.gogowei.utils.ImageLoader;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MainVideoAdapter extends BaseAdapter{
	private Context mContext;
	private List<FolderBean> mDatas;
	private LayoutInflater mInflater;
	
	public MainVideoAdapter (Context context ,List<FolderBean> datas){
		mContext = context;
		this.mDatas = datas;
		this.mInflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		return mDatas.size();
	}

	@Override
	public Object getItem(int position) {
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.item_main_photo, null);
			DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
			int height = metrics.heightPixels;
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, height/6);
			convertView.setLayoutParams(params);
			holder = new ViewHolder();
			holder.photoView = (ImageView) convertView.findViewById(R.id.iv_photo);
			holder.fileName = (TextView) convertView.findViewById(R.id.tv_fileName);
			holder.photoNum = (TextView) convertView.findViewById(R.id.tv_photoNum);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		FolderBean bean = mDatas.get(position);
		
		String fileName = bean.getName();
		if(fileName.length() > 12){
			fileName = fileName.substring(0, 12) + "...";
		}
		holder.fileName.setText(fileName);
		holder.photoNum.setText(bean.getCount()+"");
		holder.photoView.setImageResource(R.drawable.ic_launcher);
		
		ImageLoader.getInstance().loadVideoImage(bean.getFirstImgPath(), holder.photoView);
		
		return convertView;
	}

	class ViewHolder{
		ImageView photoView;
		TextView fileName;
		TextView photoNum;
	}
}
