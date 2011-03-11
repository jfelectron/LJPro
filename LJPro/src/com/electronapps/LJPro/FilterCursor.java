package com.electronapps.LJPro;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.widget.AdapterView;

public class FilterCursor extends CursorWrapper {

	private Cursor mCursor;
	private String[] mExtras;
	private Resources mResources;
	private Integer mPosition;
	private Integer mLength;
	public FilterCursor(Cursor cursor,Context c, boolean havesyn) {
		super(cursor);
		mCursor=cursor;
		
		mResources=c.getResources();
		String [] noSyn={mResources.getString(R.string.allfriends),mResources.getString(R.string.starred),mResources.getString(R.string.jonly),mResources.getString(R.string.conly)};
		String [] Syn={mResources.getString(R.string.allfriends),mResources.getString(R.string.starred),mResources.getString(R.string.jonly),mResources.getString(R.string.conly),mResources.getString(R.string.sonly)};
		
		if (havesyn) mExtras=Syn;
		else mExtras=noSyn;
		mLength=mExtras.length;// TODO Auto-generated constructor stub
	}
	
	@Override
		public boolean moveToPosition(int i) {
		if (i<mLength) {
			mPosition=i;
			return true;
		}
		else {
			mPosition=i;
			return mCursor.moveToPosition(i-mLength);
		}
		
	}
	
	@Override
	public String getString(int id) {
		if (mPosition<mLength) return mExtras[mPosition];
		else return mCursor.getString(id);
		
	}
	
	@Override
	public long getLong(int id) {
		return 0l;
	}
	
	@Override
	public int getCount() {
		return mCursor.getCount()+mLength;
		
	}

}
