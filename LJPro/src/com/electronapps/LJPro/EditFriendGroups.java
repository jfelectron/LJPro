package com.electronapps.LJPro;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;



import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.electronapps.LJPro.LJTypes.Friend;
import com.electronapps.LJPro.LJTypes.FriendGroup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class EditFriendGroups extends ListActivity {
	
	private Cursor mFriendsCursor;
	private Cursor mGroupsCursor;
	private LJDB LJDBAdapter;
	private SharedPreferences appPrefs;
	private final Object mLock = new Object();
	private HashMap<Integer,BigInteger> groupsCache=new  HashMap<Integer,BigInteger>();

	
	private FriendGroupsAdapter m_adapter;
	private static Context mContext;
	private ObjectMapper serializer=new ObjectMapper();
	boolean refreshing=false;
	boolean addShown=false;
	private int clickIndex;
	private boolean DEBUG=false;
	private boolean[] checked=new boolean[30];
	private String journalname="";
	private FriendGroup lpObj;
	private boolean[] fgSel;
	private boolean deleting=false;
	private boolean adding=false;
	private HashMap<Integer, Boolean> groupHash;
	private int fnameInd;
	private int fgroupsInd;
	private int gnameInd;
	private int gidInd;

	
	public static final String TAG = Accounts.class.getSimpleName();
	protected static final boolean[] fgroupSel = null;

	   @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        if (savedInstanceState!=null) {
	        	refreshing=savedInstanceState.getBoolean("updating");
	        	addShown=savedInstanceState.getBoolean("addShown");
	        	groupsCache=(HashMap<Integer, BigInteger>) savedInstanceState.getSerializable("groupsCache");
	        	checked=savedInstanceState.getBooleanArray("checked");
	        }
	        mContext=getApplicationContext();
	        Intent intent=getIntent();
	        
	        journalname=intent.getStringExtra("journalname");
	        View friends=View.inflate(this,R.layout.friendsgroups,null);
	        TextView header=(TextView) friends.findViewById(R.id.fgheader);
	        if (header!=null) {
			   header.setText(journalname);
			   header.invalidate();
	        }
	        setContentView(friends);
	        m_adapter = new FriendGroupsAdapter(EditFriendGroups.this,null,R.layout.friendrow);
			setListAdapter(m_adapter);
	        if (addShown) {
	        	showAddFriend(friends);
	        }
	        if(refreshing) {
	        	showUpdating();
	        }
	       
	       
	       
	        
	        appPrefs=PreferenceManager.getDefaultSharedPreferences(this);
	        registerForContextMenu(getListView());
	        populateFriendList();
	        mContext=getApplicationContext();
	        
	    }
	   

	   
	


	private void populateFriendList() {
		
		   
		       
		 
		      
		        
		     
		        
		       Runnable createFriendsList=new Runnable() {
		            public void run() {
		                getFriends();
		            }
		        };
		        
		        
		     
		        Thread thread = new Thread(null,createFriendsList, "FriendList Background");
		       
		        thread.start();
		        ListView listView=getListView();
		        listView.setOnItemClickListener(new OnItemClickListener() {
		           
		            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
		               clickIndex=position;
		               showEditGroupsDialog();
		            }

					

					
		        });
		    }
		    
		
		

	


	protected void getFriends() {
		try {
			String[] fields=new String[2];
	    	fields[0]=LJDB.KEY_FRIENDNAME;
	    	fields[1]=LJDB.KEY_GROUPMASK;
	    	LJDBAdapter=LJDB.getDB(getApplicationContext());
		    LJDBAdapter.open();
	    	mFriendsCursor=LJDBAdapter.getFriends(journalname,fields);
	    	mGroupsCursor=LJDBAdapter.getFriendGroups(journalname,null);
	    	startManagingCursor(mFriendsCursor);
			startManagingCursor(mGroupsCursor);
			getColumnIndices();
			getGroupHash();
			runOnUiThread(new Runnable() {public void run() {
				m_adapter = new FriendGroupsAdapter(EditFriendGroups.this,mGroupsCursor,R.layout.friendgrouprow);
			    setListAdapter(m_adapter);
			}});
	    
		    Date d=new Date();
			if (!refreshing&&(DEBUG|(d.getTime()-appPrefs.getLong(journalname+"friends_lastupdate",0)>appPrefs.getLong("friendsSync", 900000 ))))
			{  refreshing=true;
				Intent updatefriends=new Intent(LJNet.LJ_GETFRIENDS);
			   updatefriends.putExtra("journalname",journalname);
			   WakefulIntentService.sendWakefulWork(mContext,updatefriends);
			}
			else {
				hideUpdating();
			}
	    	}
	    	catch (Throwable r) {
	    		Log.e(TAG,r.getMessage(),r);
	    	}
	}
	
	
	void getColumnIndices() {
		fnameInd=mFriendsCursor.getColumnIndex(LJDB.KEY_FRIENDNAME);
		fgroupsInd=mFriendsCursor.getColumnIndex(LJDB.KEY_GROUPMASK);
		gnameInd=mGroupsCursor.getColumnIndex(LJDB.KEY_NAME);
		gidInd=mGroupsCursor.getColumnIndex(LJDB.KEY_ID);
		
	}
	
	 void getGroupHash() {
		 if (groupHash==null) groupHash=new HashMap<Integer,Boolean>();
		mGroupsCursor.moveToFirst();
		while(!mGroupsCursor.isAfterLast()) {
			groupHash.put(mGroupsCursor.getInt(gidInd), true);
			mGroupsCursor.moveToNext();
		}
		
	}




	@Override
		protected void onSaveInstanceState(Bundle outState){
			super.onSaveInstanceState(outState);
			outState.putBoolean("updating",refreshing);
			outState.putBoolean("addShown", addShown);
			outState.putSerializable("groupsCache", groupsCache);
			outState.putBooleanArray("checked", checked);
			
		
	}
	
	
	   
	   @Override public void onResume() {
		   super.onResume();
		   
		   
		   IntentFilter friendfilter=new IntentFilter();
		   	friendfilter.addAction(LJNet.LJ_XMLERROR);
	        friendfilter.addAction(LJNet.LJ_FRIENDSUPDATED);
	        friendfilter.addAction(LJNet.LJ_GROUPADDED);
	        friendfilter.addAction(LJNet.LJ_GROUPEDITED);
	        friendfilter.addAction(LJNet.LJ_GROUPDELETED);
	        registerReceiver(LJFriendsReceiver,friendfilter);
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
	


	public BroadcastReceiver LJFriendsReceiver=new BroadcastReceiver()
	{	

		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action=intent.getAction();



			if (action.equals(LJNet.LJ_XMLERROR)) {
				removeStickyBroadcast(intent);
				
					LJPro app=(LJPro) getApplication();
					app.alertNetworkError(EditFriendGroups.this);
					refreshing = false;
					View updating = findViewById(R.id.updatingfriends);
					updating.setVisibility(View.GONE);


			}

			else if(action.equals(LJNet.LJ_FRIENDSUPDATED)) {
					removeStickyBroadcast(intent);
					
					if (refreshing) {
						refreshing=false;
					View updating=findViewById(R.id.updatingfriends);
					updating.setVisibility(View.GONE);
					doneToast(getString(R.string.groupsupdated));
					}
					
					
				
					
					updateFriends();
			}
			
			else if(action.equals(LJNet.LJ_GROUPADDED)) {
				removeStickyBroadcast(intent);
				addShown=false;
				hideUpdating();
				broadcastUpdate();
				final Bundle extras=intent.getExtras();
				EditText addfriend=(EditText) findViewById(R.id.addfriend);
				addfriend.setText("");
				Toast.makeText(EditFriendGroups.this, R.string.groupadded, Toast.LENGTH_LONG).show();
				final Runnable addGroup=new Runnable(){

					public void run() {
						
						ContentValues group=new ContentValues();
						group.put("accountname", journalname);
						group.put("name",extras.getString("addgroup"));
						group.put("id",extras.getInt("addid"));
						group.put("updated",System.currentTimeMillis()/1000l);
						boolean success=LJDBAdapter.addDummyGroup(group);
						if (success) updateFriends();
						
					}
				};
					Thread friendadd=new Thread(null,addGroup,"Add Friend");
					friendadd.start();
					
			
			}
			
			else if(action.equals(LJNet.LJ_GROUPDELETED)||action.equals(LJNet.LJ_GROUPEDITED)) {
				removeStickyBroadcast(intent);
				updateFriends();
				hideUpdating();
				broadcastUpdate();
				
			}
			
			
			
				
			
		}
		
	};



		private void updateFriends() {
				
				SharedPreferences.Editor editor = appPrefs.edit();
				Date d=new Date();
				editor.putLong(journalname+"friends_lastupdate",d.getTime());
				editor.commit(); // Very important
				Thread thread = new Thread(null, reQueryInBackground, "RefreshGroupsList Background");
				try {thread.start();}
				catch(Throwable e) {Log.e(TAG,e.getMessage(),e);}
		}
		
		private Runnable reQueryInBackground = new Runnable() {

			public void run() {
			
				checked=new boolean[30];
				mFriendsCursor.requery();
				mGroupsCursor = LJDBAdapter.getFriendGroups(journalname, null);
				startManagingCursor(mGroupsCursor);
				checked = new boolean[mFriendsCursor.getCount()];
				updateUI();
				synchronized(mLock) {
					groupsCache.clear();
				}

			}
		};
		

		Runnable refreshList=new Runnable() { public void run() { 
			m_adapter.changeCursor(mGroupsCursor);}};
		
		private void updateUI() {
			runOnUiThread(refreshList);
		}
		
	
			
		
		
		protected void broadcastUpdate() {
			Intent updatefriends=new Intent(LJNet.LJ_GETFRIENDS);
			updatefriends.putExtra("journalname",journalname);
			WakefulIntentService.sendWakefulWork(mContext,updatefriends);
			
		}





		protected void doneToast(String text) {
			Toast.makeText(this, text, Toast.LENGTH_LONG).show();
			
		}




			
		

		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			super.onCreateOptionsMenu(menu);
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.fgroupsops, menu);
			return true;
		}
	
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			super.onOptionsItemSelected(item);
			switch (item.getItemId()) {
			case R.id.addfriend: 
				View editfriends=findViewById(R.id.editfriendgroups);
				showAddFriend(editfriends);
				
			break;
			case R.id.settings:    
				Intent prefs=new Intent(getApplicationContext(),LJProPrefs.class);
				prefs.putExtra("scope", journalname);
				startActivity(prefs);

			break;
			case R.id.refresh:     
				Intent updatefriends=new Intent(LJNet.LJ_GETFRIENDS);
				   updatefriends.putExtra("journalname",journalname);
				   WakefulIntentService.sendWakefulWork(mContext,updatefriends);
				   showUpdating();
					
				
			break;

			case R.id.help:     Toast.makeText(this, "You pressed the Help!", Toast.LENGTH_LONG).show();
			break;
			
			case R.id.removefriends:     
				showDelDialog();
				
				break;

			}
			return true;
		}
		
		private void showAddFriend(View view) {
			if (mGroupsCursor.getCount()==30) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.maxfriendgroups);
				builder.setMessage(R.string.maxfgroupsmsg);
				builder.setCancelable(true);
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			          

					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						
						
					}});
				AlertDialog maxfriends=builder.create();
				maxfriends.show();
				
				
			}
			else {
			addShown=true;
			View addfriend=view.findViewById(R.id.addfgroupfooter);
		if (addfriend!=null) addfriend.setVisibility(View.VISIBLE);
		Button add=(Button) addfriend.findViewById(R.id.pfriendbutton);
		if (add!=null) {add.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				EditText addfriend=(EditText) findViewById(R.id.addfriend);
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(addfriend.getWindowToken(), 0);
				String addFriend=addfriend.getText().toString();
				if (addFriend.length()>0) {
					CheckBox publicview=(CheckBox) findViewById(R.id.publicvis);
					Boolean publicvis=publicview.isChecked();
					addShown=false;
					View updating=findViewById(R.id.addfgroupfooter);
					updating.setVisibility(View.GONE);
					showUpdating();
					int k=1;
					Integer addid=null;
					while (k<31) {
						if (groupHash.containsKey(k)) {
							k++;
							
						}
						else {
							addid=k;
							break;
						}
						
					}
					Intent editfriends=new Intent(LJNet.LJ_EDITFRIENDGROUPS);
					editfriends.putExtra("journalname", journalname);
					editfriends.putExtra("public", publicvis?1:0);
					editfriends.putExtra("addid",addid.toString());
					editfriends.putExtra("addgroup", addFriend);
					WakefulIntentService.sendWakefulWork(mContext,editfriends);
				}
				else {
					View addfooter=findViewById(R.id.addfgroupfooter);
					addfooter.setVisibility(View.GONE);
					
				}

			}
			
		});
		}
			}
		}

		private boolean[] fgSelO;
		
		protected void showEditGroupsDialog() {
			//TODO check for groups, still harvesting??
			int numFriends=mFriendsCursor.getCount();
			fgSel=new boolean[numFriends];
			fgSelO=new boolean[numFriends];
			final CharSequence[] items=new CharSequence[numFriends];
			mGroupsCursor.moveToPosition(clickIndex);
			int id=mGroupsCursor.getInt(gidInd);
			mFriendsCursor.moveToFirst();
			synchronized(mLock) {
			while (!mFriendsCursor.isAfterLast()){
				int i=mFriendsCursor.getPosition();
				items[i]=mFriendsCursor.getString(fnameInd);
				BigInteger groups=groupsCache.get(i);
				if (groups==null) {
					groups=BigInteger.valueOf(mFriendsCursor.getInt(fgroupsInd));
					groupsCache.put(i, groups);
				}
				fgSel[i]=groups.testBit(id)?true:false;
				fgSelO[i]=fgSel[i];
				mFriendsCursor.moveToNext();
			}
			}
			
			
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.editgroupfriends));
			builder.setMultiChoiceItems(items, fgSel,fgroupListener);
			builder.setCancelable(true);
			if (items.length>0)  {
		    builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
		          

				public void onClick(DialogInterface dialog, int id) {
						showUpdating();
		               
		                Runnable getGroupMasks=new Runnable() {

							public void run() {
									HashMap<String,Integer> groupmask=new HashMap<String, Integer>();
									for (int i=0;i<items.length;i++) {
										if (fgSelO[i]!=fgSel[i]) {
											groupmask.put(items[i].toString(), groupsCache.get(i).intValue());
										}
									}
					                Intent editgroups=new Intent(LJNet.LJ_EDITFRIENDGROUPS);
					                editgroups.putExtra("journalname", journalname);
					                editgroups.putExtra("groupmask", groupmask);
					                WakefulIntentService.sendWakefulWork(mContext,editgroups);
							
								
							}
		           
		                };
		                
		                Thread dofgedits=new Thread(getGroupMasks,"groupMask Background");
		                dofgedits.start();
		              
		           }
		    }
		       );
		   builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		   });
			}
		   else {
			   builder.setTitle(getString(R.string.nofgroups));
			   builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		   }
		   });
		   }
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(EditFriendGroups.this);
			alert.show();
			
			
		}

		
		int buildGroupMask(char[] groups) {
			int max=0;
			for (int i=1;i<32;i++) {
				if (groups[i]=='1') {
					max=1;
			
			}
				
			
			}
			
			String groupMask=new String(groups);
			groupMask=groupMask.substring(0,max+1);
			groupMask=(new StringBuffer(groupMask).reverse()).toString();
			int groupmask=Integer.parseInt(groupMask, 2);
			return groupmask;
			
		}
		
		private DialogInterface.OnMultiChoiceClickListener fgroupListener=new  DialogInterface.OnMultiChoiceClickListener(){

			public void onClick(DialogInterface dialog, int which,
					boolean isChecked) {
				synchronized(mLock) {
				BigInteger groups=groupsCache.get(clickIndex);
				int id=mGroupsCursor.getInt(gidInd);
				if(groups==null) {
					mFriendsCursor.moveToPosition(which);
					groups=BigInteger.valueOf(mFriendsCursor.getInt(fgroupsInd));
					groupsCache.put(id, groups);
				}
				fgSel[which]=isChecked;
				mGroupsCursor.moveToPosition(clickIndex);
				if(isChecked) groupsCache.put(id,groups.setBit(which));
				else groupsCache.put(id,groups.clearBit(which));
				}
				
				
			}
			
		};
		
		
		//protected boolean groupedits=false;





		private void showDelDialog() {
			final ArrayList<CharSequence> del=new ArrayList<CharSequence>();
			ArrayList<Integer> delId=new ArrayList<Integer>();
			for (int i=0;i<mGroupsCursor.getCount();i++) {
				if (checked[i]){
					mGroupsCursor.moveToPosition(i);
					del.add(mGroupsCursor.getString(gnameInd));
					delId.add(mGroupsCursor.getInt(gidInd));
					
				}
			}
			final CharSequence[] items=(CharSequence[]) del.toArray(new CharSequence[del.size()]);
			final Integer[] itemID=(Integer[]) delId.toArray(new Integer[delId.size()]);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			if (items.length>0) builder.setTitle(getString(R.string.confirmdel));
			else builder.setTitle(getString(R.string.nofgchecked));
			builder.setItems(items, null);
			builder.setCancelable(true);
			if (items.length>0)  {
		    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		          

				public void onClick(DialogInterface dialog, int id) {
		                Intent delfriends=new Intent(LJNet.LJ_EDITFRIENDGROUPS);
		                showUpdating();
		                delfriends.putExtra("journalname",journalname);
		                delfriends.putExtra("delgroups", itemID);
		                WakefulIntentService.sendWakefulWork(mContext,delfriends);
		                Runnable delFriends=new Runnable() {

							public void run() {
								boolean success=LJDBAdapter.deleteGroup(del,journalname);
								if (success) {
									updateFriends();
								}
							}
		                
		                };
		                
		                Thread thread = new Thread(null, delFriends, "delFriends Background");
						try {thread.start();}
						catch(Throwable e) {Log.e(TAG,e.getMessage(),e);}
		                
		             
		                
		           }
		    }
		       );
		   builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		   });
			}
		   else {
			   builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		   }
		   });
		   }
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(EditFriendGroups.this);
			alert.show();
			
		}


		private void showUpdating() {
			
			runOnUiThread( new Runnable() {
				public void run() {refreshing=true;
				View updating=findViewById(R.id.updatingfriends);
				TextView updatemsg=(TextView)updating.findViewById(R.id.updatemsg);
				updatemsg.setText(R.string.updatingfgroups);
				updatemsg.invalidate();
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
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
			super.onCreateContextMenu(menu, v, menuInfo);
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			clickIndex=info.position;
			mGroupsCursor.moveToPosition(clickIndex);
			MenuInflater inflater = getMenuInflater();
			if (checked[clickIndex]) {
				inflater.inflate(R.menu.friendscontext2, menu);
			}
			else {
			inflater.inflate(R.menu.friendscontext, menu);
			}
			menu.setHeaderTitle(mGroupsCursor.getString(gnameInd));
		}

		@Override
		public boolean onContextItemSelected(MenuItem item) {
			super.onContextItemSelected(item);
			switch (item.getItemId()) {
			case R.id.markdelete:
				toggleDeletion();
				break;
			case R.id.unmarkdelete:
				toggleDeletion();
				break;
			case R.id.editgroups:
				showEditGroupsDialog();
				break;
			}
			return true;
		}
		
		






		public void toggleDeletion() {
			
			ListView listView=getListView();
			int wantedPosition=clickIndex;
			int firstPosition = listView.getFirstVisiblePosition(); // This is the same as child #0
			int wantedChild = wantedPosition - firstPosition;
			// Say, first visible position is 8, you want position 10, wantedChild will now be 2
			// So that means your view is child #2 in the ViewGroup:
			if (wantedChild < 0 || wantedChild >= listView.getChildCount()) {
			  Log.w(TAG, "Unable to get view for desired position, because it's not being displayed on screen.");
			}
			else 
			{
			// Could also check if wantedPosition is between listView.getFirstVisiblePosition() and listView.getLastVisiblePosition() instead.
				View row = listView.getChildAt(wantedChild);
				if (row!=null) {
					CheckBox checked=(CheckBox) row.findViewById(R.id.delete);
					checked.setChecked(!checked.isChecked());
					checked.invalidate();
				}
				
			}
			
		}






		public boolean getChecked(int index) {
			// TODO Auto-generated method stub
			return checked[index];
		}






		public void setChecked(Integer position,boolean isChecked) {
			checked[position]=isChecked;
			
		}






	
		
	
	

	    

}
