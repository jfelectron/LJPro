package com.electronapps.LJPro;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.style.ImageSpan;

public class HTMLImageSpan extends ImageSpan {

	private int mWidth=-1;
	private int mHeight=-1;
	private int mMaxDim=-1;
	private Bitmap mBitmap;
	private String mSrc;
	private String mTitle;

	public HTMLImageSpan(Bitmap b,String src,int width, int height,int dSize) {
		super(b);
		mWidth=width;
		mHeight=height;
		mSrc=src;
		mMaxDim=dSize;
		mBitmap=b;
		// TODO Auto-generated constructor stub
	}

	public HTMLImageSpan(BitmapDrawable d,String src,int width, int height) {
		super(d);
		mWidth=width;
		mHeight=height;
		mSrc=src;
		mBitmap=d.getBitmap();
		// TODO Auto-generated constructor stub
	}
	
	public HTMLImageSpan(Bitmap d,String src,int width, int height) {
		super(d);
		mBitmap=d;
		mWidth=width;
		mHeight=height;
		// TODO Auto-generated constructor stub
	}
	
	
	
	public HTMLImageSpan(Parcel p,int position) {
		super(Bitmap.CREATOR.createFromParcel(p));
		p.setDataPosition(position);
		mBitmap=Bitmap.CREATOR.createFromParcel(p);
		mSrc=p.readString();
		mWidth=p.readInt();
		mHeight=p.readInt();
		mMaxDim=p.readInt();
		
	}

	public int getMaxDim() {
		return mMaxDim;
	}
	
	public int getHeight() {
		return mHeight;
	}
	
	public int getWidth() {
		return mWidth;
	}

	



	public int getSpanTypeId() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		mBitmap.writeToParcel(dest, flags);
		dest.writeString(mSrc);
		dest.writeInt(mWidth);
		dest.writeInt(mHeight);
		dest.writeInt(mMaxDim);
		

	}

	public void setSrc(String srcUrl) {
		mSrc=srcUrl;
		
	}
	
	public String getSrc(String srcUrl){
		return mSrc;
	}

	public void setTitle(String title) {
		mTitle = title;
		
	}
	
	public String getTitle() {
		return mTitle;
	}

}
