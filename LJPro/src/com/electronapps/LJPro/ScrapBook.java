package com.electronapps.LJPro;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScrapBook extends PhotoAPIBase {

	private String mUsername;
	private String mP;
	 private static final int CONNECTION_TIMEOUT = 15000000;
	    Pattern challengeRE=Pattern.compile("(?<=<Challenge>)[^<]*");
	    Pattern photourlRE=Pattern.compile("(?<=<URL>)[^<]*");
	    Pattern errorRE=Pattern.compile("(?=</Error>)[^>]*");
	    private static final String SCRAPBOOK_ENDPOINT="http://pics.livejournal.com/interface/simple";


	public ScrapBook(Context c,String username,String p) {
		mUsername=username;
		mContext=c;
		mP=p;
		HttpParams http_params = mClient.getParams();
	    http_params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
	    HttpConnectionParams.setConnectionTimeout(http_params, CONNECTION_TIMEOUT);
	    HttpConnectionParams.setSoTimeout(http_params, CONNECTION_TIMEOUT);
	    mClient.setParams(http_params);
	}
	
	public String getChallenge() {
		String challenge=null;
		try {
		HttpResponse response = null;
		HttpGet getchal=new HttpGet(SCRAPBOOK_ENDPOINT);
		getchal.addHeader("X-FB-User",mUsername);
		getchal.addHeader("X-FB-Mode","GetChallenge");
		response=mClient.execute(getchal);
		HttpEntity resp=response.getEntity();
		String body=EntityUtils.toString(resp);
		resp.consumeContent();
		Matcher matcher=challengeRE.matcher(body);
		
		if (matcher.find()) {
			challenge=matcher.group();
		}
	
		}
		catch(Throwable t) {
			handleError(t);
			
		}
		 return challenge;
		
	}
	
	public void uploadPhoto(String title, String file,String type) {
		File photoFile=new File(file);
			String challenge=getChallenge();
			if (challenge!=null) {
			String auth="crp:"+challenge+":"+Md5.MD5(challenge+Md5.MD5(mP));
			HttpPut upload=new HttpPut(SCRAPBOOK_ENDPOINT);
			//MultipartEntity mp_entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE );  
			//mp_entity.addPart(new FormBodyPart("",new FileBody(photoFile)));
			//FileBody content=new FileBody(photoFile);
			//String type=content.getMimeType();
			upload.setEntity(new FileEntityMonitored(mContext,photoFile,title,type));
			upload.addHeader("X-FB-User",mUsername);
			upload.addHeader("X-FB-Mode","UploadPic");
			if (!title.equals("")) {
				upload.addHeader("X-FB-UploadPic.Meta.Filename",title);
			}
			upload.addHeader("X-FB-Auth",auth);
			upload.addHeader("X-FB-UploadPic.Gallery._size","1");
			upload.addHeader("X-FB-UploadPic.Gallery.0.GalName","Mobile Uploads");
			try {
			HttpResponse response=mClient.execute(upload);
			String uploadResp=EntityUtils.toString(response.getEntity());
			Matcher errorMatcher=errorRE.matcher(uploadResp);
			if (errorMatcher.find()) {
				String error=errorMatcher.group();
				Intent intent=new Intent(PhotoAPIBase.UPLOAD_ERROR);
				intent.putExtra("error", error);
				intent.putExtra("tite", title);
				intent.putExtra("file", file);
				mContext.sendBroadcast(intent);
			
			}
			else {
				Matcher urlMatcher=photourlRE.matcher(uploadResp);
				if (urlMatcher.find()) {
					String url=urlMatcher.group();
					Intent intent=new Intent(PhotoAPIBase.UPLOAD_COMPLETED);
					intent.putExtra("file",file);
					intent.putExtra("title", title);
					intent.putExtra("provider", "ScrapBook");
					intent.putExtra("source",url);
					mContext.sendBroadcast(intent);
					//TODO broadcast error
					
				}
			}
			
			Log.d("SCRAPBOOK",uploadResp);
			}
			catch(Throwable t){
				handleError(t);
			}
			}
			
			
	}

	
	
	
	

	
	
	
}
