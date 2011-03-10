package com.electronapps.LJPro;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;


public class FPFilterAdapter extends CursorAdapter {
	private Cursor mCursor;
	private Integer gnameInd;
	private Integer idInd;
	private LayoutInflater mInflater;

	public FPFilterAdapter(Context context, Cursor c) {
		super(context, c);
		this.mCursor=c;
		if (mCursor!=null) {
			this.gnameInd=mCursor.getColumnIndex(LJDB.KEY_NAME);
			this.idInd = mCursor.getColumnIndex(LJDB.KEY_ID);
			this.mInflater = LayoutInflater.from(context);
		}
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}

}
