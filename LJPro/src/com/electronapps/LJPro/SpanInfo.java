package com.electronapps.LJPro;

import com.electronapps.LJPro.BlockQuoteSpan;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.ParcelableSpan;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.BulletSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;

import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;

public class SpanInfo {
	
		public int id;
		public int start;
		public int end;
		public int color;
		Class type;
		Object what;
		
		public SpanInfo(int style) {
			id=style;
		}
		
		public SpanInfo(int style,boolean adding) {
			type=spanClassFactory(style);
			if (adding) {
				what=styleFactory(style);
			}
		}
		
		public SpanInfo(int style, boolean adding, Integer size) {
			type=spanClassFactory(style);
			if (adding) {
				what=sizeFactory(size);
			}
		}
		
		public SpanInfo(int style,boolean adding, int color) {
			type=spanClassFactory(style);
			if(adding) {
				what=colorFactory(style,color);
			}
		}
		
		public SpanInfo(int style, boolean adding, String text) {
			type=spanClassFactory(style);
			if (adding) {
				if (style==RichEditText.URL_SPAN) {
					what=linkFactory(text);
				}
				else {
					what=ljcutFactory(text);
					}
				}
		}

		static public Class spanClassFactory(int style) {
			
			switch (style) {
			case Typeface.BOLD:
			case Typeface.ITALIC:
				return StyleSpan.class;
			case RichEditText.STYLE_UNDERLINE:
				return UnderlineSpan.class;

			case RichEditText.STYLE_STRIKETHROUGH:
				return StrikethroughSpan.class;

			case RichEditText.STYLE_SUBSCRIPT:
				return SubscriptSpan.class;

			case RichEditText.STYLE_SUPERSCRIPT:
				return SuperscriptSpan.class;
			
			case RichEditText.STYLE_BLOCKQUOTE:
				return BlockQuoteSpan.class;
			
			case RichEditText.HEADER_H1:
			case RichEditText.HEADER_H2:
			case RichEditText.HEADER_H3:
			case RichEditText.HEADER_H4:
			case RichEditText.HEADER_H5:
			case RichEditText.HEADER_H6:
				return RelativeSizeSpan.class;
			case RichEditText.ALIGNMENT_SPAN:
				return AlignmentSpan.class;
			case RichEditText.SIZE_XXSMALL:
			case RichEditText.SIZE_XSMALL:
			case RichEditText.SIZE_SMALL:
			case RichEditText.SIZE_MEDIUM:
			case RichEditText.SIZE_LARGE:
			case RichEditText.SIZE_XLARGE:
			case RichEditText.SIZE_XXLARGE:
				return AbsoluteSizeSpan.class;
			case RichEditText.IMAGE_SPAN:
				return HTMLImageSpan.class;
			case RichEditText.URL_SPAN:
				return URLSpan.class;
			case RichEditText.BACKGROUND_COLOR:
				return BackgroundColorSpan.class;
			case RichEditText.TEXT_COLOR:
				return ForegroundColorSpan.class;
			case RichEditText.BULLET_SPAN:
				return BulletSpan.class;
			case RichEditText.LJCUT_SPAN:
				return LJCutSpan.class;
			}
		
			return Object.class;
			
		}

		static public Object styleFactory(int type) {
			Object what=null;
			switch(type) {
				case Typeface.BOLD:
					what=new StyleSpan(type);
					break;
				case Typeface.ITALIC:
					what=new StyleSpan(type);
					break;
				case RichEditText.STYLE_BLOCKQUOTE:
					what=new BlockQuoteSpan();
					break;
				case RichEditText.STYLE_UNDERLINE:
					what=new UnderlineSpan();
					break;
				case RichEditText.STYLE_STRIKETHROUGH:
					what=new StrikethroughSpan();
					break;
				case RichEditText.STYLE_SUPERSCRIPT:
					what=new SuperscriptSpan();
					break;
				case RichEditText.STYLE_SUBSCRIPT:
					what=new SubscriptSpan();
					break;
				case RichEditText.BULLET_SPAN:
					what=new BulletSpan();
					break;	
					
			
			}
			return what;
		}
		
		public static LJCutSpan ljcutFactory(String cuttext) {
			return new LJCutSpan(cuttext);
		}
		
		public static URLSpan linkFactory(String url) {
			return new URLSpan(url);
		}
		
		public static Object colorFactory(int type, int color) {
			switch(type) {
				case RichEditText.TEXT_COLOR:
					return new ForegroundColorSpan(color);
				case RichEditText.BACKGROUND_COLOR:
					return new BackgroundColorSpan(color);
			}
			return null;
		}
		
		public static URLSpan urlFactory(String url) {
			return new URLSpan(url);
		}
		
		
		public static AbsoluteSizeSpan sizeFactory(int size) {
			return new AbsoluteSizeSpan(size);
		}
		
		public static RelativeSizeSpan relativeSizeFactory(float proportion) {
			return new RelativeSizeSpan(proportion);
		}
		
		
		public static HTMLImageSpan imageFactory(BitmapDrawable d,int h,int w ,String src){
			return new HTMLImageSpan(d,src,h,w);
		}
		
		public static final Parcelable.Creator<CharSequence> CHAR_SEQUENCE_CREATOR
        = new Parcelable.Creator<CharSequence>() {
    /**
     * Read and return a new CharSequence, possibly with styles,
     * from the parcel.
     */
    public  CharSequence createFromParcel(Parcel p) {
        int kind = p.readInt();

        if (kind == 1)
            return p.readString();

        SpannableString sp = new SpannableString(p.readString());

        while (true) {
            kind = p.readInt();

            if (kind == 0)
                break;

            switch (kind) {
            case RichEditText.ALIGNMENT_SPAN:
                readSpan(p, sp, new AlignmentSpan.Standard(p));
                break;

            case RichEditText.TEXT_COLOR:
                readSpan(p, sp, new ForegroundColorSpan(p));
                break;

            case RichEditText.HEADER_H1:
            case RichEditText.HEADER_H2:
            case RichEditText.HEADER_H3:
            case RichEditText.HEADER_H4:
            case RichEditText.HEADER_H5:
            case RichEditText.HEADER_H6:
            	
                readSpan(p, sp, new RelativeSizeSpan(p));
                break;
                
            case RichEditText.SIZE_XXSMALL:
            case RichEditText.SIZE_XSMALL:
            case RichEditText.SIZE_SMALL:
            case RichEditText.SIZE_MEDIUM:
            case RichEditText.SIZE_LARGE:
            case RichEditText.SIZE_XLARGE:
            	
                readSpan(p, sp, new AbsoluteSizeSpan(p));
                break;
           
          

            case RichEditText.STYLE_STRIKETHROUGH:
                readSpan(p, sp, new StrikethroughSpan(p));
                break;

            case RichEditText.STYLE_UNDERLINE:
                readSpan(p, sp, new UnderlineSpan(p));
                break;

            case Typeface.BOLD:
            case Typeface.ITALIC:
                readSpan(p, sp, new StyleSpan(p));
                break;

            case RichEditText.BULLET_SPAN:
                readSpan(p, sp, new BulletSpan(p));
                break;

            case RichEditText.STYLE_BLOCKQUOTE :
                readSpan(p, sp, new BlockQuoteSpan(p));
                break;

            case RichEditText.IMAGE_SPAN:
            	readSpan(p,sp,new HTMLImageSpan(p,p.dataPosition()));

            case RichEditText.URL_SPAN:
                readSpan(p, sp, new URLSpan(p));
                break;

            case RichEditText.BACKGROUND_COLOR:
                readSpan(p, sp, new BackgroundColorSpan(p));
                break;


            case RichEditText.STYLE_SUPERSCRIPT:
                readSpan(p, sp, new SuperscriptSpan(p));
                break;

            case RichEditText.STYLE_SUBSCRIPT:
                readSpan(p, sp, new SubscriptSpan(p));
                break;

                

            default:
                throw new RuntimeException("bogus span encoding " + kind);
            }
        }

        return sp;
    }

    public CharSequence[] newArray(int size)
    {
        return new CharSequence[size];
    }
    
   


	   
		
	};
	
	 private static void readSpan(Parcel p, Spannable sp, Object o) {
	        sp.setSpan(o, p.readInt(), p.readInt(), p.readInt());
	    }
	 
	    public static void writeToParcel(CharSequence cs, Parcel p,
	            int parcelableFlags) {
	        if (cs instanceof Spanned) {
	            p.writeInt(0);
	            p.writeString(cs.toString());

	            Spanned sp = (Spanned) cs;
	            Object[] os = sp.getSpans(0, cs.length(), Object.class);

	            // note to people adding to this: check more specific types
	            // before more generic types.  also notice that it uses
	            // "if" instead of "else if" where there are interfaces
	            // so one object can be several.

	            for (int i = 0; i < os.length; i++) {
	                Object o = os[i];
	                Object prop = os[i];

	                if (prop instanceof HTMLImageSpan) {
	                    HTMLImageSpan ps = (HTMLImageSpan)prop;
	                    SpanInfo s=getSpanInfo(prop,sp);
	                    p.writeInt(s.id);
	                    ps.writeToParcel(p, parcelableFlags);
	                    writeWhere(p,s.start,s.end,sp.getSpanFlags(ps));
	                }
	                
	                if (prop instanceof BlockQuoteSpan){
	                	  BlockQuoteSpan ps = (BlockQuoteSpan)prop;
		                    SpanInfo s=getSpanInfo(prop,sp);
		                    p.writeInt(s.id);
		                    ps.writeToParcel(p, parcelableFlags);
		                    writeWhere(p,s.start,s.end,sp.getSpanFlags(ps));
	                }
	             
	                

	               if (prop instanceof ParcelableSpan) {
	                    ParcelableSpan ps = (ParcelableSpan)prop;
	                    SpanInfo s=getSpanInfo(prop,sp);
	                    p.writeInt(s.id);
	                    ps.writeToParcel(p, parcelableFlags);
	                    writeWhere(p,s.start,s.end,sp.getSpanFlags(ps));
	                }
	                
	            }

	            p.writeInt(0);
	        } else {
	            p.writeInt(1);
	            if (cs != null) {
	                p.writeString(cs.toString());
	            } else {
	                p.writeString(null);
	            }
	        }
	    }
	    
	    private static void writeWhere(Parcel p, int start, int end,int spanFlags) {
	        p.writeInt(start);
	        p.writeInt(end);
	        p.writeInt(spanFlags);
	    }
	    
	    public static SpanInfo getSpanInfo(Object span,Spanned sp2) {
			SpanInfo s=null;
			if (span instanceof StyleSpan) {
				StyleSpan sp=(StyleSpan) span;
				s=new SpanInfo(sp.getStyle());	
			}
			
		if (span instanceof UnderlineSpan) {
				s=new SpanInfo(RichEditText.STYLE_UNDERLINE);
			}
		
		if (span instanceof StrikethroughSpan) {
			s=new SpanInfo(RichEditText.STYLE_STRIKETHROUGH);
		}
		if (span instanceof SubscriptSpan) {
			s=new SpanInfo(RichEditText.STYLE_SUBSCRIPT);
		}
		
		if (span instanceof BlockQuoteSpan) {
			s=new SpanInfo(RichEditText.STYLE_BLOCKQUOTE);
		}
		
		if (span instanceof LJCutSpan) {
			s=new SpanInfo(RichEditText.LJCUT_SPAN);
		}
		
		
		
		if (span instanceof SuperscriptSpan) {
		s=new SpanInfo(RichEditText.STYLE_SUPERSCRIPT);
			
		}
		if (span instanceof RelativeSizeSpan) {
			RelativeSizeSpan rss=(RelativeSizeSpan) span;
			rss.getSizeChange();
		}
		
		
		
		if (span instanceof AlignmentSpan) {
				s=new SpanInfo(RichEditText.ALIGNMENT_SPAN);
		}
		
		if (span instanceof BackgroundColorSpan) {
			s=new SpanInfo(RichEditText.BACKGROUND_COLOR);
			s.color=((BackgroundColorSpan)span).getBackgroundColor();			
		}
		
		if (span instanceof ForegroundColorSpan) {
			s=new SpanInfo(RichEditText.TEXT_COLOR);
			s.color=((ForegroundColorSpan)span).getForegroundColor();
			
		}
		
		if (span instanceof URLSpan) {
			s=new SpanInfo(RichEditText.URL_SPAN);
			
		}
		
		if (span instanceof HTMLImageSpan) {
			s=new SpanInfo(RichEditText.IMAGE_SPAN);
			
		}
		
		

		if(span instanceof AbsoluteSizeSpan) {
			
			final int size=((AbsoluteSizeSpan)(span)).getSize();
			s=new SpanInfo(RichEditText.SizeIdMap.get(size));
			
		}
		
		if(span instanceof RelativeSizeSpan) {
			final float relsize=((RelativeSizeSpan)span).getSizeChange();
			s=new SpanInfo(RichEditText.RelativeSizeIdMap.get(relsize));
		}
			if (s!=null) {
				s.start=sp2.getSpanStart(span);
				s.end=sp2.getSpanEnd(span);
			}
			return s;
			
		
		}
		
		
		
		
	

}
