package com.electronapps.LJPro;

import java.util.HashMap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.Preference.OnPreferenceChangeListener;

public class LJProPrefs extends PreferenceActivity {
	LJDB LJDBAdapter;
	Editor mEditor;
	String mScope="All";
	String[] prefKeys={"backgroundSync","syncFrequency","syncWifi","notifyEnabled","notifyWhat","notifyVibrate",
			"alwaysRefetchOnAdd","notifySound","cacheDuration","otherJournalsCache","journalCache","photoProvider"};
	HashMap<String,String> depMap=new HashMap<String,String>();
	private Cursor mAccounts;
	private SharedPreferences appPrefs;
	
	@Override
		public void onCreate(Bundle savedInstanceState) {
			Intent intent=getIntent();
			mScope=intent.getStringExtra("scope");
			super.onCreate(savedInstanceState);
			this.addPreferencesFromResource(R.xml.prefs);
			appPrefs=PreferenceManager.getDefaultSharedPreferences(this);
			mEditor=appPrefs.edit();
			setupListeners();
			setupDependencies();
			GetAccounts setupAccounts=new GetAccounts();
			setupAccounts.execute();
			
			
			
	}
	
	private void setupListeners() {
		for (int i=0;i<prefKeys.length;i++){
			Preference pref=findPreference(prefKeys[i]);
			pref.setOnPreferenceChangeListener(applyToAll);
		}
		
	}
	
	private void setupDependencies() {
		String [] syncPrefs={"syncFrequency","syncWifi"};
		String [] notifyPrefs={"notifyWhat","notifyVibrate","notifySound"};
		for (int i=0;i<syncPrefs.length;i++) {
			depMap.put(syncPrefs[i],"backgroundSync");
		}
		for (int i=0;i<notifyPrefs.length;i++) {
			depMap.put(notifyPrefs[i],"notifyEnabled");
		}
		
	}
	
	private class GetAccounts extends AsyncTask<Void,Void,CharSequence[]>{

		@Override
		protected CharSequence[] doInBackground(Void... params) {
			LJDBAdapter= LJDB.getDB(getApplicationContext());
			LJDBAdapter.open();
			String[] columns={LJDB.KEY_ACCOUNTNAME};
			mAccounts=LJDBAdapter.getAllAccounts(columns);
			mAccounts.moveToFirst();
			int numAccounts=mAccounts.getCount();
			CharSequence[] choices=new CharSequence[numAccounts+1];
			choices[0]="All";
			for (int i=0;i<numAccounts;i++){
				choices[i+1]=mAccounts.getString(0);
				mAccounts.moveToNext();
			}
			
			return choices;
		}
		
		@Override
		protected void onPostExecute(CharSequence[] choices){
			ListPreference accChoices=(ListPreference) findPreference("settingScope");
			accChoices.setEntries(choices);
			accChoices.setEntryValues(choices);
			accChoices.setDefaultValue("All");
			
			String currScope=(String)accChoices.getValue();
			if (currScope==null) {
				mScope=currScope="All";
				accChoices.setValue("All");
			}
			if (!currScope.equals("All")) {
				changeKeys(currScope);
			}
			accChoices.setOnPreferenceChangeListener(updateKeys);
		}
		
		
	
		
		
	}
	
	OnPreferenceChangeListener updateKeys=new OnPreferenceChangeListener(){

		public boolean onPreferenceChange(Preference preference, Object newValue) {
			changeKeys((String)newValue);
			return true;
		}
	};
	
	OnPreferenceChangeListener applyToAll=new OnPreferenceChangeListener(){
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if (mScope.equals("All")){
				mAccounts.moveToFirst();
				while (!mAccounts.isAfterLast()){
					String account=mAccounts.getString(0);
					if(newValue instanceof Boolean){
						mEditor.putBoolean(account+"_"+preference.getKey(), (Boolean)newValue);
						
					}
					else mEditor.putString(account+"_"+preference.getKey(),(String)newValue);
					mAccounts.moveToNext();
				}
				mEditor.commit();
				if(preference.getKey().contains("backgroundSync")) {
					Long frequency=Long.parseLong(appPrefs.getString("syncFrequency", "900000"));
					((LJPro)getApplicationContext()).setupAlarms(mAccounts,null,(Boolean)newValue,frequency);

				}
				else if(preference.getKey().contains("syncFrequency")){
					((LJPro)getApplicationContext()).setupAlarms(mAccounts,null,true,Long.parseLong((String)newValue));
				}
			}
			else {
				
				
					if(preference.getKey().contains("backgroundSync")) {
						Long frequency=Long.parseLong(appPrefs.getString("syncFrequency", "900000"));
						((LJPro)getApplicationContext()).setupAlarms(null,mScope,(Boolean)newValue,frequency);

					}
					else if(preference.getKey().contains("syncFrequency")){
						((LJPro)getApplicationContext()).setupAlarms(null,mScope,true,Long.parseLong((String)newValue));
					}
				
				
			}
			return true;
		}
		
	};
	
	private void changeKeys(String scope){
		String prefix=null;
		String oldPrefix=null;
		if(scope.equals("All")) {
			prefix="";
		}
		else prefix=scope+"_";
		if (mScope.equals("All")){
			oldPrefix="";
		}
		else{
			oldPrefix=mScope+"_";
		}
		mScope=scope;
		for (int i=0;i<prefKeys.length;i++){
			Preference p=findPreference(oldPrefix+prefKeys[i]);
			String oldKey=p.getKey();
			String keyname=null;
			if (oldKey.contains("_")) {
				keyname=oldKey.split("_")[1];
				}
			else keyname=oldKey;
			if (depMap.get(keyname)!=null) {
				p.setDependency(prefix+depMap.get(keyname));
			}
			p.setKey(prefix+keyname);
			if (p instanceof CheckBoxPreference) {
				((CheckBoxPreference)p).setChecked(appPrefs.getBoolean(prefix+keyname,false));
			}
			
			else if (p instanceof ListPreference) ((ListPreference)p).setValue(appPrefs.getString(prefix+keyname,""));
		}
	}
	
	@Override
		protected void onDestroy() {
		super.onDestroy();
		mAccounts.close();
	}

}
