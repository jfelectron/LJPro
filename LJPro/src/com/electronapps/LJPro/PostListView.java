package com.electronapps.LJPro;

import com.commonsware.cwac.thumbnail.ThumbnailAdapter;
import com.commonsware.cwac.wakeful.WakefulIntentService;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class PostListView extends ListView {

	private String mJournalName;
	private int itemid;
	private  boolean refreshing=false;
	private int mPage=0;
	Context mContext;
	
	public PostListView(Context context) {
		super(context);
		mContext=context;
		//setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		//registerReceiver();
		
	}

	public PostListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext=context;
		//setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);

		//registerReceiver();
		// TODO Auto-generated constructor stub
	}

	public PostListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext=context;
		//registerReceiver();
		// TODO Auto-generated constructor stub
	}
	

	private int mItemId;
	private String mAccountName;
	private Button mButton;
	private String mOldNum;
	public void setPost(String accountname,String journalname, int itemid) {
		mAccountName=accountname;
		mJournalName=journalname;
		mItemId=itemid;
	}
	
	public void registerReceiver() {
		IntentFilter filter=new IntentFilter();
		filter.addAction(LJNet.LJ_COMMENTSUPDATED);
		filter.addAction(LJNet.LJ_NOCOMMENTS);
		filter.addAction(LJNet.LJ_COMMENTADDED);
		filter.addAction(LJNet.LJ_COMMENTERROR);
		filter.addAction(LJNet.LJ_ADDEDCOMMENT);
		mContext.registerReceiver(CommentsReceiver, filter);
	}
	
	
	public void loadComments(Button replycount,String numC) {
		mPage++;
		mButton=replycount;
		refreshing=true;
		Intent comments=new Intent(LJNet.LJ_GETCOMMENTS);
		comments.putExtra("journal", mJournalName);
		mOldNum=numC;
		comments.putExtra("numcomments", numC);
		comments.putExtra("journalname", mAccountName);
		comments.putExtra("ditemid", mItemId);
		comments.putExtra("page", mPage);
		WakefulIntentService.sendWakefulWork(mContext,comments);
		registerReceiver();
		
	}
	
	public BroadcastReceiver CommentsReceiver= new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (action.equals(LJNet.LJ_COMMENTADDED)) {
				
				if(intent.getIntExtra("ditemid",0)==mItemId&&intent.getStringExtra("journal").equals(mJournalName)) 
				{
				 PostCommentsAdapter adapter=(PostCommentsAdapter)( (ThumbnailAdapter) ((HeaderViewListAdapter)getAdapter()).getWrappedAdapter()).getWrappedAdapter();
	             PostCommentsCursor c=(PostCommentsCursor) adapter.getCursor();
	             int count=c.getCount();
	             c.requery();
	             if(count==1) PostListView.this.setSelection(1);
				}
	            
			}
			else if (action.equals(LJNet.LJ_COMMENTSUPDATED)) {

				updateComments(intent);
				
			
			}
			
			else if(action.equals(LJNet.LJ_NOCOMMENTS)) {
				
				noComments(intent);
			}
			else if (action.equals(LJNet.LJ_ADDEDCOMMENT)) {

			
				Toast.makeText(getContext(),R.string.comment_added_, Toast.LENGTH_SHORT).show();
			}
			
			else if (action.equals(LJNet.LJ_COMMENTERROR)){
				String type=intent.getStringExtra("type");
				 AlertDialog.Builder dialog=new AlertDialog.Builder(getContext());
				 dialog.setTitle("Comment Error");
				 if (type.equals("adding")){
					 dialog.setMessage("An error occured adding the comment. The entry may have been deleted or comments frozen");
				 }
				 else if (type.equals("fetching")) {
					 dialog.setMessage("An error occured fetching the comments. The entry was likely deleted.");

				 }
					 dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							
						}
					 }
					 );
				 dialog.create().show();
				 ViewSwitcher footer=(ViewSwitcher) PostListView.this.findViewById(R.id.footerswitcher);
				 footer.showPrevious();
				 //Button load=(Button)footer.findViewById(R.id.btn_loadcomments);
				 //load.setEnabled(false);
				  mContext.unregisterReceiver(CommentsReceiver);
		             
				
			}
		}

	};
	
	private void updateComments(Intent intent) {
		if(intent.getIntExtra("ditemid",0)==mItemId&&intent.getStringExtra("journal").equals(mJournalName)) 
		{
			
			 PostCommentsAdapter adapter=(PostCommentsAdapter)( (ThumbnailAdapter) ((HeaderViewListAdapter)getAdapter()).getWrappedAdapter()).getWrappedAdapter();
            final PostCommentsCursor c=(PostCommentsCursor) adapter.getCursor();
             //c.requery();
             ViewSwitcher footer=(ViewSwitcher) this.findViewById(R.id.footerswitcher);
            if (refreshing) {
            	refreshing=false;
            	footer.showPrevious();
            	Button load=(Button)footer.findViewById(R.id.btn_loadcomments);
                if (Integer.parseInt(mOldNum)==(c.getCount()-1)) load.setText("No More Comments");
                else load.setText("Load More Comments");
            }
            if (mButton==null) {
            	mButton=(Button) footer.findViewById(R.id.commentcount);
            	mOldNum=(String)mButton.getText();
            	
            }
            if ((c.getCount()-1)>Integer.parseInt(mOldNum)) {
            	mButton.setText(((Integer)(c.getCount()-1)).toString());
            	Thread update=new Thread(new Runnable(){

					public void run() {
						LJDB db=LJDB.getDB(PostListView.this.getContext());
		            	ContentValues reply=new ContentValues();
		            	reply.put("replycount",c.getCount()-1);
		            	db.open();
						db.updateReplyCount(mAccountName,mJournalName, mItemId, reply);
						
					}
            	}
            		
            	);
            	update.start();
            	

            }
            
            
             mContext.unregisterReceiver(CommentsReceiver);
             
		}
	}
	
	private void noComments(Intent intent) {
		if(intent.getIntExtra("ditemid",0)==mItemId&&intent.getStringExtra("journal").equals(mJournalName)) 
		{
			
             ViewSwitcher footer=(ViewSwitcher) this.findViewById(R.id.footerswitcher);
             footer.showPrevious();
             Button load=(Button)footer.findViewById(R.id.btn_loadcomments);
             load.setText("No More Comments");
             setSelection(getAdapter().getCount()-1);
             
             
		}
	}
	
	
	
	
	
	@Override 
		public int getSolidColor() {
			return 0;
		
		
	}
	
	
	
	
	
	
	
	

}
