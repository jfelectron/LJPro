package com.electronapps.LJPro;



import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotoAccountAdapter extends CursorAdapter {

	private int mLayoutResource;
	private LayoutInflater mInflater;
	private int acctInd;
	private int provInd;


	public PhotoAccountAdapter(Context context, Cursor c,int childResource) {
		super(context, c);
		mLayoutResource=childResource;
		 this.mInflater = LayoutInflater.from(context);
		 acctInd=c.getColumnIndex(LJDB.KEY_PACCOUNT);
	     provInd=c.getColumnIndex(LJDB.KEY_PROVIDER);
	}

	public PhotoAccountAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		 this.mInflater = LayoutInflater.from(context);
			
		 acctInd=c.getColumnIndex(LJDB.KEY_PACCOUNT);
	     provInd=c.getColumnIndex(LJDB.KEY_PROVIDER);
		// TODO Auto-generated constructor stub
	}

	public final static String SCRAPBOOK="ScrapBook";
	public final static String PICASA="Picasa";
	public final static String FLICKR="Flickr";
	public final static String PHOTOBUCKET="PhotoBucket";
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		  ViewHolder holder = (ViewHolder) view.getTag();
		  String provider=cursor.getString(provInd);
          holder.provider.setText(provider);
          holder.account.setText(cursor.getString(acctInd));
          if (provider.equals(SCRAPBOOK)) {
        	  holder.icon.setBackgroundResource(R.drawable.scrapbook);
        	  
          }
          else if (provider.equals(PICASA)) {
        	  holder.icon.setBackgroundResource(R.drawable.picasa);
          }
          else if (provider.equals(FLICKR)) {
        	  holder.icon.setBackgroundResource(R.drawable.flickr);
          }
          
          else if(provider.equals(PHOTOBUCKET)){
        	  holder.icon.setBackgroundResource(R.drawable.photobucket);
          }
          
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = mInflater.inflate(mLayoutResource, null);
        ViewHolder holder = new ViewHolder();
        holder.icon= (ImageView) v.findViewById(R.id.providerIcon);
        holder.provider =(TextView) v.findViewById(R.id.provider);
        holder.account = (TextView) v.findViewById(R.id.uname);
        
        v.setTag(holder);
        return v;
		
	}
	
	private static class ViewHolder {
        ImageView icon;
        TextView provider;
        TextView account;
    }

}
