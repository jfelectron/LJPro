package com.electronapps.LJPro;


import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.electronapps.LJPro.PicasaAPI.TokenCallback;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class UploadPhoto extends ListActivity implements TokenCallback {
	LJDB mDB;
	int nProviders;
	private String mPhotoPath;
	private String mJournalname;
	private Context mContext;
	private EditText mTitle;
	private SeekBar mSizeSeek;
	private String mContentType;
	private String mToken;
	private String mSecret;
	
	
@Override
		public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Intent intent=getIntent();
		mJournalname=intent.getStringExtra("journalname");
		mPhotoPath=intent.getStringExtra("file");
		mContentType=intent.getStringExtra("mime");
		mContext=getApplicationContext();
		setContentView(R.layout.uploadphoto);
		TextView header=(TextView)findViewById(R.id.uphotoheader);
		ListView listView=getListView();
		mTitle=(EditText)findViewById(R.id.phototitle);
		mSizeSeek=(SeekBar) findViewById(R.id.sizeSeek);
	
		mSizeSeek.setMax(100);
		mSizeSeek.setProgress(0); //Native,T,S,M,L,
		final TextView photoSize=(TextView) findViewById(R.id.photoSize);
		mSizeSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (progress<(100/6)) photoSize.setText("Square Thumbnail");
				else if (progress>=100/6&&progress<200/6) photoSize.setText("Thumbnail");
				else if (progress>=200/6&&progress<300/6) photoSize.setText("Small");
				else if (progress>=200/6&&progress<400/6) photoSize.setText("Medium");
				else if (progress>=400/6&&progress<500/6) photoSize.setText("Large");
				else if (progress>=500/6&&progress<600/6) photoSize.setText("Original");
				
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}});
		listView.setOnItemClickListener(new OnItemClickListener() {
			
			

			public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
		
				Cursor clicked=(Cursor) getListAdapter().getItem(position);
				String provider=clicked.getString(clicked.getColumnIndex(LJDB.KEY_PROVIDER));
				mToken=clicked.getString(clicked.getColumnIndex(LJDB.KEY_AUTHTOKEN));
				
				 if (provider.equals(PhotoAccountAdapter.SCRAPBOOK)) {
					 uploadScrapBook();
		        	  
		          }
		          else if (provider.equals(PhotoAccountAdapter.PICASA)) {
		        	  uploadPicasa();
		        	
		          }
		          else if (provider.equals(PhotoAccountAdapter.FLICKR)) {
		        	 uploadFlickr();
		          }
		          
		          else if(provider.equals(PhotoAccountAdapter.PHOTOBUCKET)){
		        	  mSecret=clicked.getString(clicked.getColumnIndex(LJDB.KEY_AUTHSECRET));
		        	  uploadPhotoBucket(clicked);
		        	  
		        	  
		          }
				 Intent uploading=new Intent(PhotoAPIBase.UPLOAD_STARTED);
				 uploading.putExtra("file", mPhotoPath);
				 uploading.putExtra("provider", provider);
				uploading.putExtra("title",mTitle.getText().toString());
				uploading.putExtra("size",mSizeSeek.getProgress());
				UploadPhoto.this.setResult(RESULT_OK, uploading);
				UploadPhoto.this.finish();

			}

			


		});
		header.setText("Upload Photo");
		SetupList setup=new SetupList();
		setup.execute();
		
	}

 	private static CharSequence[] providers={"ScrapBook","Picasa","Flickr","PhotoBucket"};

	public class SetupList extends AsyncTask<Void,Void, Cursor>{

		@Override
		protected Cursor doInBackground(Void... params) {
			mDB=LJDB.getDB(mContext);
			mDB.open();
			Cursor paccts=mDB.getPhotoAccounts(null);
			UploadPhoto.this.startManagingCursor(paccts);
			nProviders=paccts.getCount();
			return paccts;
		}
		
		@Override
			protected void onPostExecute(Cursor c){
				if (nProviders==0){
					UploadPhoto.this.setListAdapter(new PhotoAccountAdapter(UploadPhoto.this,c,R.layout.photoaccountrow));
					showProviderChoices(null);
				
					
				}
				else {
					UploadPhoto.this.setListAdapter(new PhotoAccountAdapter(UploadPhoto.this,c,R.layout.photoaccountrow));
				}
		}
		
		private static final String TAG="UploadPhoto";
		
		
	}
	private static final int REQUEST_AUTHENTICATE = 0;
	private static final int FLICKR_AUTH=1;
	private static final int PB_AUTH=2;
	private PicasaAPI mPicasaAPI;
	public void showProviderChoices(View view) {
		AlertDialog.Builder builder=new AlertDialog.Builder(UploadPhoto.this);
		builder.setTitle("Choose Provider");
		builder.setSingleChoiceItems(providers, -1, new DialogInterface.OnClickListener(){

			

			

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				switch(which){
				case 0:
					AlertDialog.Builder confirm=new AlertDialog.Builder(UploadPhoto.this);
					confirm.setTitle("Confirm LJ Account");
					confirm.setMessage("LiveJournal ScrapBook is only available for Plus and Paid/Permanent LJ Accounts.Is your account one of these?");
					confirm.setPositiveButton("YES", new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							ContentValues account=new ContentValues();
							account.put("accountname", mJournalname);
							account.put("photo_account", mJournalname);
							account.put("provider", "ScrapBook");
							try {
							Boolean success=mDB.insertPhotoAccount(account);
						
							if(success) {
								CursorAdapter adapter=(CursorAdapter) UploadPhoto.this.getListAdapter();
								adapter.getCursor().requery();
								adapter.notifyDataSetChanged();
								dialog.dismiss();
								//uploadScrapBook();
							}
							
							}
							catch(Throwable t) {
								Log.e("UploadPhoto",t.getMessage(),t);
							}
							
							
						}
					});
				confirm.setNegativeButton("NO", new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							showProviderChoices(null);
							
						}
					});
				confirm.create().show();
					break;
				case 1:
					mPicasaAPI=new PicasaAPI(UploadPhoto.this,mJournalname,null,UploadPhoto.this);
					mPicasaAPI.authorizeAccount();
					break;
				case 2:
					Intent flickrAuth=new Intent(UploadPhoto.this,Authenticate.class);
					flickrAuth.putExtra("journalname",mJournalname);
					flickrAuth.putExtra("provider","Flickr");
					startActivityForResult(flickrAuth,FLICKR_AUTH);
					break;
				case 3:
					
					
					Intent pbAuth=new Intent(UploadPhoto.this,Authenticate.class);
					pbAuth.putExtra("journalname",mJournalname);
					pbAuth.putExtra("provider","PhotoBucket");
					startActivityForResult(pbAuth,PB_AUTH);

					break;
					
				}
				
			}
			
		});
		builder.create().show();
	}
	
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		if (requestCode==FLICKR_AUTH||requestCode==PB_AUTH) {
			CursorAdapter adapter=(CursorAdapter) UploadPhoto.this.getListAdapter();
			adapter.getCursor().requery();
			adapter.notifyDataSetChanged();
			
			//uploadFlickr();
		}
		else if (requestCode==REQUEST_AUTHENTICATE) {
	        if (resultCode == RESULT_OK) {
	            mPicasaAPI.gotAccount(false);
	          } else {
	            mPicasaAPI.showAccountDialog();
	          }
	       
		}
		
	}
	
	private void uploadScrapBook() {
		Intent upload=new Intent(LJNet.LJ_SCRAPBOOK);
		upload.putExtra("file", mPhotoPath);
		upload.putExtra("type", mContentType);
		upload.putExtra("title",mTitle.getText().toString());
		upload.putExtra("journalname", mJournalname);
		WakefulIntentService.sendWakefulWork(getApplicationContext(), upload);
	}
	
	private void uploadFlickr() {
		Intent upload=new Intent(LJNet.LJ_FLICKR);
		upload.putExtra("file", mPhotoPath);
		upload.putExtra("title",mTitle.getText().toString());
		upload.putExtra("token",mToken);
		upload.putExtra("journalname", mJournalname);
		WakefulIntentService.sendWakefulWork(getApplicationContext(), upload);
	}
	
	private void uploadPhotoBucket(Cursor clicked) {
		Intent upload=new Intent(LJNet.LJ_PBUCKET);
		upload.putExtra("file", mPhotoPath);
		upload.putExtra("title",mTitle.getText().toString());
		upload.putExtra("token",mToken);
		upload.putExtra("secret", mSecret);
		upload.putExtra("account", clicked.getString(clicked.getColumnIndex(LJDB.KEY_PACCOUNT)));
		upload.putExtra("subdomain",clicked.getString(clicked.getColumnIndex(LJDB.KEY_PACCOUNTID)));
		upload.putExtra("journalname", mJournalname);
		WakefulIntentService.sendWakefulWork(getApplicationContext(), upload);
		// TODO Auto-generated method stub
		
	}
	
	private void uploadPicasa() {
		Intent upload=new Intent(LJNet.LJ_PICASA);
		upload.putExtra("file", mPhotoPath);
		upload.putExtra("type", mContentType);
		upload.putExtra("title",mTitle.getText().toString());
		upload.putExtra("token",mToken);
		upload.putExtra("journalname", mJournalname);
		WakefulIntentService.sendWakefulWork(getApplicationContext(), upload);
	}


	public void onHaveToken(String accountname,String token) {
		
		mToken=token;
		CursorAdapter adapter=(CursorAdapter) UploadPhoto.this.getListAdapter();
		adapter.getCursor().requery();
		adapter.notifyDataSetChanged();
	}
	
	
	
	

}
