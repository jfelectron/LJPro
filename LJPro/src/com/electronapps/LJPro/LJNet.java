package com.electronapps.LJPro;

import java.io.File;
import java.io.IOException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.scribe.model.Token;
import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.electronapps.LJPro.LJTypes.LJUser;
import com.electronapps.LJPro.PicasaAPI.SendData;
import com.zmosoft.flickrfree.MultipartEntityMonitored;


import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;


public class LJNet extends WakefulIntentService {
	
	//INTENT Actions handled by this service
	public static final String LJ_LOGIN="com.electronapps.LJPro.intent.login";
	public static final String LJ_NEWACCT="com.electronapps.LJPro.intent.newaccount";
	public static final String LJ_GETFRIENDS="com.electronapps.LJPro.intent.getfriends";
	public static final String LJ_POSTEVENT="com.electronapps.LJPro.intent.postevent";
	public static final String LJ_EDITEVENT="com.electronapps.LJPro.intent.editevent";
	public static final String LJ_GETUSERTAGS="com.electronapps.LJPro.intent.getusertags";
	public static final String LJ_EDITFRIENDS="com.electronapps.LJPro.intent.editfriends";
	public static final String LJ_EDITFRIENDGROUPS="com.electronapps.LJPro.intent.editfriendgroups";
	public static final String LJ_GETFRIENDSPAGE="com.electronapps.LJPro.intent.getfriendspage";
	public static final String LJ_GETRECENTCOMMENTS="com.electronapps.LJPro.intent.getrecentcomments";
	public static final String LJ_ADDCOMMENT="com.electronapps.LJPro.intent.addcomment";
	public static final String LJ_SYNCITEMS="com.electronapps.LJPro.intent.syncintems";
	public static final String LJ_GETSESSION="com.electronapps.LJPro.intent.getsession";
	private static final String LJ_POSTUPLOADED ="com.electronapps.LJPro.intent.postuploaded";
	public static final String LJ_GETCOMMENTS="com.electronapps.LJPro.intent.getcomments";
	public static final String LJ_XMLERROR="com.electronapps.LJPro.intent.xmlerror";
	public static final String LJ_CONNECTIONERROR="com.electronapps.LJPro.intent.connectionerror";
	public static final String LJ_WRONGLOGIN="com.electronapps.LJPro.intent.wronglogin";
	public static final String LJ_LOGINUPDATED="com.electronapps.LJPro.intent.loginupdated";
	public static final String LJ_ACCOUNTADDED="com.electronapps.LJPro.intent.accountadded";
	public static final String LJ_FRIENDSUPDATED="com.electronapps.LJPro.intent.friendsupdated";
	public static final String LJ_NOSUCHUSER="com.electronapps.LJPro.intent.nosuchuser";
	public static final String LJ_NOSUCHGROUP="com.electronapps.LJPro.intent.nosuchuser";
	public static final String LJ_FRIENDADDED="com.electronapps.LJPro.intent.friendadded";
	public static final String LJ_FRIENDDELETED="com.electronapps.LJPro.intent.frienddeleted";
	public static final String LJ_FRIENDEDITED="com.electronapps.LJPro.intent.frienddedited";
	public static final String LJ_GROUPADDED="com.electronapps.LJPro.intent.groupadded";
	public static final String LJ_GROUPDELETED="com.electronapps.LJPro.intent.groupdeleted";
	public static final String LJ_GROUPEDITED="com.electronapps.LJPro.intent.groupedited";
	public static final String LJ_FRIENDSPAGEUPDATED = "com.electronapps.LJPro.intent.friendspageupdated";
	public static final String LJ_FPDONEUPDATING = "com.electronapps.LJPro.intent.dpdone";
	public static final String LJ_COMMENTSUPDATED= "com.electronapps.LJPro.intent.newcomments";
	public static final String LJ_COMMENTADDED= "com.electronapps.LJPro.intent.commentadded";
	public static final String LJ_ADDEDCOMMENT= "com.electronapps.LJPro.intent.addedcomment";
	public static final String LJ_COMMENTERROR= "com.electronapps.LJPro.intent.commenterror";
	public static final String LJ_NOCOMMENTS= "com.electronapps.LJPro.intent.noomments";
	public static final String LJ_TAGSUPDATED= "com.electronapps.LJPro.intent.tagsupdated";
	
	public static final String LJ_SCRAPBOOK= "com.electronapps.LJPro.intent.scrapbook";
	public static final String LJ_PICASA= "com.electronapps.LJPro.intent.picasa";
	public static final String LJ_FLICKR= "com.electronapps.LJPro.intent.flickr";
	public static final String LJ_PBUCKET= "com.electronapps.LJPro.intent.photobucket"; 
	
	private DateFormat locallong=DateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.SHORT);

		
	private XMLRPCClient ljclient = new XMLRPCClient("http://www.livejournal.com/interface/xmlrpc");
	private Cursor cUser;
	private SharedPreferences appPrefs;
	private LJDB LJDBAdapter;
	private int skip=0;
	private LJPro app;
	private boolean background=false;
	private ObjectMapper serializer=new ObjectMapper();
	public static final String TAG=LJNet.class.getSimpleName();
		private LJTypes.LJUser ljUser;
	
		
		
	public LJNet() {
	        //We need to call the Constructor of the superclass with a name for our class.
	        //This is required but only useful for debugging
	        super("LJNet");
	 }
	public final static String NO_CONNECTION="noconnection";
	
		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			return super.onStartCommand(intent, flags, startId);
			
		}
	    @Override
	    protected void doWakefulWork(Intent intent) {
	    	String action=intent.getAction();
	    	Bundle extras=intent.getExtras();
	    	ljUser=new LJTypes.LJUser();
	    	ljUser.journalname=extras.getString("journalname");
	    	app=(LJPro) getApplicationContext();
	    	if (!app.haveConnection()) {
	    		handleError(NO_CONNECTION);
	    		return;
	    	}
	    	background=intent.hasExtra("background");
	    	LJDBAdapter=LJDB.getDB(getApplicationContext());
	        LJDBAdapter.open();
	        appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	    	
	    	
	    	if (!action.equals(LJ_NEWACCT)){
	    		getAuthInfo();
	    	}
	    	
	    	

	    	
	    	
	    	
	    		if (action.equals(LJ_LOGIN)) {
	    			
	    			
					if (ljUser.authInfo!=null) {
						login(ljUser,false);
					}
	    			
	    			
	    			
	    		}
	    		
	    		else if (action.equals(LJ_NEWACCT)){
	    			
	    			addNewAccount(extras);
	    			
	    			
	    			
	    			}
	    	
	    		else if (action.equals(LJ_GETFRIENDS)) {
	    			
	    			doGetFriends();
	    			
	    			
	    			
	    			
	    		}
	    		
	    		else if (action.equals(LJ_EDITFRIENDS)) {
	    			
	    			doEditFriends(intent);
	    		}
	    		
	    		else if (action.equals(LJ_GETCOMMENTS)) {
	    			doGetComments(intent);
					
					
					
	    			
						

	    			
	    		}
	    		
	    		else if (action.equals(LJ_EDITFRIENDGROUPS)) {
	    			doEditGroups(intent);
	    		}
	    		
	    		else if (action.equals(LJ_POSTEVENT)) {
	    			doPostEvent(intent);
	    			
	    		}
	    		
	    		else if (action.equals(LJ_EDITEVENT)) {
	    			
	    		}
	    		
	    		
	    		else if (action.equals(LJ_GETFRIENDSPAGE)) {
	    			
	    		
	    			if (intent.hasExtra("refreshOld")) friendsPageLooper(false,false);
	    			else friendsPageLooper(false,true);
	    			
	    		}
	    		
	    		else if (action==LJ_GETRECENTCOMMENTS) {
	    			doAddComment(intent);
	    		}
	    		
	    		else if (action.equals(LJ_ADDCOMMENT)) {
	    			doAddComment(intent);
	    			
	    		}
	    		
	    		else if (action.equals(LJ_GETUSERTAGS)) {
	    			doGetTags();
	    		}
	    		
	    		
	    		
	    		else if (action.equals(LJ_SYNCITEMS)) {
	    			
	    		}
	    		
	    		else if (action.equals(LJ_GETSESSION)) {
	    			
	    			getSession();
	    		}
	    		
	    		else if (action.equals(LJ_SCRAPBOOK)) {
	    			uploadScrapBook(intent.getStringExtra("title"),intent.getStringExtra("file"),intent.getStringExtra("type"));
	    		}
	    		
	    		else if (action.equals(LJ_FLICKR)){
	    			uploadFlickr(intent.getStringExtra("token"),intent.getStringExtra("file"),intent.getStringExtra("title"));
	    		}
	    		else if (action.equals(LJ_PBUCKET)) {
	    			uploadPhotoBucket(intent);
	    		}
	    		
	    		else if(action.equals(LJ_PICASA)){
	    			uploadPicasa(intent);
	    		}
	    		
	    		if (cUser!=null&&!cUser.isClosed()) {
	    			cUser.close();
	    		}
	    		
	    		
	    		
	    		
	    		
	    		
	    		
	    		
	    	
	  
	    }
	   private static final String[] noprops={"subject","event","security","usejournal"};
	 	private static final ArrayList<String> noProps=new ArrayList<String>(Arrays.asList(noprops));
		
	 	
	    @SuppressWarnings("unchecked")
		private void doPostEvent(Intent intent) {
	   
	    	HashMap<String,Object> result=new HashMap<String,Object>();
	    	
	    	 try {
	    		 ContentValues post=(ContentValues)intent.getParcelableExtra("post");
	 	    	Set<Entry<String, Object>> postOps=post.valueSet();
	    	 
				   HashMap<String,Object> params=initAuth(ljUser);
				  HashMap<String,Object> props=new HashMap<String,Object>();
	    	for (Entry<String,Object> entry:postOps){
	    		if (noProps.indexOf(entry.getKey())!=-1) {
	    		params.put(entry.getKey(), entry.getValue());
	    		}
	    		else {
	    			props.put(entry.getKey(), entry.getValue());
	    		}
	    	}
	    	params.put("props", props);
	    	Calendar calendar=Calendar.getInstance();
	    	params.put("year",calendar.get(Calendar.YEAR));
	    	params.put("mon", calendar.get(Calendar.MONTH)+1);
	    	params.put("day", calendar.get(Calendar.DAY_OF_MONTH));
	    	params.put("hour", calendar.get(Calendar.HOUR_OF_DAY));
	    	params.put("min", calendar.get(Calendar.MINUTE));
	    	result=(HashMap<String,Object>) ljclient.call("LJ.XMLRPC.postevent",params);
	    	if (result.containsKey("url")){
	    		Intent uploaded=new Intent(LJ_POSTUPLOADED);
	    	}
	    	 }
	    	catch(Throwable e){
	    		handleError(e.getMessage());
	    	}
		}
		
		private void uploadPhotoBucket(Intent intent) {
	    	
			String file=intent.getStringExtra("file");
			String title=intent.getStringExtra("title");
			Token token=new Token(intent.getStringExtra("token"),intent.getStringExtra("secret"));
			String account=intent.getStringExtra("account");
			String subdomain=intent.getStringExtra("subdomain");
			PhotoBucketAPI pbapi=new PhotoBucketAPI(getApplicationContext(),token,account,subdomain);
			
	    	if (!appPrefs.getBoolean(account+"_albumcreated", false)) {
	    		Boolean success=pbapi.createAlbum();
	    		if(success) {
	    			Editor editor=appPrefs.edit();
	    			editor.putBoolean(account+"_albumcreated", true);
	    			editor.commit();
	    			pbapi.uploadPhoto(subdomain, token, file, title);
	    		}
	    		else {
	    			handleError("albumcreation");
	    		}
	    	}
	    	else{
	    		HashMap<String,String> result=pbapi.uploadPhoto(subdomain, token, file, title);
	    		Intent done=new Intent(PhotoAPIBase.UPLOAD_COMPLETED);
				done.putExtra("file",file);
				done.putExtra("title", title);
				done.putExtra("provider", "PhotoBucket");
				done.putExtra("source",result.get("source"));
				done.putExtra("link", result.get("link"));
				sendBroadcast(done);
	    	}
			
			
		}
		private void uploadScrapBook(String title,String file,String type) {
	    	try {
	    		String key=LJTypes.createKey(ljUser.authInfo.hash, ljUser.accountadded);
	    		String p=SimpleCrypto.decrypt(key,ljUser.authInfo.pcrypt);
	    		ScrapBook scrapbook=new ScrapBook(getApplicationContext(),ljUser.journalname,p);
	    		scrapbook.uploadPhoto(title,file,type);	
	    	}
	    	catch(Throwable t){
	    		handleError("scrapbook");
	    	}
			
			
		}
	    
	    private void uploadFlickr(String token,String photopath,String title) {
	    	Resources res=getApplicationContext().getResources();
	    	FlickrAPI flickr=new FlickrAPI(getApplicationContext(),res.getString(R.string.flickr_key),res.getString(R.string.flickr_secret));
	    	HashMap<String,String> result=flickr.uploadPhoto(token, title, photopath);
	    	Intent intent=new Intent(PhotoAPIBase.UPLOAD_COMPLETED);
			intent.putExtra("file",photopath);
			intent.putExtra("title", title);
			intent.putExtra("provider", "Flickr");
			intent.putExtra("source",result.get("source"));
			intent.putExtra("link", result.get("link"));
			sendBroadcast(intent);
			//TODO broadcast error
	    }
	    
	    private void uploadPicasa(Intent intent){
	    	String file=intent.getStringExtra("file");
	    	String title=intent.getStringExtra("title");
	    	SendData upload=new SendData(file,intent.getStringExtra("type"),title);
	    	PicasaAPI picasa=new PicasaAPI(getApplicationContext(),null,null,null);
	    	HashMap<String,String> result=picasa.doUpload(upload, intent.getStringExtra("token"));
	    	
	    	Intent done=new Intent(PhotoAPIBase.UPLOAD_COMPLETED);
			done.putExtra("file",file);
			done.putExtra("title", title);
			done.putExtra("provider", "Picasa");
			done.putExtra("source",result.get("source"));
			done.putExtra("link", result.get("link"));
			sendBroadcast(done);
	    	
	    	
	    }
		private void doGetTags() {
	    	HashMap<String, Object> result=new HashMap<String,Object>();
			   try {
				   HashMap<String,Object> params=initAuth(ljUser);
					
					//params.put("itemshow",intent.getIntExtra("itemshow", 25));
					result  =(HashMap<String,Object>) ljclient.call("LJ.XMLRPC.getusertags",params);
					if (result.containsKey("tags")){
						processTags(result.get("tags"));
					}
					
					else if (result.containsKey("error")){
						handleError("tags");
					}
	
					}
			   
			catch (Throwable e) {
				result.put("Error",true);
				
				String error=e.getMessage();
				Log.e(TAG,error,e);
				handleError(error);
				
				
			}
			
		}
		private void processTags(Object tags) {
			Object[] Tags=null;
			try{
				Tags=(Object[]) tags;
			}
			catch(ClassCastException e) {
				Log.e(TAG,e.getMessage(),e);
				
			}
			int numTags;
			if (Tags==null) numTags=0;
			else numTags=Tags.length;
			if (numTags>0) {
				ContentValues[] tagValues=new ContentValues[numTags];
				try {
					for (int i=0;i<numTags;i++) {
					
						HashMap<String,Object> Tag=(HashMap<String, Object>) Tags[i];
						ContentValues tag=new ContentValues(3);
						tag.put("accountname", ljUser.journalname);
						tag.put("name", getStringOrUTF(Tag.get("name")));
						tag.put("uses",(Integer)Tag.get("uses"));
						tagValues[i]=tag;
					}
					
					if (LJDBAdapter.updateTags(tagValues)) {
						Intent tagsupdated=new Intent(LJ_TAGSUPDATED);
						tagsupdated.putExtra("journalname",ljUser.journalname);
						sendBroadcast(tagsupdated);
					}
					else {
						handleError("tags");
					}
					
				}
					catch (ClassCastException e) {
						Log.e(TAG,e.getMessage(),e);
						handleError("tags");
					}
				
				
			}
			
		}
		private void doAddComment(Intent intent) {
	    	 HashMap<String,Object> result=new HashMap<String,Object>();
	    	 Integer talkid=intent.getIntExtra("talkid", 0);
			Integer parentTalkId=(int) Math.floor(talkid/256d);
			Integer ditemid=intent.getIntExtra("ditemid", 0);
			   try {
				   HashMap<String,Object> params=initAuth(ljUser);
					params.put("poster",ljUser.journalname);
					params.put("journal",intent.getStringExtra("postjournal"));
					params.put("subject",intent.getStringExtra("subject"));
					params.put("body",intent.getStringExtra("comment"));
					params.put("ditemid",ditemid);
					params.put("parenttalkid", parentTalkId);
					result=(HashMap<String,Object>) ljclient.call("LJ.XMLRPC.addcomment",params);
					if (result.containsKey("status")){
						Intent commentadded=new Intent(LJ_ADDEDCOMMENT);
						commentadded.putExtra("journal",intent.getStringExtra("postjournal"));
						commentadded.putExtra("ditemid",ditemid);
						sendBroadcast(commentadded);
						doGetComments(commentadded);
						
						
						//app.clearNotification(LJPro.COMMENT_ID);
					
					
					}
			   }
			catch (Throwable e) {
				result.put("Error",true);
				Log.e(TAG,e.getMessage(),e);
				String error=e.getMessage();
				Intent commenterror=new Intent(LJ_COMMENTERROR);
				commenterror.putExtra("type","adding");
				sendBroadcast(commenterror);
				app.notifyComment(LJPro.COMMENT_ERROR,null,ljUser.journalname);
				handleError(error);
				
				
			}
			
		}
		private void doEditGroups(Intent intent) {
	    	
			if (intent.hasExtra("addgroup")) {
				doAddGroup(intent);
			
			}
			else if (intent.hasExtra("delgroups")) {
				doDelGroups(intent);
				
			}
			else if (intent.hasExtra("groupmask")) {
				
				doEditGroupmasks(intent);
    			
    		
			}
			
		}

		private void doEditGroupmasks(Intent intent) {
			HashMap<String, Object> result=editFriendGroups(ljUser,(HashMap<String,Integer>)intent.getSerializableExtra("groupmask"));
			String error=((Object) result.get("Error")).toString();
			if (!Boolean.valueOf(error) &&  !result.containsKey("faultString")) {
	
				Intent friendedited=new Intent(LJNet.LJ_GROUPEDITED);
				cUser.close();
				
				sendStickyBroadcast(friendedited);
		
			}
			else {
    			handleError(error,result);
    				
    		}
			
		}

		private void doDelGroups(Intent intent) {
			Object [] delfriends=(Object[]) intent.getSerializableExtra("delgroups");
			HashMap<String, Object> result=delGroups(ljUser,delfriends);
			String error=((Object) result.get("Error")).toString();
			if (!Boolean.valueOf(error) &&  !result.containsKey("faultString")) {
				Intent friendadded=new Intent(LJNet.LJ_GROUPDELETED);
				cUser.close();
			
				sendStickyBroadcast(friendadded);
		
			}
			else {
    			handleError(error,result);
    				
    		}
			
		}

		private void doAddGroup(Intent intent) {
			HashMap<String, Object> result=addGroup(ljUser,intent);
			String error=((Object) result.get("Error")).toString();
			if (!Boolean.valueOf(error) &&  !result.containsKey("faultString")) {
				

				intent.setAction(LJNet.LJ_GROUPADDED);
				cUser.close();
				sendStickyBroadcast(intent);
		
			}
			else {
    			handleError(error,result);
    				
    		}
		}
			

		private void doGetComments(Intent intent){
			
			HashMap<String, Object> result=new HashMap<String,Object>();
			   try {
				   HashMap<String,Object> params=initAuth(ljUser);
					params.put("journal",intent.getStringExtra("journal"));
					params.put("extra", 1);
					params.put("ditemid", intent.getIntExtra("ditemid", 0));
					params.put("expand_strategy","mobile_thread");
					params.put("only_loaded", 1);
					params.put("format","list");
					//params.put("itemshow",intent.getIntExtra("itemshow", 25));
					result  =(HashMap<String,Object>) ljclient.call("LJ.XMLRPC.getcomments",params);
					if (result.containsKey("comments")){
						processComments(result,intent);
					}
					
					else if (result.containsKey("error")){
						Intent postdeleted=new Intent(LJ_COMMENTERROR);
						postdeleted.putExtra("type", "fetching");
						sendBroadcast(postdeleted);
						cUser.close();
						
						return;
					}
	
					}
			   
			catch (Throwable e) {
				result.put("Error",true);
				
				String error=e.getMessage();
				Log.e(TAG,error,e);
				handleError(error);
				
				
			}
		}
	
		
		private void processComments(HashMap<String,Object> result,Intent intent) {
			int commentcount=0;
			Integer threadroot=0;
			Object[] comments=(Object[]) result.get("comments");
			final int postid=intent.getIntExtra("ditemid",0);
			final String accountname=ljUser.journalname;
			for (int i=0;i<comments.length;i++) {
				commentcount++;
				ContentValues comment=new ContentValues();
				HashMap<String,Object> cmap=(HashMap<String,Object>) comments[i];
				comment.put("subject", getStringOrUTF(cmap.get("subject")));
				comment.put("event_raw",getStringOrUTF(cmap.get("body")));
				Date commentTime=new Date(Integer.parseInt(cmap.get("datepost_unix").toString())*1000l);
				comment.put("date",locallong.format(commentTime));
				comment.put("logtime",commentTime.getTime());
				comment.put("postername", (String)cmap.get("user"));
				comment.put("ditemid", postid);
				comment.put("accountname", accountname);
				comment.put("userpic", (String)cmap.get("userpic"));
				Integer id=(Integer)cmap.get("dtalkid");
				Integer parentid=(Integer)cmap.get("parentdtalkid");
				Integer level=(Integer) cmap.get("level");
				if (level==0) threadroot=id;
				comment.put("talkid", id);
				if (parentid==0) comment.put("thread", id);
				else comment.put("thread", threadroot);
				comment.put("parentid",parentid);
				ContentValues[] commentvals=new ContentValues[1];
				commentvals[0]=comment;
				boolean success=LJDBAdapter.updateComments(commentvals);
				if (success) {
					intent.setAction(LJ_COMMENTADDED);
					sendBroadcast(intent);
				}
			
				
			}
			if (comments.length>0) {
				intent.setAction(LJ_COMMENTSUPDATED);
				intent.putExtra("numcomments", commentcount);
				sendBroadcast(intent);
			}
			else {
				intent.setAction(LJ_NOCOMMENTS);
				sendBroadcast(intent);
			}
			cUser.close();
				
		}
		
		
			
		
		
			
		

		

		private void doEditFriends(Intent intent) {
	    	if (intent.hasExtra("addfriend")) {
				doAddFriend(intent);
			
			}
			else if (intent.hasExtra("delfriends")) {
				doDelFriends(intent);
				
			}
			else if (intent.hasExtra("groupmask")) {
				doEditFriendMask(intent);
    			
    		
			}
			
		}

		private void doAddFriend(Intent intent) {
			HashMap<String, Object> result=addFriend(ljUser,intent.getStringExtra("addfriend"));
			String error=((Object) result.get("Error")).toString();
			if (!Boolean.valueOf(error) &&  !result.containsKey("faultString")) {
				Object[] added=(Object []) result.get("added");
				HashMap<String,Object> fadded=(HashMap<String,Object>) added[0];
				Intent friendadded=new Intent(LJNet.LJ_FRIENDADDED);
				friendadded.putExtra("friendname", fadded.get("username").toString());
				
				if (fadded.containsKey("fullname")) friendadded.putExtra("fullname",(String) fadded.get("fullname").toString());
				
				if (fadded.containsKey("defaultpicurl")) friendadded.putExtra("userpic",(String) fadded.get("defaultpicurl").toString());
				cUser.close();
				
				sendStickyBroadcast(friendadded);
		
			}
			else {
    			handleError(error,result);
    				
    		}
			
		}
		
		private void doEditFriendMask(Intent intent) {
			HashMap<String, Object> result=editGroups(ljUser,intent.getStringExtra("friend"),intent.getIntExtra("groupmask",1));
			String error=((Object) result.get("Error")).toString();
			if (!Boolean.valueOf(error) &&  !result.containsKey("faultString")) {
	
				Intent friendedited=new Intent(LJNet.LJ_FRIENDEDITED);
				cUser.close();
				
				sendStickyBroadcast(friendedited);
		
			}
			else {
    			handleError(error,result);
    				
    		}
		}
		
		private void doDelFriends(Intent intent) {
			Object [] delfriends=(Object[]) intent.getSerializableExtra("delfriends");
			HashMap<String, Object> result=delFriend(ljUser,delfriends);
			String error=((Object) result.get("Error")).toString();
			if (!Boolean.valueOf(error) &&  !result.containsKey("faultString")) {
				Object[] added=(Object []) result.get("deleted");
				Intent friendadded=new Intent(LJNet.LJ_FRIENDDELETED);
				cUser.close();
				
				sendStickyBroadcast(friendadded);
		
			}
			else {
    			handleError(error,result);
    				
    		}
		}

		private void doGetFriends() {
			HashMap<String, Object> result=getFriends(ljUser);
			String error=((Object) result.get("Error")).toString();
			
			if (!Boolean.valueOf(error) &&  !result.containsKey("faultString")) {
				ContentValues[] friends=processFriends(result);
				boolean success=processFriendGroups((Object[]) result.get("friendgroups"));
				success=success&saveFriends(friends);
				
				
			}
			else {
				handleError(error,result);
				
			}
			
		}

		private void addNewAccount(Bundle extras) {
	    	Date dd=new Date();
			ljUser.accountadded= (Math.round(dd.getTime() / 1000));
			ljUser.authInfo.passMD5=extras.getString("passMD5");
			ljUser.authInfo.hash=LJTypes.createHash();
			try {
				ljUser.authInfo.pcrypt=SimpleCrypto.encrypt(LJTypes.createKey(ljUser.authInfo.hash, ljUser.accountadded),extras.getString("password"));
			} catch (Exception e) {
				
				Log.e(TAG,"Password Encryption Error: "+e.getMessage(),e);
			}
	    	login(ljUser,true);
			
		}

		private void getSession() {
	    	try {
				String key=LJTypes.createKey(ljUser.authInfo.hash, ljUser.accountadded);
				String p=SimpleCrypto.decrypt(key,ljUser.authInfo.pcrypt);

				Connection login=Jsoup.connect("https://www.livejournal.com/login.bml?ret=1&nojs=1");
				login.data("user",ljUser.journalname);
				login.data("password",p);
				login.data("remember_me","1");
				login.data("action:login","Log in...");
				Document response=login.post();
				Connection.Response result2=login.response();
				String expires=result2.header("expires");
				//
				DateFormat formatter=new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
				Date expiration=formatter.parse(expires);
				long etime=expiration.getTime();
				//{ljuniq=imgRxcAgrmdJEdF:1295597273:pgstats0:m0, ljmastersession=v1:u13976821:s438:a0e45S5kYuh//Thanks%20for%20signing%20in%20%2F%20LiveJournal%20loves%20you%20a%20lot%20%2F%20Here%20have%20a%20cookie, ljloggedin=u13976821:s438, BMLschemepref=, langpref=, ljsession=v1:u13976821:s438:t1295596800:g6306e64c5247483e98f2a30a4b471b2d68dbed53//Thanks%20for%20signing%20in%20%2F%20LiveJournal%20loves%20you%20a%20lot%20%2F%20Here%20have%20a%20cookie}
				
			
				Map<String,String> cookies2=result2.cookies();
				String ljmastersession=cookies2.get("ljmastersession");
				String ljloggedin=cookies2.get("ljloggedin");
				String ljsession=cookies2.get("ljsession");
				ContentValues session=new ContentValues();
				session.put("ljmastersession",ljmastersession);
				session.put("ljsession",ljsession);
				session.put("ljloggedin",ljloggedin);
				session.put("expiration",etime);
				LJDBAdapter.updateAccountSession(ljUser.journalname, session);
				ljUser.ljmastersession=ljmastersession;
				ljUser.ljsession=ljsession;
				ljUser.ljloggedin=ljloggedin;
				ljUser.expiration=etime;

				
				
			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(TAG,e.getMessage(),e);
				handleError("getsession");
			}
	    	
	    	
	    }
	    
	    private void friendsPageLooper(boolean b,boolean useSync) {
	    	
	    	HashMap<String, Object> result=getFriendsPage(ljUser,b,useSync);
			String error=((Object) result.get("Error")).toString();
			if (!Boolean.valueOf(error) &&  !result.containsKey("faultString")) {
					ContentValues[] posts=processFriendsPage(result);
				if (posts!=null) {
					saveFriendsPage(posts,true,useSync);
				}
				
		
				}
			else {
    			handleError(error,result);
    				
    		}
	    }
	    
	    private void saveFriendsPage(ContentValues[] posts,boolean broadcast,boolean useSync) {
	    	
			Boolean success=false;
			
			if (posts.length>0) success=LJDBAdapter.updateFriendsPage(posts);
			if (success) {
				try {
					if(broadcast){
						Intent loginupdated=new Intent(LJ_FRIENDSPAGEUPDATED);
						loginupdated.putExtra(LJDB.KEY_JOURNALNAME, ljUser.journalname);
						if(background) loginupdated.putExtra("background", true);
						sendOrderedBroadcast(loginupdated,null);
						
					}
					if (moreItems&&skip<=100) {
						friendsPageLooper(true,useSync);
						
					}
					else {
						SharedPreferences.Editor editor = appPrefs.edit();
						Date d = new Date();
						editor.putLong(ljUser.journalname + "friendspage_lastupdate", d.getTime());
						editor.commit();
						final LJPro app=(LJPro) getApplicationContext();
						if (app.fprefreshing.get(ljUser.journalname)) {
							app.fprefreshing.put(ljUser.journalname, false);
							app.clearNotification(LJPro.SYNC_ID);
						}
						Intent loginupdated=new Intent(LJ_FPDONEUPDATING);
						loginupdated.putExtra(LJDB.KEY_JOURNALNAME, ljUser.journalname);
						sendStickyBroadcast(loginupdated);
						cUser.close();
						
					}
					
				}
				catch(Throwable e) {
					Log.e(TAG,e.getMessage(),e);
					cUser.close();
					
					Intent loginupdated=new Intent(LJ_XMLERROR);
					loginupdated.putExtra(LJDB.KEY_JOURNALNAME, ljUser.journalname);
					sendStickyBroadcast(loginupdated);
					stopSelf();
					
				}
			}
			else {
				SharedPreferences.Editor editor = appPrefs.edit();
				Date d = new Date();
				editor.putLong(ljUser.journalname + "friendspage_lastupdate", d.getTime());
				editor.commit();
				final LJPro app=(LJPro) getApplicationContext();
				if (app.fprefreshing.get(ljUser.journalname)) {
					app.fprefreshing.put(ljUser.journalname, false);
					app.clearNotification(LJPro.SYNC_ID);
				}
				Intent loginupdated=new Intent(LJ_FPDONEUPDATING);
				loginupdated.putExtra(LJDB.KEY_JOURNALNAME, ljUser.journalname);
				sendStickyBroadcast(loginupdated);
				cUser.close();
				
				
			}
			
			
		}

		private ContentValues[] processFriendsPage(HashMap<String, Object> result) {
			
			Object [] entries=(Object []) result.get("entries");
			ContentValues[] posts=new ContentValues[entries.length];
			String elipsis=" ...";
			String empty="";
			int numEntries=entries.length;
			for (int i=0;i<numEntries;i++) {
				ContentValues post=new ContentValues();
				HashMap<String,Object> entry=(HashMap<String,Object>) entries[i];
				try {
				post.put("_id", UUID.randomUUID().getMostSignificantBits());
				post.put("starred",0);
				post.put("accountname",ljUser.journalname);
				post.put("ditemid",Integer.parseInt(entry.get("ditemid").toString()));
				post.put("event_raw",getStringOrUTF(entry,"event_raw"));//.replaceAll("\n","<br><br\\>"));
				/*if (post.getAsString("event_raw")!=null) {
					
	            	String nohtml=post.getAsString("event_raw").replaceAll("\\<.*?>","");
	            	String d=nohtml.length()>100?elipsis:empty;	
	            	if (nohtml.length()>0)
	            		post.put("snippet",StringEscapeUtils.unescapeHtml(nohtml.substring(0, nohtml.length()>100?100:nohtml.length())+d));
	            	else post.put("snippet","");
	            	}
	            else post.put("snippet","");*/
				if (post.getAsString("event_raw")!=null) {
					Document doc=Jsoup.parseBodyFragment(post.getAsString("event_raw"));
					Elements images=doc.getElementsByTag("img");
					if (images!=null&&images.size()!=0){
						Element parent=images.get(0).parent();
						//avoid stupid social links that use images!
						if (!(parent.hasAttr("rel")&&parent.attr("rel").equals("nofollow"))) {
						post.put("teaser", images.get(0).attr("src"));
						}
					}
					
				}
	            
				post.put("journalname",(String) entry.get("journalname"));
				post.put("journaltype",(String) entry.get("journaltype"));
				post.put("journalurl",(String) entry.get("journalurl"));
				post.put("logtime",Integer.parseInt(entry.get("logtime").toString()));
				//DateFormat locallong=DateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.SHORT);
				//DateFormat timelong=DateFormat.getTimeInstance(DateFormat.MEDIUM);
				post.put("date",locallong.format(new Date (post.getAsLong("logtime")*1000l)));
				post.put("postername",(String) entry.get("postername"));
				post.put("postertype",(String) entry.get("postertype"));
				post.put("subject",StringEscapeUtils.unescapeHtml(getStringOrUTF(entry,"subject_raw")));

				post.put("replycount",Integer.parseInt(entry.get("reply_count").toString()));
				post.put("userpic",(String) entry.get("poster_userpic_url"));
				if (entry.containsKey("props")) {
					HashMap<String,Object> props=(HashMap<String,Object>) entry.get("props");
					if (props.containsKey("taglist")) post.put("tagstring",getStringOrUTF(props,"taglist"));
					if (props.containsKey("current_location")) post.put("location",(String) props.get("current_location"));
					if (props.containsKey("current_coords")) post.put("coords",(String) props.get("current_coords"));
					
				}
				}
				catch(Throwable e) {
					Log.e(TAG,e.getMessage(),e);
				}
				posts[i]=post;
			}
			moreItems=numEntries>0;
			skip+=numEntries;
			return posts;
			
		}
		
		private boolean moreItems=true;
		
		public String getStringOrUTF(HashMap<String,Object> item,String key) {
			String output=null;
			Object string=item.get(key);
			if(string!=null) {
			if (string instanceof java.lang.String) {
				try {output=(String) string;}
				catch(Throwable e){ 
					Log.e(TAG,e.getMessage(),e);
				}
			}
			else {
				try {output=new String((byte[]) string,"UTF8");}
				catch(Throwable e){ 
					Log.e(TAG,e.getMessage());
				}
			}
			}
			return output;
			
		}
		
		public String getStringOrUTF(Object string) {
			String output=null;
			if(string!=null) {
			if (string instanceof java.lang.String) {
				try {output=(String) string;}
				catch(Throwable e){ 
					Log.e(TAG,e.getMessage(),e);
				}
			}
			else {
				try {output=new String((byte[]) string,"UTF8");}
				catch(Throwable e){ 
					Log.e(TAG,e.getMessage(),e);
				}
			}
			}
			return output;
			
		}

		private HashMap<String, Object> getFriendsPage(LJUser ljuser, boolean b, boolean useSync) {
	    	HashMap<String,Object> result=new HashMap<String,Object>();
			
			 
			   try {
				   HashMap<String,Object> params=initAuth(ljuser);
				   int lastsync=(int)(appPrefs.getLong(ljUser.journalname + "friendspage_lastupdate",0l)/1000l);
				   
				    params.put("ver", 1);
				    if (useSync&&lastsync>0) params.put("lastsync",lastsync);
				    params.put("skip", skip);
					params.put("parseljtags",1);
					params.put("itemshow", !b?20:50);
					result=(HashMap<String,Object>) ljclient.call("LJ.XMLRPC.getfriendspage",params);
					result.put("Error",false);
					return result;
					
				}
			   catch (Throwable e) {
					
					result.put("Error", true);
					
					return result;
					
				}
		}

		private HashMap<String, Object> editFriendGroups(LJUser ljuser,HashMap<String, Integer> groupmask) {
	    	HashMap<String,Object> result=new HashMap<String,Object>();
			
			 
			   try {
				   HashMap<String,Object> params=initAuth(ljuser);
					params.put("groupmasks",groupmask);
					result=(HashMap<String,Object>) ljclient.call("LJ.XMLRPC.editfriendgroups",params);
					result.put("Error",false);
					return result;
					
				}
			   catch (Throwable e) {
					result.put("Error",true);
					String error=e.getMessage();
					if (error.contains("adding")) result.put("AddError", true);
					return result;
					
				}
		}

		private HashMap<String, Object> delGroups(LJUser ljuser,Object[] delfriends) {
	    	HashMap<String,Object> result=new HashMap<String,Object>();
			
			 
			   try {
				   HashMap<String,Object> params=initAuth(ljuser);
					params.put("delete",delfriends);
					result=(HashMap<String,Object>) ljclient.call("LJ.XMLRPC.editfriendgroups",params);
					result.put("Error",false);
					return result;
					
				}
			catch (Throwable e) {
				result.put("Error",true);
				String error=e.getMessage();
				if (error.contains("adding")) result.put("AddError", true);
				return result;
				
			}
		}

		private HashMap<String, Object> addGroup(LJUser ljuser, Intent intent) {
	    	
	   	 HashMap<String,Object> result=new HashMap<String,Object>();
		 HashMap<String,Object> set=new  HashMap<String,Object>();
		 HashMap<String,Object> groupinfo=new HashMap<String,Object>();
		 groupinfo.put("name", intent.getStringExtra("addgroup"));
		 groupinfo.put("public", intent.getIntExtra("public", 0));
		 set.put(intent.getStringExtra("addid"),groupinfo);
		 		
		 
		   try {
			   HashMap<String,Object> params=initAuth(ljuser);
				params.put("set",set);
				result=(HashMap<String,Object>) ljclient.call("LJ.XMLRPC.editfriendgroups",params);
				result.put("Error",false);
				return result;
				
			}
		catch (Throwable e) {
			result.put("Error",true);
			String error=e.getMessage();
			if (error.contains("adding")) result.put("AddError", true);
			return result;
			
		}
		}

		private HashMap<String, Object> editGroups(LJUser ljuser,String username,int groupmask) {
	    	 HashMap<String,Object> result=new HashMap<String,Object>();
	    	 HashMap<String,Object> addfriend=new HashMap<String,Object>();
	    	 addfriend.put("username",username);
	    	 addfriend.put("groupmask",groupmask);
			 Object[] addF=new Object[1];
			 addF[0]=addfriend;
			
			 
			   try {
				   HashMap<String,Object> params=initAuth(ljuser);
					params.put("add",addF);
					result=(HashMap<String,Object>) ljclient.call("LJ.XMLRPC.editfriends",params);
					result.put("Error",false);
					return result;
					
				}
			catch (Throwable e) {
				result.put("Error",true);
				String error=e.getMessage();
				if (error.contains("adding")) result.put("AddError", true);
				return result;
				
			}
		}

	

		private HashMap<String, Object> delFriend(LJUser ljuser,Object[] delfriends) {
			HashMap<String,Object> result=new HashMap<String,Object>();
			
			 
			   try {
				   HashMap<String,Object> params=initAuth(ljuser);
					params.put("delete",delfriends);
					result=(HashMap<String,Object>) ljclient.call("LJ.XMLRPC.editfriends",params);
					result.put("Error",false);
					return result;
					
				}
			catch (Throwable e) {
				result.put("Error",true);
				String error=e.getMessage();
				if (error.contains("adding")) result.put("AddError", true);
				return result;
				
			}
		}

		private HashMap<String, Object> addFriend(LJUser ljuser, String addfriend) {
			
			 HashMap<String,Object> result=new HashMap<String,Object>();
			 Object[] addF=new Object[1];
			 addF[0]=addfriend;
			
			 
			   try {
				   HashMap<String,Object> params=initAuth(ljuser);
					params.put("add",addF);
					result=(HashMap<String,Object>) ljclient.call("LJ.XMLRPC.editfriends",params);
					result.put("Error",false);
					return result;
					
				}
			catch (Throwable e) {
				result.put("Error",true);
				String error=e.getMessage();
				if (error.contains("adding")) result.put("AddError", true);
				return result;
				
			}
		}

		private boolean saveFriends(ContentValues[] friends) {
	    	Boolean success=false;
			if (friends.length>0) success=LJDBAdapter.updateFriends(friends);
			else success=true;
			if (success) {
				SharedPreferences.Editor editor = appPrefs.edit();
				Date d = new Date();
				
				editor.putLong(ljUser.journalname + "friends_lastupdate", d.getTime());
				editor.commit(); 
				
				try {
					Intent loginupdated=new Intent(LJ_FRIENDSUPDATED);
					loginupdated.putExtra(LJDB.KEY_JOURNALNAME, ljUser.journalname);
					cUser.close();
					
					sendStickyBroadcast(loginupdated);
				}
				catch(Throwable e) {
					Log.e(TAG,e.getMessage(),e);
				}
			}
			return success;
		
			
		}

		private ContentValues[] processFriends(HashMap<String, Object> result) {
			
			Object[] friendmaps=(Object[]) result.get("friends");
			ContentValues[] friends=new ContentValues[friendmaps.length];
			 long updated=System.currentTimeMillis()/1000l;
			for (int i=0;i<friendmaps.length;i++) {
				HashMap<String,Object> friendmap=(HashMap<String,Object>) friendmaps[i];
				ContentValues  newfriend=new ContentValues();
				if ((String) friendmap.get("birthday")!=null)  newfriend.put("birthday",(String) friendmap.get("birthday"));
				else  newfriend.put("birthday","");
				newfriend.put(LJDB.KEY_ACCOUNTNAME, ljUser.journalname);
				if (friendmap.get("fullname")!=null) {
				if (friendmap.get("fullname") instanceof java.lang.String) {
					try {newfriend.put("fullname",(String) friendmap.get("fullname"));}
					catch(Throwable e){ 
						Log.e(TAG,e.getMessage(),e);
					}
				}
				else {
					try {newfriend.put("fullname",new String((byte[]) friendmap.get("fullname"),"UTF8"));}
					catch(Throwable e){ 
						Log.e(TAG,e.getMessage(),e);
					}
				}
				}
				else  newfriend.put("fullname","");
					
				newfriend.put("userpic",(String) friendmap.get("defaultpicurl"));
				newfriend.put("username",(String) friendmap.get("username"));
				newfriend.put("updated",updated);
				String groupmask=null;
				try { groupmask=friendmap.get("groupmask").toString();}
				catch(Throwable e) {
					e.getMessage();
				}
				if (groupmask!=null) {
					newfriend.put("groupmask",Integer.parseInt(groupmask));
					/*String groups=Integer.toBinaryString(newfriend.getAsInteger("groupmask"));
					groups=(new StringBuffer(groups).reverse()).toString();
					char[] groupbin=new char[32];
					groupbin[0]='1';
					char[] fgroups=groups.toCharArray();
					int numGroups=fgroups.length;
					if (fgroups.length!=1 ) {
						for (int j=1;j<32;j++) {
							
							if(j<numGroups&&fgroups[j]=='1') {
								groupbin[j]='1';
							}
							else groupbin[j]='0';
					
						}
					
					}
					newfriend.put("groups",new String(groupbin));*/
				}
				else {
					newfriend.put("groupmask",1);
					//newfriend.put("groups","1000000000000000000000000000000");
				}
				if (friendmap.get("type")!=null) newfriend.put("type",friendmap.get("type").toString());
				else newfriend.put("type","P");
				
				
				
				friends[i]=newfriend;
				
			}
			return friends;
			
			
			
		}

		private HashMap<String,Object> getFriends(LJUser ljuser) {
	 	   HashMap<String,Object> result=new HashMap<String,Object>();
		   try {
			   HashMap<String,Object> params=initAuth(ljuser);
				params.put("includegroups",1);
				params.put("includebdays",1);
				result=(HashMap<String,Object>) ljclient.call("LJ.XMLRPC.getfriends",params);
				result.put("Error",false);
				return result;
				
			}
		catch (Throwable e) {
			result.put("Error",true);
			String error=e.getMessage();
			if (error.contains("password")) result.put("Wrong Password", true);
			else if (error.contains("username")) result.put("Wrong Username", true);
			return result;
			
		}
			
			
		}

		private void getAuthInfo() {
	    	
			
			try { cUser=LJDBAdapter.getAccount(ljUser.journalname,null);}
			catch(Throwable e) {
				Log.e(TAG,e.getMessage(),e);
				}
			
			int authInd=cUser.getColumnIndex(LJDB.KEY_AUTHINFO);
			int addInd=cUser.getColumnIndex(LJDB.KEY_ACCOUNTADDED);
			int ljs=cUser.getColumnIndex(LJDB.KEY_LJSESSION);
			int ljms=cUser.getColumnIndex(LJDB.KEY_MASTERSESSION);
			int ljlgd=cUser.getColumnIndex(LJDB.KEY_LOGGEDIN);
			int exp=cUser.getColumnIndex(LJDB.KEY_EXPIRATION);
			try {
				LJTypes.AuthInfo authInfo=serializer.readValue(cUser.getString(authInd), LJTypes.AuthInfo.class);
				ljUser.authInfo=authInfo;
				ljUser.accountadded=cUser.getLong(addInd);
				ljUser.ljsession=cUser.getString(ljs);
				ljUser.ljmastersession=cUser.getString(ljms);
				ljUser.expiration=cUser.getLong(exp);
				ljUser.ljloggedin=cUser.getString(ljlgd);
				
			} catch (JsonParseException e) {
				
				Log.e(TAG,"AuthInfo parse error: "+e.getMessage());
			} catch (JsonMappingException e) {
				Log.e(TAG,"AuthInfo mapping error: "+e.getMessage());
			
			} catch (IOException e) {
				Log.e(TAG,"AuthInfo IOException: "+e.getMessage());
				
			}
	    }
	    
	    
	 
	   
	   private void login(LJUser ljuser,boolean newAcct) {
		   HashMap<String,Object> result;
		  
		   if (newAcct)
		   { result=doLogin(ljuser,0);}
		   else 
		   {
			  int numMoods=getMoodCount();
			   result=doLogin(ljuser,numMoods);  
		   }
			String error=((Object) result.get("Error")).toString();
			
			if (!Boolean.valueOf(error) &&  !result.containsKey("faultString")) {
				boolean success=false;
			
				try {
					success=processLogin(result);
				}
				catch(Throwable e) {
					Log.e(TAG,e.getMessage(),e);
					handleError("error");
					
				}
				if (success) {
					if (newAcct) {
					
						saveAccount();
					}
					else {
						updateAccount(true);
					}
				}
				else handleError("login");
				
				
			}
			else {
				handleError(error,result);
				
			}
			
		}
	  
	   private void handleError(String error) {
		   
		   Intent xmlerror=new Intent(LJ_XMLERROR);
		   xmlerror.putExtra("errortype", error);
		   if (cUser!=null) {
			cUser.close();
			
		   }
			sendStickyBroadcast(xmlerror);
			
			if (app.fprefreshing!=null&&app.fprefreshing.get(ljUser.journalname)!=null) {
				if (app.fprefreshing.get(ljUser.journalname));
				app.fprefreshing.put(ljUser.journalname, false);
				app.clearNotification(LJPro.SYNC_ID);
			}
		
	}

	private void handleError(String error,HashMap<String,Object> result) {
		   if (Boolean.valueOf(error)) {
				if (result.containsKey("Wrong Password")|result.containsKey("Wrong Username")) 
				{
					Intent wronglogin=new Intent(LJ_WRONGLOGIN);
					//cUser.close();
					
				
					sendStickyBroadcast(wronglogin);
				}
				else {
				Intent xmlerror=new Intent(LJ_XMLERROR);
				if (cUser!=null){
					cUser.close();
				}
				
				sendStickyBroadcast(xmlerror);
				}
				if (app.fprefreshing!=null) {
				if (app.fprefreshing.get(ljUser.journalname)!=null) {
					app.fprefreshing.put(ljUser.journalname, false);
					app.clearNotification(LJPro.SYNC_ID);
				}
				}
				
			
			}
		   
	   }

	private void updateAccount(boolean broadcast) {
		
		
		
		
		Boolean success=LJDBAdapter.updateAccountBasic(ljUser.toJSON(),ljUser.journalname);
		if (success) {
			try {
				appPrefs=PreferenceManager.getDefaultSharedPreferences(this);
				SharedPreferences.Editor editor = appPrefs.edit();
				Date d=new Date();
				editor.putLong(ljUser.journalname+"login_lastupdate",d.getTime());
				editor.commit(); 
				if (broadcast) {
					Intent loginupdated=new Intent(LJ_LOGINUPDATED);
					loginupdated.putExtra(LJDB.KEY_JOURNALNAME, ljUser.journalname);
					sendStickyBroadcast(loginupdated);
				}
				
				
				
			}
			catch(Throwable e) {
				Log.e(TAG,e.getMessage(),e);
			}
		}
		
		
	}

	private String getChallenge()  {
		   Boolean success=false;
		  try {
			  HashMap<String,Object> response=(HashMap<String,Object>) ljclient.call("LJ.XMLRPC.getchallenge");
			return (String) response.get("challenge");
		} catch (XMLRPCException e) {
			
			Log.e("LJNet.getChallenge()",e.getMessage(),e);
			return "error";
			
		}   
	   }
	   
	   private HashMap<String,Object> initAuth(LJTypes.LJUser ljuser) throws IOException 
	   {   
		   	HashMap<String,Object> params = new HashMap<String, Object>();
			params.put("username", ljuser.journalname);
			params.put("auth_method", "challenge");
			//String challenge=getChallenge();
			params.put("auth_challenge",getChallenge());
			String challenge=(String) params.get("auth_challenge");
			if (challenge.equals("error")) {
				throw new IOException();
			}
			String response=Md5.MD5(challenge+ljuser.authInfo.passMD5);
			params.put("auth_response",response);
			params.put("ver",1);
			return params;
	   
	   }
	   
	   private HashMap<String,Object> doLogin(LJTypes.LJUser ljuser,int havemoods) {
		   
		   HashMap<String,Object> result=new HashMap<String,Object>();
		   try {
			   HashMap<String,Object> params=initAuth(ljuser);
				params.put("getmoods",havemoods);
				params.put("getpickws","true");
				params.put("getpickwurls","true");
				result=(HashMap<String,Object>) ljclient.call("LJ.XMLRPC.login",params);
				result.put("Error",false);
				return result;
				
			}
		catch (Throwable e) {
			result.put("Error",true);
			String error=e.getMessage();
			if (error==null) error="Network IO";
			if (error.contains("password")) result.put("Wrong Password", true);
			else if (error.contains("username")) result.put("Wrong Username", true);
			handleError(error,result);
			return result;
			
		}
		   	
		   
	   }
	   
	   private boolean processLogin(HashMap<String,Object> result){
		   if (cUser!=null) cUser.close();
			   ljUser.defaultuserpic=(String) result.get("defaultpicurl");
			   Object[] journals=(Object[]) result.get("usejournals");
			   long updatetime=System.currentTimeMillis()/1000l;
			   ContentValues[] usejournals=new ContentValues[journals.length+1];
			   ContentValues usejournal=new ContentValues(3);
			   usejournal=new ContentValues(3);
				  usejournal.put(LJDB.KEY_ACCOUNTNAME, ljUser.journalname);
				  usejournal.put(LJDB.KEY_JOURNALNAME,ljUser.journalname);
				  usejournal.put(LJDB.KEY_UPDATED, updatetime);
				  usejournals[0]=usejournal;
			   for(int i=0;i<journals.length;i++){
				  usejournal=new ContentValues(3);
				  usejournal.put(LJDB.KEY_ACCOUNTNAME, ljUser.journalname);
				  usejournal.put(LJDB.KEY_JOURNALNAME,journals[i].toString());
				  usejournal.put(LJDB.KEY_UPDATED, updatetime);
				usejournals[i+1]=usejournal;
			   }
			   if (usejournals.length>0) LJDBAdapter.updateUseJournals(usejournals);
			   Object[] moods=(Object[]) result.get("moods");
			   Boolean success=true;
			   if (moods.length>0) success=success&processMoods(moods);
			   success=success&processPics((Object[]) result.get("pickwurls"),(Object[]) result.get("pickws"));
			   success=success&processFriendGroups((Object []) result.get("friendgroups"));
			   return success;
		   
		   		
		   
	   }
	   
	   
	   private  boolean processMoods(Object[] moods) {
		
			   ContentValues[] newmoods=new ContentValues[moods.length];
			   for (int i = 0;i<moods.length;i++) {
			      HashMap<String, Object> moodmap= (HashMap<String,Object>) moods[i];
			      ContentValues c=new ContentValues(3);
			      	c.put(LJDB.KEY_ACCOUNTNAME, ljUser.journalname);
					c.put(LJDB.KEY_NAME,getStringOrUTF(moodmap.get("name")));
					c.put(LJDB.KEY_ID,Integer.parseInt(moodmap.get("id").toString()));
					newmoods[i]=c;
			   }
			
			   return LJDBAdapter.updateMoods(newmoods);
		   
	   }
	   
	   private int getMoodCount() {
		   Cursor moods=LJDBAdapter.getMoods(ljUser.journalname,null);
		   int numMoods=moods.getCount();
		   moods.close();
		   return numMoods;
		
	   }
	   private boolean processPics(Object[] pickwurls, Object[] pickws){
		  long updated=System.currentTimeMillis()/1000l;
		  ContentValues[] userpics=new ContentValues[pickwurls.length];
		  for (int i=0;i<pickwurls.length;i++){
			  ContentValues userpic=new ContentValues(4);
			  userpic.put("accountname", ljUser.journalname);
			  userpic.put("name",getStringOrUTF(pickws[i]));
			  userpic.put("updated",updated);
			  userpic.put("url",(String) pickwurls[i]);
			  userpics[i]=userpic;
		   }
		  if (userpics.length>0) return LJDBAdapter.updateUserPics(userpics);
		  else return true;
		   
	   }
	   
	   private  boolean processFriendGroups(Object[] friendgroups) {
		   
		   long updated=System.currentTimeMillis()/1000l;
		  
		   ContentValues[] groups=new ContentValues[friendgroups.length];
			   for (int i = 0;i<friendgroups.length;i++) {
			      HashMap<String, Object> fgmap= (HashMap<String,Object>) friendgroups[i];
			      
			      try {
			    	  
			    	  	ContentValues c=new ContentValues(6);
			    	  	c.put(LJDB.KEY_ACCOUNTNAME, ljUser.journalname);
						c.put(LJDB.KEY_ID,Integer.parseInt(fgmap.get("id").toString()));
						c.put(LJDB.KEY_UPDATED, updated);
						c.put(LJDB.KEY_NAME,getStringOrUTF(fgmap.get("name")));
						c.put(LJDB.KEY_PUBLIC,Integer.parseInt(fgmap.get("public").toString()));
						c.put(LJDB.KEY_VISIBLE,Integer.parseInt(fgmap.get("public").toString()));
						c.put(LJDB.KEY_SORTORDER,Integer.parseInt(fgmap.get("sortorder").toString()));
			    	  
			    
					groups[i]=c;
			      }
			      catch (Throwable e) {
			    	  Log.e(TAG,e.getMessage(),e);
			      }
			   }
			  
			   if (groups.length>0) return LJDBAdapter.updateFriendGroups(groups);
			   else return true;
			  
	   }
	   
	   private void saveAccount() {
		 
		   ContentValues ljuser=ljUser.contentValues();
		   if (LJDBAdapter.insertAccount(ljuser)) {
			   appPrefs=PreferenceManager.getDefaultSharedPreferences(this);
			   SharedPreferences.Editor editor = appPrefs.edit();
				Date d=new Date();
				editor.putLong(ljUser.journalname+"login_lastupdate",d.getTime());
				editor.commit(); 
			
			   Intent accountadded=new Intent(LJ_ACCOUNTADDED);
			   accountadded.putExtra(LJDB.KEY_JOURNALNAME, ljUser.journalname);
			   if (cUser!=null) cUser.close();
			   
			   sendStickyBroadcast(accountadded);
		   }
	   }
		   
	   
	   
	
}
