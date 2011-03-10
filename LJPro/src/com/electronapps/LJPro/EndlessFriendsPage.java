package com.electronapps.LJPro;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;

import com.commonsware.cwac.endless.EndlessAdapter;

public class EndlessFriendsPage extends EndlessAdapter {
	
private int mOffset=0;
private final int MAX_FETCH=25;
CursorAdapter fpadapter;

	public EndlessFriendsPage(CursorAdapter wrapped) {
		super(wrapped);
		fpadapter=wrapped;
		
	}

	@Override
	protected void appendCachedData() {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean cacheInBackground() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected View getPendingView(ViewGroup arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
