package com.electronapps.LJPro;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import com.commonsware.cwac.cache.AsyncCache;
import com.commonsware.cwac.cache.SimpleWebImageCache;

import com.commonsware.cwac.thumbnail.ThumbnailBus;
import com.commonsware.cwac.thumbnail.ThumbnailMessage;
import com.commonsware.cwac.wakeful.WakefulIntentService;


import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;

import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Editable;

import android.database.Cursor;
import android.util.Log;

@ReportsCrashes(formKey = "dGx5a2xCWVZhR3lIYkw2S1RmWGNwMlE6MQ")

public class LJPro extends Application {
	
	  private ThumbnailBus bus;
      private SimpleWebImageCache<ThumbnailBus, ThumbnailMessage> imgCache;
      public WeakReference Dialog=null;
      private File cacheDir=null;
      private  LJDB db;
     private Object mDBLock=new Object();
      private Boolean DEBUG=false;
      private ConnectivityManager mConnManager;
      public HashMap<String,Boolean> fprefreshing=new HashMap<String,Boolean>();
     public final HashMap<String, SoftReference<Editable>> editableCache=new HashMap<String, SoftReference<Editable>>();
	private Editor editor;
	private SharedPreferences appPrefs;
     @Override 
     public void onCreate() {
    	ACRA.init(this);
    	 Log.d("LJPRO","Creating ThumnbnailBus");
    	 bus = new ThumbnailBus();
    	 appPrefs=PreferenceManager.getDefaultSharedPreferences(this);
    	 editor=appPrefs.edit();
    	 if(DEBUG) {
    		 editor.clear();
    	 }
 		 registerReceiver();
 		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	 mConnManager=(ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
    	 editor.putBoolean("allowsBackgroundData",mConnManager.getBackgroundDataSetting());
    	 editor.commit();
    	NetworkInfo activeinfo=mConnManager.getActiveNetworkInfo();
    	 mNetworkType=activeinfo==null?-1:activeinfo.getType();
    	mHaveConnection=activeinfo==null?false:activeinfo.isConnected();
    	
    
    	 File sdcard=Environment.getExternalStorageDirectory();
     	
         if(sdcard!=null&sdcard.canWrite()) {
        	 cacheDir=new File(sdcard.getAbsolutePath()+"/Android/data/com.electronapps.LJPro/files/");
        	 cacheDir.mkdirs();
    	  
      }
         else{
        	 cacheDir=null;
         }
    
    	 Log.d("LJPRO","Creating imgCache");
    	 imgCache = new SimpleWebImageCache<ThumbnailBus, ThumbnailMessage>(cacheDir, policy, 1000, 
                 bus);
  
    	AppSetup init=new AppSetup();
    	init.execute();
     
    
    	 
     }
     
     private class AppSetup extends AsyncTask<Void,Void,Void>{

		@Override
		protected Void doInBackground(Void... params) {
			if(mHaveConnection) {
	    		db=LJDB.getDB(getApplicationContext());
	     		db.open();
	     		String[] columns={LJDB.KEY_ACCOUNTNAME};
	     		Cursor accounts=db.getAllAccounts(columns);
	     		
	     		for (int i=0;i<accounts.getCount();i++) {
	     			accounts.moveToPosition(i);
	     			//TODO: Update default account first if there is one
	     			String journalname=accounts.getString(0);
	     		
	    			//refresh friendspage
	     			Intent getfriendspage = new Intent(LJNet.LJ_GETFRIENDSPAGE);
	     			fprefreshing.put(journalname,true);
	    			getfriendspage.putExtra("journalname", journalname);
	    			WakefulIntentService.sendWakefulWork(getApplicationContext(), getfriendspage);
	    			//refresh login info
	     			Intent updatelogin=new Intent(LJNet.LJ_LOGIN);
	    			updatelogin.putExtra("journalname", journalname);
	    			WakefulIntentService.sendWakefulWork(getApplicationContext(), updatelogin);
	     		}
	     		setupAlarms(accounts,null,null,null);
	     		accounts.close();
	     	
	     		
	    	
	    	}
			return null;
		}
    	 
     }
     
     
     
     public File getCacheDir() {
    	 return cacheDir;
     }
     private boolean mHaveConnection=false;
     private int mNetworkType=-1;
     
     public void registerReceiver() {
    	 IntentFilter filter=new IntentFilter();
    	 filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    	 filter.addAction(ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED);
    	 registerReceiver(ConnectivityReceiver,filter);
    	 IntentFilter external=new IntentFilter();
    	 filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
    	 filter.addAction(Intent.ACTION_MEDIA_SHARED);
    	 filter.addAction(Intent.ACTION_MEDIA_REMOVED);
    	 registerReceiver(externalStorageReceiver,external);
     }
     
     
     public BroadcastReceiver externalStorageReceiver=new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
				 File sdcard=Environment.getExternalStorageDirectory();
			     	
		         if(sdcard!=null&sdcard.canWrite()) {
		        	 cacheDir=new File(sdcard.getAbsolutePath()+"/Android/data/com.electronapps.LJPro/files/");
		        	 cacheDir.mkdirs();
		    	  
		      }
		         else{
		        	 cacheDir=null;
		         }
		         
				
			}
			else if (action.equals(Intent.ACTION_MEDIA_REMOVED)||action.equals(Intent.ACTION_MEDIA_SHARED)) {
				cacheDir=null;
			}
			 imgCache = new SimpleWebImageCache<ThumbnailBus, ThumbnailMessage>(cacheDir, policy, 1000, 
	                 bus);
			
		}
    	 
     };
     
     public BroadcastReceiver ConnectivityReceiver= new BroadcastReceiver() {
    	

		@Override
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				
				noNetShown=false;
				Bundle extras=intent.getExtras();
				NetworkInfo netinfo=intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
				if (netinfo.isConnected()) {
					mHaveConnection=true;
					mNetworkType=netinfo.getType();
				}
				else {
					mHaveConnection=false;
					mNetworkType=-1;
				}
				
				if (extras.getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY)) {
					mHaveConnection=false;
					mNetworkType=-1;
				}
			
			}
			
			else if (action==ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED)
				 editor.putBoolean("allowsBackgroundData",mConnManager.getBackgroundDataSetting());
	    	 	editor.commit();
			
		}
     };
  
      public boolean haveConnection() {
    	  boolean haveRoute=false;
    	haveRoute=isOnline(this);
		
    	  return mHaveConnection&haveRoute;
      }
      
      public static boolean isOnline(Context context) {
    	  try {
    	 
    	  URL url = new URL("http://www.google.com");
    	  HttpURLConnection urlc = (HttpURLConnection) url
    	  .openConnection();
    	  urlc.setRequestProperty("Connection", "close");
    	  urlc.setConnectTimeout(3000); // mTimeout is in seconds

    	  urlc.connect();

    	  if (urlc.getResponseCode() == 200) {
    	  return true;
    	  } else {
    	  return false;
    	  }
    	  
    	  } catch (IOException e) {
    	  // TODO Auto-generated catch block
    	  e.printStackTrace();
    	  }
    	  return false;
    	  }
      
      public int networkType() {
    	  return mNetworkType;
      }
      
  	private boolean noNetShown=false;
      public void alertNetworkError(Context c) {
    	  if (!noNetShown) {
    		  AlertDialog.Builder builder=new AlertDialog.Builder(c);
    		  builder.setTitle(R.string.network_error);
    		  builder.setMessage("A network error occured.Check your connection.Only cached content will be dislayed.");
    		  builder.setCancelable(true);
    		  builder.setPositiveButton(R.string.ok, new OnClickListener() {

    			  public void onClick(DialogInterface dialog, int which) {
    				  dialog.dismiss();

    			  }
    		  } 
    		  );
    		  builder.create().show();
    		  noNetShown=true;
    	  }
      }
  

	public SimpleWebImageCache<ThumbnailBus, ThumbnailMessage> getImageCache() {
    	  Log.d("LJPRO","Getting Image Cache");
              if (imgCache == null){ 
                      imgCache = new SimpleWebImageCache<ThumbnailBus, ThumbnailMessage>(cacheDir, policy, 1000, 
                                      bus);
              }
              return imgCache;
      }
      
      
      
  private final AsyncCache.DiskCachePolicy policy=new AsyncCache.DiskCachePolicy() {
      public boolean eject(File file) {
          return(System.currentTimeMillis()-file.lastModified()>1000*60*60*24*7);
      }
  };
private NotificationManager mNotificationManager;
      
      public ThumbnailBus getThumbnailBus() {
              return bus;
      }
      
      @Override
	public void onTerminate() {
    	  super.onTerminate();
    	  unregisterReceiver(ConnectivityReceiver);
      }

      public static final int SYNC_ID = R.layout.friendspage;
     public void notifySync(String journalname) {
    	 int icon=R.drawable.notifyicon;
 		CharSequence tickerText = getString(R.string.syncing_friendspage);
 		long when = System.currentTimeMillis();
 		Notification notification = new Notification(icon, tickerText, when);
 		notification.flags=notification.flags|Notification.FLAG_NO_CLEAR;
 		CharSequence contentTitle = getString(R.string.syncing_livejournal_data);
 		CharSequence contentText = getString(R.string.fetching_)+" "+journalname+" "+getString(R.string._friendspage);
 		Intent notificationIntent = new Intent(this, FriendsPage.class);
 		notificationIntent.putExtra("journalname",journalname);
 		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
 		notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
 		this.fprefreshing.put(journalname, true);
 		mNotificationManager.notify(SYNC_ID, notification);
    	 
     }
     public static final int FP_ID=R.string.friendspage_updated_;
     public void notifyFP(String journalname) {
    	 int icon=R.drawable.notifyicon;
    	 CharSequence tickerText = getString(R.string.friendspage_updated_);
	 		long when = System.currentTimeMillis();
	 		Notification notification = new Notification(icon, tickerText, when);
	 		notification.flags |= Notification.FLAG_AUTO_CANCEL;
	 		int vibrate=Integer.parseInt(appPrefs.getString("notifyVibrate","0"));
	 		String ringtone=(String)appPrefs.getString("notifySound", "");
	 		Uri notifysound=Uri.parse(ringtone);
	 		notification.sound=notifysound;
	 		switch(vibrate) {
	 		case 1:
	 			notification.defaults|=Notification.DEFAULT_VIBRATE;
	 			break;
	 		case 2:
	 			AudioManager ringer=(AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
	 			if (ringer.getRingerMode()==AudioManager.RINGER_MODE_VIBRATE) {
	 				notification.defaults|=Notification.DEFAULT_VIBRATE;
	 			}
	 			break;
	 		}
	 		
	 		CharSequence contentTitle = getString(R.string.friendspage_updated_);
	 		CharSequence contentText = journalname;
	 		Intent notificationIntent = new Intent(this,FriendsPage.class);
	 		notificationIntent.putExtra("journalname",journalname);
	 		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
	 		notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
	 		mNotificationManager.notify(COMMENT_ID, notification);
    	 
     }
     public static final int COMMENT_ADDING=0;
     public static final int COMMENT_ERROR=1;
     public static final int COMMENT_ID=R.string.adding_comment;
     public void notifyComment(int type,String postername,String journalname) {
    	 int icon=R.drawable.notifyicon;
    	 switch(type){
    	 case COMMENT_ADDING:
    	 		CharSequence tickerText = getString(R.string.adding_comment);
    	 		long when = System.currentTimeMillis();
    	 		Notification notification = new Notification(icon, tickerText, when);
    	 		CharSequence contentTitle = getString(R.string.adding_comment);
    	 		CharSequence contentText = getString(R.string.in_reply_to)+" "+postername;
    	 		Intent notificationIntent = new Intent();
    	 		notificationIntent.putExtra("journalname",journalname);
    	 		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
    	 		notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
    	 		mNotificationManager.notify(COMMENT_ID, notification);
    	 		break;
    	 case COMMENT_ERROR:
 	 		tickerText = getString(R.string.error_adding_comment);
 	 		when = System.currentTimeMillis();
 	 		notification = new Notification(icon, tickerText, when);
 	 		notification.flags |= Notification.FLAG_AUTO_CANCEL;
 	 		contentTitle = getString(R.string.error_adding_comment);
 	 		contentText = getString(R.string.in_reply_to)+" "+postername;
 	 		notificationIntent = new Intent();
 	 		notificationIntent.putExtra("journalname",journalname);
 	 		contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
 	 		notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
 	 		mNotificationManager.notify(COMMENT_ID, notification);
 	 		break;
    	 
    		 
    	 }
    	 
     }
     
     public void clearNotification(int id) {
    	 mNotificationManager.cancel(id);
     }
     
     public static String LJ_SYNC_FRIENDSPAGE="com.electronapps.LJPro.intent.syncfriendspage";
     public static String LJ_SYNC_LOGIN="";
     public static String LJ_SYNC_FRIENDS="com.electronapps.LJPro.intent.syncfriends";
     
    public void setupAlarms(Cursor accounts,String journalname,Boolean setAlarm,Long delay) {
    	Boolean open=false;
    	if (accounts==null) {
    		open=true;
    		db=LJDB.getDB(getApplicationContext());
    		db.open();
    		String[] columns={LJDB.KEY_ACCOUNTNAME};
    		accounts=db.getAllAccounts(columns); 
    	}
    	accounts.moveToFirst();
    	AlarmManager alarms=(AlarmManager)getSystemService(Context.ALARM_SERVICE);


    		while (!accounts.isAfterLast()) {
    			String account=accounts.getString(0);
    			if (journalname==null||journalname.equals(account)) {
    				Intent getfriendspage=new Intent(LJ_SYNC_FRIENDSPAGE);
    				Intent getfriends=new Intent(LJ_SYNC_FRIENDS);
    				getfriendspage.putExtra("journalname", account);
    				getfriendspage.putExtra("background",true);
    				getfriends.putExtra("journalname", account);
    				getfriends.putExtra("background",true);
    				setAlarm=setAlarm==null?appPrefs.getBoolean(account+"_"+"backgroundSync", false):setAlarm;
    				PendingIntent fpSync=PendingIntent.getBroadcast(getApplicationContext(),accounts.getPosition(),getfriendspage, PendingIntent.FLAG_UPDATE_CURRENT);
    				PendingIntent frSync=PendingIntent.getBroadcast(getApplicationContext(),accounts.getPosition(),getfriends, PendingIntent.FLAG_UPDATE_CURRENT);
    				if (setAlarm&& appPrefs.getBoolean("allowsBackgroundData", false)){
    					String freq=appPrefs.getString(account+"_"+"syncFrequency","900000");
    					delay=delay==null?Long.parseLong(freq):delay;
    					alarms.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,SystemClock.elapsedRealtime()+delay, delay, frSync);
    					alarms.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,SystemClock.elapsedRealtime()+delay, delay, fpSync);
    					
    				}
    				else {
    					alarms.cancel(fpSync);
    					alarms.cancel(frSync);
    				}
    				accounts.moveToNext();
    			}
    		}
    	
    	
    	if(open){
    		accounts.close();
    	}
    }
    	
      
  

}
