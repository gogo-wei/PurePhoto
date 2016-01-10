package com.gogowei.adapter;

import java.util.List;

import com.gogowei.activity.R;
import com.gogowei.bean.FolderBean;
import com.gogowei.bean.PhotoBean;
import com.gogowei.utils.ImageLoader;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class FolderVideoAdapter extends BaseAdapter{
	private Context mContext;
	private List<PhotoBean> mDatas;
	private LayoutInflater mInflater;
	
	public FolderVideoAdapter (Context context ,List<PhotoBean> datas){
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
			convertView = mInflater.inflate(R.layout.item_folder_photo, null);
			DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
			int height = metrics.heightPixels;
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, height/7);
			convertView.setLayoutParams(params);
			holder = new ViewHolder();
			holder.photoView = (ImageView) convertView.findViewById(R.id.iv_photo);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		PhotoBean bean = mDatas.get(position);
		
		holder.photoView.setImageResource(R.drawable.ic_launcher);
		
		ImageLoader.getInstance().loadVideoImage(bean.getPath(), holder.photoView);
		
		return convertView;
	}

	class ViewHolder{
		ImageView photoView;
	}
}
