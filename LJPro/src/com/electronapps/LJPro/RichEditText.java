package com.electronapps.LJPro;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;



import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.DynamicLayout;
import android.text.Editable;
import android.text.Layout;
import android.text.ParcelableSpan;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.ParagraphStyle;
import android.text.style.QuoteSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;

public class RichEditText extends EditText {


	private Map<Integer,Boolean> mCurrentStyles=new HashMap<Integer,Boolean>();

	private Context mContext;
	private Editable mEditable;
	private Integer mNewStyle;
	
	// Following ids are used for mapping to span types
	
	//Supported "advanced" styles
	
	public final static int STYLE_UNDERLINE=3;
	public final static int STYLE_STRIKETHROUGH=4;
	public final static int STYLE_BLOCKQUOTE=5;
	public final static int STYLE_SUPERSCRIPT=6;
	public final static int STYLE_SUBSCRIPT=7;
	
	//generic text sizes supported
	
	public final static int ABSOLUTESIZE_SPAN=8;
	public final static int SIZE_XXSMALL=9;
	public final static int SIZE_XSMALL=10;
	public final static int SIZE_SMALL=11;
	public final static int SIZE_MEDIUM=12;
	public final static int SIZE_LARGE=13;
	public final static int SIZE_XLARGE=14;
	public final static int SIZE_XXLARGE=15;
	private int mTextColor=0xFF000000;
	private int mBgColor=0xFFFFFFFF;
	
	public final static int IMAGE_SPAN=16;
	public final static int ALIGNMENT_SPAN=17;
	public final static int URL_SPAN=18;
	private Paint mPaint;
	public final static int RELATIVESIZE_SPAN=19;
	public final static int HEADER_H1=20;
	public final static int HEADER_H2=21;
	public final static int HEADER_H3=22;
	public final static int HEADER_H4=23;
	public final static int HEADER_H5=24;
	public final static int HEADER_H6=25;
	
	public final static int TEXT_COLOR=26;
	public final static int BACKGROUND_COLOR=27;
	public final static int BULLET_SPAN=28;
	public final static int LJCUT_SPAN=29;

	
	private static int[] mStyles={STYLE_UNDERLINE,STYLE_STRIKETHROUGH,STYLE_BLOCKQUOTE,STYLE_SUPERSCRIPT,STYLE_SUBSCRIPT};
	private static int[] mRelativeSizes={SIZE_XXSMALL,SIZE_XSMALL,SIZE_SMALL,SIZE_MEDIUM,SIZE_LARGE,SIZE_XLARGE,SIZE_XXLARGE};
	private static int mOffset=3;
	  private static final float[] HEADER_SIZES = {
	        1.5f, 1.4f, 1.3f, 1.2f, 1.1f, 1f,
	    };
	private static int[] mHeaders={HEADER_H1,HEADER_H2,HEADER_H3,HEADER_H4,HEADER_H5,HEADER_H6}; 
	private static final int[] mOffsets={-3*mOffset,-2*mOffset,-mOffset,0,mOffset,2*mOffset,3*mOffset};
	
	
	public RichEditText(Context context) {
		super(context);
		setupEditor(context);
	}

	public RichEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupEditor(context);
	}

	public RichEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setupEditor(context);
	}
	
	
	private HashMap<Integer, Button> mButtons=new HashMap<Integer,Button>();

	private float mSize;
	Bitmap mCutIcon;

	private DynamicLayout mInternalLayout;

	private float mScale;

	private Bitmap mImagePlaceholder;

	public void setFontSizeOffset(int offset) {
		mOffset=offset;
	}
	
	
	
	private void setupEditor(Context context) {
		mContext=context;
		mScale = getContext().getResources().getDisplayMetrics().density;
		final Resources res=mContext.getResources();
		mImagePlaceholder=BitmapFactory.decodeResource(res,R.drawable.missing_image);
		mPaint=new Paint();
		//relaative sizes are computed based on current Text size
		mSize=getTextSize();
		mEditable=getEditableText();
		makeSizeHashes(mSize);
		addTextChangedListener(onType);
		mInternalLayout=(DynamicLayout)getLayout();
		
	}
	public static HashMap<Integer, Integer> SizeMap=new HashMap<Integer,Integer>();
	public static HashMap<Integer, Integer> SizeIdMap=new HashMap<Integer,Integer>();
	public static HashMap<Integer, Float> RelativeSizeMap=new HashMap<Integer,Float>();
	public static  HashMap<Float,Integer> RelativeSizeIdMap=new HashMap<Float,Integer>();

	
	
	private static void makeSizeHashes(float fSize) {
		int i=0;
		int size=Math.round(fSize);
		while (i<mRelativeSizes.length) {
			SizeMap.put(mRelativeSizes[i], size+mOffsets[i]);
			SizeIdMap.put(size+mOffsets[i], mRelativeSizes[i]);
		   i++;
		}
		
		int j=0;
		
		while(j<mHeaders.length) {
			RelativeSizeMap.put(mHeaders[j],HEADER_SIZES[j]);
			RelativeSizeIdMap.put(HEADER_SIZES[j], mHeaders[j]);
			j++;
		}
		
	
	
		
		
	}
	
	public void registerBoldButton(Button t) {
		mButtons.put(Typeface.BOLD,t);
	}
	
	
	public void registerItalicButton(Button t) {
		mButtons.put(Typeface.ITALIC,t);
	}
	
	public void registerStyleButton(Button t) {
		for (int style:mStyles) {
			mButtons.put(style, t);
		}

	}
	
	public void registerSizeButton(Button t) {
		for (int size:mRelativeSizes) {
			mButtons.put(size, t);
		}

	}
	
	
	
	public void registerUnderlineButton(Button t) {
		mButtons.put(STYLE_UNDERLINE, t);
	}
	
	public void registerStrikethroughButton(Button t) {
		mButtons.put(STYLE_STRIKETHROUGH, t);
	}
	
	public void registerQuoteButton(Button t) {
		mButtons.put(STYLE_BLOCKQUOTE, t);
	}
	
	public void registerSuperscriptButton(Button t) {
		mButtons.put(STYLE_SUPERSCRIPT, t);
	}
	
	public void registerSubscriptButton(Button t) {
		mButtons.put(STYLE_SUBSCRIPT, t);
	}
	
	
	
	public int getColor() {
		return mTextColor;
	}
	
	public void setColor(int color) {
		mTextColor=color;
		
	}
	
	public void setBackgroundColor(int color) {
		mBgColor=color;
		
	}
	
	public int getBackgroundColor() {
		return mBgColor;
	}
	
	public boolean isStyleSet(int style) {
		boolean set=mCurrentStyles.get(style)==null?false:true;
		return set;
	}
	
	public void toggleStyle(int style) {
		mNewStyle=style;
		
		if(mCurrentStyles.get(style)==null) { setStyle(style);
		}
		else removeStyle(style);
		
		setOrRemoveStyle();
	}
	private String mURL;
	
	public void setCurrentURL(String url){
		mURL=url;
	}
	@SuppressWarnings("unchecked")
	public void toggleStyle(int style, Object extra) {
		mNewStyle=style;
		switch (style) {
		case LJCUT_SPAN:
			mCutText=(String)extra;
			break;
		case IMAGE_SPAN:
			HashMap<String,Object> image=(HashMap<String,Object>) extra;
			String src=image.get("tag").toString();
			mDesiredSize=(Integer) image.get("size");
			Spanned text=Html.fromHtml(src, null, tagHandler);
			getEditableText().insert(getSelectionStart(), text);
			
			break;
		case URL_SPAN:
			mURL=(String)extra;
		}
		if(mCurrentStyles.get(style)==null) { setStyle(style);
		}
		else removeStyle(style);
		
		setOrRemoveStyle();
	}
	
	public void setStyle(int style) {
		mCurrentStyles.put(style,true);
		
	}
	
	public void removeStyle(int style) {
		mCurrentStyles.remove(style);
		
	}
	
	
	
	public Set<Integer> getCurrentStyles() {
		return mCurrentStyles.keySet();
	}
	
	private String mCutText;
	
	@SuppressWarnings("unchecked")
	private void setOrRemoveStyle(){
		final int style=mNewStyle==null?-1:mNewStyle;
		int selectionStart =getSelectionStart();
		Class type=null;
		Object what=null;
		boolean adding=isStyleSet(style);
		
		SpanInfo si;
		if (style>=SIZE_XXSMALL&&style<=SIZE_XXLARGE) {
			si=new SpanInfo(style,adding,SizeMap.get(style));
		}
		else if (style==TEXT_COLOR) {
			si=new SpanInfo(style,true,mTextColor);
		}
		
		else if (style==BACKGROUND_COLOR) {
			si=new SpanInfo(style,true,mBgColor);
		}
		
	
		else if (style==LJCUT_SPAN) {
			si=new SpanInfo(style,adding,mCutText);
		}
		else if (style==URL_SPAN) {
			si=new SpanInfo(style,adding,mURL);
		}
		else {
			si=new SpanInfo(style,adding);
		}
		
		
		
		
		
		
       
        int selectionEnd = getSelectionEnd();

        if (selectionStart > selectionEnd){
            int temp = selectionEnd;
            selectionEnd = selectionStart;
            selectionStart = temp;
        }

        
        if (selectionEnd > selectionStart)
        {
            
           Object[] ss = mEditable.getSpans(selectionStart, selectionEnd, si.type);
           
            boolean exists = false;
            boolean newColor=false;
            for (int i = 0; i < ss.length; i++) {
            	 si=SpanInfo.getSpanInfo(ss[i],mEditable);
            	if (si!=null) {
            	if (si.id==TEXT_COLOR||si.id==BACKGROUND_COLOR) newColor=isNewColor(si);
            	if ((mCurrentStyles.get(si.id)==null)||newColor)
            {
                	 mEditable.removeSpan(ss[i]);
                	if (selectionStart>si.start) mEditable.setSpan(getSpan(style), si.start, selectionStart, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                	if (selectionEnd<si.end) mEditable.setSpan(getSpan(style), selectionEnd, si.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    exists =true;
                }
            }
            }

            if (!exists|newColor)
                mEditable.setSpan(si.what, selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            

            
        
	}
	

	
	private boolean isNewColor(SpanInfo si){
		boolean newColor=false;
		if ((si.id==TEXT_COLOR&&si.color!=mTextColor)|(si.id==BACKGROUND_COLOR&&si.color!=mBgColor)) newColor=true;
		return newColor;
		
	}



	private int mOldTextEnd=0;
	private TextWatcher onType=new TextWatcher() { 
        
		public void afterTextChanged(Editable s) { 
        	
            //add style as the user types based on style of prev character
            
            int position = getSelectionStart();
            if (position < 0){
                position = 0;
            }

            
            if (position > 0){
            	
            	
         
            		int start;
            		int end;
            		int newTextEnd=s.length();
            
            		int delta=newTextEnd-mOldTextEnd;
            	
            		int firstNewChar;
            		if (delta>0) {
            	
            		firstNewChar=position-delta+1;
            		start=firstNewChar-2<0?0:firstNewChar-2;
                   	    end=firstNewChar-1<0?0:firstNewChar-1;	
            		}
            		else {
            			firstNewChar=position;
            			start=position-1<0?0:position-1;
                   	    end=position<0?0:position;	
            		}
            		
            		
            		
            		mOldTextEnd=newTextEnd;
            		boolean deleted=false;
                	Object[] ss = s.getSpans(start, end, Object.class);
                	 if (ss.length>0) {
                     for (int i = 0; i < ss.length; i++) {
                    	 boolean newColor=false;
                    	 SpanInfo si=SpanInfo.getSpanInfo(ss[i],mEditable);
                    	 if (si!=null) {
                    	Object what=getSpan(si.id);
                    	if (si.id==TEXT_COLOR||si.id==BACKGROUND_COLOR) newColor=isNewColor(si);
                         if (mCurrentStyles.get(si.id)!=null&&!newColor){
                        	 //we are extending this style, so it's not new
                        	 if (position>si.end) {
                        	mCurrentStyles.put(si.id,false);
                             s.removeSpan(ss[i]);
                             
                             s.setSpan(what, si.start, position, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        	 }
                        	 else {
                        		 mCurrentStyles.put(si.id,false);
                        		 s.removeSpan(ss[i]);
    
                                 s.setSpan(what, si.start, si.end,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                

                        	 }
                         }
                         else {
                        	
                        	if (position<si.end) {
                        		s.removeSpan(ss[i]);
                        		s.setSpan(what, si.start,firstNewChar-1<0?0:firstNewChar-1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        		s.setSpan(what,position,si.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        	}
                        	else if (position==si.end) {
                        		deleted=true;
                        	
                        		
                        	}
                         }
                         
                    	 }  
                     }
                     if (deleted) {
                    	 onSelectionChanged(position,position);
                    	 return;
                     }
                     for (Integer style:mCurrentStyles.keySet()) {
                    	 if(mCurrentStyles.get(style)) {
                    		
                    			if (delta>0) {
                    		
                    				s.setSpan(getSpan(style), firstNewChar-1,position, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    			}
                    			


                    		 
                    	 }
                    	 else {mCurrentStyles.put(style, true);
                    	 }
                    }
                     
                	 }
                	 else {
                		 for (Integer style:mCurrentStyles.keySet()){
               
                         	s.setSpan(getSpan(style), firstNewChar-1,position, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                		 
                		 }
                	 }
              
                
                

                
            }
        } 
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { 
                //unused
        } 
        public void onTextChanged(CharSequence s, int start, int before, int count) { 
                    //unused
            } 
};

	private Rect sTempRect=new Rect();
	private Object getSpan(int style) {
		Object what;
		 if (style>=SIZE_XXSMALL&&style<=SIZE_XXLARGE) {
    		 what=SpanInfo.sizeFactory(SizeMap.get(style)); 
    	 }
		 
		 else if (style==TEXT_COLOR) {
			 what=SpanInfo.colorFactory(style, mTextColor);
			 
		 }
		 else if (style==BACKGROUND_COLOR) {
			 what=SpanInfo.colorFactory(style, mBgColor);
			 
		 }
		 else if (style==LJCUT_SPAN){
			 what=SpanInfo.ljcutFactory(mCutText);
		 }
		 else if (style==URL_SPAN) {
			 what=SpanInfo.linkFactory(mURL);
		 }
    	 else {
    		 what=SpanInfo.styleFactory(style);
    	 }
		 return what;
		
	}
	
	Rect curLine=new Rect();
	Rect nextLine=new Rect();
	@Override
	protected void onDraw(Canvas c) {
		 int dtop, dbottom;
		 super.onDraw(c);
	        synchronized (sTempRect) {
	            if (!c.getClipBounds(sTempRect)) {
	                return;
	            }

	            dtop = sTempRect.top;
	            dbottom = sTempRect.bottom;
	        }

	        final Layout layout=getLayout();

	        int top = 0;
	        int bottom = layout.getLineTop(getLineCount());

	        if (dtop > top) {
	            top = dtop;
	        }
	        if (dbottom < bottom) {
	            bottom = dbottom;
	        }
	        
	        int first = layout.getLineForVertical(top); 
	        int last = layout.getLineForVertical(bottom);
	       
	        
	        int previousLineEnd = layout.getLineStart(first);
	        
	        TextPaint p =getPaint();
	        CharSequence buf = mEditable;
	        for (int i = first; i <= last; i++) {
	            int start = previousLineEnd;

	            previousLineEnd = layout.getLineStart(i+1);
	            int end = getLineVisibleEnd(i, start, previousLineEnd);

	         
	            
	        
	           
	            Spanned spanned=(Spanned) buf;
	            BlockSpan[] blocks=spanned.getSpans(start,end,BlockSpan.class);
	            for (BlockSpan block:blocks) {
	            	int spanStart=spanned.getSpanStart(block);
	            	int spanEnd=spanned.getSpanEnd(block);
	            	float x;
	            	if (spanStart>start) {
	            		start=spanStart;
	            		
	            		}
	            	if(end>spanEnd){
	            		end=spanEnd;
	            	}
	            	x=layout.getPrimaryHorizontal(start);
	            	
	            	int baseline = getLineBounds(i, curLine);
	            	block.drawBlock(c, p,x, curLine,baseline, buf, start, end,layout);
	            	//reset 
	            	   previousLineEnd = layout.getLineStart(i+1);
	   	            end = getLineVisibleEnd(i, start, previousLineEnd);
	            }
	            
	            }
	       
	        
		
	}
	
	public Object[] getSpansAtSelection(Class t) {
			int start=getSelectionStart();
			int end=getSelectionEnd();
			if (end<start) {
				int tmp=end;
				end=start;
				start=tmp;
				
			}
			Object[] spans=((Spanned)mEditable).getSpans(start, end, t);
			return spans;
	}
	        
	        private int getLineVisibleEnd(int line, int start, int end) {
	            

	            CharSequence text = mEditable;
	            char ch;
	            if (line == getLineCount() - 1) {
	                return end;
	            }

	            for (; end > start; end--) {
	                ch = text.charAt(end - 1);

	                if (ch == '\n') {
	                    return end - 1;
	                }

	                if (ch != ' ' && ch != '\t') {
	                    break;
	                }

	            }

	            return end;
	        }
	
	@Override
	protected void onSelectionChanged (int selStart, int selEnd){
		super.onSelectionChanged(selStart,selEnd);
		if (mEditable!=null) {
			int position=selEnd;
			int start=position-2<0?0:position-1;
			int end=position-1<0?0:position;
			final Editable s=mEditable;
			Object[] ss = s.getSpans(start, end, Object.class);
			
			for (int i:mButtons.keySet()){
				Button b=mButtons.get(i);
				if (b!=null) {
					if (b instanceof ToggleButton) ((ToggleButton) b).setChecked(false);
					else b.setPressed(false);
					
				}
				
			}
			mCurrentStyles.clear();
			if (ss.length>0) {
				Boolean withinImageSpan=false;
				for (int i=0;i<ss.length;i++) {
					SpanInfo si=SpanInfo.getSpanInfo(ss[i],mEditable);
					if (si!=null) {
					if (si.id==RichEditText.IMAGE_SPAN) withinImageSpan=true;
						
						mCurrentStyles.put(si.id,true);
						
					if (si.id==TEXT_COLOR) mTextColor=si.color;
					if (si.id==BACKGROUND_COLOR) mBgColor=si.color;
						
						Button b=mButtons.get(si.id);
						if (b!=null) {
							if (b instanceof ToggleButton) ((ToggleButton) b).setChecked(true);
							else b.setPressed(true);
							
						}
						
				}
				
				}
				if (withinImageSpan) {
					//we don't want extend an imagespan or a link wrapping an image
					mCurrentStyles.remove(RichEditText.IMAGE_SPAN);
					mCurrentStyles.remove(RichEditText.URL_SPAN);
					
				}
				
			}
			
			
	
				
			
		
		}
	}
	
private int mDesiredSize=-1;
	
	Html.TagHandler tagHandler = new Html.TagHandler() {

		public void handleTag(boolean opening, String tag,Attributes attributes, Editable output, XMLReader xmlReader) {
			if (opening) {
				if (tag.equalsIgnoreCase("img")) {
					handleImg(attributes, output);
				}
				
			}
		}
	};
	
	private void handleImg(Attributes attributes, Editable output) {
		String src = attributes.getValue("src");
		String h=attributes.getValue("height");
		String w=attributes.getValue("width");
		int width, height;
		if (w!=null) {
			width= Integer.parseInt(w);
		}
		else {
			width=-1;
		}
		
		if (h!=null) {
			height=Integer.parseInt(h);
		}
		else {
			height=-1;
		}
		
		

		
		int start = output.length();
		output.append("\uFFFC");
		int end = output.length();

		

		
		HTMLImageSpan span = new HTMLImageSpan(mImagePlaceholder,null,width,height,mDesiredSize);
		if (src != null) {
			(new ImageTask(span,100)).execute(src);
			output.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

	

	}
	private class ImageTask extends AsyncTask<String,Void,Bitmap> {

		private HTMLImageSpan mPlaceHolder;
		private int mSize;
		private String mSrc;


		public ImageTask(HTMLImageSpan placeholder,int width) {
			mPlaceHolder=placeholder;
			mSize=width;
			
		}
		@Override
		protected Bitmap doInBackground(String... params) {
			String url=params[0].toString();
			mSrc=url;
			
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
			
				
				
				
				in.close();
				byte[] raw=out.toByteArray();
				BitmapFactory.Options ops= new BitmapFactory.Options();
				ops.inTempStorage = new byte[32*1024];
				Bitmap scaled=BitmapFactory.decodeStream(new ByteArrayInputStream(raw), padding, ops);
				return scaled;
			}
			catch (Throwable t) {
				Log.e("NewPost", "Exception downloading image", t);
				return null;
			}
			}
			
		
		protected void onPostExecute(Bitmap bitmap) {
			if(bitmap!=null) {
				int width=bitmap.getWidth();
				int height=bitmap.getHeight();
				float scale;
				int displayHeight,displayWidth;
				
				if (width>height) {
				displayWidth=mPlaceHolder.getMaxDim()==-1?width:mPlaceHolder.getMaxDim();
				 scale = ((float) this.mSize*mScale)/width;
				float dratio=((float) bitmap.getWidth()) / displayWidth;
				displayHeight=(int) (height / dratio);
				}
				else {
					displayHeight=mPlaceHolder.getMaxDim()==-1?height:mPlaceHolder.getMaxDim();
					 scale = ((float) this.mSize*mScale)/height;
					float dratio=((float) height / displayHeight);
					displayWidth=(int) (width/ dratio);
					
				}

				Matrix matrix = new Matrix();
				matrix.postScale(scale, scale);

				Bitmap scaled = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
				Editable editableText=getEditableText();
				if (editableText != null) {
					int start = editableText.getSpanStart(mPlaceHolder);
					int end = editableText.getSpanEnd(mPlaceHolder);
					if (start != -1 && end != -1) {
						editableText.removeSpan(mPlaceHolder);
						
						HTMLImageSpan span = new HTMLImageSpan(scaled,mSrc,displayWidth,displayHeight);
						
						
						editableText.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}	
						
						
					
				}
			}
		}
			
		
		
	}
	private static final Rect padding=new Rect(-1,-1,-1,-1);
	private static final int IMAGE_THUMB=100;
	
	HashMap<String,HTMLImageSpan> imageStubs=new HashMap<String,HTMLImageSpan>();
	
	public void insertImageStub(String filepath, int desiredSize) {
		BitmapFactory.Options ops=new BitmapFactory.Options();
		ops.inTempStorage = new byte[32*1024];
		Bitmap bitmap=BitmapFactory.decodeFile(filepath,ops);
		if(bitmap!=null) {
			int width=bitmap.getWidth();
			int height=bitmap.getHeight();
			float scale;
			int displayHeight,displayWidth;
			
			if (width>height) {
			displayWidth=desiredSize==-1?width:desiredSize;
			 scale = ((float) IMAGE_THUMB*mScale)/width;
			float dratio=((float) bitmap.getWidth()) / displayWidth;
			displayHeight=(int) (height / dratio);
			}
			else {
				displayHeight=desiredSize==-1?height:desiredSize;
				 scale = ((float) IMAGE_THUMB*mScale)/height;
				float dratio=((float) height / displayHeight);
				displayWidth=(int) (width/ dratio);
				
			}

			Matrix matrix = new Matrix();
			matrix.postScale(scale, scale);

			Bitmap scaled = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
			
			Editable editableText=getEditableText();
			if (editableText != null) {
				int start=editableText.length();
				editableText.append("\uFFFC");
				int end = editableText.length();
				if (start != -1 && end != -1) {
					
					HTMLImageSpan span = new HTMLImageSpan(scaled,null,displayWidth,displayHeight);
					imageStubs.put(filepath,span);
					editableText.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}	
					
					
				
			}
		
		}
		
	}
	
	public void setImageDetails(String filepath,String link, String srcUrl,String title){
		HTMLImageSpan span=imageStubs.get(filepath);
		if (span!=null){
			if (link!=null) {
				final Editable text=getEditableText();
				int start=text.getSpanStart(span);
				int end=text.getSpanEnd(span);
				text.setSpan(new URLSpan(link), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			span.setSrc(srcUrl);
			span.setTitle(title);
		}
		imageStubs.remove(filepath);
	}
	

	
	
	
	
	public static class SavedState extends View.BaseSavedState {
		CharSequence text;
		int selStart;
		int selEnd;
		Map<Integer, Boolean> currentStyles;
		SavedState(Parcelable superState) {
			  super(superState);
			 
		}
		
	        @Override
	        public void writeToParcel(Parcel out, int flags) {
	            super.writeToParcel(out, flags);
	            out.writeInt(selStart);
	            out.writeInt(selEnd);
	            for (Integer style:currentStyles.keySet()) {
	            	out.writeInt(style);
	            	
	            }
	            out.writeInt(0);
	            SpanInfo.writeToParcel(text, out, flags);
	        }

	        @Override
	        public String toString() {
	            String str = "TextView.SavedState{"
	                    + Integer.toHexString(System.identityHashCode(this))
	                    + " start=" + selStart + " end=" + selEnd;
	            if (text != null) {
	                str += " text=" + text;
	            }
	            return str + "}";
	        }

	
	        public static final Parcelable.Creator<SavedState> CREATOR
	                = new Parcelable.Creator<SavedState>() {
	            public SavedState createFromParcel(Parcel in) {
	                return new SavedState(in);
	            }

	            public SavedState[] newArray(int size) {
	                return new SavedState[size];
	            }
	        };

	        private SavedState(Parcel in) {
	            super(in);
	            selStart = in.readInt();
	            selEnd = in.readInt();
	            while(true) {
	            	int style=in.readInt();
	            	if (style==0) break;
	            	currentStyles.put(style, true);
	            }
	           text = (Editable) SpanInfo.CHAR_SEQUENCE_CREATOR.createFromParcel(in);

	           
	        }
	    }
	
	@Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        // Save state if we are forced to
      
        int start = 0;
        int end = 0;

        if (mEditable != null) {
            start = getSelectionStart();
            end = getSelectionEnd();
            

            SavedState ss = new SavedState(superState);
            // XXX Should also save the current scroll position!
            ss.selStart = start;
            ss.selEnd = end;
            ss.currentStyles=mCurrentStyles;

         
                /*
                 * Calling setText() strips off any ChangeWatchers;
                 * strip them now to avoid leaking references.
                 * But do it to a copy so that if there are any
                 * further changes to the text of this view, it
                 * won't get into an inconsistent state.
                 */

                Spannable sp = new SpannableString(mEditable);

               /* for (ChangeWatcher cw :
                     sp.getSpans(0, sp.length(), ChangeWatcher.class)) {
                    sp.removeSpan(cw);
                }*/

                ss.text = sp;
           

         

            

            return ss;
        }

        return superState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState)state;
        super.onRestoreInstanceState(ss.getSuperState());

        // XXX restore buffer type too, as well as lots of other stuff
        if (ss.text != null) {
            setText(ss.text);
            mEditable=getEditableText();
        }

        mCurrentStyles=ss.currentStyles;
        for (Integer style:mCurrentStyles.keySet()) {
        	Button b=mButtons.get(style);
        	if (b!=null) {
        		if (b instanceof ToggleButton) ((ToggleButton) b).setChecked(true);
        		else b.setPressed(true);

        	}
        }
        if (ss.selStart >= 0 && ss.selEnd >= 0) {
            if (mEditable instanceof Spannable) {
                int len = mEditable.length();

                if (ss.selStart > len || ss.selEnd > len) {
                    String restored = "";

                    if (ss.text != null) {
                        restored = "(restored) ";
                    }

                    Log.e("RicEditText", "Saved cursor position " + ss.selStart +
                          "/" + ss.selEnd + " out of range for " + restored +
                          "text " + mEditable);
                } else {
                    Selection.setSelection((Spannable) mEditable, ss.selStart,
                                           ss.selEnd);

                }
            }
        }

      
    }
        
    
		
		
		
		
		
	
	 
	}

	
	
	
	
	
	

	



