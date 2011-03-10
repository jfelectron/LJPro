package com.electronapps.LJPro;


import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.TextView;



public class AccountsAdapter extends CursorAdapter  {

	    public static final String TAG = AccountsAdapter.class.getSimpleName();
	   
	    private Boolean DEBUG=true;
	    private LayoutInflater mInflater;
	    private int layoutResource;
	    private Cursor mCursor;
	    private int acctInd;
	    private int upicInd;
	    private int mRowId;
	    private  ViewHolder holder;
	    public AccountsAdapter(Context context,Cursor c,int resourceId) {
	        super(context,c);
	        this.mCursor=c;
	        acctInd=mCursor.getColumnIndex(LJDB.KEY_ACCOUNTNAME);
	        upicInd=mCursor.getColumnIndex(LJDB.KEY_DEFAULTUSERPIC);
	        this.layoutResource=resourceId;
	        this.mInflater = LayoutInflater.from(context);
	    }
	    
	 
	    private static class ViewHolder {
	        ImageView userpic;
	        TextView journalname;
	    }
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
		    holder = (ViewHolder) view.getTag();
            holder.journalname.setText(cursor.getString(acctInd));
            holder.userpic.setTag(cursor.getString(upicInd));
        
        
        
			
		}
	
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			 	View v = mInflater.inflate(layoutResource, null);
	            holder = new ViewHolder();
	            holder.userpic = (ImageView) v.findViewById(R.id.duserpic);
	            holder.journalname = (TextView) v.findViewById(R.id.uname);
	            
	            v.setTag(holder);
	            return v;
		}

		
		
}

