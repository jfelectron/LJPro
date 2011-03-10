package com.electronapps.LJPro;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.view.View;
import android.view.Window;
import android.widget.CursorAdapter;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.commonsware.cwac.cache.SimpleWebImageCache;
import com.commonsware.cwac.thumbnail.ThumbnailAdapter;
import com.commonsware.cwac.thumbnail.ThumbnailBus;
import com.commonsware.cwac.thumbnail.ThumbnailMessage;
import com.electronapps.LJPro.HorizontalSnapView.SelectionObserver;
import com.electronapps.LJPro.LJTypes.Post;

public class FullPost extends Activity implements SelectionObserver {
	private Cursor mCursor;
	private Context mContext;
	private CursorAdapter m_adapter;
	private String mExtraWhere=null;
	private String[] mExtraArgs=null;
	private String journalname;
	private SimpleWebImageCache<ThumbnailBus, ThumbnailMessage> imgCache;
	private LJDB LJDBAdapter;
	private boolean TRACE=false;
	private int mPosition=-1;

	
	@Override 
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
			mPosition=savedInstanceState.getInt("position");
		}
        if (TRACE) Debug.startMethodTracing("FullPost");
        setContentView(R.layout.fullpostcontainer);
        Intent intent=getIntent();
        
        journalname=intent.getStringExtra("journalname");
        mExtraWhere=intent.getStringExtra("extraWhere");
        mExtraArgs=intent.getStringArrayExtra("extraArgs");
       
        mContext=getApplicationContext();
       
        imgCache = ((LJPro) mContext).getImageCache();
        GetPosts setup=new GetPosts(intent);
        setup.execute();
        
       
		}
	
	private class GetPosts extends AsyncTask<Void,Void,Void> {


	
		
		public GetPosts(Intent intent) {
			if (mPosition==-1) mPosition=intent.getIntExtra("position",0);
			
		}
		@Override
		protected Void doInBackground(Void... params) {
			 LJDBAdapter=LJDB.getDB(getApplicationContext());
			 LJDBAdapter.open();
			 mCursor = LJDBAdapter.getFriendsPage(journalname,mExtraWhere,mExtraArgs,null);
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			
			 final int[] IMAGE_IDS = { R.id.duserpic };
				m_adapter = new FullPostAdapter(FullPost.this, mCursor, R.layout.fullpost);
				 HorizontalSnapView snapview=(HorizontalSnapView) findViewById(R.id.fullpostcontainer);
			        
			    snapview.registerSelectionObserver(FullPost.this);
				snapview.setAdapter(new ThumbnailAdapter(FullPost.this, m_adapter, imgCache,
						IMAGE_IDS),mPosition);
			
		}
		
		
		
		
		
	}
	
	@Override 
		protected void onDestroy() {
		super.onDestroy();
		mCursor.close();
	}
	
		@Override 
			public void onPause() {
			super.onPause();
			
;		}
		
		public LJDB getDBConn() {
			return LJDBAdapter;
		}

		public void onSelectionChanged(int position) {
			setResult(position);
			mPosition=position;
			
		}
		
		@Override 
		protected void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putInt("position", mPosition);
			
		}
        
       
        
        
        
        
	
}
