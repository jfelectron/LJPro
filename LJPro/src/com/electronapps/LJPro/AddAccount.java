package com.electronapps.LJPro;

import java.lang.ref.WeakReference;

import com.commonsware.cwac.wakeful.WakefulIntentService;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

public class AddAccount extends Activity {


	private ProgressDialog signon;
	public final static int ACCOUNT_ADDED=1;
	private final String TAG=AddAccount.class.getSimpleName();
	Context mContext;
	private LJDB LJDBAdapter;
	private Boolean waiting=false;
	private boolean firstaccount=true;
	private boolean DEBUG=false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState!=null) {
			restoreProgress(savedInstanceState);
		  
		}

		Intent launched=getIntent();
		firstaccount=Boolean.parseBoolean(launched.getStringExtra("allowback"));
		mContext=getApplicationContext();
		Log.d(TAG,"setting ContentView");
		setContentView(R.layout.addaccount);
		getWindow().setBackgroundDrawableResource(R.drawable.background_old);

	}

	@Override
	protected 
	void onResume() {
		super.onResume();
		IntentFilter mErrors=new IntentFilter();
		mErrors.addAction(LJNet.LJ_XMLERROR);
		mErrors.addAction(LJNet.LJ_ACCOUNTADDED);
		mErrors.addAction(LJNet.LJ_WRONGLOGIN);
		registerReceiver(LJAddAcountReceiver,mErrors);



	}
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(LJAddAcountReceiver);
	}

	
	@Override
	protected void onSaveInstanceState(Bundle saveState) {
		super.onSaveInstanceState(saveState);
		saveState.putBoolean("waiting",waiting);
	}
	
	private void restoreProgress(Bundle savedInstanceState) {
		waiting=savedInstanceState.getBoolean("waiting");
		if (waiting) {
			LJPro app=(LJPro) getApplication();
			ProgressDialog refresher=(ProgressDialog) app.Dialog.get();
			refresher.dismiss();
			String logingon=getString(R.string.signon);
			app.Dialog=new WeakReference<ProgressDialog>(ProgressDialog.show(AddAccount.this, "", logingon, true));

			

		}

	}

	private class SignIn extends AsyncTask<Void,Void,Void>{

		@Override
		protected Void doInBackground(Void... params) {
			LJDBAdapter=LJDB.getDB(getApplicationContext());
			LJDBAdapter.open();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result){
			LJPro app=(LJPro) getApplication();
			String logingon=getString(R.string.signon);
			EditText uname=(EditText) findViewById(R.id.uname);
			EditText psswd=(EditText) findViewById(R.id.pwd);
			if (DEBUG) {
				uname.setText("jfelectron");
				psswd.setText("2891jf!");
			}
			boolean haveaccount=false;
			String username=uname.getText().toString();
			String password=psswd.getText().toString();
			if(username.equals("")||password.equals("")) {
				haveaccount=true;
				AlertDialog.Builder builder=new AlertDialog.Builder(AddAccount.this);
				builder.setCancelable(true);
				builder.setTitle(getString(R.string.loginreqs));
				builder.setNegativeButton("OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
				(builder.create()).show();
				return;
				
			}
			String [] args=new String[1];
			args[0]=LJDB.KEY_ACCOUNTNAME;
			Cursor accounts=LJDBAdapter.getAllAccounts(args);
			
			if (accounts.getCount()>0) {
				accounts.moveToFirst();
				while(!accounts.isAfterLast()) {
					if(accounts.getString(0).equals(username)) {
			
						haveaccount=true;
						AlertDialog.Builder builder=new AlertDialog.Builder(AddAccount.this);
						builder.setCancelable(true);
						builder.setTitle(getString(R.string.accountexists));
						builder.setNegativeButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
						(builder.create()).show();
						accounts.close();
					
						return;	
					}
					
					accounts.moveToNext();
				}
			}
			if(!haveaccount) {
				accounts.close();
				app.Dialog=new WeakReference<ProgressDialog>(ProgressDialog.show(AddAccount.this, "", logingon, true));
				Intent addacct=new Intent("com.electronapps.LJPro.intent.newaccount");
				addacct.putExtra("journalname", username);

				addacct.putExtra("passMD5",Md5.MD5(password));
				addacct.putExtra("password",password);
				WakefulIntentService.sendWakefulWork(mContext, addacct);
			}
		}
		
	}



	public void doSignIn(View view) {
		waiting=true;
		
		SignIn signIn=new SignIn();
		signIn.execute();
		
	}

	public void doNewAcct(View view) {
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.livejournal.com/create.bml")); 
		startActivity(i); 

	}

	public BroadcastReceiver LJAddAcountReceiver=new BroadcastReceiver()
	{	
		//TODO: Register receiver in OnCreate
		@Override
		public void onReceive(Context context, Intent intent)
		{
			
			String action=intent.getAction();
			LJPro app=(LJPro) getApplication();
			ProgressDialog signon=(ProgressDialog) app.Dialog.get();
			signon.dismiss();

			if (action.equals(LJNet.LJ_XMLERROR)|action.equals(LJNet.LJ_WRONGLOGIN)) {
				removeStickyBroadcast(intent);
				dispErrorDialog(action);
				

			}

			else if (intent.getAction().equals(LJNet.LJ_ACCOUNTADDED)) {
				removeStickyBroadcast(intent);
				handleLogin();
				
			}
		}



	};








	void dismissDialog() {
		if (signon.isShowing()) {
			signon.dismiss();
			signon=null;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		if (firstaccount && keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			// do something on back.
			//TODO: Make some toast and butter it
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private void dispErrorDialog(String error) {

		AlertDialog builder =new AlertDialog.Builder(AddAccount.this).create();
		if (error.equals(LJNet.LJ_XMLERROR)) {

			String xmlerror=getString(R.string.xmlerror);
			builder.setMessage(xmlerror);


		}

		else if (error.equals(LJNet.LJ_WRONGLOGIN)) {
			String xmlerror=getString(R.string.wrongpw);
			builder.setMessage(xmlerror);

		}

		builder.setButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {

				return;

			} });


		builder.show();		
	}

	private void handleLogin()  {
		Intent added=new Intent(LJNet.LJ_ACCOUNTADDED);
		EditText uname=(EditText) findViewById(R.id.uname);
		added.putExtra("journalname",uname.getText().toString());
		setResult(ACCOUNT_ADDED,added);

		finish();

	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//LJDBAdapter.close();
	}

}

