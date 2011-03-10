package com.electronapps.LJPro;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.sample.picasa.model.PicasaUrl;
import com.google.api.client.sample.picasa.model.Util;
import com.google.api.client.xml.atom.AtomParser;

public class PicasaAPI {
	  private static final String AUTH_TOKEN_TYPE = "lh2";

	  private static final String TAG = "PicasaAndroidSample";

	  private static final int MENU_ADD = 0;

	  private static final int MENU_ACCOUNTS = 1;

	  private static final int CONTEXT_EDIT = 0;

	  private static final int CONTEXT_DELETE = 1;

	  private static final int CONTEXT_LOGGING = 2;

	  private static final int REQUEST_AUTHENTICATE = 0;

	  private static final String PREF = "MyPrefs";

	 

	  private static ApacheHttpTransport transport;

	  private String authToken;

	  private String postLink;

	private Context mContext;

	private String mAccountName=null;

	private TokenCallback mCallback;

	private String mJournalName;
	 
	public interface TokenCallback{
		public void onHaveToken(String accountname,String token);
	}
	 public PicasaAPI(Context c,String journalname,String accountName,TokenCallback callback) {
		  	mContext=c;
		  	mAccountName=accountName;
		  	mJournalName=journalname;
		  	mCallback=callback;
		    transport =new ApacheHttpTransport();
		    transport.defaultHeaders=new GoogleHeaders();
		    GoogleHeaders headers =(GoogleHeaders) transport.defaultHeaders;
		    headers.setApplicationName("Google-PicasaAndroidAample/1.0");
		    headers.gdataVersion = "2";
		    JsonHttpParser parser = new JsonHttpParser();
		    //parser.namespaceDictionary = Util.NAMESPACE_DICTIONARY;
		    transport.addParser(parser);
		  }
	  
	 public void authorizeAccount() {
		 gotAccount(false);
	 }
	  public void gotAccount(boolean tokenExpired) {
		   
		    if (mAccountName != null) {
		      AccountManager manager = AccountManager.get(mContext);
		      Account[] accounts = manager.getAccountsByType("com.google");
		      int size = accounts.length;
		      for (int i = 0; i < size; i++) {
		        Account account = accounts[i];
		        if (mAccountName.equals(account.name)) {
		          if (tokenExpired) {
		            manager.invalidateAuthToken("com.google", this.authToken);
		          }
		          gotAccount(manager, account);
		          return;
		        }
		      }
		    }
		    showAccountDialog();
		  }
	  
	public void showAccountDialog() {
		  AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
	        builder.setTitle("Select a Google account");
	        final AccountManager manager = AccountManager.get(mContext);
	        final Account[] accounts = manager.getAccountsByType("com.google");
	        final int size = accounts.length;
	        String[] names = new String[size];
	        for (int i = 0; i < size; i++) {
	          names[i] = accounts[i].name;
	        }
	        builder.setItems(names, new DialogInterface.OnClickListener() {
	          public void onClick(DialogInterface dialog, int which) {
	        	  mAccountName=accounts[which].name;
	            gotAccount(manager, accounts[which]);
	          }
	        });
	        builder.create().show();
		
	}

	private void gotAccount(final AccountManager manager, final Account account) {
		   /* SharedPreferences settings = mContext.getSharedPreferences(PREF, 0);
		    SharedPreferences.Editor editor = settings.edit();
		    editor.putString("accountName", account.name);
		    editor.commit();*/
		    new Thread() {

		      @Override
		      public void run() {
		        try {
		        	final LJDB db=LJDB.getDB(mContext);
		        	db.open();
		          final Bundle bundle =
		              manager.getAuthToken(account, AUTH_TOKEN_TYPE, true, null, null)
		                  .getResult();
		          ((Activity)mContext).runOnUiThread(new Runnable() {

		            public void run() {
		              try {
		                if (bundle.containsKey(AccountManager.KEY_INTENT)) {
		                  Intent intent =
		                      bundle.getParcelable(AccountManager.KEY_INTENT);
		                  int flags = intent.getFlags();
		                  flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK;
		                  intent.setFlags(flags);
		                 
		                  
		                  ((Activity)mContext).startActivityForResult(intent, REQUEST_AUTHENTICATE);
		                } else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
		                	 ContentValues photoAccount=new ContentValues();
		                	photoAccount.put("accountname", mJournalName);
		                	photoAccount.put("photo_account",account.name);
		     				photoAccount.put("auth_token", bundle.getString(AccountManager.KEY_AUTHTOKEN));
		     				photoAccount.put("provider", "Picasa");
		     				Boolean success=db.insertPhotoAccount(photoAccount);
		     				if (success){
		     					mCallback.onHaveToken(account.name,bundle.getString(AccountManager.KEY_AUTHTOKEN));
		     				}
		                }
		              } catch (Exception e) {
		                handleException(e);
		              }
		            }
		          });
		        } catch (Exception e) {
		          handleException(e);
		        }
		      }
		    }.start();
		  }
	  
	  static SendData sendData;

	  public  HashMap<String,String> doUpload(SendData d,String authToken) {
		  this.authToken = authToken;
			HashMap<String,String> photodata=new HashMap<String,String>();
		  sendData=d;
		    ((GoogleHeaders) transport.defaultHeaders).setGoogleLogin(authToken);
	    if (sendData != null) {
	      try {
	        if (sendData.fileName != null) {
	          boolean success = false;
	          try {
	            HttpRequest request = transport.buildPostRequest();
	            request.url = PicasaUrl.relativeToRoot(
	                "feed/api/user/default/albumid/default/");
	            ((GoogleHeaders) request.headers).setSlugFromFileName(sendData.title);
	            FileContentMonitored content=new FileContentMonitored(mContext,sendData.data,sendData.title,sendData.contentType);
	            request.content = content;
	            HttpResponse response=request.execute();
	            String resp=response.parseAsString();
	            String src=null;
	            String link=null;
	            try {
					JSONObject entry=new JSONObject(resp).getJSONObject("entry");
					src=entry.getJSONObject("content").getString("src");
					JSONArray links=entry.getJSONArray("link");
					int numLinks=links.length();
					for (int i=0;i<numLinks;i++){
						JSONObject linkObj=links.getJSONObject(i);
						if (linkObj.getString("rel").contains("canonical")){
							link=linkObj.getString("href");
							break;
						}
					}
					photodata.put("link", link);
					photodata.put("source", src);
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            success = true;
	          } catch (IOException e) {
	            handleException(e);
	          }
	        
	        }
	      } finally {
	        sendData = null;
	      }
	    } 
	    return photodata;
	  }
	  static class SendData {
		    String fileName;
		    File data;
		   String title;
		    String contentType;
		    long contentLength;

		    SendData(String filepath,String type,String title) {
		    	this.data=new File(filepath);
		    
		          this.fileName = data.getName();
		          String c=null;
		          this.title=title;
		         
		          this.contentType = type;
		          this.contentLength =data.length();
		        }
		    
		  }
	  
	 
	  private void handleException(Exception e) {
		    e.printStackTrace();
		    SharedPreferences settings = mContext.getSharedPreferences(PREF, 0);
		    boolean log = settings.getBoolean("logging", false);
		    if (e instanceof HttpResponseException) {
		      HttpResponse response = ((HttpResponseException) e).response;
		      int statusCode = response.statusCode;
		      try {
		        response.ignore();
		      } catch (IOException e1) {
		        e1.printStackTrace();
		      }
		      if (statusCode == 401 || statusCode == 403) {
		        gotAccount(true);
		        return;
		      }
		      if (log) {
		        try {
		          Log.e(TAG, response.parseAsString());
		        } catch (IOException parseException) {
		          parseException.printStackTrace();
		        }
		      }
		    }
		    if (log) {
		      Log.e(TAG, e.getMessage(), e);
		    }
		  }
	  
}
