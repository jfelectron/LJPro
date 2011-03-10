package com.electronapps.LJPro;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.commonsware.cwac.cache.AsyncCache;
import com.commonsware.cwac.cache.SimpleWebImageCache;
import com.commonsware.cwac.thumbnail.ThumbnailAdapter;
import com.commonsware.cwac.thumbnail.ThumbnailBus;
import com.commonsware.cwac.thumbnail.ThumbnailMessage;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.electronapps.LJPro.LJTypes.Friend;
import com.electronapps.LJPro.LJTypes.Post;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater.Filter;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class FriendsPage extends ListActivity {

	private Cursor mCursor;
	private LJDB LJDBAdapter;
	private SharedPreferences appPrefs;
	private FriendsPageAdapter m_adapter;
	private static Context mContext;
	boolean refreshing = false;
	boolean adddel=false;
	private static boolean TRACE=true;
	private SharedPreferences.Editor editor;
	

	private HashMap<String,String[]> groupsHash;
	private String journalname = "";
	private SimpleWebImageCache<ThumbnailBus, ThumbnailMessage> imgCache;

	public static final String TAG = Accounts.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (TRACE) Debug.startMethodTracing("FriendsPage");
		if (savedInstanceState != null) {
			refreshing = savedInstanceState.getBoolean("updating");
			groupsHash=(HashMap<String, String[]>) savedInstanceState.getSerializable("groupsHash");
			mPosition=savedInstanceState.getInt("position");
		}
		
		Intent intent = getIntent();
		journalname = intent.getStringExtra("journalname");
		mContext = getApplicationContext();
		View friendspage = View.inflate(this, R.layout.friendspage, null);
		TextView header = (TextView) friendspage.findViewById(R.id.fpheader);
		if (header != null) {
			header.setText(getString(R.string.friendspage));
		}
		setContentView(friendspage);
		
		
		appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		imgCache = ((LJPro) mContext).getImageCache();
		mCurrentFilter=getString(R.string.allfriends);
		populateFriendsPage();
		registerForContextMenu(getListView());
		

	}
	private int clickIndex;
	private String lpName;
	private int jnameInd;
	private int jtypeInd;
	private String lpPName;
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		Cursor clicked=(Cursor) m_adapter.getItem(info.position);
		clickIndex=info.position;
		MenuInflater inflater = getMenuInflater();
		String type=clicked.getString(jtypeInd);
		lpName=clicked.getString(jnameInd);
		lpPName=clicked.getString(pnameInd);
		if (type.equals("P")||type.equals("Y")){
			inflater.inflate(R.menu.fpcontext1, menu);
		}
		else {
			inflater.inflate(R.menu.fpcontext2, menu);
		}

	
		menu.setHeaderTitle(lpName);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.delete_friend:
			CharSequence[] del={lpName};
			Toast.makeText(this, getString(R.string.deleting_)+lpName+getString(R.string.as_friend), Toast.LENGTH_SHORT).show();
			Intent delfriends = new Intent(LJNet.LJ_EDITFRIENDS);
			adddel=true;
			delfriends.putExtra("journalname", journalname);
			delfriends.putExtra("delfriends",del);
			WakefulIntentService.sendWakefulWork(mContext,delfriends);
			break;
		case R.id.addposter:
			Intent editfriends = new Intent(LJNet.LJ_EDITFRIENDS);
			adddel=true;
			Toast.makeText(this, getString(R.string.adding_)+lpName+getString(R.string.as_friend), Toast.LENGTH_SHORT).show();
			editfriends.putExtra("journalname", journalname);
			editfriends.putExtra("addfriend", lpPName);
			WakefulIntentService.sendWakefulWork(mContext,
					editfriends);
			break;
		case R.id.view_journal:
			break;
		case R.id.view_community:
			break;
		
		}
	
		return true;
	}
	public HashMap<String,String[]> getGroupsHash() {
		return groupsHash;
		
	}
	
	
	private String [] mExtraArgs=null;
	private String mExtraWhere=null;
	
	public void setLastQuery(String where, String[] args) {
		mExtraArgs=args;
		mExtraWhere=where;
		
	}

	Cursor mSpinnerCursor=null;
	String mCurrentFilter;
	int mFilterPosition=0;
	boolean spinnerLoaded=false;
	private int pnameInd;
	private int mPosition=0;
	private void populateSpinner(boolean haveSyn) {
		
		final Spinner s = (Spinner) findViewById(R.id.fpfilter);
	
		s.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				mSpinnerCursor.moveToPosition(position);
				String filter=mSpinnerCursor.getString(mSpinnerCursor.getColumnIndex(LJDB.KEY_NAME));
				if (spinnerLoaded&&!filter.equals(mCurrentFilter)) {
					mCurrentFilter=filter;
					mFilterPosition=position;
					Cursor filtered=m_adapter.runQueryOnBackgroundThread(filter);
					if(!filtered.equals(mCursor)) {
						m_adapter.changeCursor(filtered);
						FriendsPage.this.getListView().setSelection(0);
						mCursor.close();
						mCursor=filtered;
				}
				else {
					//Todo:TOAST!
				}
					
				}
				if (!spinnerLoaded) {
					spinnerLoaded=true;
					s.setSelection(mFilterPosition);
				}
			}

			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}}
		);
		String [] fgFields={LJDB.KEY_NAME,LJDB.KEY_ID,"_id"};
		
		Cursor fg=LJDBAdapter.getFriendGroups(journalname,fgFields);
		mSpinnerCursor= new FilterCursor(fg,mContext,haveSyn);
		startManagingCursor(mSpinnerCursor);
		startManagingCursor(fg);
		populateGroupsHash(fg);
		
		runOnUiThread(new Runnable() {public void run(){
			
			SimpleCursorAdapter adapter2 = new SimpleCursorAdapter(FriendsPage.this,R.layout.simple_spinner_item,mSpinnerCursor,new String[] {LJDB.KEY_NAME}, new int[] {android.R.id.text1});  // Give the cursor to the list adapter
			
				adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			s.setAdapter(adapter2);}} );
	
		
	}
	
	private void populateGroupsHash(Cursor fg) {
		if (groupsHash==null) {
			groupsHash=new HashMap<String,String[]>();
		String [] frFields={LJDB.KEY_FRIENDNAME,LJDB.KEY_GROUPMASK};
 		Cursor fr=LJDBAdapter.getFriends(journalname, frFields);
 	
		 int numFriends=fr.getCount();
		 int numGroups=fg.getCount();
		 if (numFriends!=0&&numGroups!=0) {
			 String [] fnames=new String[numFriends];
			 BigInteger[] groupmasks=new BigInteger[numFriends];
			 fr.moveToFirst();
			for (int i=0;i<numFriends;i++) {
				fnames[i]=fr.getString(0);
				groupmasks[i]=BigInteger.valueOf(fr.getInt(1)); 
				fr.moveToNext();
			 }
			fg.moveToFirst();
			for (int j=0;j<numGroups;j++) {
				int id=fg.getInt(1);
				ArrayList<String >members=new ArrayList<String>();
				String name=fg.getString(0);
				for (int k=0;k<numFriends;k++) {
					if (groupmasks[k].testBit(id)) members.add(fnames[k]);
				}
				int size=members.size();
				if (size>0) {
					groupsHash.put(name, (String []) members.toArray(new String[size]));
				}
				fg.moveToNext();
			}
			
			
		 }
		 fr.close();
		}
		
		
		
	}
	@Override
	
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("groupsHash", groupsHash);
	
		outState.putInt("position",getSelectedItemPosition());
		
	}

	private void populateFriendsPage() {
	

		Runnable createFriendsPage = new Runnable() {
			public void run() {
				getPosts();
			}
		};

		Thread thread = new Thread(null, createFriendsPage,
				"FriendsPage Background");

		thread.start();
		ListView listView = getListView();
		listView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> adapter, View view,
					int position, long arg3) {
				Intent loadFull = new Intent(mContext, FullPost.class);
				loadFull.putExtra("journalname",journalname);
				loadFull.putExtra("position", position);
				loadFull.putExtra("extraWhere", mExtraWhere);
				loadFull.putExtra("extraArgs", mExtraArgs);
				
				startActivityForResult(loadFull,1);
			}

		});
	}
	
	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		setSelection(resultCode);
		mPosition=resultCode;
		
		
	}
	
	

	protected void getPosts() {
		LJDBAdapter = LJDB.getDB(getApplicationContext());
		LJDBAdapter.open();
		mCursor = LJDBAdapter.getFriendsPage(journalname,mExtraWhere,mExtraArgs,null);
		mCursor.getCount();
		jnameInd=mCursor.getColumnIndex(LJDB.KEY_JOURNALNAME);
		jtypeInd=mCursor.getColumnIndex(LJDB.KEY_JOURNALTYPE);
		pnameInd=mCursor.getColumnIndex(LJDB.KEY_POSTERNAME);
		Cursor syn=LJDBAdapter.getSyn(journalname);
		//startManagingCursor(mCursor);
		Integer haveSyn=syn.getCount();
		syn.close();
		
		
		runOnUiThread(new Runnable(){ public void run() {
		final int[] IMAGE_IDS = { R.id.duserpic };
		m_adapter = new FriendsPageAdapter(FriendsPage.this,journalname, mCursor, R.layout.postrow);
		setListAdapter(new ThumbnailAdapter(FriendsPage.this, m_adapter, imgCache,
				IMAGE_IDS));
		}
		});
		
		populateSpinner(haveSyn>0);
		Date d = new Date();
		Boolean appRefresh=((LJPro)mContext).fprefreshing.get(journalname)==null?false:((LJPro)mContext).fprefreshing.get(journalname);
		if (refreshing||appRefresh) {
			showUpdating();
			if(appRefresh) {
				((LJPro)mContext).clearNotification(LJPro.SYNC_ID);
			}
		}
		
		

	}

	public BroadcastReceiver LJFriendsPageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (action.equals(LJNet.LJ_XMLERROR)) {
				removeStickyBroadcast(intent);
				
					LJPro app=(LJPro) getApplication();
					app.alertNetworkError(FriendsPage.this);
					refreshing = false;
					View updating = findViewById(R.id.updatingfriends);
					updating.setVisibility(View.GONE);


			}

			if (action.equals(LJNet.LJ_FRIENDSPAGEUPDATED)) {
				abortBroadcast();
				if (m_adapter!=null) {
				  updateFriendsPage();
				}
			}
			
			if(action.equals(LJNet.LJ_FPDONEUPDATING)) {
				removeStickyBroadcast(intent);
				if (m_adapter!=null) {
				 updateFriendsPage();
				}
				hideUpdating();
			}
			
			if (action.equals(LJNet.LJ_FRIENDSUPDATED)) {
				removeStickyBroadcast(intent);
				Runnable updateHash=new Runnable() {

					public void run() {
						if (mSpinnerCursor!=null) {
						populateGroupsHash(mSpinnerCursor);
						}
					}
					
				};
				Thread t=new Thread(updateHash,"groupsHash updater");
				t.start();
			}
			if (action.equals(LJNet.LJ_FRIENDADDED)) {
				removeStickyBroadcast(intent);
				if (adddel) {
				Toast.makeText(FriendsPage.this, "Friend Added", Toast.LENGTH_SHORT);
				Intent updatefriends = new Intent(LJNet.LJ_GETFRIENDS);
				updatefriends.putExtra("journalname", journalname);
				WakefulIntentService.sendWakefulWork(mContext, updatefriends);
				if(appPrefs.getBoolean(journalname+"_alwaysRefetchOnAdd", false)) {
					((LJPro)mContext).notifySync(journalname);
					Intent refetch = new Intent(LJNet.LJ_GETFRIENDSPAGE);
					refetch.putExtra("journalname", journalname);
					refetch.putExtra("refreshOld", true);
					WakefulIntentService.sendWakefulWork(mContext, refetch);
				}
				}
			}
			if (action.equals(LJNet.LJ_FRIENDDELETED)) {
				removeStickyBroadcast(intent);
				if (adddel) {
					Toast.makeText(FriendsPage.this, "Friend Added", Toast.LENGTH_SHORT);
					Intent updatefriends = new Intent(LJNet.LJ_GETFRIENDS);
					updatefriends.putExtra("journalname", journalname);
					WakefulIntentService.sendWakefulWork(mContext, updatefriends);
					if (mCursor!=null) mCursor.requery();
				}
			}
		}
		
		

	};

	private void updateFriendsPage() {
		
	
		Thread thread = new Thread(null, reQueryInBackground,
				"RefreshFriendsPage Background");
		try {
			thread.start();
		} catch (Throwable e) {
			Log.e(TAG, e.getMessage(),e);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.fpops, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		
		case R.id.settings:
			Intent prefs=new Intent(getApplicationContext(),LJProPrefs.class);
			prefs.putExtra("scope", journalname);
			startActivity(prefs);

			break;
		case R.id.refresh:
			refreshing = true;
			Intent getfriendspage = new Intent(LJNet.LJ_GETFRIENDSPAGE);
			getfriendspage.putExtra("journalname", journalname);
			showUpdating();
			WakefulIntentService.sendWakefulWork(mContext, getfriendspage);
			break;

		case R.id.refetch:
			refreshing = true;
			Intent refetch = new Intent(LJNet.LJ_GETFRIENDSPAGE);
			refetch.putExtra("journalname", journalname);
			refetch.putExtra("refreshOld", true);
			showUpdating();
			WakefulIntentService.sendWakefulWork(mContext, refetch);
			break;

		case R.id.help:
			

			break;
			
		case R.id.setfilter:
			break;

		}
		return true;
	}


		private Runnable reQueryInBackground = new Runnable() {

			public void run() {
				mCursor = LJDBAdapter.getFriendsPage(journalname,mExtraWhere,mExtraArgs,null);
				updateUI();
				//startManagingCursor(mCursor);
				}
				

			
		};
		
		Runnable refreshList=new Runnable() { public void run() { 
			if (m_adapter!=null) m_adapter.changeCursor(mCursor);
			}};
		
		private void updateUI() {
			runOnUiThread(refreshList);
		}
	

	private void showUpdating() {
		runOnUiThread(updating);

	}
	
	Runnable updating=new Runnable() {
		public void run() {
			refreshing = true;
			View updating = findViewById(R.id.updatingfriends);
			TextView updatemsg = (TextView) updating.findViewById(R.id.updatemsg);
			updatemsg.setText(R.string.updatingfpage);
			updatemsg.invalidate();
			updating.setVisibility(View.VISIBLE);
		}
	};
	

	private void hideUpdating() {
		refreshing = false;
		View updating = findViewById(R.id.updatingfriends);
		updating.setVisibility(View.GONE);

	}
	
	@Override protected void onDestroy() {
		super.onDestroy();
		mCursor.close();
	}
	
	@Override protected void onPause() {
		super.onPause();
		unregisterReceiver(LJFriendsPageReceiver);
		Boolean appRefresh=((LJPro)mContext).fprefreshing.get(journalname)==null?false:((LJPro)mContext).fprefreshing.get(journalname);
		if (appRefresh) {
			((LJPro)mContext).notifySync(journalname);
		}
		if (TRACE) Debug.stopMethodTracing();
	}
	
	@Override protected void onResume() {
		super.onResume();
		spinnerLoaded=false;
		if (mCursor!=null) mCursor.requery();
		setSelection(mPosition);
		IntentFilter friendfilter = new IntentFilter();
		friendfilter.setPriority(1);
	  	friendfilter.addAction(LJNet.LJ_XMLERROR);
		friendfilter.addAction(LJNet.LJ_FRIENDSPAGEUPDATED);
		friendfilter.addAction(LJNet.LJ_FPDONEUPDATING);
		friendfilter.addAction(LJNet.LJ_FRIENDSUPDATED);
		friendfilter.addAction(LJNet.LJ_FRIENDADDED);
		friendfilter.addAction(LJNet.LJ_FRIENDDELETED);
		registerReceiver(LJFriendsPageReceiver, friendfilter);
		
	}
		
	

	public LJDB getDBConn() {
		return LJDBAdapter;
	}
	
	
	

	
	
	

}
