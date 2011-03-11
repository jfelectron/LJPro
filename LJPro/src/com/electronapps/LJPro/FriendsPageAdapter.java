package com.electronapps.LJPro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.annotate.JsonProperty;

import com.electronapps.LJPro.LJTypes.Post;



import com.electronapps.LJPro.Html;


import android.content.Context;
import android.database.Cursor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.FilterQueryProvider;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;


public class FriendsPageAdapter extends CursorAdapter implements FilterQueryProvider{
	
    public static final String TAG = AccountsAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private Cursor mCursor;
    private FriendsPage mActivity;
    private int layoutResource;
    private int ltInd;
    private int dateInd;
    private int subjInd;
    private int jnameInd;
    private int upicInd;
    private int jtypeInd;
    private int pnameInd;
    private int coordInd;
    private int locInd;
    private int tagInd;
    private String mAccount;
    Context mContext;
    private int replyInd;
    private int snipInd;
	private int starInd;

 
    public FriendsPageAdapter(Context context, String accountname, Cursor c, int layoutId) {
        super(context, c);
       this.mCursor=c;
       mAccount=accountname;
       mContext=context.getApplicationContext();
       this.mActivity=(FriendsPage) context;
       getIndices();
   
   	 	this.mInflater = LayoutInflater.from(context);
        this.layoutResource = layoutId;
        setFilterQueryProvider(this);
   
    }
    

	

   
	






	private void getIndices() {
		 ltInd=mCursor.getColumnIndex(LJDB.KEY_LOGTIME);
		 dateInd=mCursor.getColumnIndex(LJDB.KEY_DATE);
		 subjInd=mCursor.getColumnIndex(LJDB.KEY_SUBJECT);
		 jnameInd=mCursor.getColumnIndex(LJDB.KEY_JOURNALNAME);
		 jtypeInd=mCursor.getColumnIndex(LJDB.KEY_JOURNALTYPE);
		  upicInd=mCursor.getColumnIndex(LJDB.KEY_USERPIC);
		  pnameInd=mCursor.getColumnIndex(LJDB.KEY_POSTERNAME);
		  coordInd=mCursor.getColumnIndex(LJDB.KEY_COORDS);
		  locInd=mCursor.getColumnIndex(LJDB.KEY_LOCATION);
		  starInd=mCursor.getColumnIndex(LJDB.KEY_STARRED);
		  tagInd=mCursor.getColumnIndex(LJDB.KEY_TAGS);
		  replyInd=mCursor.getColumnIndex(LJDB.KEY_REPLYCOUNT);
		  snipInd=mCursor.getColumnIndex(LJDB.KEY_SNIPPET);
		  ALL_FRIENDS=mActivity.getString(R.string.allfriends);
		 ALL_J=mActivity.getString(R.string.jonly);
		 ALL_C=mActivity.getString(R.string.conly);
		 ALL_S=mActivity.getString(R.string.sonly);
		 STARRED=mActivity.getString(R.string.starred);
		 mConstraint=ALL_FRIENDS;
		
	}

	
	private String mConstraint;











	private static class ViewHolder{
        ImageView userpic;
        ToggleButton starred;
        TextView journalname;
        TextView postername;
        TextView subject;
        TextView date;
        TextView snippet;
        TextView tags;
        TextView location;
        Button replycount;
    }







	HashMap<Integer,Boolean> starMap=new HashMap<Integer,Boolean>();


	@Override
	public void bindView(View v, Context context, Cursor c)
	{
		ViewHolder holder = (ViewHolder) v.getTag();
        holder.journalname.setText(c.getString(jnameInd));
        if (c.getString(jtypeInd).equals("C")) {
        	holder.postername.setText(c.getString(pnameInd));
        	
        }
        else {
        	holder.postername.setText("");
        }
        int pos=c.getPosition();
        holder.starred.setTag(pos);
        boolean checked=starMap.get(pos)==null?(c.getInt(starInd)==0?false:true):starMap.get(pos);
        holder.starred.setChecked(checked);
        holder.subject.setText(c.getString(subjInd)==null?"":c.getString(subjInd));
        holder.date.setText(c.getString(dateInd));
        holder.snippet.setText(c.getString(snipInd)==null?"": c.getString(snipInd));
        holder.location.setText(c.getString(locInd)==null?"":c.getString(locInd));
        holder.location.setVisibility(c.getString(locInd)==null?View.GONE:View.VISIBLE);
        holder.tags.setText(c.getString(tagInd)==null?"":Html.fromHtml("<b>Tags: </b>"+c.getString(tagInd)));
        holder.tags.setVisibility(c.getString(tagInd)==null?View.GONE:View.VISIBLE);
        holder.replycount.setText(c.getString(replyInd));
        holder.userpic.setImageResource(R.drawable.defaultuserpic);
        if (c.getString(upicInd)==null||c.getString(upicInd).length()>0) holder.userpic.setTag(c.getString(upicInd));
        else holder.userpic.setTag(null);
       
	}
	 final OnCheckedChangeListener onStarChanged=new OnCheckedChangeListener(){
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			Integer position=(Integer) buttonView.getTag();
			starMap.put(position, isChecked);
			mActivity.toggleStarred(position, isChecked);
			
		}
		
	};


	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		 View v = mInflater.inflate(layoutResource, null);
         ViewHolder holder = new ViewHolder();
         holder.starred=(ToggleButton) v.findViewById(R.id.starred);
         holder.starred.setOnCheckedChangeListener(onStarChanged);
         holder.userpic = (ImageView) v.findViewById(R.id.duserpic);
         holder.journalname = (TextView) v.findViewById(R.id.uname);
         holder.postername=(TextView) v.findViewById(R.id.postername);
         holder.date=(TextView) v.findViewById(R.id.date);
         holder.subject=(TextView) v.findViewById(R.id.subject);
         holder.snippet=(TextView) v.findViewById(R.id.snippet);
         holder.location=(TextView) v.findViewById(R.id.posterlocation);
         holder.tags=(TextView) v.findViewById(R.id.posttags);
         holder.replycount=(Button) v.findViewById(R.id.commentcount);
	
         v.setTag(holder);
         return v;
	}

	public static String ALL_FRIENDS;
	public static String ALL_J;
	public static String ALL_C;
	public static String STARRED;
	public static String ALL_S;
	
	
	
	public Cursor runQuery(CharSequence constraint) {
		if(constraint==null||constraint.equals(mConstraint)) {
			return mCursor; 
		}
		else {
			mConstraint=(String)constraint;
			String extraWhere="";
			String [] args;
			if (constraint.equals(ALL_J)|constraint.equals(ALL_C)|constraint.equals(ALL_S)) {
				extraWhere="AND "+LJDB.KEY_JOURNALTYPE+"=?";
				if (constraint.equals(ALL_J)) {
					String [] extraArgs={"P"};
					args=extraArgs;
				}
				else if (constraint.equals(ALL_C)) {
					String [] extraArgs={"C"};
					args=extraArgs;
				}
				else {
					String [] extraArgs={"Y"};
					args=extraArgs;
				}
				
			}
			else if(constraint.equals(STARRED)) {
				extraWhere="AND "+LJDB.KEY_STARRED+"=?";
				String[] extraArgs={"1"};
				args=extraArgs;
			}
			
			else if(constraint.equals(ALL_FRIENDS)) {
				extraWhere=null;
				args=null;
			}
			
			else {
				String[] extraArgs=mActivity.getGroupsHash().get(constraint);
				args=extraArgs;
			if (extraArgs!=null) {
				for (int i=0;i<extraArgs.length;i++){
					if (i==0) extraWhere+=" AND ("+LJDB.KEY_JOURNALNAME+"=?";
					else extraWhere+=" OR "+LJDB.KEY_JOURNALNAME+"=?";
				}
				extraWhere+=")";
				
				}
			}
			
			LJDB db=mActivity.getDBConn();
			mActivity.setLastQuery(extraWhere,args);
		    Cursor results=db.getFriendsPage(mAccount,extraWhere,args,null);
		    
			return results;
			
		}
	}
	
	@Override
		public void changeCursor(Cursor c) {
		super.changeCursor(c);
		starMap.clear();
	}
	

    
    

}
