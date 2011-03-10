package com.electronapps.LJPro;


import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.ArrayList;

import com.commonsware.cwac.cache.SimpleWebImageCache;
import com.commonsware.cwac.thumbnail.ThumbnailAdapter;
import com.commonsware.cwac.thumbnail.ThumbnailBus;
import com.commonsware.cwac.thumbnail.ThumbnailMessage;
import com.commonsware.cwac.wakeful.WakefulIntentService;


import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;


public class Accounts extends ListActivity  {

	private LJDB LJDBAdapter;
	private static final int ACCOUNT_ADDED=1;
	private SharedPreferences appPrefs;
	private Boolean refreshing=false;
	private Boolean deleted=false;
	private boolean added;
	
	private int refreshCount=0;
	public static final String TAG = Accounts.class.getSimpleName();
	public static final boolean DEBUG = false;
	public static final boolean DROPTABLE=false;
	private int accInd=-1;
	private int clickIndex=-1;
	private String lpName=null;
	private Cursor mCursor = null;
	private AccountsAdapter m_adapter;
	protected SimpleWebImageCache<ThumbnailBus, ThumbnailMessage> imgCache;
	private static Context mContext;

	public BroadcastReceiver LJAccountReceiver=new BroadcastReceiver()
	{	

		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action=intent.getAction();



			if (action.equals(LJNet.LJ_XMLERROR)) {
				removeStickyBroadcast(intent);
				
					LJPro app=(LJPro) getApplication();
					app.alertNetworkError(Accounts.this);
				


			}

			else if (action.equals(LJNet.LJ_ACCOUNTADDED)) {
				Log.d(TAG,"Account added");
			}
			else if (action.equals(LJNet.LJ_LOGINUPDATED)) {
				if(!refreshing) {
					String journalname=intent.getStringExtra("journalname");
					removeStickyBroadcast(intent);
					updateAccounts();
				}
				else {
					LJPro app=(LJPro) getApplication();
					ProgressDialog refresher=(ProgressDialog) app.Dialog.get();
					int numAccounts=mCursor.getCount();
					if (Accounts.this.refreshCount<numAccounts) {
							removeStickyBroadcast(intent);
							
							refreshCount++;
							double frac=(double)refreshCount/(double)numAccounts;
							int p=(int) (frac*100);

							if (refresher!=null) {
								refresher.setProgress(p);

								if (p >= 100){
									updateAccounts();
									refreshing=false;
									refresher.dismiss();
									String alldone=getString(R.string.alldone);
									Toast.makeText(Accounts.this, alldone, Toast.LENGTH_LONG).show();

								}
							}
						}
					}

				}
			}
		
	};





	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState!=null) {
			restoreProgress(savedInstanceState);
		}
		Log.v(TAG,"setting Content View");
		setContentView(R.layout.accounts);
	
		
		//Debug.startMethodTracing("accounts");
		Log.v(TAG, "creating Database");
		mContext=getApplicationContext();
		
		
		if(DROPTABLE) {
			try {
				LJDBAdapter.dropTables();
				LJDBAdapter.createTables();
				LJDBAdapter.createTriggers();

			}
			catch(Throwable r) {
				Log.e(TAG,r.getMessage(),r);
			}
		}
		imgCache = ((LJPro)getApplication()).getImageCache();
		appPrefs=PreferenceManager.getDefaultSharedPreferences(this);
		registerForContextMenu(getListView());
		
		populateAccountList();
		if(appPrefs.getString("defaultlogin",null)!=null){
			String journalname=appPrefs.getString("defaultlogin","");
			/*Intent getfriendspage = new Intent(LJNet.LJ_GETFRIENDSPAGE);
			
 			((LJPro)getApplicationContext()).fprefreshing.put(journalname,true);
			getfriendspage.putExtra("journalname", journalname);
			WakefulIntentService.sendWakefulWork(getApplicationContext(), getfriendspage);*/
			login(journalname);
		}
		

	}

	private void restoreProgress(Bundle savedInstanceState) {
		refreshing=savedInstanceState.getBoolean("refreshing");
		refreshCount=savedInstanceState.getInt("refreshCount");
		if (refreshing) {
			LJPro app=(LJPro) getApplication();
			ProgressDialog refresher=(ProgressDialog) app.Dialog.get();
			refresher.dismiss();
			app.Dialog = new WeakReference<ProgressDialog>(new ProgressDialog(Accounts.this));
			refresher=(ProgressDialog) app.Dialog.get();
			refresher.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			refresher.setMessage(getString(R.string.refreshing));
			refresher.setCancelable(false);
			refresher.setProgress(savedInstanceState.getInt("progress"));
			refresher.show();

		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		

		
	}

	@Override
	public void onPause() {
		super.onPause();
		
		unregisterReceiver(LJAccountReceiver);
		

	}
	@Override
	protected void onSaveInstanceState(Bundle saveState) {
		super.onSaveInstanceState(saveState);
		saveState.putBoolean("refreshing",refreshing);
		if(refreshing) {
			LJPro app=(LJPro) getApplication();
			ProgressDialog refresher=(ProgressDialog) app.Dialog.get();
			saveState.putInt("progress", refresher.getProgress());
			saveState.putInt("refreshCount",refreshCount);
		}
		
	}

	@Override 
	public void onResume() {
		super.onResume();
		IntentFilter loginfilter=new IntentFilter();
		loginfilter.addAction(LJNet.LJ_XMLERROR);
		loginfilter.addAction(LJNet.LJ_LOGINUPDATED);
		registerReceiver(LJAccountReceiver,loginfilter);
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		TextView header=(TextView) findViewById(R.id.acheader);
		header.setText(R.string.accounts);
		header.invalidate();
		
	}



	private void populateAccountList() {
		


		Runnable createAccountList=new Runnable() {
			public void run() {
				getAccounts();
			}
		};

		Thread thread = new Thread(null, createAccountList, "AccountList Background");

		thread.start();
		ListView listView=getListView();
		listView.setOnItemClickListener(new OnItemClickListener() {
			
			public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
		
				Cursor clicked=(Cursor) m_adapter.getItem(position);
				String journalname=clicked.getString(accInd);
				/*Intent getfriendspage = new Intent(LJNet.LJ_GETFRIENDSPAGE);
     			((LJPro)getApplicationContext()).fprefreshing.put(journalname,true);
    			getfriendspage.putExtra("journalname", journalname);
    			WakefulIntentService.sendWakefulWork(getApplicationContext(), getfriendspage);*/
				 login(journalname);
					//Invoke LJNet to update selected account if more than sync time has passed
				

			}


		});
	}
	
	private void login(String journalname) {
		
		Intent launchnav=new Intent(Accounts.this,Navigation.class);
		launchnav.putExtra("journalname", journalname);
		startActivity(launchnav);
	}




	private void getAccounts() {
		
		try {
			LJDBAdapter=LJDB.getDB(this);
			LJDBAdapter.open();
			mCursor=LJDBAdapter.getAllAccounts(null);
			accInd = mCursor.getColumnIndexOrThrow(LJDB.KEY_ACCOUNTNAME);
			if (mCursor!=null) {
				startManagingCursor(mCursor);
				mCursor.moveToFirst();
				runOnUiThread(new Runnable() {
					public void run() {
					m_adapter = new AccountsAdapter(Accounts.this,mCursor,R.layout.accountrow);
					final int[] IMAGE_IDS = {R.id.duserpic};
					setListAdapter(new ThumbnailAdapter(Accounts.this, m_adapter, imgCache, IMAGE_IDS));
					//Debug.stopMethodTracing();
					}

				
					
				});
				
			}
		}
		catch (Throwable r) {
			Log.e(TAG,r.getMessage(),r);
		}
		//Check to see that we have at least 1 user, otherwise force creation
		int numAccts=mCursor.getCount();
		if (numAccts==0) {
			Intent newAccount = new Intent(Accounts.this, AddAccount.class);
			newAccount.putExtra("allowBack","false");
			Accounts.this.startActivityForResult(newAccount,ACCOUNT_ADDED);

		}

	}

	void updateAccounts() {
		Thread thread = new Thread(null, reQueryInBackground,
		"RefreshFriendList Background");
		try {
			thread.start();
		} catch (Throwable e) {
			Log.e(TAG, e.getMessage(),e);
		}
	}
	
	 
	

	private Runnable reQueryInBackground=new Runnable() {

		public void run() {
			if (mCursor!=null) {
			mCursor=LJDBAdapter.getAllAccounts(null);
			startManagingCursor(mCursor);
			if (mCursor.getCount()==0) {
				Intent newAccount = new Intent(Accounts.this, AddAccount.class);
				newAccount.putExtra("allowBack","false");
				Accounts.this.startActivityForResult(newAccount,ACCOUNT_ADDED);
			}
			else {
			 updateUI();
			
			}
		}
			
		}
	
		
	};
	Runnable refreshList=new Runnable() { public void run() { m_adapter.changeCursor(mCursor);}};
	private void updateUI() {
		runOnUiThread(refreshList);
	}






	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==AddAccount.ACCOUNT_ADDED) {
			String journalname=data.getStringExtra("journalname");
			fetchUserData(journalname);
			SharedPreferences.Editor editor = appPrefs.edit();
			Date d=new Date();
			editor.putLong(journalname+"login_lastupdate",d.getTime());
			editor.commit(); // Very important
			updateAccounts();
			login(journalname);
		}

	}
	
private void fetchUserData(String journalname) {
		Intent updatefriends = new Intent(LJNet.LJ_GETFRIENDS);
		updatefriends.putExtra("journalname", journalname);
		WakefulIntentService.sendWakefulWork(mContext, updatefriends);
		((LJPro)mContext).notifySync(journalname);
		Intent refetch = new Intent(LJNet.LJ_GETFRIENDSPAGE);
		refetch.putExtra("journalname", journalname);
		WakefulIntentService.sendWakefulWork(mContext, refetch);
		
	}


	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.accountops, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.addaccount:  Intent newAccount = new Intent(Accounts.this, AddAccount.class);
		//newAccount.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		newAccount.putExtra("allowBack","true");
		Accounts.this.startActivityForResult(newAccount,ACCOUNT_ADDED);
		break;
		case R.id.settings:     
			Intent prefs=new Intent(getApplicationContext(),LJProPrefs.class);
			prefs.putExtra("scope", "All");
			startActivity(prefs);
			break;
		case R.id.refresh:     refreshAllAccounts();
		break;

		case R.id.help:     Toast.makeText(this, "You pressed the Help!", Toast.LENGTH_LONG).show();
		break;

		}
		return true;
	}


	private void refreshAllAccounts() {

		refreshing=true;
		refreshCount=0;
		LJPro app=(LJPro) getApplication();
		app.Dialog = new WeakReference<ProgressDialog>(new ProgressDialog(Accounts.this));
		ProgressDialog refresher=(ProgressDialog) app.Dialog.get();
		refresher.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

		refresher.setMessage(getString(R.string.refreshing));
		refresher.setCancelable(false);
		refresher.show();
		boolean haveMore=mCursor.moveToFirst();
		while(!mCursor.isAfterLast()) {
			String journalname=mCursor.getString(accInd);
			Intent updatelogin=new Intent(LJNet.LJ_LOGIN);
			updatelogin.putExtra("journalname", journalname);
			WakefulIntentService.sendWakefulWork(mContext, updatelogin);
			mCursor.moveToNext();
		}


	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		Cursor clicked=(Cursor) m_adapter.getItem(info.position);
		clickIndex=info.position;
		lpName=clicked.getString(accInd);
		MenuInflater inflater = getMenuInflater();
		if (appPrefs.getString("defaultlogin", "").equals(lpName))
		{inflater.inflate(R.menu.accountcontext2, menu);}
		else inflater.inflate(R.menu.accountcontext, menu);
		menu.setHeaderTitle(lpName);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.deluser:   
		(new Thread(new Runnable() { public void run(){	Boolean success=Accounts.this.LJDBAdapter.deleteAcct(lpName);
			if (success) {
				SharedPreferences.Editor editor = appPrefs.edit();
				Date d = new Date();
				
				editor.putLong(lpName + "friends_lastupdate", 0);
				editor.putLong(lpName + "friendspage_lastupdate", 0);
				editor.commit(); 
				updateAccounts();
			}
		}
			})).start();

			break;
		case R.id.editfriends:     
									//Debug.startMethodTracing("EditFriends");
									Intent editfriends=new Intent(this,FriendsTab.class);
									editfriends.putExtra("journalname", lpName);
									editfriends.putExtra("tab", 0);
								   startActivity(editfriends);
								   break;
								   
		case R.id.editgroups:     
		Intent editgroups=new Intent(this,FriendsTab.class);
		editgroups.putExtra("journalname", lpName);
		editgroups.putExtra("tab", 1);
	   startActivity(editgroups);
	   break;
	   
		case R.id.manuallogin: 
		edit=appPrefs.edit();
		edit.putString("defaultlogin", null);
		edit.commit();
		break;					   
		case R.id.makedefault:     	edit=appPrefs.edit();
									edit.putString("defaultlogin", lpName);
									edit.commit();
		break;

		}
		return true;
	}

	private Editor edit;



}


