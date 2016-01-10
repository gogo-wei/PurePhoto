package com.gogowei.bean;

public class FolderBean {

	private String name;
	private String dir;
	private String firstImgPath;
	private int count;
	public String getName() {
		return name;
	}
	
	public String getDir() {
		return dir;
	}
	public void setDir(String dir) {
		this.dir = dir;
		int lastIndexOf = this.dir.lastIndexOf("/");
		this.name = this.dir.substring(lastIndexOf+1);
	}
	public String getFirstImgPath() {
		return firstImgPath;
	}
	public void setFirstImgPath(String firstImgPath) {
		this.firstImgPath = firstImgPath;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
	

	
}
