package com.electronapps.LJPro;




import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class FriendsAdapter extends CursorAdapter {
	
    public static final String TAG = AccountsAdapter.class.getSimpleName();
	   

    private Boolean DEBUG=true;
    private LayoutInflater mInflater;
    private int layoutResource;
    private Cursor mCursor;
    private int nameIndex;
    private int picIndex;
    private boolean[] checked;
	private EditFriends mActivity;
    public FriendsAdapter(Context context,Cursor c, int ViewResourceId)
    {
        super(context, c);
        this.mCursor=c;
        if (mCursor!=null) {
        	nameIndex=mCursor.getColumnIndex(LJDB.KEY_FRIENDNAME);
        	picIndex=mCursor.getColumnIndex(LJDB.KEY_USERPIC);
        }
        this.mInflater = LayoutInflater.from(context);
        this.layoutResource = ViewResourceId;
        mActivity=(EditFriends) context;
    }

    private static class ViewHolder {
        ImageView userpic;
        TextView journalname;
        CheckBox checkbox;
    }
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();
		if (cursor!=null) {
		holder.journalname.setText(cursor.getString(nameIndex));
		
        int position=cursor.getPosition();
        Log.e(TAG,"BindView position: "+((Integer)position).toString());
    	holder.checkbox.setTag(cursor.getPosition());
    	holder.checkbox.setChecked(mActivity.getChecked(position));
    	String picurl=cursor.getString(picIndex);
    	holder.userpic.setImageResource(R.drawable.defaultuserpic);
        if (picurl!=null&&picurl.length()>0) holder.userpic.setTag(picurl);
        else  {
        	holder.userpic.setTag(null);
        }
		}
        }
	
        
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		 		int position=cursor.getPosition();
		 		Log.e(TAG,"BindView position: "+((Integer)position).toString());
	            View v = mInflater.inflate(layoutResource, null);
	            ViewHolder holder = new ViewHolder();
	            holder.userpic = (ImageView) v.findViewById(R.id.duserpic);
	            holder.journalname = (TextView) v.findViewById(R.id.uname);
	            holder.checkbox=(CheckBox) v.findViewById(R.id.delete);
	            holder.checkbox.setTag(position);
			    holder.checkbox.setChecked(mActivity.getChecked(position));
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
