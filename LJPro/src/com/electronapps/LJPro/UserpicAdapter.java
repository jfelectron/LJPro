package com.electronapps.LJPro;



import com.example.coverflow.CoverFlow;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UserpicAdapter extends CursorAdapter {

	private int layoutResource;
	private LayoutInflater mInflater;
	private float mScale;

	public UserpicAdapter(Context context, Cursor c,int resourceId) {
		super(context, c);
		mScale = context.getResources().getDisplayMetrics().density;
		this.layoutResource=resourceId;
        this.mInflater = LayoutInflater.from(context);
	}

	public UserpicAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		((ImageView)view).setImageResource(R.drawable.missing_image);
		view.setTag(cursor.getString(1));

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		ImageView pic=(ImageView) mInflater.inflate(layoutResource, null);
		int w=Math.round(mScale*100);
		int h=Math.round(mScale*100);
		pic.setLayoutParams(new CoverFlow.LayoutParams(w, h));
		return pic;
        
	}
	
	

}
