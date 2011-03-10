package com.electronapps.LJPro;

import java.io.IOException;
import java.util.List;


import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;



public class GetLocation {


	private Context mContext;
	private Location mBestLocation;
	private CoordsCallBack mCoordsCallBack;
	private LocationCallBack mLocCallBack;
	private LocationProvider mLow;
	private LocationManager mLocMgr;
	
	 public interface LocationCallBack {
	        public void onHaveAddress(String location);
	    }
	 
	 public interface CoordsCallBack {
		 public void onNewLocation(Location location);
	 }
	

	public GetLocation(Context c,CoordsCallBack coords, LocationCallBack callback){
		mContext=c;
		mGeocoder=new Geocoder(mContext);
		mCoordsCallBack=coords;
		mLocCallBack=callback;
	}
	/** this criteria will settle for less accuracy, high power, and cost */
	public static Criteria createCoarseCriteria() {
	 
	  Criteria c = new Criteria();
	  c.setAccuracy(Criteria.ACCURACY_COARSE);
	  c.setAltitudeRequired(false);
	  c.setBearingRequired(false);
	  c.setSpeedRequired(false);
	  c.setCostAllowed(true);
	  c.setPowerRequirement(Criteria.POWER_HIGH);
	  return c;
	 
	}
	 
	/** this criteria needs high accuracy, high power, and cost */
	public static Criteria createFineCriteria() {
	 
	  Criteria c = new Criteria();
	  c.setAccuracy(Criteria.ACCURACY_FINE);
	  c.setAltitudeRequired(false);
	  c.setBearingRequired(false);
	  c.setSpeedRequired(false);
	  c.setCostAllowed(true);
	  c.setPowerRequirement(Criteria.POWER_HIGH);
	  return c;
	 
	}
	 
	/** 
	  make sure to call this in the main thread, not a background thread
	  make sure to call locMgr.removeUpdates(...) when you are done
	*/
	public void init(){
	 
	  mLocMgr =(LocationManager) mContext.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
	 
	  Location location = null;
      List<String> providers = mLocMgr.getAllProviders();
      for (int i = 0; i < providers.size(); ++i) {
          String provider = providers.get(i);
          location = (provider != null) ? mLocMgr.getLastKnownLocation(provider) : null;
          if (location != null)
              break;
      }
      
     mBestLocation = location;
     if (location!=null) {
    	 mCoordsCallBack.onNewLocation(location);
    	 makeUseOfNewLocation(location);
     }
      
	  // get low accuracy provider
	   mLow=mLocMgr.getProvider(mLocMgr.getBestProvider(createCoarseCriteria(), false));
	 
	  // get high accuracy provider
	  LocationProvider mHigh=mLocMgr.getProvider(mLocMgr.getBestProvider(createFineCriteria(), false));
	 
	  // using low accuracy provider... to listen for updates
	  mLocMgr.requestLocationUpdates(mLow.getName(), 5000, 0f,mListener);
	 
	  // using high accuracy provider... to listen for updates
	  mLocMgr.requestLocationUpdates(mHigh.getName(), 5000, 0f,mListener);
	      
	}
	static float ACCURACY_THRESHOLD=10f;
	 LocationListener mListener=new LocationListener() {
	        public void onLocationChanged(Location location) {
	        	makeUseOfNewLocation(location);
	        	// we have a good enough fix so let's stop
	        	if (location.getAccuracy()<ACCURACY_THRESHOLD);
	        		cancel();
	        }
	       
			public void onStatusChanged(String s, int i, Bundle bundle) {
	 
	        }
	        public void onProviderEnabled(String s) {
	          // try switching to a different provider
	        }
	        public void onProviderDisabled(String s) {
	          // try switching to a different provider
	        }
	      };
	
	Geocoder mGeocoder;
	 private void makeUseOfNewLocation(Location location) {
			if (isBetterLocation(location)){
				mBestLocation=location;
				float[] latlong={(float) mBestLocation.getLatitude(),(float) mBestLocation.getLongitude()};
				ReverseGeocoderTask lookupAddress=new ReverseGeocoderTask(mGeocoder, latlong, mLocCallBack);
				lookupAddress.execute();
			}
			
		}
	
	private static final int TWO_MINUTES = 1000 * 60 * 2;

	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location) {
		final Location currentBestLocation=mBestLocation;
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}

	
	
	
	
	
	
	public class ReverseGeocoderTask extends AsyncTask<Void, Void, String> {
	    private static final String TAG = "ReverseGeocoder";

	   

	    private Geocoder mGeocoder;
	    private float mLat;
	    private float mLng;
	    private LocationCallBack mCallback;

	    public ReverseGeocoderTask(Geocoder geocoder,float[] latlng,
	            LocationCallBack callback) {
	        mGeocoder = geocoder;
	        mLat = latlng[0];
	        mLng = latlng[1];
	        mCallback = callback;
	    }

	    @Override
	    protected String doInBackground(Void... params) {
	        String value ="";
	        try {
	            List<Address> address =
	                    mGeocoder.getFromLocation(mLat, mLng, 1);
	            StringBuilder sb = new StringBuilder();
	            Address addr=address.get(0);
	                if(addr.getLocality()!=null) sb.append(addr.getLocality());
	                if(addr.getAdminArea()!=null) sb.append(","+addr.getAdminArea());
	                if(addr.getCountryCode()!=null) sb.append(","+addr.getCountryCode());
	                
	            value = sb.toString();
	        } catch (IOException ex) {
	            value = "";
	            Log.e(TAG, "Geocoder exception: ", ex);
	        } catch (RuntimeException ex) {
	            value = "";
	            Log.e(TAG, "Geocoder exception: ", ex);
	        }
	        return value;
	    }

	    @Override
	    protected void onPostExecute(String location) {
	        mCallback.onHaveAddress(location);
	    }
	}
	
	public void cancel() {
		mLocMgr.removeUpdates(mListener);
	}

}
