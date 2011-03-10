package com.electronapps.LJPro;

import android.database.Cursor;
import android.database.CursorWrapper;

public class PostWrapper extends CursorWrapper {
	private Cursor mCursor;
	private int mIndex;
	
	public PostWrapper(Cursor cursor,int position) {
		super(cursor);
		mCursor=cursor;
		mIndex=position;
		// TODO Auto-generated constructor stub
	}
	
	@Override 
	public int getCount() {
		return 1;
	}
	
	@Override
	public void close() {
		//do nothing, the wrapped cursor is closed elsewhere
	}
	
	@Override
	public boolean moveToPosition(int i) {
		return mCursor.moveToPosition(mIndex);
		
	}
	
	public void setIndex(int i){
		mIndex=i;
	}
	
	

}
