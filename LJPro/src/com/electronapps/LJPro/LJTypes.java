package com.electronapps.LJPro;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import android.content.ContentValues;
import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.util.Log;


public class LJTypes{
	
	


	
	static public class Mood{
		@JsonProperty String label;
		@JsonProperty int id;
	
	}
	
	
	static public class UserPic {
		@JsonProperty String url;
		@JsonProperty long updated;
		@JsonProperty String name;
	
	}
	
	static public class UseJournal {
		@JsonProperty String journalname;
		@JsonProperty long updated;
	
		
	}
	

	
	static public class AuthInfo {
		public String passMD5;
		public String hash;
		public String pcrypt;
	}
	
	
	static public class LJUser{
		public String journalname;
		public AuthInfo authInfo;
		public long accountadded;
		public final String TAG="LJUser";
		public String defaultuserpic;
		public String ljsession=null;
		public String ljmastersession=null;
		public String ljloggedin=null;
		public long expiration=-1;
		
		public LJUserJSON toJSON() {
				ObjectMapper serializer=new ObjectMapper();
				LJUserJSON ljuser=new LJUserJSON();
				try {
					ljuser.defaultuserpic=this.defaultuserpic;
					ljuser.accountadded=this.accountadded;
					ljuser.journalname=this.journalname;
					ljuser.authInfo=serializer.writeValueAsString(this.authInfo);

				} catch (JsonGenerationException e) {
					// TODO Auto-generated catch block
					Log.e(TAG,e.getMessage());
				} catch (JsonMappingException e) {
					Log.e(TAG,e.getMessage());
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.e(TAG,e.getMessage());
				}

				
				return ljuser;
			
			
			
		}
		public LJUser() {
			this.authInfo=new AuthInfo();
		}
		public ContentValues contentValues() {
			LJUserJSON ljuser=this.toJSON();
			ContentValues account=new ContentValues();
			account.put("accountname",ljuser.journalname);
			account.put("defaultuserpic", ljuser.defaultuserpic);
			account.put("authinfo", ljuser.authInfo);
			account.put("accountadded", ljuser.accountadded);
			// TODO Auto-generated method stub
			return account;
		}
	}
	
	static public class LJUserJSON {
		public String journalname;
		public String authInfo;
		public long accountadded;
		public String defaultuserpic;
	
		
	}
	
	static public class FriendGroup {
		@JsonProperty int id;
		@JsonProperty String  name;
		@JsonProperty int visible;
		@JsonProperty long updated;
		@JsonProperty int sortorder;
		
	}
	
	static public class Friend {
		@JsonProperty String userpic;
		@JsonProperty String birthday;
		@JsonProperty String username;
		@JsonProperty String fullname;
		@JsonProperty Integer groupMask;
		@JsonProperty String groups;
		@JsonProperty String type;
		@JsonProperty long updated;
	
		
		
		
		
	}
	
	static public class Post {
		@JsonProperty Integer logtime;
		@JsonProperty String date;
		@JsonProperty Integer ditemid;
		@JsonProperty String  event_raw;
		@JsonProperty String subject;
		@JsonProperty String journalname;
		@JsonProperty String journaltype;
		@JsonProperty String journalurl;
		@JsonProperty String userpic;
		@JsonProperty String postername;
		@JsonProperty String postertype;
		@JsonProperty String coords;
		@JsonProperty String location;
		@JsonProperty String tagstring;
		@JsonProperty Integer replycount;
		@JsonProperty String snippet;

	
	}
	
	
	
	static public String createHash(){
		
		int len=12;
		String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
		
		String randomString = "";
		for (int i=0; i < len; i++) 
		{
			int rnum = (int) Math.floor(Math.random() * chars.length());
			randomString += chars.substring(rnum,rnum+1);
		}
		
		return randomString;
		
	}
	
	static public String createKey(String hash,long date_added){
		
		return Long.toString(date_added)+hash + "--key";
		
	}
}