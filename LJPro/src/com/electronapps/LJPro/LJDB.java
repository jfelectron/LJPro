package com.electronapps.LJPro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


import com.electronapps.LJPro.LJTypes.Friend;
import com.electronapps.LJPro.LJTypes.FriendGroup;
import com.electronapps.LJPro.LJTypes.LJUserJSON;
import com.electronapps.LJPro.LJTypes.Mood;
import com.electronapps.LJPro.LJTypes.Post;
import com.electronapps.LJPro.LJTypes.UseJournal;
import com.electronapps.LJPro.LJTypes.UserPic;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.DatabaseUtils.InsertHelper;
import android.preference.PreferenceManager;
import android.util.Log;

public class LJDB
{


	private static final String TAG= "LJDB";


	//private static final String TAG = "DBAdapter";

	private static final String DATABASE_NAME = "ljprodb";
	private static final String ACCOUNTS_TABLE = "accounts";
	private static final String FRIENDS_TABLE = "friends";
	private static final String PHOTOACCOUNTS_TABLE ="photo_accounts";
	private static final String FRIENDGROUPS_TABLE = "friendgroups";
	private static final String FRIENDSPAGE_TABLE="friendspage";
	private static final String MOODS_TABLE="moods";
	private static final String USERPICS_TABLE="userpics";
	private static final String USEJOURNALS_TABLE="usejournals";
	private static final String TAGS_TABLE="tags";
	private static final String COMMENTS_TABLE="comments";
	
	
	

	private static final int DATABASE_VERSION = 1;





	private static final String ACCOUNTS_CREATE =
		"create table if not exists accounts ( "
		+ "_id INTEGER , authinfo text,"
		+"accountname text not null primary key,"
		+ "accountadded unisgned integer not null,"
		+" ljsession text,"
		+"ljmastersession text,"
		+"ljloggedin text,"
		+"expiration integer,"
		+"defaultuserpic text);";
	
	private static final String PHOTOACCOUNTS_CREATE =
		"create table if not exists photo_accounts ( "
		+ "_id INTEGER , "
		+"provider text,"
		+"photo_account text,"
		+"photo_accountid text,"
		+"accountname text not null," 
		+"auth_secret text,"
		+"auth_token text,PRIMARY KEY(provider,photo_account));";
		
	
	public static final String KEY_PROVIDER="provider";
	public static final String KEY_PACCOUNT="photo_account";
	public static final String KEY_PACCOUNTID="photo_accountid";
	public static final String KEY_AUTHTOKEN="auth_token";
	public static final String KEY_AUTHSECRET="auth_secret";

	
	
	public static final String KEY_JOURNALNAME = "journalname";
	public static final String KEY_AUTHINFO = "authinfo";
	public static final String KEY_ACCOUNTADDED = "accountadded";  
	public static final String KEY_DEFAULTUSERPIC="defaultuserpic";
	public static final String KEY_LJSESSION="ljsession";
	public static final String KEY_MASTERSESSION="ljmastersession";
	public static final String KEY_EXPIRATION="expiration";
	public static final String KEY_LOGGEDIN="ljloggedin";
	
	
	private static final String TAGS_CREATE=
		"create table if not exists tags ("
		+"_id INTEGER,"
		+"accountname TEXT,"
		+"name TEXT,"
		+"uses INTEGER, PRIMARY KEY(accountname,name));";
	
	public static final String KEY_USES="uses";
	
	
	






	private static final String FRIENDS_CREATE="create table if not exists friends ("
		+ "_id INTEGER ,accountname text not null,"
		+"userpic text,"
		+"birthday text,"
		+"updated integer,"
		+ "username text," 
		+ "fullname text,"
		+"groupmask integer,"
		+"groups text,"
		+"type text, PRIMARY KEY(accountname,username));";



	public static final String KEY_ACCOUNTNAME = "accountname";
	public static final String KEY_USERPIC= "userpic";
	public static final String KEY_BIRTHDAY = "birthday";
	public static final String KEY_FRIENDNAME= "username";
	public static final String KEY_FULLNAME = "fullname";  
	public static final String KEY_GROUPMASK="groupmask";
	public static final String KEY_GROUPS="groups";
	public static final String KEY_TYPE="type";



	private static final String MOODS_CREATE="CREATE TABLE if not exists moods ("
		+"_id INTEGER ,accountname TEXT NOT NULL,"
		+"name TEXT,"
		+"id INTEGER, PRIMARY KEY(accountname,name));";

	private static final String USERPICS_CREATE="CREATE TABLE if not exists userpics ("
		+"_id INTEGER PRIMARY KEY AUTOINCREMENT ,accountname TEXT NOT NULL,"
		+"name TEXT,"
		+"updated integer,"
		+"url text);";

	private static final String USEJOURNALS_CREATE="CREATE TABLE if not exists usejournals ("
		+"_id INTEGER ,accountname TEXT NOT NULL,"
		+"journalname TEXT,"
		+"updated integer,"
		+"PRIMARY KEY(accountname,journalname));";



	public static final String KEY_LABEL="label";
	public static final String KEY_VALUE="value";
	public static final String KEY_URL="url";



	private static final String FRIENDGROUPS_CREATE="CREATE TABLE if not exists friendgroups ("
		+ "_id INTEGER ,accountname text not null,"
		+"id integer,"
		+"name string,"
		+"public integer,"
		+"updated integer,"
		+ "visible integer, " 
		+ "sortorder integer," 
		+"PRIMARY KEY(accountname,name));";

	public static final String KEY_ID = "id";
	public static final String KEY_NAME= "name";
	public static final String KEY_VISIBLE = "visible";
	public static final String KEY_SORTORDER= "sortorder";
	public static final String KEY_UPDATED="updated";
	
	private static final String COMMENTS_CREATE=
		"create table if not exists comments ("
		+"_id INTEGER,"
		+"accountname TEXT,"
		+"date TEXT,"
		+"logtime INTEGER,"
		+"postername TEXT,"
		+"ditemid TEXT,"
		+"subject text,"
		+"talkid INTEGER,"
		+"parentid INTEGER,"
		+"thread INTEGER,"
		+"userpic text,"
		+"event_raw TEXT, PRIMARY KEY(accountname,ditemid,talkid));";
	
	public static final String KEY_TALKID="talkid";
	public static final String KEY_PARENTID="parentid";
	public static final String KEY_THREAD="thread";
	

	private static final String FRIENDSPAGE_CREATE ="CREATE TABLE if not exists friendspage ( "
		+ "_id INTEGER ,accountname text not null,"
		+"logtime integer,"
		+"date text,"
		+ "ditemid integer," 
		+ "event_raw text,"
		+"subject text,"
		+"starred integer,"
		+"journalname text,"
		+"journaltype text,"
		+"journalurl text,"
		+"userpic text,"
		+"postername text,"
		+"postertype text,"
		+"coords text,"
		+"location text,"
		+"tagstring text,"
		+"replycount text,"
		+"snippet text, PRIMARY KEY(accountname,journalname,ditemid));";

	public static final String KEY_LOGTIME = "logtime";
	public static final String KEY_DATE = "date";
	public static final String KEY_ITEMID = "ditemid";
	public static final String KEY_EVENTRAW = "event_raw";
	public static final String KEY_SUBJECT = "subject";  
	public static final String KEY_JOURNALTYPE="journaltype";
	public static final String KEY_JOURNALURL="journalurl";
	public static final String KEY_POSTERNAME="postername";
	public static final String KEY_POSTERTYPE="postertype";
	public static final String KEY_COORDS="coords";
	public static final String KEY_LOCATION="location";
	public static final String KEY_TAGS="tagstring";
	public static final String KEY_REPLYCOUNT="replycount";
	public static final String KEY_SNIPPET="snippet";


	private static final String FRIENDSTRIG="CREATE TRIGGER IF NOT EXISTS delete_friends AFTER DELETE ON accounts BEGIN DELETE FROM friends WHERE accountname=OLD.accountname;END;";
	private static final String GROUPSTRIG="CREATE TRIGGER IF NOT EXISTS delete_friendgroups AFTER DELETE ON accounts BEGIN DELETE FROM friendgroups WHERE accountname=OLD.accountname;END;";
	private static final String FPTRIG="CREATE TRIGGER IF NOT EXISTS delete_friendspage AFTER DELETE  ON accounts BEGIN DELETE FROM friendspage WHERE accountname=OLD.accountname;END;";
	private static final String MOODSTRIG="CREATE TRIGGER IF NOT EXISTS delete_moods AFTER DELETE  ON accounts BEGIN DELETE FROM moods WHERE accountname=OLD.accountname;END;";
	private static final String USERPICSTRIG="CREATE TRIGGER IF NOT EXISTS delete_userpics AFTER DELETE  ON accounts BEGIN DELETE FROM userpics WHERE accountname=OLD.accountname;END;";
	private static final String USEJOURNALSTRIG="CREATE TRIGGER IF NOT EXISTS delete_usejournals AFTER DELETE ON accounts BEGIN DELETE FROM usejournals WHERE accountname=OLD.accountname;END;";
	private static final String COMMENTSTRIG="CREATE TRIGGER IF NOT EXISTS delete_comments AFTER DELETE ON accounts BEGIN DELETE FROM comments WHERE accountname=OLD.accountname;END;";
	private static final String TAGSTRIG="CREATE TRIGGER IF NOT EXISTS delete_tags AFTER DELETE ON accounts BEGIN DELETE FROM tags WHERE accountname=OLD.accountname;END;";
	private static final String PACCOUNTSTRIG="CREATE TRIGGER IF NOT EXISTS delete_paccounts AFTER DELETE ON accounts BEGIN DELETE FROM photo_accounts WHERE accountname=OLD.accountname;END;";

	
	private static final String FRIENDSUTRIG="CREATE TRIGGER IF NOT EXISTS update_friends AFTER UPDATE OF accountname ON accounts BEGIN UPDATE friends SET accoountname=NEW.accountname WHERE accountname=OLD.accountname;END;";
	private static final String GROUPSUTRIG="CREATE TRIGGER IF NOT EXISTS update_friendgroups AFTER UPDATE OF accountname  ON accounts BEGIN UPDATE friendgroups SET accoountname=NEW.accountname WHERE accountname=OLD.accountname;END;";
	private static final String FPUTRIG="CREATE TRIGGER IF NOT EXISTS update_friendspage AFTER UPDATE OF accountname  ON accounts BEGIN UPDATE friendspage SET accoountname=NEW.accountname WHERE accountname=OLD.accountname;END;";
	private static final String FPPOSTTRIG="CREATE TRIGGER IF NOT EXISTS update_fpposts AFTER UPDATE OF accountname  ON accounts BEGIN UPDATE friendspage SET journalname=NEW.accountname WHERE journalname=OLD.accountname;END;";
	private static final String MOODSUTRIG="CREATE TRIGGER IF NOT EXISTS update_moods AFTER UPDATE  OF accountname ON accounts BEGIN UPDATE moods SET accoountname=NEW.accouuntname WHERE accountname=OLD.accountname;END;";
	private static final String USERPICSUTRIG="CREATE TRIGGER IF NOT EXISTS update_userpics AFTER UPDATE OF accountname  ON accounts BEGIN UPDATE userpics SET accountname=NEW.accountname WHERE accountname=OLD.accountname;END;";
	private static final String USEJOURNALSUTRIG="CREATE TRIGGER IF NOT EXISTS update_usejournals AFTER UPDATE OF accountname  ON accounts BEGIN UPDATE usejournals SET accountname=NEW.accountname WHERE accountname=OLD.accountname;END;";
	private static final String TAGSUTRIG="CREATE TRIGGER IF NOT EXISTS update_tags AFTER UPDATE OF accountname  ON accounts BEGIN UPDATE tags SET accountname=NEW.accountname WHERE accountname=OLD.accountname;END;";
	private static final String COMMENTSUTRIG="CREATE TRIGGER IF NOT EXISTS update_comments AFTER UPDATE OF accountname  ON accounts BEGIN UPDATE comments SET accountname=NEW.accountname WHERE accountname=OLD.accountname;END;";
	private static final String PACCOUNTSUTRIG="CREATE TRIGGER IF NOT EXISTS update_paccounts AFTER UPDATE OF accountname  ON accounts BEGIN UPDATE photo_accounts SET accountname=NEW.accountname WHERE accountname=OLD.accountname;END;";

	
	public static String KEY_PUBLIC="public";

	

	private LJDatabaseHelper LJDBHelper;
	private  SQLiteDatabase db;
	private Context context;
	private  InsertHelper ihac;
	private InsertHelper ihfr;
	private	InsertHelper ihfg;
	private InsertHelper ihfp;
	private InsertHelper ihm;
	private InsertHelper ihup;
	private InsertHelper ihc;
	private InsertHelper ihtg;
	private InsertHelper ihuj;
	private InsertHelper ihpac;
	private SharedPreferences appPrefs;
	private static LJDB mDB;
	
	private LJDB(Context ctx) 
	{
		this.context = ctx;
		LJDBHelper = new LJDatabaseHelper(context);
		appPrefs=PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public static synchronized LJDB getDB(Context ctx) {
		if (mDB==null) {
			mDB=new LJDB(ctx);
		}
		return mDB;
	}

	private static class LJDatabaseHelper extends SQLiteOpenHelper 
	{
		LJDatabaseHelper(Context context) 
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		

		@Override
		public void onCreate(SQLiteDatabase db) 
		{	
			db.execSQL(ACCOUNTS_CREATE);
			db.execSQL(FRIENDS_CREATE);
			db.execSQL(FRIENDGROUPS_CREATE);
			db.execSQL(FRIENDSPAGE_CREATE);
			db.execSQL(MOODS_CREATE);
			db.execSQL(USERPICS_CREATE);
			db.execSQL(USEJOURNALS_CREATE);
			db.execSQL(COMMENTS_CREATE);
			db.execSQL(TAGS_CREATE);
			db.execSQL(PHOTOACCOUNTS_CREATE);
			
			db.execSQL(FRIENDSTRIG);
			db.execSQL(GROUPSTRIG); 
			db.execSQL(FPTRIG); 
			db.execSQL(MOODSTRIG); 
			db.execSQL(USERPICSTRIG); 
			db.execSQL(USEJOURNALSTRIG); 
			db.execSQL(PACCOUNTSTRIG); 
			
			
			db.execSQL(FRIENDSUTRIG);
			db.execSQL(GROUPSUTRIG); 
			db.execSQL(FPUTRIG); 
			db.execSQL(MOODSUTRIG); 
			db.execSQL(USERPICSUTRIG); 
			db.execSQL(USEJOURNALSUTRIG); 
			db.execSQL(PACCOUNTSUTRIG); 
			

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, 
				int newVersion) 
		{
			Log.w(TAG, "Upgrading database from version " + oldVersion 
					+ " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS "+ACCOUNTS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS "+FRIENDS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS "+FRIENDGROUPS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS "+FRIENDSPAGE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS "+MOODS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS "+USERPICS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS "+USEJOURNALS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS "+COMMENTS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS "+TAGS_TABLE);
			onCreate(db);
		}
	}    

	//---opens the database---
	public LJDB open() throws SQLException 
	{
		
		if(db==null||!db.isOpen()) {
			db = LJDBHelper.getWritableDatabase();
			ihac=new InsertHelper(db,ACCOUNTS_TABLE);
			ihfr=new InsertHelper(db,FRIENDS_TABLE);
			ihfg=new InsertHelper(db,FRIENDGROUPS_TABLE);
			ihfp=new InsertHelper(db,FRIENDSPAGE_TABLE);
			ihm=new InsertHelper(db,MOODS_TABLE);
			ihup=new InsertHelper(db,USERPICS_TABLE);
			ihuj=new InsertHelper(db,USEJOURNALS_TABLE);
			ihc=new InsertHelper(db,COMMENTS_TABLE);
			ihtg=new InsertHelper(db,TAGS_TABLE);
			ihpac=new InsertHelper(db,PHOTOACCOUNTS_TABLE);
		}
		while(db.isDbLockedByCurrentThread() || db.isDbLockedByOtherThreads()){
			
		}
		return this;
	}

	
	
	public void finalize() throws Throwable {
	    if(null != LJDBHelper)
	        LJDBHelper.close();
	    if(null != db)
	        db.close();
	    super.finalize();
	}
	

	



	public boolean insertAccount(ContentValues account)
	{
		return ihac.insert(account)>0;

	}

	public boolean deleteAcct(String jname) 
	{
		String[] args=new String[1];
		args[0]=jname;
		return db.delete(ACCOUNTS_TABLE, KEY_ACCOUNTNAME+ 
				"=?", args) > 0;
	}
	private final String AccountsASCT=KEY_ACCOUNTADDED+" ASC";
	
	public Cursor getAllAccounts(String[] columns) 
	{
		return db.query(ACCOUNTS_TABLE, columns,null,null,
				null, 
				null, 
				AccountsASCT);
	}
	
	public boolean insertPhotoAccount(ContentValues account)
	{
		return ihpac.replace(account)>0;

	}

	public boolean deletePhotoAcct(String jname) 
	{
		String[] args=new String[1];
		args[0]=jname;
		return db.delete(PHOTOACCOUNTS_TABLE, KEY_ACCOUNTNAME+ 
				"=? AND "+KEY_PACCOUNT+"=?", args) > 0;
	}
	
	public Cursor getPhotoAccounts(String[] columns) 
	{
		return db.query(PHOTOACCOUNTS_TABLE, columns,null,null,
				null, 
				null, 
				null);
	}


	public Cursor getAccount(String journalname,String [] columns) 
	{
		return getRows(ACCOUNTS_TABLE,journalname,null,null,columns);
	}
	
	public boolean updatePhotoToken(String provider,String token,String journalname) {
		String[] args=new String[2];
		args[1]=journalname;
		args[2]=provider;
		args[0]=token;
		boolean success=false;
		try {
			db.execSQL("UPDATE photo_accounts SET auth_token=? WHERE accountname=? AND provider=?;", args);
			success=true;
		}
		catch(SQLiteException t) {
			Log.e(TAG,t.getMessage(),t);
		}
		return success;
	}



	public boolean updateAccountAuth(LJUserJSON ljuser,String journalname) {
		String[] args=new String[2];
		args[1]=journalname;
		args[0]=ljuser.authInfo;
		boolean success=false;
		try {
			db.execSQL("UPDATE accounts SET authinfo=? WHERE accountname=?;", args);
			success=true;
		}
		catch(SQLiteException t) {
			Log.e(TAG,t.getMessage(),t);
		}
		return success;
	}
	
	public boolean updateAccountSession(String journalname, ContentValues session) {
		String[] args={journalname};
		boolean success=false;
		try {
			db.update(ACCOUNTS_TABLE,session,KEY_ACCOUNTNAME+"=?", args);
			success=true;
		}
		catch(SQLiteException t) {
			Log.e(TAG,t.getMessage(),t);
		}
		return success;
		
	}
	
	public boolean updateReplyCount(String accountname,String journalname, Integer ditemid, ContentValues reply) {
		String[] args={accountname,journalname,ditemid.toString()};
		boolean success=false;
		try {
			db.update(FRIENDSPAGE_TABLE,reply,KEY_ACCOUNTNAME+"=? AND "+KEY_JOURNALNAME+"=? AND "+KEY_ITEMID+"=?", args);
			success=true;
		}
		catch(SQLiteException t) {
			Log.e(TAG,t.getMessage(),t);
		}
		return success;
		
	}
	
	public boolean updateAccountBasic(LJUserJSON ljuser,String journalname) {
		String[] args=new String[2];
		args[1]=journalname;
		args[0]=ljuser.defaultuserpic;
		boolean success=false;
		try {
			db.execSQL("UPDATE accounts SET defaultuserpic=? WHERE accountname=?;", args);
			success=true;
		}
		catch(SQLiteException t) {
			Log.e(TAG,t.getMessage(),t);
		}
		return success;
		
	}
	


	private final String friendsASC=KEY_FRIENDNAME+" ASC";

	public Cursor getFriends(String journalname,String[] columns) 
	{
		return getRows(FRIENDS_TABLE,journalname,null,friendsASC,columns);
	}
	
	public boolean addDummyFriend(ContentValues friend) {
		boolean success=false;
		try{ 
			success=ihfr.insert(friend)>0;
		}
		catch(Throwable e) {
			Log.e(TAG,e.getMessage(),e);
		}
		return success;
	}
	
	
	public boolean deleteFriend(ArrayList<CharSequence> del, String accountname) 
	{
		boolean success=true;
		db.beginTransaction();
		try {
		for(CharSequence fname:del) {
			String[] args=new String[2];
			args[0]=fname.toString();
			args[1]=accountname;
			success=success&db.delete(FRIENDS_TABLE, KEY_FRIENDNAME + "=? AND "+KEY_ACCOUNTNAME+"=?;", args)>0;
			//prune friendspage entries from deleted friend
			success=success&db.delete(FRIENDSPAGE_TABLE,KEY_JOURNALNAME + "=? AND "+KEY_ACCOUNTNAME+"=?;", args)>0;
		}
		db.setTransactionSuccessful();
		}
		catch(Throwable t) {
			success=false;
		}
		finally {
			db.endTransaction();
		}
;		return success;
	}
	
	public boolean deleteGroup(ArrayList<CharSequence> del, String accountname) 
	{
		boolean success=true;
		db.beginTransaction();
		try {
		for(CharSequence fname:del) {
			String[] args=new String[2];
			args[0]=fname.toString();
			args[1]=accountname;
			success=success&db.delete(ACCOUNTS_TABLE, KEY_NAME + 
				"=? AND "+KEY_ACCOUNTNAME+"=?", args)>0;
		}
		db.setTransactionSuccessful();
		}
		catch(Throwable t) {
			success=false;
		}
		finally {
			db.endTransaction();
		}
;		return success;
		
	}
	
	
	
	
	
	public boolean updateFriends(ContentValues[] friends) {
		
		boolean success=doInsertion(ihfr,friends);
		
		String[] args=new String[2];
		args[0]=friends[0].getAsString("accountname");
		args[1]=friends[0].getAsLong("updated").toString();
		String[] columns=new String[1];
		columns[0]=KEY_FRIENDNAME;
		Cursor c=db.query(true, FRIENDS_TABLE,columns, KEY_ACCOUNTNAME+"=? AND "+KEY_UPDATED+"<?", args,null, null, null,null);
		if (c.getCount()>0) {
		c.moveToFirst();
		ArrayList<CharSequence> delnames=new ArrayList<CharSequence>();
		while(!c.isAfterLast()) {
			delnames.add(c.getString(0));
			c.moveToNext();
		}
		success=success&deleteFriend(delnames,args[0]);
		}
		c.close();
		return success;
	}




	
	public boolean addDummyGroup(ContentValues group) {
		return ihfg.insert(group)>0;
	}


	public boolean updateFriendGroups(ContentValues[] groups) {
		Object[] args=new Object[2];
		args[0]=groups[0].getAsString("accountname");
		args[1]=groups[0].getAsLong("updated");
		boolean success=doInsertion(ihfg,groups);
		try {
			db.execSQL("DELETE FROM friendgroups  WHERE accountname=? AND updated<?;",args);
		}
		catch (Throwable t) {
			Log.e(TAG,t.getMessage(),t);
			success=false;
		}
		return success;
	}

	private final String fgroupsASC=KEY_NAME+" ASC";


	public Cursor getFriendGroups(String journalname,String[] columns) 
	{
		return getRows(FRIENDGROUPS_TABLE,journalname,null,fgroupsASC,columns);

	}


	
	public boolean updateMoods(ContentValues[] newmoods) {
		return doInsertion(ihm,newmoods);
	}
	
	public boolean updateComments(ContentValues[] comments) {
		return doInsertion(ihc,comments);
	}
	
	public boolean updateTags(ContentValues[] tags) {
		return doInsertion(ihtg,tags);
	}
	
		private boolean doInsertion(InsertHelper ih,ContentValues[] newrows) {
			db.beginTransaction();
			 try {
				 for (int i=0;i<newrows.length;i++) {
						ih.replace(newrows[i]);
					}
			     db.setTransactionSuccessful();
			   } 
			 catch(Throwable t) {
				 Log.e(TAG,t.getMessage(),t);
				 return false;
			 }
			 
			 	finally {
			     db.endTransaction();
			   }
			 	return true;
			 
			
		
			
		}

	private final String moodsASC=KEY_ID+" ASC";


	public Cursor getMoods(String journalname,String[] columns) 
	{	
		return getRows(MOODS_TABLE,journalname,null,moodsASC,columns);
	}
	

	
	public boolean updateUserPics(ContentValues[] userpics) {
		Object[] args=new Object[2];
		args[0]=userpics[0].getAsString("accountname");
		args[1]=userpics[0].getAsLong("updated");
		boolean success=doInsertion(ihup,userpics);
		try {
			db.execSQL("DELETE FROM userpics WHERE accountname=? AND updated<?;",args);
			
		}
		catch (Throwable t) {
			Log.e(TAG,t.getMessage(),t);
			success=false;
		}
		return success;
	}



	public Cursor getUserPics(String journalname,String[] columns) 
	{
		return getRows(USERPICS_TABLE,journalname,KEY_URL,null,columns);
	}
	
	
	

	public boolean updateUseJournals(ContentValues[] usejournals) {
		Object[] args=new Object[2];
		args[0]=usejournals[0].getAsString("accountname");
		args[1]=usejournals[0].getAsLong("updated");
		boolean success=doInsertion(ihuj,usejournals);
		
		try {
			db.execSQL("DELETE FROM usejournals WHERE accountname=? AND updated<?;",args);	
			}
		catch (Throwable t) {
			Log.e(TAG,t.getMessage(),t);
		}
		return success;
	}

	private final String usejournalASC=KEY_JOURNALNAME+" ASC";


	public Cursor getUseJournals(String journalname,String[] columns) 
	{
		
		return getRows(USEJOURNALS_TABLE,journalname,null,usejournalASC,columns);
		
	}
	
	
	public Cursor getMatchingTags(String accountname,String constraint) {
		String[] columns={"_id",KEY_NAME};
		String[] args={accountname};
		Cursor c=null;
		try {
			c=db.query(TAGS_TABLE,columns,KEY_ACCOUNTNAME+"=? AND "+KEY_NAME+" LIKE '"+constraint+"%'",args,null,null,null);
		}
		catch(Throwable t) {
			Log.e(TAG,t.getMessage(),t);
			
		}
		return c;
	}
	
	public Cursor getMatchingMoods(String accountname,String constraint) {
		String[] columns={"_id",KEY_NAME};
		String[] args={accountname};
		Cursor c=null;
		try {
			c=db.query(MOODS_TABLE,columns,KEY_ACCOUNTNAME+"=? AND "+KEY_NAME+" LIKE '"+constraint+"%'",args,null,null,null);
		}
		catch(Throwable t) {
			Log.e(TAG,t.getMessage(),t);
			
		}
		return c;
	}
	
	
	
	
	
	
	
	private Cursor getRows(String table,String journalname,String groupBy,String orderBy,String[] columns) {
		String args[] = new String[1];
		args[0]=journalname;
		Cursor mCursor=null;
		try {
			mCursor =db.query(table,columns,KEY_ACCOUNTNAME+"=?",args,groupBy,null,orderBy);
		}
		catch(Throwable e ) {
			Log.e(TAG,e.getMessage(),e);
		}
		if (mCursor != null) {
			mCursor.moveToFirst();
		}

		return mCursor;
		
	}
	
	






	

	public boolean updateFriendsPage(ContentValues[] posts) {
		


		
		String[] args={posts[0].getAsString("accountname")};
		

		boolean success=doInsertion(ihfp,posts);
		//Prune older entries
		try {
			String[] columns={"logtime"};
			String orderBy="logtime DESC";
			SharedPreferences appPrefs=PreferenceManager.getDefaultSharedPreferences(context);
			int maxCount=Integer.parseInt(appPrefs.getString(args[0]+"_cacheDuration", "300"));
			Cursor fp=db.query(FRIENDSPAGE_TABLE, columns, KEY_ACCOUNTNAME+"=?", args, null, null, orderBy);
			int maxTime;
			if (fp.getCount()>maxCount) {
				fp.moveToPosition(maxCount+1);
				maxTime=fp.getInt(0);
				Object[] delArgs={args[0],maxTime,1};
				//delete entries older than the older we want to keep except for starred entries
				db.execSQL("DELETE FROM friendspage WHERE accountname=? AND logtime<? AND starred!=?;",delArgs);
				
			}
			fp.close();
			
		}
		catch (Throwable t) {
			Log.e(TAG,t.getMessage(),t);
		}
		return success;
	}
	
	public boolean updateStarred(String accountname,Integer ditemid,String journal,Boolean starred){
		boolean success=false;
		ContentValues values=new ContentValues();
		values.put(KEY_STARRED, starred?1:0);
		String[] args={accountname,ditemid.toString(),journal};
		try{
			success=db.update(FRIENDSPAGE_TABLE, values,KEY_ACCOUNTNAME+"=? AND "+KEY_ITEMID+"=? AND "+KEY_JOURNALNAME+"=?", args)>0;
		}
		catch(Throwable e){
			Log.e(TAG,e.getMessage(),e);
		}
		
		return success;
	}
	
	
	
	
	
	
	private final String fpDESC=KEY_LOGTIME+" DESC";
	public static final String KEY_STARRED="starred";
	
	public Cursor getFriendsPage(String accountname,String extraWhere,String[] extraArgs,Integer limit) 
	{
		int extra=extraArgs==null?0:extraArgs.length;
		String args[] = new String[1+extra];
		extraWhere=extraWhere==null?"":extraWhere;
		args[0]=accountname;
		if (extra>0) {
			System.arraycopy(extraArgs, 0,args, 1, extra);
		}
		Cursor mCursor=null;
		
		String limitBy=null;
		if (limit!=null) {
			limitBy="0, "+String.valueOf(limit);		}
	
		
		try {
			mCursor =db.query(true,FRIENDSPAGE_TABLE,null,KEY_ACCOUNTNAME+"=?"+extraWhere,args,null,null,fpDESC,limitBy);
		}
		catch(Throwable e ) {
			Log.e(TAG,e.getMessage(),e);
		}
		if (mCursor != null) {
			//mCursor.moveToFirst();
		}

		return mCursor;
	}
	
	
	public Cursor getComments(String accountname,int ditemid) 
	{
		
		String args[] ={accountname,String.valueOf(ditemid)};
		Cursor mCursor=null;
	
		try {
			mCursor =db.query(COMMENTS_TABLE,null,KEY_ACCOUNTNAME+"=? AND "+KEY_ITEMID+"=?",args,null,null,KEY_THREAD+", "+KEY_PARENTID+", "+KEY_LOGTIME);
		}
		catch(Throwable e ) {
			Log.e(TAG,e.getMessage(),e);
		}
		if (mCursor != null) {
			mCursor.moveToFirst();
		}

		return mCursor;
	}
	
	
	
	public void dropTables() {
		db.execSQL("DROP TABLE IF EXISTS "+ACCOUNTS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS "+FRIENDS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS "+FRIENDGROUPS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS "+FRIENDSPAGE_TABLE);
		db.execSQL("DROP TABLE IF EXISTS "+MOODS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS "+PHOTOACCOUNTS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS "+USERPICS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS "+FRIENDSPAGE_TABLE);
		db.execSQL("DROP TABLE IF EXISTS "+USEJOURNALS_TABLE);
	}

	public void createTables() {
		db.execSQL(ACCOUNTS_CREATE);
		db.execSQL(FRIENDS_CREATE);
		db.execSQL(FRIENDGROUPS_CREATE);
		db.execSQL(FRIENDSPAGE_CREATE);
		db.execSQL(PHOTOACCOUNTS_CREATE);
		db.execSQL(MOODS_CREATE);
		db.execSQL(USERPICS_CREATE);
		db.execSQL(USEJOURNALS_CREATE);
		db.execSQL(TAGS_CREATE);
		db.execSQL(COMMENTS_CREATE);
		
	}
	
	public void createTriggers() {
		db.execSQL(FRIENDSTRIG);
		db.execSQL(GROUPSTRIG); 
		db.execSQL(FPTRIG); 
		db.execSQL(MOODSTRIG); 
		db.execSQL(USERPICSTRIG); 
		db.execSQL(USEJOURNALSTRIG); 
		db.execSQL(TAGSTRIG);
		db.execSQL(COMMENTSTRIG);
		db.execSQL(PACCOUNTSTRIG);
		
		
		db.execSQL(FRIENDSUTRIG);
		db.execSQL(GROUPSUTRIG); 
		db.execSQL(FPUTRIG); 
		db.execSQL(FPPOSTTRIG);
		db.execSQL(MOODSUTRIG); 
		db.execSQL(USERPICSUTRIG); 
		db.execSQL(USEJOURNALSUTRIG); 
		db.execSQL(TAGSUTRIG); 
		db.execSQL(COMMENTSUTRIG); 
		db.execSQL(PACCOUNTSUTRIG);
		
	}

	public Cursor getSyn(String journalname) {
		String[] args = {journalname,"Y"};
		
		Cursor mCursor=null;
		try {
			mCursor =db.query(true,FRIENDSPAGE_TABLE,null,KEY_ACCOUNTNAME+"=? AND "+KEY_JOURNALTYPE+"=?",args,null,null,null,"0,1");
		}
		catch(Throwable e ) {
			Log.e(TAG,e.getMessage(),e);
		}
		if (mCursor != null) {
			mCursor.moveToFirst();
		}

		return mCursor;
	}

	
}
