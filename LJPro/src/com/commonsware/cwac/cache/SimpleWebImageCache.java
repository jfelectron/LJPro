/***
	Copyright (c) 2008-2009 CommonsWare, LLC
	
	Licensed under the Apache License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may obtain
	a copy of the License at
		http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package com.commonsware.cwac.cache;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.widget.ImageView;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import com.commonsware.cwac.bus.AbstractBus;
import com.commonsware.cwac.task.AsyncTaskEx;

public class SimpleWebImageCache<B extends AbstractBus, M>
	extends CacheBase<String, Drawable> {
	private static final String TAG="SimpleWebImageCache";
	private B bus=null;
	private static final Rect padding=new Rect(-1,-1,-1,-1);
	private static BitmapFactory.Options ops;
	
	static public File buildCachedImagePath(File cacheRoot, String url)
		throws Exception {
		return(new File(cacheRoot, md5(url)));
	}
	
	static protected String md5(String s) throws Exception {
		MessageDigest md=MessageDigest.getInstance("MD5");
		
		md.update(s.getBytes());
		
		byte digest[]=md.digest();
		StringBuffer result=new StringBuffer();
		
		for (int i=0; i<digest.length; i++) {
			result.append(Integer.toHexString(0xFF & digest[i]));
		}
				
		return(result.toString());
	}
	
	public SimpleWebImageCache(File cacheRoot,
														 AsyncCache.DiskCachePolicy policy,
														 int maxSize,
														 B bus) {
		super(cacheRoot, policy, maxSize);
		ops=new BitmapFactory.Options();
		ops.inTempStorage = new byte[16*1024];
		this.bus=bus;
	}
	
	@Override
	public int getStatus(String key) {
		int result=super.getStatus(key);
		//int result=CACHE_NONE;
		if (result==CACHE_NONE && getCacheRoot()!=null) {
			try {
				File cache=buildCachedImagePath(key);
				
				if (cache.exists()) {
					result=CACHE_DISK;
				}
			}
			catch (Throwable t) {
				Log.e(TAG, "Exception getting cache status", t);
			}
		}
		
		return(result);
	}
	
	public File buildCachedImagePath(String url)
		throws Exception {
		if (getCacheRoot()==null) {
			return(null);
		}
		
		return(buildCachedImagePath(getCacheRoot(), url));
	}
	
	public void notify(String key, M message)
		throws Exception {
 		int status=getStatus(key);
		
		if (status==CACHE_NONE) {
			new FetchImageTask().execute(message, key,
																	 buildCachedImagePath(key));
		}
		else if (status==CACHE_DISK) {
			new LoadImageTask().execute(message, key,
																	 buildCachedImagePath(key));
		}
		else {
			bus.send(message);
		}
	}
	
	public B getBus() {
		return(bus);
	}
	
	class FetchImageTask
		extends AsyncTaskEx<Object, Void, Void> {
		@Override
		protected Void doInBackground(Object... params) {
			String url=params[1].toString();
			File cache=(File)params[2];
			
			try {
				URLConnection connection=new URL(url).openConnection();
				InputStream stream=connection.getInputStream();
				BufferedInputStream in=new BufferedInputStream(stream);
				ByteArrayOutputStream out=new ByteArrayOutputStream(10240);
				int read;
				byte[] b=new byte[4096];
				
				while ((read = in.read(b)) != -1) {
						out.write(b, 0, read);
				}
				
				out.flush();
				out.close();
				M message=(M)params[0];
				
				
				
				in.close();
				byte[] raw=out.toByteArray();
				ops.inTempStorage = new byte[32*1024];
				Bitmap scaled=BitmapFactory.decodeStream(new ByteArrayInputStream(raw), padding, ops);
				//failsafe because the cache sometimes gets in an inconsistent state and above returns null
				if (scaled==null) {
					new FetchImageTask().execute(message, url,cache);
					return(null);
				}
				put(url,new BitmapDrawable(scaled));
				//put(url, new BitmapDrawable(new ByteArrayInputStream(raw)));
				
				
				
				if (message!=null) {
					bus.send(message);
				}
				
				if (cache!=null) {
					FileOutputStream file=new FileOutputStream(cache);
					
					file.write(raw);
					file.flush();
					file.close();
				}
			}
			catch (Throwable t) {
				Log.e(TAG, "Exception downloading image", t);
			}
			
			return(null);
		}
	}
	
	static class PostContainer {
		public Editable post;
		public String status;
		
	}
	
	class LoadImageTask extends AsyncTaskEx<Object, Void, Void> {
		@Override
		protected Void doInBackground(Object... params) {
			String url=params[1].toString();
			File cache=(File)params[2];
			
			
			
			
			try {
				ops.inTempStorage = new byte[32*1024];
				Bitmap scaled=BitmapFactory.decodeFile(cache.getAbsolutePath(),ops);

				put(url,new BitmapDrawable(scaled));
				//put(url, new BitmapDrawable(cache.getAbsolutePath()));
				
//				M message=(M)params[0];
				
				if (params[0]!=null) {
					bus.send(params[0]);
				}
			}
			catch (Throwable t) {
				Log.e(TAG, "Exception downloading image", t);
			}
			
			return(null);
		}
	}
}