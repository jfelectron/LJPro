package com.electronapps.LJPro;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.electronapps.LJPro.FlickrAPI.FlickrBaseEncoder;
import com.zmosoft.flickrfree.MultipartEntityMonitored;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

public class PhotoBucketAPI extends DefaultApi10a {

	private static final String API_BASE="http://api.photobucket.com";
	 private static final String BASE_LOGIN=API_BASE+"/login";
	 private static final String ALBUM_ENDPOINT="/album";
	 private static final String UPLOAD_ENDPOINT="%2FMobile%20Uploads/upload";
	 
	 private static final String AUTHORIZATION_URL = "http://photobucket.com/apilogin/login?oauth_token=%s";
	private OAuthService mPBAuth;
	private Token mToken;
	private String mUsername;
	private PhotoAPIBase mBase=new PhotoAPIBase();
	private String mDomain;
	private Context mContext;
	@Override
	public String getAccessTokenEndpoint() {
		// TODO Auto-generated method stub
		return BASE_LOGIN+"/access";
	}
	
	
	public PhotoBucketAPI() {
		
	}
	public PhotoBucketAPI (Context context,Token token,String username,String domain){
		Resources res=context.getResources();
		mPBAuth= new ServiceBuilder()
        .provider(PhotoBucketAPI.class)
        .apiKey(res.getString(R.string.photobucket_key))
        .apiSecret(res.getString(R.string.photobucket_secret))
        .build();
		mToken=token;
		mContext=context;
		mUsername=username;
		mDomain=domain;
	}

	@Override
	public String getAuthorizationUrl(Token requestToken) {
		// TODO Auto-generated method stub
		return String.format(AUTHORIZATION_URL, requestToken.getToken());
	}

	@Override
	public String getRequestTokenEndpoint() {
		// TODO Auto-generated method stub
		return BASE_LOGIN+"/request";
	}
	
	public Boolean createAlbum(){
		OAuthRequest request = new OAuthRequest(Verb.POST,API_BASE+ALBUM_ENDPOINT+"/"+mUsername+UPLOAD_ENDPOINT);
		request.addBodyParameter("format","json");
		request.addBodyParameter("name","MobileUploads" );
		mPBAuth.signRequest(mToken, request);
		String content=request.getBodyContents();
		try {
			request = new OAuthRequest(Verb.POST,"http://"+mDomain+ALBUM_ENDPOINT+"/"+mUsername+UPLOAD_ENDPOINT);
			request.addPayload(content);
			Response response=request.send();
			String resp=response.getBody();
			JSONObject json=mBase.parseJSONObject(resp);
			if (json.getString("status").equals("OK")||json.getString("code").equals("116")) {
				return true;
			}
			
			Log.d("CREATEALBUM",json.toString());
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			Log.e("PBCREATEALBUM",e.getMessage(),e);
		}
		return false;
	}
	public HashMap<String,String> uploadPhoto(String subdomain,Token token,String photopath, String title){
		OAuthRequest request = new OAuthRequest(Verb.POST,API_BASE+ALBUM_ENDPOINT+"/"+mUsername+UPLOAD_ENDPOINT);
		request.addBodyParameter("format","json");
		HashMap<String,String> photodata=new HashMap<String,String>();
		if (title.length()!=0) request.addBodyParameter("title", title);
		request.addBodyParameter("type", "image");
		mPBAuth.signRequest(mToken, request);
		Map<String,String> params=request.getBodyParams();
		Set<String> keys=params.keySet();
		try {
		MultipartEntityMonitored postbody=new MultipartEntityMonitored(mContext,photopath,title);
		for (String key:keys) {
			
			postbody.addPart(key,new StringBody(params.get(key)));
			
		}
		postbody.addPart("uploadfile",new FileBody(new File(photopath)));
		//http://api861.photobucket.com/album/Mobile%20Uploads
		String resp=mBase.doPost("http://"+mDomain+ALBUM_ENDPOINT+"/"+mUsername+UPLOAD_ENDPOINT,postbody);
		JSONObject json=mBase.parseJSONObject(resp);
		JSONObject content=json.getJSONObject("content");
		
		photodata.put("source",content.getString("url"));
		photodata.put("link",content.getString("browseurl"));
		Log.d("PBUPLOAD",json.toString());
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			Log.e("PBUPLOAD",e.getMessage(),e);
		}
		
		return photodata;
	}

}
