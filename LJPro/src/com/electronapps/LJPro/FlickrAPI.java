package com.electronapps.LJPro;

import java.io.File;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.zmosoft.flickrfree.MultipartEntityMonitored;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;



public class FlickrAPI extends PhotoAPIBase {
	
	private String mKey;
	private String mFrob;
	private String mSecret;
	private HashMap<String,String> params=new HashMap<String,String>();
	private String mSignature;
	private TreeSet<String> paramKeys;
	private final static String BASE_ENDPOINT="http://api.flickr.com/services/";
	private final static String AUTH_ENDPOINT="http://www.flickr.com/services/auth/?";
	private final static String REST_ENDPOINT=BASE_ENDPOINT+"rest/?";
	private final static String UPLOAD_ENDPOINT=BASE_ENDPOINT+"upload/?";
	
	public FlickrAPI(Context c,String APIKey, String APISecret) {
		mKey=APIKey;
		mSecret=APISecret;
		mContext=c;
	}
	
	private void makeEmptyParams() {
		params=new HashMap<String,String>();
		params.put("api_key", mKey);
		params.put("format", "json");
		params.put("nojsoncallback", "1");
		
	}
	
	private void getFrob(){
		makeEmptyParams();
		params.put("method","flickr.auth.getFrob");
		makeSignature();
		String url=getURL(REST_ENDPOINT);
		JSONObject resp=restCall(url);
		String frob=null;
		try {
			frob=resp.getJSONObject("frob").getString("_content");
		} catch (JSONException e) {
			Log.e("GETFROB",e.getMessage(),e);
		}
		mFrob=frob;

	
		
		
	}
	
	public ContentValues finishAuthentication(String frob) {
		makeEmptyParams();
		params.put("method","flickr.auth.getToken");
		params.put("frob", frob);
		makeSignature();
		String result=doGet(getURL(REST_ENDPOINT));
		
		JSONObject resp=parseJSONObject(result);
		JSONObject auth=null;
		JSONObject user=null;
		try {
			auth = resp.getJSONObject("auth");
			user=auth.getJSONObject("user");
			if (auth!=null&&user!=null) {
				ContentValues account=new ContentValues();
				account.put("auth_token",auth.getJSONObject("token").getString("_content"));
				account.put("photo_account",user.getString("username"));
				account.put("photo_accountid",user.getString("nsid"));
				account.put("provider", "Flickr");
				return account;
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return null;
	}
	
	public  HashMap<String,String> initAuth() {
		getFrob();
		params=new HashMap<String,String>();
		params.put("api_key", mKey);
		params.put("frob", mFrob);
		params.put("perms", "write");
		makeSignature();
		HashMap<String,String> auth=new HashMap<String,String>();
		auth.put("frob", mFrob);
		auth.put("url",getURL(AUTH_ENDPOINT));
		return auth;
		
	}
	
	private String getURL(String base) {
		StringBuilder urlBuilder=new StringBuilder(base);
		int i=0;
		int numParams=paramKeys.size();
		for(String key:paramKeys){
			
			urlBuilder.append(key+"="+params.get(key)+(i==numParams-1?"":"&"));
			i++;
		}
		String authURL=urlBuilder.toString();
		return authURL;
	}
	private static final Pattern photoID=Pattern.compile("(?<=<photoid>)[0-9]*");
	public  HashMap<String,String> uploadPhoto(String token,String title, String photopath){
		params.put("api_key", mKey);
		params.put("auth_token",token);
		try {
			MultipartEntityMonitored postbody=new MultipartEntityMonitored(mContext,photopath,title);
			File photoFile=new File(photopath);
			postbody.addPart("photo",new FileBody(photoFile));
			postbody.addPart("api_key",new StringBody(mKey));
			postbody.addPart("auth_token",new StringBody(token));
			if (!title.equals("")) {
				params.put("title",title);
				postbody.addPart("title", new StringBody(title));
			}
			makeSignature();
			postbody.addPart("api_sig",new StringBody(params.get("api_sig")));
			String resp=doPost(UPLOAD_ENDPOINT,postbody);
			Matcher photomatch=photoID.matcher(resp);
			String photoid=null;
			if(photomatch.find()) {
				photoid=photomatch.group();
			}
			makeEmptyParams();
			params.put("method","flickr.photos.getSizes");
			params.put("auth_token", token);
			params.put("photo_id", photoid);
			makeSignature();
			String httpresp=doGet(getURL(REST_ENDPOINT));
			JSONObject json=parseJSONObject(httpresp);
			JSONArray sizes=json.getJSONObject("sizes").getJSONArray("size");
			HashMap<String,String> photodata=new HashMap<String,String>();
			int nSizes=sizes.length();
			JSONObject size=sizes.getJSONObject(nSizes-1);

			photodata.put("source", size.getString("source"));
			photodata.put("link","http://flic.kr/p/"+FlickrBaseEncoder.encode(Long.parseLong(photoid)));




			return photodata;
		
		
		
		}
		
		catch(Throwable t) {
			Log.e("FLICKRUPLOAD",t.getMessage(),t);
			
		}
		
		return null;
		
		
	}
	
	
	
	private void makeSignature() {
		paramKeys=new TreeSet<String>(params.keySet());
		StringBuilder sigStringBuilder=new StringBuilder(mSecret);
		for(String key:paramKeys){
			sigStringBuilder.append(key+params.get(key));
		}
		String sigString=sigStringBuilder.toString();
		params.put("api_sig",Md5.MD5(sigString));
		paramKeys=new TreeSet<String>(params.keySet());
		
	}
	
	private JSONObject restCall(String url){
		String httpresp=doGet(url);
		JSONObject resp=parseJSONObject(httpresp);
		return resp;
	}
	
	public static class FlickrBaseEncoder {
		protected static final String alphabetString = "123456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ";
		protected static final char[] alphabet = alphabetString.toCharArray();
		protected static final int base_count = alphabet.length;
		
		public static String encode(long num){
			String result = "";
			long div;
			int mod = 0;
			
			while (num >= base_count) {
				div = num/base_count;
				mod = (int)(num-(base_count*(long)div));
				result = alphabet[mod] + result;
				num = (long)div;
			}
			if (num>0){
				result = alphabet[(int)num] + result;
			}
			return result;
		}
		
		public static long decode(String link){
				long result= 0;
				long multi = 1;
				while (link.length() > 0) {
					String digit = link.substring(link.length()-1);
					result = result + multi * alphabetString.lastIndexOf(digit);
					multi = multi * base_count;
					link = link.substring(0, link.length()-1);
				}
				return result;
		}
	}

}
