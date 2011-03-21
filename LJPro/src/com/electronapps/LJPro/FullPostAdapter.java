package com.electronapps.LJPro;


import java.util.HashMap;

import com.commonsware.cwac.cache.SimpleWebImageCache;
import com.commonsware.cwac.thumbnail.ThumbnailAdapter;
import com.commonsware.cwac.thumbnail.ThumbnailBus;
import com.commonsware.cwac.thumbnail.ThumbnailMessage;
import com.electronapps.LJPro.LJTypes.Post;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.MergeCursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.ViewSwitcher;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class FullPostAdapter extends CursorAdapter {

	private int mWidth;
	Context mContext;
	private int mHeight;
	private int jnameInd;

	private int dInd;
	private int pInd;
	private int locInd;
	private int tagsInd;
	private int replyInd;
	private int dIdInd;
	private int upicInd;
	private LJDB LJDBAdapter;
	private final int[] IMAGE_IDS = { R.id.duserpic };
	private SimpleWebImageCache<ThumbnailBus, ThumbnailMessage> imgCache;

	
	
	private int mLayoutResource;
	Cursor mCursor;
	private int anameInd;
	private int jtypeInd;
	private int pnameInd;
	private int starInd;
	private FullPost mActivity;

	public FullPostAdapter(Context context, Cursor c,int resourceId) {
		super(context, c);
		mCursor=c;
		mLayoutResource=resourceId;
		if (c!=null) getIndices();
		mContext=context;
		mActivity=(FullPost) context;
		LJDBAdapter=((FullPost)mContext).getDBConn();
        imgCache = ((LJPro) mContext.getApplicationContext()).getImageCache();

		
	
	
		// TODO Auto-generated constructor stub
	}
	


	private void getIndices() {
		jnameInd=mCursor.getColumnIndex(LJDB.KEY_JOURNALNAME);
		anameInd=mCursor.getColumnIndex(LJDB.KEY_ACCOUNTNAME);
		
		dInd=mCursor.getColumnIndex(LJDB.KEY_DATE);
		pInd=mCursor.getColumnIndex(LJDB.KEY_EVENTRAW);
		dIdInd=mCursor.getColumnIndex(LJDB.KEY_ITEMID);
		locInd=mCursor.getColumnIndex(LJDB.KEY_LOCATION);
		tagsInd=mCursor.getColumnIndex(LJDB.KEY_TAGS);
		jnameInd=mCursor.getColumnIndex(LJDB.KEY_JOURNALNAME);
		 starInd=mCursor.getColumnIndex(LJDB.KEY_STARRED);
		 pnameInd=mCursor.getColumnIndex(LJDB.KEY_POSTERNAME);
		jtypeInd=mCursor.getColumnIndex(LJDB.KEY_JOURNALTYPE);
		replyInd=mCursor.getColumnIndex(LJDB.KEY_REPLYCOUNT);
		upicInd=mCursor.getColumnIndex(LJDB.KEY_USERPIC);
	}
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
			ViewHolder holder=(ViewHolder) view.getTag();
             holder.journalname.setText(mCursor.getString(jnameInd));
             int pos=cursor.getPosition();
             holder.starred.setTag(pos);
             boolean checked=starMap.get(pos)==null?(cursor.getInt(starInd)==0?false:true):starMap.get(pos);
             holder.starred.setChecked(checked);
             holder.date.setText(mCursor.getString(dInd));
             if (mCursor.getString(jtypeInd).equals("C")) {
             	holder.postername.setText(mCursor.getString(pnameInd));
             	
             }
             else {
             	holder.postername.setText("");
             }
            Cursor comments=LJDBAdapter.getComments(mCursor.getString(anameInd),mCursor.getInt(dIdInd));
            holder.post.setPost(mCursor.getString(anameInd),mCursor.getString(jnameInd),mCursor.getInt(dIdInd));
            if (comments.getCount()>0) {
            	holder.commentstatus.setText("Refresh Comments");	
            }
            else holder.commentstatus.setText("Load Comments");	
         	holder.replycount.setText(((Integer)mCursor.getInt(replyInd)).toString());

             ThumbnailAdapter wrapper=(ThumbnailAdapter) ((HeaderViewListAdapter)holder.post.getAdapter()).getWrappedAdapter();
             CursorAdapter adapter=(CursorAdapter) wrapper.getWrappedAdapter();
        
            	 CursorWrapper post=new PostWrapper(mCursor,mCursor.getPosition());
            	 Cursor [] a={post,comments};
            	 PostCommentsCursor pc=new PostCommentsCursor(a);
            	 adapter.changeCursor(pc);
            	 
             
            
         
             
         	
                if  (mCursor.getString(upicInd)!=null&&mCursor.getString(upicInd).length()>0) {
             	holder.userpic.setImageResource(R.drawable.defaultuserpic);
             	holder.userpic.setTag(mCursor.getString(upicInd));
             }
             else { 
             	holder.userpic.setTag(null);
             	holder.userpic.setImageResource(R.drawable.defaultuserpic);
             }

	}
	
	HashMap<Integer,Boolean> starMap=new HashMap<Integer,Boolean>();
	
	final OnCheckedChangeListener onStarChanged=new OnCheckedChangeListener(){
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			Integer position=(Integer) buttonView.getTag();
			starMap.put(position, isChecked);
			mActivity.toggleStarred(position, isChecked);
			
		}
		
	};

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		 RelativeLayout v = (RelativeLayout) View.inflate(mContext,mLayoutResource,null);
		 final ViewSwitcher footer=(ViewSwitcher) View.inflate(mContext,R.layout.commentsfooter,null);
         final ViewHolder holder = new ViewHolder();
         holder.postername=(TextView) v.findViewById(R.id.postername);
         holder.starred=(ToggleButton) v.findViewById(R.id.starred);
         holder.starred.setOnCheckedChangeListener(onStarChanged);
         holder.post=(PostListView) v.findViewById(android.R.id.list);
         holder.replycount=(Button) footer.findViewById(R.id.commentcount);
         holder.commentstatus=(Button) footer.findViewById(R.id.btn_loadcomments);
         holder.commentstatus.setOnClickListener( new OnClickListener() {
			public void onClick(View v) {
				
				holder.post.loadComments(holder.replycount, (String)holder.replycount.getText());
				footer.showNext();
				
			}
         });
         holder.post.addFooterView(footer);
         
         PostCommentsAdapter pca=new PostCommentsAdapter(mContext,null,R.layout.postbody,R.layout.comment);
         holder.post.setAdapter(new ThumbnailAdapter((Activity)mContext,pca,imgCache,IMAGE_IDS));
         holder.userpic = (ImageView) v.findViewById(R.id.duserpic);
         holder.journalname = (TextView) v.findViewById(R.id.uname);
         holder.date=(TextView) v.findViewById(R.id.date);
     
         v.setTag(holder);
         return v;
	}
	
	   

		 
	   @Override
		public void changeCursor(Cursor c) {
		super.changeCursor(c);
		starMap.clear();
	}
     
	
	  private static class ViewHolder{
          ImageView userpic;
          TextView journalname;
          TextView postername;
          ToggleButton starred;
          TextView date;
          PostListView post;
          Button replycount;
          Button commentstatus;
          
      }

}
