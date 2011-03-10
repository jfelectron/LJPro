package com.electronapps.LJPro;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class PhotoAPIBase {
	public static final String UPLOAD_STARTED="com.electronapps.LJPro.photos.uploadstarted";
	public static final String UPLOAD_PROGRESS_UPDATE="com.electronapps.LJPro.photos.uploadprogress";
	public static final String UPLOAD_ERROR="com.electronapps.LJPro.photos.uploaderror";
	public static final String UPLOAD_COMPLETED="com.electronapps.LJPro.photos.uploadcomplete";
	protected DefaultHttpClient mClient=new DefaultHttpClient();
	protected Context mContext;
	
	public static int JSON_OBJECT=0;
	public static int JSON_ARRAY=0;
	
	protected void handleError(Throwable t) {
		// TODO Auto-generated method stub
		
	}
	
	protected String doGet(String url) {
		HttpGet req=new HttpGet(url);
		String respString=null;
		try{
			HttpResponse resp=mClient.execute(req);
			respString=EntityUtils.toString(resp.getEntity());
		}
		catch(Throwable t){
			Log.e("PHOTOAPIBASE",t.getMessage(),t);
			
		}
		return respString;
		
	}
	
	protected String doPost(String url,HttpEntity entity){
		HttpPost httppost=new HttpPost(url);
		httppost.setEntity(entity);
		HttpResponse response=null;
		String resp=null;
		try {
			response=mClient.execute(httppost);
			resp=EntityUtils.toString(response.getEntity());
		} catch (Throwable t) {
			Log.e("PHOTOAPIBASE",t.getMessage(),t);
		}
		return resp;
		
	}
	
	protected JSONObject parseJSONObject(String json){
		JSONObject j=null;
		try {
			j=new JSONObject(json);
		}
		catch(JSONException e){
			Log.e("PHOTOAPI",e.getMessage(),e);
		}
		return j;
		
	}
	
	protected JSONArray parseJSONArray(String result){
		JSONArray j=null;
		try {
			j=new JSONArray(result);
		}
		catch(JSONException e){
			Log.e("PHOTOAPI",e.getMessage(),e);
		}
		return j;
		
	}
	
}
