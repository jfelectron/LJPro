package com.electronapps.LJPro;



import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.commonsware.cwac.cache.SimpleWebImageCache;
import com.commonsware.cwac.thumbnail.ThumbnailBus;
import com.commonsware.cwac.thumbnail.ThumbnailMessage;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class PostCommentsAdapter extends CursorAdapter {
	private static final int POST_VIEW=0;
	private static final int COMMENT_VIEW=1;
	private Cursor mCursor;
	private int pInd;
	private int dIdInd;
	private int locInd;
	private int tagsInd;
	private int replyInd;
	private int cnameInd=-1;
	private int timeInd;
	private int picInd;
	private int jnameInd;
	private int postRes;
	private int commentRes;
	private Context mContext;
	private int subInd;
	private int dInd;
	private int cpInd;
	private int parentInd;
	private int tIdInd;
	private int anameInd;
	private String mAccountName;
	private Integer ditemid;
	private String jname;
	private int pnameInd;
	private int psubInd;
	private static final Pattern html=Pattern.compile("\\<.*?>|&[^\\s]*;");
	
	public PostCommentsAdapter(Context context, Cursor c, int postRes, int commentRes) {
		super(context, c);
		mCursor=c;
		mContext=context;
		this.postRes=postRes;
		this.commentRes=commentRes;
		if (c!=null) {
			getIndices();
			c.moveToFirst();
			mAccountName=c.getString(anameInd);
			ditemid=c.getInt(dIdInd);
			jname=c.getString(jnameInd);
		}
		// TODO Auto-generated constructor stub
	}
	
	@Override 
		public int getViewTypeCount() {
		return 2;
		
	}
	
	@Override 
	public int getItemViewType(int position){
		if (position>0) {
			return COMMENT_VIEW;
		}
		else return POST_VIEW;
	}
	
	private void getIndices(){
		mCursor.moveToFirst();
		anameInd=mCursor.getColumnIndex(LJDB.KEY_ACCOUNTNAME);
		pInd=mCursor.getColumnIndex(LJDB.KEY_EVENTRAW);
		psubInd=mCursor.getColumnIndex(LJDB.KEY_SUBJECT);
		jnameInd=mCursor.getColumnIndex(LJDB.KEY_JOURNALNAME);
		dIdInd=mCursor.getColumnIndex(LJDB.KEY_ITEMID);
		locInd=mCursor.getColumnIndex(LJDB.KEY_LOCATION);
		tagsInd=mCursor.getColumnIndex(LJDB.KEY_TAGS);
		pnameInd=mCursor.getColumnIndex(LJDB.KEY_POSTERNAME);
		replyInd=mCursor.getColumnIndex(LJDB.KEY_REPLYCOUNT);
		if (mCursor.getCount()>1) {
			getCommentIndices();
		}
	}
	
	public void getCommentIndices() {
		mCursor.moveToPosition(1);
		cpInd=mCursor.getColumnIndex(LJDB.KEY_EVENTRAW);
		picInd=mCursor.getColumnIndex(LJDB.KEY_USERPIC);
		subInd=mCursor.getColumnIndex(LJDB.KEY_SUBJECT);
		dInd=mCursor.getColumnIndex(LJDB.KEY_DATE);
		cnameInd=mCursor.getColumnIndex(LJDB.KEY_POSTERNAME);
		tIdInd=mCursor.getColumnIndex(LJDB.KEY_TALKID);
		parentInd=mCursor.getColumnIndex(LJDB.KEY_PARENTID);
	}
	

	@Override 
	 public long getItemId(int position) {
        if (mCursor != null) {
            if (mCursor.moveToPosition(position)) {
            	if (position==0) {
            		return Long.valueOf(mCursor.getInt(dIdInd));
            	}
            	else return Long.valueOf(mCursor.getInt(tIdInd));
                
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }
	
	
	@Override
	public 
	
	void changeCursor(Cursor c) {
		if (c!=null) {
		mCursor=c;
		getIndices();
		c.moveToFirst();
		mAccountName=c.getString(anameInd);
		ditemid=c.getInt(dIdInd);
		jname=c.getString(jnameInd);
		super.changeCursor(c);
		
		}
		
	}
	
	private class onReplyTo implements OnClickListener {
		private String mJournalname;
		private int mItemId;
		private int mTalkId;
		private String mHTML;
		private String mPosterName;
		
		public void onClick(View v) {
			PostListView list=((PostListView)v.getParent().getParent());
			list.registerReceiver();
			Intent comment=new Intent(mContext,ReplyTo.class);
			comment.putExtra("journalname", mJournalname);
			comment.putExtra("accountname", mAccountName);
			comment.putExtra("itemid",mItemId);
			comment.putExtra("talkid", mTalkId);
			comment.putExtra("postername", mPosterName);
			comment.putExtra("html", mHTML);
			mContext.startActivity(comment);
			
			
			
		}
		
		public void setReplyTo(String journalname,int id,int talkid,String html,String postername) {
			mJournalname=journalname;
			mTalkId=talkid;
			mItemId=id;
			mHTML=html;
			mPosterName=postername;
		}
		
	}
	
	


	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		int viewType=this.getItemViewType(mCursor.getPosition());
		
		switch (viewType) {
		case POST_VIEW:
			PostHolder holder=(PostHolder) view.getTag();
			final String journalname=mCursor.getString(jnameInd);
			final String postername=mCursor.getString(pnameInd);
			final int itemid=mCursor.getInt(dIdInd);
			  holder.post.setTag(journalname+((Integer)itemid).toString());
			  holder.subject.setText(mCursor.getString(psubInd));
			  String post=mCursor.getString(pInd);
			  post=post.replaceAll("\\<br>","\n");
			  post=post.replaceAll("\\<br\\\\>","\n");
			  if (post!=null) {
				  //only parse HTML if there is actually HTML to be parsed
				  Matcher hashtml=html.matcher(post);
	            	if (hashtml.find()){
	            		
	            	
				  holder.post.setLoading();
				  
				  holder.post.setHTML(post);
	            	}
	            	else holder.post.setText(post);
			  }
			  else holder.post.setText("");
			  
			  onReplyTo listener=new onReplyTo();
			  listener.setReplyTo(journalname,itemid,0,post==null?"":post,postername);
			  
			  holder.replyto.setOnClickListener(listener);
			  
			
			if (mCursor.getString(locInd)!=null&&mCursor.getString(locInd).length()>0) {
             	holder.location.setText(mCursor.getString(locInd));
             	holder.location.setVisibility(View.VISIBLE);
             }
             else {
             	holder.location.setText("");
             	holder.location.setVisibility(View.GONE);
             }
             if  (mCursor.getString(tagsInd)!=null&&mCursor.getString(tagsInd).length()>0) {
             	holder.tags.setText(Html.fromHtml("<b>Tags: </b>"+mCursor.getString(tagsInd)));
             	holder.tags.setVisibility(View.VISIBLE);
             }
             else
             {
             	holder.tags.setText("");
             	holder.tags.setVisibility(View.GONE);
             }
             
         
			break;
		case COMMENT_VIEW:
		
			if (cnameInd==-1) getCommentIndices();
			
				CommentHolder cholder=(CommentHolder) view.getTag();
				if (!(mCursor.getInt(parentInd)==0)) cholder.header.setBackgroundResource(R.drawable.childheader);
				else cholder.header.setBackgroundResource(R.drawable.commentheader);
				cholder.journalname.setText(mCursor.getString(cnameInd));
				if (mCursor.getString(subInd)!=null) {
					cholder.subject.setText(mCursor.getString(subInd));
				}
				else cholder.subject.setText("");
	            cholder.date.setText(mCursor.getString(dInd));
				  cholder.comment.setTag(mCursor.getString(cnameInd)+((Integer)mCursor.getInt(tIdInd)).toString());
				  String commentText=null;
	            if (mCursor.getString(cpInd)!=null) {
	            	Matcher hashtml=html.matcher(mCursor.getString(cpInd));
	            	commentText=mCursor.getString(cpInd);
	            	if (hashtml.find()){
					  cholder.comment.setLoading();
					  cholder.comment.setHTML(commentText);
	            	}
	            	else cholder.comment.setText(mCursor.getString(cpInd));
				  }
				  else cholder.comment.setText("");
	            int talkid=mCursor.getInt(tIdInd);
	            onReplyTo clistener=new onReplyTo();
				  clistener.setReplyTo(jname,ditemid,talkid,commentText==null?"":commentText,mCursor.getString(cnameInd));
				  
				  cholder.replyto.setOnClickListener(clistener);
	            if  (mCursor.getString(picInd)!=null&&mCursor.getString(picInd).length()>0) {
	             	cholder.userpic.setImageResource(R.drawable.defaultuserpic);
	             	cholder.userpic.setTag(mCursor.getString(picInd));
	             }
	             else { 
	             	cholder.userpic.setTag(null);
	             	cholder.userpic.setImageResource(R.drawable.defaultuserpic);
	             }
	            
			break;
		}
	}
    

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		int viewType=this.getItemViewType(cursor.getPosition());
		
		switch (viewType) {
		case POST_VIEW:
			 RelativeLayout v = (RelativeLayout) View.inflate(mContext,postRes,null);
	         PostHolder holder = new PostHolder();
	         holder.subject=(TextView) v.findViewById(R.id.subject);
			  	holder.post=(PostView) v.findViewById(R.id.post);
		         holder.location=(TextView) v.findViewById(R.id.posterlocation);
		         holder.tags=(TextView) v.findViewById(R.id.posttags);
		         holder.replyto=(Button) v.findViewById(R.id.replyto);
		        
		         v.setTag(holder);
		         return v;
		case COMMENT_VIEW:
			 RelativeLayout w = (RelativeLayout) View.inflate(mContext,commentRes,null);
			 CommentHolder cholder = new CommentHolder();
			 cholder.comment=(PostView) w.findViewById(R.id.post);
			 cholder.replyto=(Button) w.findViewById(R.id.replyto);
			cholder.header=(LinearLayout) w.findViewById(R.id.pheader);
			cholder.userpic=(ImageView) w.findViewById(R.id.duserpic);
			 
		         cholder.journalname = (TextView) w.findViewById(R.id.uname);
		         cholder.date=(TextView) w.findViewById(R.id.cdate);
		         cholder.subject=(TextView) w.findViewById(R.id.csubject);
		         w.setTag(cholder);
		         return w;
		}
		return null;
	}
	
	private static class PostHolder {
	    TextView subject;
		PostView post;
		TextView tags;
        TextView location;
        Button replyto;
	}
	
	  private static class CommentHolder{
		  Button replyto;
		  LinearLayout header;
          ImageView userpic;
          TextView journalname;
          TextView subject;
          TextView date;
          PostView comment;
          
      }
	  

}
