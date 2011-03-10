package com.electronapps.LJPro;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;



import com.commonsware.cwac.cache.SimpleWebImageCache;
import com.commonsware.cwac.thumbnail.ThumbnailAdapter;
import com.commonsware.cwac.thumbnail.ThumbnailBus;
import com.commonsware.cwac.thumbnail.ThumbnailMessage;
import com.commonsware.cwac.wakeful.WakefulIntentService;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class EditFriends extends ListActivity {

	private Cursor mFriendsCursor;
	private Cursor mGroupsCursor;
	private LJDB LJDBAdapter;
	private SimpleWebImageCache<ThumbnailBus, ThumbnailMessage> imgCache;
	private SharedPreferences appPrefs;

	private FriendsAdapter m_adapter;
	private static Context mContext;
	boolean refreshing = false;
	boolean[] checked = null;
	boolean addShown = false;
	private boolean DEBUG =false;
	private HashMap<Integer,BigInteger> groupMasks=new HashMap<Integer,BigInteger>();
	private String journalname = "";
	private int clickIndex = -1;
	private boolean[] fgSel = new boolean[30];
	private boolean deleting = false;
	private boolean adding = false;

	public static final String TAG = Accounts.class.getSimpleName();
	protected static final boolean[] fgroupSel = null;
	private boolean Tracing=false;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			refreshing = savedInstanceState.getBoolean("updating");
			deleting = savedInstanceState.getBoolean("deleting");
			adding = savedInstanceState.getBoolean("adding");
			groupMasks = (HashMap<Integer, BigInteger>) savedInstanceState.getSerializable("groupsCache");
			checked = savedInstanceState.getBooleanArray("checked");
			addShown = savedInstanceState.getBoolean("addShown");
		}
		
		mContext = getApplicationContext();
		Intent intent = getIntent();
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			Tracing=true;
			//Debug.startMethodTracing("EditFriends");
		}
		journalname = intent.getStringExtra("journalname");
		View friends = View.inflate(this, R.layout.friends, null);
		TextView header = (TextView) friends.findViewById(R.id.fheader);
		if (header != null) {
			header.setText(journalname);
			header.invalidate();
		}
		if (addShown) {
			showAddFriend(friends);
		}
		setContentView(friends);
		m_adapter = new FriendsAdapter(EditFriends.this,null,R.layout.friendrow);
		setListAdapter(m_adapter);
	

		imgCache = ((LJPro) getApplication()).getImageCache();
		imgCache.getBus().register(toString(), onCache);
		appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		registerForContextMenu(getListView());
		populateFriendList();
		mContext = getApplicationContext();

	}

	private ThumbnailBus.Receiver<ThumbnailMessage> onCache = new ThumbnailBus.Receiver<ThumbnailMessage>() {
		public void onReceive(final ThumbnailMessage message) {
			final ImageView image = message.getImageView();

			runOnUiThread(new Runnable() {
				public void run() {
					if (image.getTag() != null
							&& image.getTag().toString().equals(
									message.getUrl())) {
						image.setImageDrawable(imgCache.get(message.getUrl()));
					}
				}
			});
		}

	};

	private void populateFriendList() {
	

		Runnable createFriendsList = new Runnable() {
			public void run() {
				getFriends();
			}
		};

		Thread thread = new Thread(null, createFriendsList,
				"FriendList Background");

		thread.start();
		ListView listView = getListView();
		listView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> adapter, View view,
					int position, long arg3) {
				EditFriends.this.clickIndex=position;
				showFriendInfo(position, view);
			}

		});
	}

	protected void getFriends() {
		try {
			LJDBAdapter = LJDB.getDB(getApplicationContext());
			LJDBAdapter.open();
			mFriendsCursor = LJDBAdapter.getFriends(journalname, null);
			mGroupsCursor = LJDBAdapter.getFriendGroups(journalname, null);
			mFriendsCursor.getCount();
			startManagingCursor(mFriendsCursor);
			startManagingCursor(mGroupsCursor);
			getColumnIndices();
			if (checked == null) {
				checked = new boolean[mFriendsCursor.getCount()];
				for (int i=0;i<checked.length;i++) {
					checked[i]=false;
					
				}
			}
			runOnUiThread(new Runnable() { public void run() {
				m_adapter = new FriendsAdapter(EditFriends.this, mFriendsCursor,R.layout.friendrow);
				final int[] IMAGE_IDS = { R.id.duserpic };
				setListAdapter(new ThumbnailAdapter(EditFriends.this, m_adapter, imgCache,IMAGE_IDS));
				
			}
			});
			Date d = new Date();
			if (!refreshing
					&& (DEBUG | (d.getTime()- appPrefs.getLong(journalname + "friends_lastupdate",0) > appPrefs.getLong("friendsSync", 900000)))) {
				refreshing = true;
				Intent updatefriends = new Intent(LJNet.LJ_GETFRIENDS);
				updatefriends.putExtra("journalname", journalname);
				WakefulIntentService.sendWakefulWork(mContext, updatefriends);
			} else {
				hideUpdating();
			}
		
	}
		catch(Throwable t) {
		  Log.e(TAG,t.getMessage(),t);
		}
	}

	private int fnameInd;
	private int fbirthdayInd;
	private int fupicInd;
	private int ffullnameInd;
	private int fgroupsInd;
	private int gnameInd;
	private int gidInd;

	void getColumnIndices() {
		fnameInd = mFriendsCursor.getColumnIndex(LJDB.KEY_FRIENDNAME);
		fupicInd=mFriendsCursor.getColumnIndex(LJDB.KEY_USERPIC);
		fbirthdayInd = mFriendsCursor.getColumnIndex(LJDB.KEY_BIRTHDAY);
		ffullnameInd = mFriendsCursor.getColumnIndex(LJDB.KEY_FULLNAME);
		fgroupsInd = mFriendsCursor.getColumnIndex(LJDB.KEY_GROUPMASK);
		gnameInd = mGroupsCursor.getColumnIndex(LJDB.KEY_NAME);
		gidInd = mGroupsCursor.getColumnIndex(LJDB.KEY_ID);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean("updating", refreshing);
		outState.putBoolean("deleting", deleting);
		outState.putBoolean("adding", adding);
		outState.putBoolean("addShown", addShown);
		outState.putSerializable("groupsCache", groupMasks);
		outState.putBooleanArray("checked", checked);

	}

	@Override
	public void onResume() {
		super.onResume();
		IntentFilter friendfilter = new IntentFilter();
		friendfilter.addAction(LJNet.LJ_XMLERROR);
		friendfilter.addAction(LJNet.LJ_FRIENDSUPDATED);
		friendfilter.addAction(LJNet.LJ_FRIENDADDED);
		friendfilter.addAction(LJNet.LJ_FRIENDEDITED);
		friendfilter.addAction(LJNet.LJ_FRIENDDELETED);
		registerReceiver(LJFriendsReceiver, friendfilter);
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(LJFriendsReceiver);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
	}
	
	

	public BroadcastReceiver LJFriendsReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(LJNet.LJ_XMLERROR)) {
				removeStickyBroadcast(intent);
				
					LJPro app=(LJPro) getApplication();
					app.alertNetworkError(EditFriends.this);
					refreshing = false;
					View updating = findViewById(R.id.updatingfriends);
					updating.setVisibility(View.GONE);


			}

			else if (action.equals(LJNet.LJ_FRIENDSUPDATED)) {
				removeStickyBroadcast(intent);

				if (refreshing) {
					refreshing = false;
					View updating = findViewById(R.id.updatingfriends);
					updating.setVisibility(View.GONE);
					doneToast(getString(R.string.friendsupdated));
				}

				updateFriends();
			}

			else if (action.equals(LJNet.LJ_FRIENDADDED)) {
				removeStickyBroadcast(intent);
				hideUpdating();
				broadcastUpdate();
				final SharedPreferences appPrefs= PreferenceManager.getDefaultSharedPreferences(EditFriends.this);
				if(appPrefs.getBoolean(journalname+"_alwaysRefetchOnAdd",false )) {
					refetchFriendsPage();
				}
				else if (!appPrefs.getBoolean(journalname+"_alwaysRefetchOnAdd",false )&&!appPrefs.getBoolean(journalname+"_neverRefectechOnAdd",false ))
				{
					refetchDialog();
					}
				
				final Bundle extras=intent.getExtras();
				final Runnable addFriend=new Runnable(){

					public void run() {
						
						ContentValues group=new ContentValues();
						group.put("accountname",journalname);
						group.put("username", extras.getString("friendname"));
						group.put("fullname",extras.getString("fullname"));
						group.put("updated",System.currentTimeMillis()/1000l);
						boolean success=LJDBAdapter.addDummyFriend(group);
						if (success) updateFriends();
						
					}
				};
					Thread friendadd=new Thread(null,addFriend,"Add Friend");
					friendadd.start();

				View updating = findViewById(R.id.updatingfriends);
				EditText addfriend = (EditText) findViewById(R.id.addfriend);
				addfriend.setText("");

				updating.setVisibility(View.GONE);
				Toast.makeText(EditFriends.this, R.string.friendadded,
						Toast.LENGTH_LONG).show();
			}

			else if (action.equals(LJNet.LJ_FRIENDDELETED)) {
				removeStickyBroadcast(intent);
				hideUpdating();
				broadcastUpdate();
				updateFriends();

			}

			else if (action.equals(LJNet.LJ_FRIENDEDITED)) {
				removeStickyBroadcast(intent);
				hideUpdating();
				broadcastUpdate();
				updateFriends();
			}

		}

		
		

	};
	
	private void refetchDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.refetchfp));
		LinearLayout dialog=(LinearLayout) View.inflate(this, R.layout.refetchdialog, null);
		CheckBox alwaysadd=(CheckBox) dialog.findViewById(R.id.alwaysadd);
		final SharedPreferences.Editor editor = appPrefs.edit();
		alwaysadd.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				
					editor.putBoolean(journalname+"_alwaysRefetchOnAdd",isChecked);
					editor.commit();
				
			}
			
		});
		
		CheckBox neveradd=(CheckBox) dialog.findViewById(R.id.neveradd);
		alwaysadd.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
					
					editor.putBoolean(journalname+"_neverRefetchOnAdd",isChecked);
					editor.commit();

			}
			
		});
		builder.setView(dialog);
		builder.setCancelable(false);
	
			builder.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int id) {
							refetchFriendsPage();
							
						}
					});
			builder.setNegativeButton("No",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
		 
		AlertDialog alert = builder.create();
		alert.setOwnerActivity(EditFriends.this);
		alert.show();
		
	}
	
	


	private void refetchFriendsPage() {
		
		((LJPro)mContext).notifySync(journalname);
		Intent refetch = new Intent(LJNet.LJ_GETFRIENDSPAGE);
		refetch.putExtra("journalname", journalname);
		refetch.putExtra("refreshOld", true);
		WakefulIntentService.sendWakefulWork(mContext, refetch);
		
	}

	
	private Runnable reQueryInBackground = new Runnable() {

		public void run() {
			mFriendsCursor = LJDBAdapter.getFriends(journalname, null);
			mFriendsCursor.getCount();
			updateUI();
			mGroupsCursor.requery();
			startManagingCursor(mFriendsCursor);
			checked = new boolean[mFriendsCursor.getCount()];
			for (int i=0;i<checked.length;i++) {
				checked[i]=false;
				
			}
			

		}
	};
	
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		if (addShown&&keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			addShown=false;
			View updating = findViewById(R.id.addfriendfooter);
			updating.setVisibility(View.GONE);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
	

	Runnable refreshList=new Runnable() { public void run() { 
		m_adapter.changeCursor(mFriendsCursor);
		if(Tracing) Debug.stopMethodTracing();
		}};
	
	private void updateUI() {
		runOnUiThread(refreshList);
	}


	private void updateFriends() {
		// Very important
		groupMasks.clear();
		Thread thread = new Thread(null, reQueryInBackground,
				"RefreshFriendList Background");
		try {
			thread.start();
		} catch (Throwable e) {
			Log.e(TAG, e.getMessage(),e);
		}
	}

	protected void broadcastUpdate() {
		Intent updatefriends = new Intent(LJNet.LJ_GETFRIENDS);
		updatefriends.putExtra("journalname", journalname);
		WakefulIntentService.sendWakefulWork(mContext, updatefriends);

	}

	protected void doneToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.friendsops, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.addfriend:
			View editfriends = findViewById(R.id.editfriends);
			showAddFriend(editfriends);

			break;
		case R.id.settings:
			Intent prefs=new Intent(getApplicationContext(),LJProPrefs.class);
			prefs.putExtra("scope", journalname);
			startActivity(prefs);

			break;
		case R.id.refresh:
			Intent updatefriends = new Intent(LJNet.LJ_GETFRIENDS);
			updatefriends.putExtra("journalname", journalname);
			WakefulIntentService.sendWakefulWork(mContext, updatefriends);
			showUpdating();

			break;

		case R.id.help:
			Toast.makeText(this, "You pressed the Help!", Toast.LENGTH_LONG)
					.show();
			break;

		case R.id.removefriends:
			showDelDialog();

			break;

		}
		return true;
	}

	private void showAddFriend(View view) {
		addShown = true;
		View addfriend = view.findViewById(R.id.addfriendfooter);
		if (addfriend != null)
			addfriend.setVisibility(View.VISIBLE);
		Button add = (Button) addfriend.findViewById(R.id.pfriendbutton);
		if (add != null) {
			add.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					EditText addfriend = (EditText) findViewById(R.id.addfriend);
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(addfriend.getWindowToken(), 0);
					String addFriend = addfriend.getText().toString();
					if (addFriend.length() > 0) {
						View updating = findViewById(R.id.addfriendfooter);
						updating.setVisibility(View.GONE);
						showUpdating();
						Intent editfriends = new Intent(LJNet.LJ_EDITFRIENDS);
						editfriends.putExtra("journalname", journalname);
						editfriends.putExtra("addfriend", addFriend);
						WakefulIntentService.sendWakefulWork(mContext,
								editfriends);
					} else {
						View addfooter = findViewById(R.id.addfriendfooter);
						addfooter.setVisibility(View.GONE);

					}

				}

			});
		}
	}

	protected void showEditGroupsDialog() {
		// TODO check for groups, still harvesting??
		int numGroups = mGroupsCursor.getCount();
		final CharSequence[] items = new CharSequence[numGroups];

		BigInteger groupmask=groupMasks.get(clickIndex);
		if(groupmask ==null) {
			mFriendsCursor.moveToPosition(clickIndex);
			groupmask=BigInteger.valueOf(mFriendsCursor.getInt(fgroupsInd));
			groupMasks.put(clickIndex, groupmask);
		}
			mGroupsCursor.moveToFirst();
			while (!mGroupsCursor.isAfterLast()) {
				int i = mGroupsCursor.getPosition();
				items[i] = mGroupsCursor.getString(gnameInd);
				int id = mGroupsCursor.getInt(gidInd);
				if (groupmask.testBit(id))
					fgSel[i] = true;
				else
					fgSel[i] = false;
				mGroupsCursor.moveToNext();
			}
		

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.editfgroups));
		builder.setMultiChoiceItems(items, fgSel, fgroupListener);
		builder.setCancelable(true);
		if (items.length > 0) {
			builder.setPositiveButton("Update",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int id) {
							showUpdating();
							mFriendsCursor.moveToPosition(clickIndex);
							String friendname = mFriendsCursor.getString(fnameInd);
							Intent editgroups = new Intent(LJNet.LJ_EDITFRIENDS);
							editgroups.putExtra("journalname", journalname);
							editgroups.putExtra("groupmask",groupMasks.get(clickIndex).intValue());
							//editgroups.putExtra("groupedits", new String(groupsCache.get(clickIndex)));
							editgroups.putExtra("friend", friendname);
							WakefulIntentService.sendWakefulWork(mContext,
									editgroups);
						}
					});
			builder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
		} else {
			builder.setTitle(getString(R.string.nofgroups));
			builder.setNegativeButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
		}
		AlertDialog alert = builder.create();
		alert.setOwnerActivity(EditFriends.this);
		alert.show();

	}

	private DialogInterface.OnMultiChoiceClickListener fgroupListener = new DialogInterface.OnMultiChoiceClickListener() {

		public void onClick(DialogInterface dialog, int which, boolean isChecked) {
			//char[] fgroups = groupsCache.get(clickIndex);
			BigInteger groupmask=groupMasks.get(clickIndex);
			fgSel[which] = isChecked;
			mGroupsCursor.moveToPosition(which);
			if (isChecked) groupMasks.put(clickIndex, groupmask.setBit(mGroupsCursor.getInt(gidInd)));
			else groupMasks.put(clickIndex,groupmask.clearBit(mGroupsCursor.getInt(gidInd)));
			//fgroups[mGroupsCursor.getInt(gidInd)] = isChecked ? '1' : '0';

		}

	};
	protected boolean groupedits = false;

	private void showDelDialog() {
		final ArrayList<CharSequence> del = new ArrayList<CharSequence>();
		for (int i = 0; i < checked.length; i++) {
			if (checked[i]) {
				//checked[i]=false;
				mFriendsCursor.moveToPosition(i);
				del.add(mFriendsCursor.getString(fnameInd));
			}
		}
		final CharSequence[] items = (CharSequence[]) del
				.toArray(new CharSequence[del.size()]);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (items.length > 0)
			builder.setTitle(getString(R.string.confirmdel));
		else
			builder.setTitle(getString(R.string.nochecked));
		builder.setItems(items, null);
		builder.setCancelable(true);
		if (items.length > 0) {
			builder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int id) {
							Intent delfriends = new Intent(LJNet.LJ_EDITFRIENDS);
							showUpdating();
							delfriends.putExtra("journalname", journalname);
							delfriends.putExtra("delfriends", items);
							WakefulIntentService.sendWakefulWork(mContext,
									delfriends);
							Runnable delFriends = new Runnable() {

								public void run() {
									LJDBAdapter.deleteFriend(del, journalname);
									updateFriends();
								}

							};

							Thread thread = new Thread(null, delFriends,
									"delFriends Background");
							try {
								thread.start();
							} catch (Throwable e) {
								Log.e(TAG, e.getMessage(),e);
							}

						}
					});
			builder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
		} else {
			builder.setNegativeButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
		}
		AlertDialog alert = builder.create();
		alert.setOwnerActivity(EditFriends.this);
		alert.show();

	}

	private void showUpdating() {
		
		runOnUiThread( new Runnable() {
			public void run() {refreshing=true;
			View updating=findViewById(R.id.updatingfriends);
			updating.setVisibility(View.VISIBLE);
		}
		});
		
		
		
	}
	
	private void hideUpdating() {
		
		runOnUiThread( new Runnable() {
		public void run() {
			refreshing=true;
			View updating=findViewById(R.id.updatingfriends);
			updating.setVisibility(View.GONE);
		}
		});

	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		clickIndex = info.position;
		MenuInflater inflater = getMenuInflater();
		if (checked[clickIndex]) {
			inflater.inflate(R.menu.friendscontext2, menu);
		} else {
			inflater.inflate(R.menu.friendscontext, menu);
		}
		mFriendsCursor.moveToPosition(clickIndex);
		menu.setHeaderTitle(mFriendsCursor.getString(fnameInd));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.markdelete:
			toggleDeletion(clickIndex);
			break;
		case R.id.unmarkdelete:
			toggleDeletion(clickIndex);
			break;
		case R.id.editgroups:
			showEditGroupsDialog();
			break;
		}
		return true;
	}

	private void showFriendInfo(int position, View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);

		mFriendsCursor.moveToPosition(position);

		View infoview = View.inflate(this, R.layout.friendinfoview, null);
		View headerview = View.inflate(this, R.layout.friendinfoheader, null);
		TextView username = (TextView) headerview.findViewById(R.id.friendname);
		username.setText(mFriendsCursor.getString(fnameInd));
		ImageView friendpic = (ImageView) headerview
				.findViewById(R.id.friendpic);
		String url = mFriendsCursor.getString(fupicInd);
		if (url != null) {
			friendpic.setTag(url);
			ThumbnailMessage msg = imgCache.getBus().createMessage(toString());

			msg.setImageView(friendpic);
			msg.setUrl(friendpic.getTag().toString());

			try {
				imgCache.notify(msg.getUrl(), msg);
			} catch (Throwable t) {
				Log.e(TAG, "Exception trying to fetch image", t);
			}

		} else {
			friendpic.setImageResource(R.drawable.defaultuserpic);
		}
		builder.setCustomTitle(headerview);
		String full = mFriendsCursor.getString(ffullnameInd);
		String bday = mFriendsCursor.getString(fbirthdayInd);
		if (full.length()>0)
			((TextView) infoview.findViewById(R.id.fullname)).setText(Html
					.fromHtml("<b>Full Name: </b>" + full));
		if (bday.length()>0)
			((TextView) infoview.findViewById(R.id.birthday)).setText(Html
					.fromHtml("<b>Birthday: </b>" + bday));
		builder.setView(infoview);
		builder.setPositiveButton("Edit Groups",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						showEditGroupsDialog();

					}
				});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog friendinfo = builder.create();
		friendinfo.setOwnerActivity(EditFriends.this);
		friendinfo.show();

	}

	public void toggleDeletion(int index) {
		ListView listView = getListView();
		int firstPosition = listView.getFirstVisiblePosition(); // This is the
																// same as child
																// #0
		int wantedChild = index - firstPosition;
		// Say, first visible position is 8, you want position 10, wantedChild
		// will now be 2
		// So that means your view is child #2 in the ViewGroup:
		if (wantedChild < 0 || wantedChild >= listView.getChildCount()) {
			Log
					.w(
							TAG,
							"Unable to get view for desired position, because it's not being displayed on screen.");
		} else {
			// Could also check if wantedPosition is between
			// listView.getFirstVisiblePosition() and
			// listView.getLastVisiblePosition() instead.
			View row = listView.getChildAt(wantedChild);
			if (row != null) {
				CheckBox checked = (CheckBox) row.findViewById(R.id.delete);
				checked.setChecked(!checked.isChecked());
				checked.invalidate();
			}

		}

	}

	public void setChecked(Integer index, boolean isChecked) {

		checked[index] =isChecked;
	}

	public boolean getChecked(Integer i) {
		return checked[i];
	}

}
