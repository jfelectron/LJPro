package com.electronapps.LJPro;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class LJAlarmReceiver extends BroadcastReceiver {


	@Override
	public void onReceive(Context context, Intent intent) {
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		 PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Alarm");
		 wl.acquire();
		 String action=intent.getAction();
		 if (action.equals(LJPro.LJ_SYNC_FRIENDSPAGE)){
			 Intent fp=new Intent(LJNet.LJ_GETFRIENDSPAGE);
			 fp.putExtra("background", true);
			 fp.putExtra("journalname",intent.getStringExtra("journalname"));
			 WakefulIntentService.sendWakefulWork(context, fp);
		 }
		 else if(action.equals(LJPro.LJ_SYNC_FRIENDS)){
			
			 Intent fp=new Intent(LJNet.LJ_GETFRIENDS);
			 fp.putExtra("background", true);
			 fp.putExtra("journalname",intent.getStringExtra("journalname"));
			 WakefulIntentService.sendWakefulWork(context, fp);
		
		 }
		 else if(action.equals(LJNet.LJ_FRIENDSPAGEUPDATED)) {
			if(intent.hasExtra("background")) {
				SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(context);
				if(prefs.getBoolean(intent.getStringExtra("journalname")+"_"+"notifyEnabled",false)){
					((LJPro)context.getApplicationContext()).notifyFP(intent.getStringExtra("journalname"));
				}
			}
		 }
		 else if (action.equals(Intent.ACTION_BOOT_COMPLETED)){
			 Log.d("LJPRP","Setting up update alarms");
			LJPro app=(LJPro)context.getApplicationContext();
			app.setupAlarms(null, null, null, null);
		 }
		 wl.release();
		Log.d("LJRECEIVER",intent.getAction());
		
	}

}
