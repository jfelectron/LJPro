package com.electronapps.LJPro;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Authenticate extends Activity {
	
	FlickrAPI mFlickr;
	private OAuthService mPBAuth;
	public String mFrob;
	private Token mToken;
	private WebView mAuthWeb;
	private ProgressBar mProgress;
	private TextView mHeader;
	LJDB mDB;
	private String mJournalname;
	private ProgressBar mWebProgress;
	private String mProvider;
	protected void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);
		Intent intent=getIntent();
		mJournalname=intent.getStringExtra("journalname");
		setContentView(R.layout.authview);
		mHeader=(TextView) findViewById(R.id.authheader);
		mHeader.setText("Authentication");
		mProgress=(ProgressBar) findViewById(R.id.status_progress);
		mWebProgress=(ProgressBar) findViewById(R.id.webprogress);
		mProgress.setMax(100);
		mAuthWeb=(WebView) findViewById(R.id.authweb);
		mAuthWeb.getSettings().setJavaScriptEnabled(true);
		mAuthWeb.setWebViewClient(new HelloWebViewClient());
		final Activity activity=this;
		mAuthWeb.setWebChromeClient(new WebChromeClient() {
   public void onProgressChanged(WebView view, int progress) {
     // Activities and WebViews measure progress with different scales.
     // The progress meter will automatically disappear when we reach 100%
     mWebProgress.setProgress(progress * 100);
   }
 });
	
		mProvider=intent.getStringExtra("provider");
		if (mProvider.equals("Flickr"))
		{
			mFlickr=new FlickrAPI(getApplicationContext(),getString(R.string.flickr_key),getString(R.string.flickr_secret));
			FlickrTask auth1=new FlickrTask("init");
			auth1.execute();
		}
		else if(mProvider.equals("PhotoBucket")){
			mPBAuth= new ServiceBuilder()
            .provider(PhotoBucketAPI.class)
            .apiKey(getString(R.string.photobucket_key))
            .apiSecret(getString(R.string.photobucket_secret))
            .build();
			PBTask auth1=new PBTask("init");
			auth1.execute();
			
		}
				
	}
	
	public void handleClick(View v){
		int id=v.getId();
		if (id==R.id.done) {
			AlertDialog.Builder builder=new AlertDialog.Builder(Authenticate.this);
			builder.setTitle("Confirm sign in");
			builder.setMessage("Have you signed into Flickr and authorized access?");
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){

				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					mProgress.setProgress(66);
					mHeader.setText("Fetching Token");
					if (mProvider.equals("Flickr")) {
					FlickrTask auth2=new FlickrTask("complete");
					auth2.execute();
					}
					else {
						PBTask auth2=new PBTask("complete");
						auth2.execute();
					}
					
					
					
				}
				
			});
			builder.setNegativeButton("No",new DialogInterface.OnClickListener(){

				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					
				}
				
			});
			builder.create().show();
			
		}
		else if (id==R.id.cancel){
			
		}
	}
	
	private class FlickrTask extends AsyncTask<Void,Void,Object>{

		private String mMode;

		public FlickrTask(String mode){
			mMode=mode;
		
		}
		@Override
		protected Object doInBackground(Void... params) {
			Object result=null;
			if (mMode.equals("init")) {
				result=mFlickr.initAuth();
				
			}
			else if (mMode.equals("complete")) {
				mDB=LJDB.getDB(Authenticate.this);
				mDB.open();
				result=mFlickr.finishAuthentication(mFrob);
			}
			return result;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(Object result) {
		
			if (mMode.equals("init")) {
				HashMap<String,String> data=(HashMap<String,String>) result;
				mFrob=data.get("frob");
				String url=data.get("url");
				mHeader.setText("Grant Permissions");
				mProgress.setProgress(33);
				mAuthWeb.loadUrl(url);
			}
			else if (mMode.equals("complete")) {
				mProgress.setProgress(90);
				ContentValues account=(ContentValues) result;
				account.put("accountname", mJournalname);
				String token=(String) account.get("auth_token");
				Boolean success=mDB.insertPhotoAccount(account);
				Intent intent=new Intent();
				intent.putExtra("token",token);
				setResult(RESULT_OK,intent);
				finish();
				
				
			}
		}
		
	}
	
	private class PBTask extends AsyncTask<Void,Void,Token>{

		private String mMode;
		private Pattern subdRE=Pattern.compile("(?<=subdomain=)[^&]*");
		private Pattern unameRE=Pattern.compile("(?<=username=)[^&]*");

		public PBTask(String mode){
			mMode=mode;
		
		}
		@Override
		protected Token doInBackground(Void... params) {
			if (mMode.equals("init")) {
				
				mToken=mPBAuth.getRequestToken();
				
				
				
			}
			else if (mMode.equals("complete")) {
				mDB=LJDB.getDB(Authenticate.this);
				mDB.open();
				HashMap<String,Object> result=mPBAuth.getAccessToken(mToken,null);
				mToken=(Token) result.get("token");
				mProgress.setProgress(90);
				ContentValues account=new ContentValues();
				account.put("accountname", mJournalname);
				account.put("auth_token", mToken.getToken());
				account.put("auth_secret",mToken.getSecret());
				account.put("provider", "PhotoBucket");
				String response=result.get("response").toString();
				Matcher umatcher=unameRE.matcher(response);
				Matcher dmatcher=subdRE.matcher(response);
				
				if (umatcher.find()){
					account.put("photo_account",umatcher.group());
				}
				if (dmatcher.find()){
					account.put("photo_accountid", dmatcher.group());
				}
				Boolean success=mDB.insertPhotoAccount(account);
				Intent intent=new Intent();
				intent.putExtra("token",mToken.getToken());
				intent.putExtra("secret", mToken.getSecret());
				setResult(RESULT_OK,intent);
				
				
//oauth_token=53.821187_1299568533&oauth_token_secret=f9981b614d1e8ef445a33243314cdcf2a1ad0144&username=jfelectron&subdomain=api861.photobucket.com&homeurl=http%3A%2F%2Fs861.photobucket.com%2Falbums%2Fab180%2Fjfelectron%2F
				Log.d("PBAuth",response);
				
			}
			
			return mToken;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(Token token) {
		
			if (mMode.equals("init")) {
				String url=mPBAuth.getAuthorizationUrl(token);
				mHeader.setText("Grant Permissions");
				mProgress.setProgress(33);
				mAuthWeb.loadUrl(url);
			}
			else if (mMode.equals("complete")) {
				mProgress.setProgress(90);
				
				//String accessToken=token.getToken();
				//String secret=token.getSecret();
				finish();
				/*ContentValues account=new ContentValues();
				
				account.put("accountname", mJournalname);
				String token=(String) account.get("auth_token");
				Boolean success=mDB.insertPhotoAccount(account);
				Intent intent=new Intent();
				intent.putExtra("token",token);
				setResult(RESULT_OK,intent);
				finish();*/
				
				
			}
		}
		
	}
	
	private class HelloWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        view.loadUrl(url);
	        return true;
	    }
	   
	}
	
	

}
