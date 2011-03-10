package com.electronapps.LJPro;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class FriendGroupsAdapter extends CursorAdapter {
	
    public static final String TAG = AccountsAdapter.class.getSimpleName();
	   
    private Boolean DEBUG=true;
    private Cursor mCursor;
    private LayoutInflater mInflater;
    private int layoutResource;
    private int fgnameInd;
	private EditFriendGroups mActivity;
	
    public FriendGroupsAdapter(Context context,Cursor c, int ViewResourceId
            ) {
        super(context,c);
        this.mCursor=c;
        if (mCursor!=null) fgnameInd=mCursor.getColumnIndex(LJDB.KEY_NAME);
        this.mInflater = LayoutInflater.from(context);
        this.layoutResource = ViewResourceId;
    
        this.mActivity=(EditFriendGroups) context;
    }
 
    private static class ViewHolder {
        TextView journalname;
        CheckBox checkbox;
    }
    
	@Override
	public void bindView(View v, Context context, Cursor c) {
	       ViewHolder holder = (ViewHolder) v.getTag();
	       if (c!=null) {
	       int index=c.getPosition();
	            holder.journalname.setText(c.getString(fgnameInd));
	        	holder.checkbox.setTag(c.getPosition());
	        	holder.checkbox.setChecked(mActivity.getChecked(index));
	       }
		
	}
	
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
	    	final ViewHolder holder;
            View v = mInflater.inflate(layoutResource, null);
            holder = new ViewHolder();
            holder.journalname = (TextView) v.findViewById(R.id.groupname);
            holder.checkbox=(CheckBox) v.findViewById(R.id.delete);
            holder.checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener(){

				public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
						CheckBox checkbox=(CheckBox) buttonView;
						Integer position=(Integer) checkbox.getTag();
						mActivity.setChecked(position,isChecked);
				}}
            );
            v.setTag(holder);    
        return v;

        }
        
        
        
	

}
