package com.electronapps.LJPro;

import java.io.File;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;

import com.commonsware.cwac.bus.AbstractBus.Receiver;
import com.commonsware.cwac.cache.AsyncCache;
import com.commonsware.cwac.cache.SimpleWebImageCache;
import com.commonsware.cwac.thumbnail.ThumbnailBus;
import com.commonsware.cwac.thumbnail.ThumbnailMessage;
import com.electronapps.LJPro.PostViewBus;
import com.electronapps.LJPro.PostViewMessage;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class PostView extends TextView {

	private PostViewBus bus;
	private static final String TAG=PostView.class.getSimpleName();
	private SimpleWebImageCache<PostViewBus, PostViewMessage> imgCache;
	private final AsyncCache.DiskCachePolicy policy=new AsyncCache.DiskCachePolicy() {
		public boolean eject(File file) {
			return(System.currentTimeMillis()-file.lastModified()>1000*60*60*24*7);
		}
	};
	Context mContext;
	private int mNumImg=0;
	private int mLoadedImg=0;
	private Map<String, Editable> editableCache;
	Html.ImageGetter imageGetter = null;
	Object mLock=new Object();
	 private static UriMatcher sUriMatcher;
	 private static int EMBED_YOUTUBE=1;
	 private static int IFRAME_YOUTUBE=2;
	 private static final Pattern nakedLinks=Pattern.compile(
		        "((?<!=)(?:(http|https|Http|Https|rtsp|Rtsp):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)"
		        + "\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_"
		        + "\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?"
		        + "((?:(?:[" + Patterns.GOOD_IRI_CHAR + "][" + Patterns.GOOD_IRI_CHAR + "\\-]{0,64}\\.)+"   // named host
		        + Patterns.TOP_LEVEL_DOMAIN_STR_FOR_WEB_URL
		        + "|(?:(?:25[0-5]|2[0-4]" // or ip address
		        + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(?:25[0-5]|2[0-4][0-9]"
		        + "|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1]"
		        + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
		        + "|[1-9][0-9]|[0-9])))"
		        + "(?:\\:\\d{1,5})?)" // plus option port number
		        + "(\\/(?:(?:[" + Patterns.GOOD_IRI_CHAR + "\\;\\/\\?\\:\\@\\&\\=\\#\\~"  // plus option query params
		        + "\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?"
		        + "(?:\\b|$)");
	 static {
	        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	        //Old Style
	        sUriMatcher.addURI("www.youtube.com", "v/*", EMBED_YOUTUBE);
	        // New iframe style "http://www.youtube.com/embed/VIDEO_ID"
	        sUriMatcher.addURI("www.youtube.com","embed/*", IFRAME_YOUTUBE);
	    }

	Html.TagHandler tagHandler = new Html.TagHandler() {

		public void handleTag(boolean opening, String tag,Attributes attributes, Editable output, XMLReader xmlReader) {
			if (opening) {
				if (tag.equalsIgnoreCase("img")) {
					mNumImg++;
					handleImg(attributes, output);
				}
				if (tag.equalsIgnoreCase("iframe")|tag.equalsIgnoreCase("embed")); {
					handleEmbed(attributes,output);
				}
			}
		}
	};
	


	public PostView(Context context) {
		super(context);
		mContext=context;
		setMovementMethod(LinkMovementMethod.getInstance());
		setLinksClickable(true);
		setupImgCache();
		loadDrawables();

	}

	public PostView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext=context;
		setMovementMethod(LinkMovementMethod.getInstance());
		//0xffA1F598 98DAF5
		setLinkTextColor(0xffA1F598);
		setLinksClickable(true);
		setupImgCache();
		loadDrawables();

	}

	public PostView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext=context;
		setMovementMethod(LinkMovementMethod.getInstance());
		setLinkTextColor(0xffA1F598);
		setLinksClickable(true);
		setupImgCache();
		loadDrawables();

	}

	private void setupImgCache() {
		bus = new PostViewBus();
		
		File cacheDir=((LJPro)mContext.getApplicationContext()).getCacheDir();
		imgCache = new SimpleWebImageCache<PostViewBus, PostViewMessage>(mContext.getCacheDir(), policy, 300, bus);
		imgCache.getBus().register(toString(), onCache);

	}

	



	public void setHTML(String html){
		
	
			AsyncTask<String,Void,Editable> parseTask=new parseHTML();
			parseTask.execute(html);
	
				
			
		
		
		
	
	}
	
	private class parseHTML extends AsyncTask<String,Void,Editable> {

		@Override
		protected Editable doInBackground(String... params) {
			String html=params[0];// TODO Auto-generated method stub
			Editable output=(Editable) Html.fromHtml(html,imageGetter,tagHandler);
			Linkify.addLinks(output, nakedLinks, "");
			return output;
		}
		@Override
		protected void onPostExecute(Editable output) {
			synchronized(mLock) {
				setText(output,TextView.BufferType.EDITABLE);
			}
			
		}
		
	}
	
	
	@Override public void setText(CharSequence text, BufferType type) {
		super.setText(text, type);
	}
	

    private void handleEmbed(Attributes attributes, Editable output) {
        String src = attributes.getValue("src");
        String type = attributes.getValue("type");
        boolean allowFullScreen = Boolean.parseBoolean(attributes.getValue("allowfullscreen"));

        Uri uri = null;
        int match = UriMatcher.NO_MATCH;
        if (src != null) {
            uri = Uri.parse(src);
            match = sUriMatcher.match(uri);
        }

        Intent[] intents = null;
        String snapshotUrl = null;
        Drawable drawable;
        LayerDrawable frame = null;
        if (match == EMBED_YOUTUBE|match==IFRAME_YOUTUBE) {
            String videoId = getYouTubeVideoId(uri);
            drawable = frame = createVideoDrawable(mDrawableYouTubeLogo);
            snapshotUrl = getYouTubeSnapshotUrl(videoId);
            intents = new Intent[] {
                    // Try opening with YouTube application
                    new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId)),
                    // Fallback to opening website
                    new Intent(Intent.ACTION_VIEW, Uri.parse("www.youtube.com/watch?v=" + videoId))
            };
        }
      
        
        else {
            if ("application/x-shockwave-flash".equals(type) && allowFullScreen) {
                // The embed looks like a generic Flash video
                Drawable logo = null;
                drawable = createVideoDrawable(logo);
            } else {
                // The embed was not recognized
                drawable = mDrawableMissingEmbed;
            }
            if (src != null) {
                if (type != null) {
                    intents = new Intent[] {
                            // Try opening with URL and type (use application)
                            new Intent(Intent.ACTION_VIEW, Uri.parse(src)).setType(type),
                            // Fallback to opening with URL (use browser)
                            new Intent(Intent.ACTION_VIEW, Uri.parse(src))
                    };
                } else {
                    intents = new Intent[] {
                        // Try opening source URL directly
                        new Intent(Intent.ACTION_VIEW, Uri.parse(src))
                    };
                }
            }
        }
        int start = output.length();
        output.append("\uFFFC");
        int end = output.length();
        output.setSpan(new ImageSpan(drawable, src), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (intents != null) {
            output.setSpan(new IntentsSpan(intents), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (frame != null && snapshotUrl != null) {
            int layerId = android.R.id.background;
            
            handleSnapshot(snapshotUrl,frame,layerId,output);
           
        }
    }


	private static String getYouTubeSnapshotUrl(String videoId) {
        if (videoId == null) {
            throw new NullPointerException();
        }
        // Returns a 480x360 snapshot of the video
        return "http://img.youtube.com/vi/" + videoId + "/0.jpg";
    }
    
    private static String getYouTubeVideoId(Uri uri) {
        assert uri != null;
        String id=null;
        if (sUriMatcher.match(uri) == EMBED_YOUTUBE) {
         id = uri.getPathSegments().get(1);
        int index = id.indexOf('&');
        if (index != -1) {
            id = id.substring(0, index);
        }
        }
        else if (sUriMatcher.match(uri)==IFRAME_YOUTUBE) {
        	id=uri.getPathSegments().get(1);
        }
        
        return id;
    }
    
    private static LayerDrawable createLayerDrawable(Drawable... layers) {
        return new LayerDrawable(layers);
    }

    private Drawable mDrawableVideoBackground;

    private Drawable mDrawableMissingEmbed;
    private Drawable mDrawableVideoPlay;

    private Drawable mDrawableYouTubeLogo;
	private Bitmap mImagePlaceholder;
	private Bitmap mVideoPlaceholder;
    
    private LayerDrawable createVideoDrawable(Drawable logo) {
        // Note: It is important that the LayerDrawable is not inflated from a
        // resource because Drawable#mutate() does not make it safe to swap
        // layers.
        LayerDrawable drawable = (logo != null) ? createLayerDrawable(mDrawableVideoBackground,
                mDrawableVideoPlay, logo) : createLayerDrawable(mDrawableVideoBackground,
                mDrawableVideoPlay);

        int backgroundIndex = 0;
        drawable.setId(backgroundIndex, android.R.id.background);

        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        drawable.setBounds(0, 0, w, h);

        return drawable;
    }
	
    private void loadDrawables() {
    	
    	final Resources res=mContext.getResources();
		mImagePlaceholder=BitmapFactory.decodeResource(res,R.drawable.missing_image);
		mVideoPlaceholder=BitmapFactory.decodeResource(res,R.drawable.video_downloading);
    	mDrawableVideoBackground = res.getDrawable(R.drawable.background);
    	mDrawableVideoPlay = res.getDrawable(R.drawable.play_center);
    	mDrawableYouTubeLogo = res.getDrawable(R.drawable.logo);
    	mDrawableMissingEmbed= res.getDrawable(R.drawable.missing_embed);
    }








	private void handleImg(Attributes attributes, Editable output) {
		String src = attributes.getValue("src");


		int start = output.length();
		output.append("\uFFFC");
		int end = output.length();

		

		
		ImageSpan span = new ImageSpan(mImagePlaceholder);
		if (src != null) {
			PostViewMessage msg = imgCache.getBus ( ).createMessage ( toString( ) );
			output.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			msg.setId(getTag().toString());
			msg.setEditable(output);
			msg.setSpan(span);
			msg.setUrl (src);
			try {
				imgCache.notify ( msg.getUrl (), msg );
			} catch ( Throwable t ) {
				Log.e ( TAG, "Exception trying to fetch image", t );
			}
		}

	

	}
	
    
    private void handleSnapshot(String src, LayerDrawable frame,
			int layerId,Editable output) {
    	



		
		if (src != null) {
			PostViewMessage msg = imgCache.getBus ( ).createMessage ( toString( ) );
			
			msg.setId(getTag().toString());
			msg.setLayerId(layerId);
			msg.setFrame(frame);
			msg.setEditable(output);
			msg.setUrl (src);
			try {
				imgCache.notify ( msg.getUrl (), msg );
			} catch ( Throwable t ) {
				Log.e ( TAG, "Exception trying to fetch image", t );
			}
		}
		
	}
	
	

	
	public void setOwner(Context context) {
		mContext=context;
	}


	@Override
	public Editable getEditableText() {
		// Hide the fact that this TextView is editable from external classes
		return null;
	}
	
    private PostViewBus.Receiver<PostViewMessage> onCache=
		new PostViewBus.Receiver<PostViewMessage>() {
    	
		public void onReceive(final PostViewMessage message) {
			((Activity)mContext).runOnUiThread( new Runnable() {
			
				public void run() {
					if (getTag()!=null&&getTag().toString().equals(message.getId()));
						Drawable d=imgCache.get(message.getUrl());
						if (message.getFrame()==null) {
							int w=d.getIntrinsicWidth();
							int h=d.getIntrinsicHeight();
							if (w>getWidth()) {
								float scale=(getWidth()*1.0f)/w;
								h=Math.round(h*scale);
								w=Math.round(w*scale);
								
							}
							d.setBounds(0, 0, w, h); 
							if (d!=null) {
								replaceSpan(message,d);
							}
						}
						else {
							LayerDrawable frame=message.getFrame();
							d.setBounds(frame.getBounds());
							frame.setDrawableByLayerId(message.getLayerId(), d);
							replaceSpan(message,frame);
						}
					
				}
			});
				
			
		}

		
	}; 

	public void replaceSpan(PostViewMessage m, Drawable d) {
		ImageSpan placeholder=m.getSpan();
		final String url=m.getUrl();
		if (placeholder != null) {
			// Call super.getEditableText() because
			// this.getEditableText() always returns null.
			Editable editableText =m.getEditable();
			if (editableText != null) {
				int start = editableText.getSpanStart(placeholder);
				int end = editableText.getSpanEnd(placeholder);
				if (start != -1 && end != -1) {
					editableText.removeSpan(placeholder);

					ImageSpan span = new ImageSpan(d);
					ClickableSpan imageclick=new ClickableSpan() {

						@Override
						public void onClick(View widget) {
							Toast.makeText(mContext,url, Toast.LENGTH_LONG).show();
							
						}};
					
					editableText.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					editableText.setSpan(imageclick,start,end,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					
					//Synchronized to make sure we don't get in an inconsistent state
					synchronized(mLock) {
						setText(editableText);
					}
					
				}
			}
		}
	}
	
	class IntentsSpan extends ClickableSpan {

	    private static final String TAG = "IntentsSpan";

	    private final Intent[] mIntents;

	    public IntentsSpan(Intent... intents) {
	        if (intents == null) {
	            throw new NullPointerException();
	        }
	        if (intents.length < 1) {
	            throw new IllegalArgumentException();
	        }
	        mIntents = intents;
	    }

	    @Override
	    public void onClick(View widget) {
	        for (Intent intent : mIntents) {
	            try {
	                Context context = widget.getContext();
	                context.startActivity(intent);
	                return;
	            } catch (ActivityNotFoundException e) {
	                Log.w(TAG, "Activity not found", e);
	                continue;
	            }
	        }
	    }
	}

	public void setLoading() {
		setText("Loading...",BufferType.EDITABLE);
		
		
	}



}
