package com.electronapps.LJPro;



import android.graphics.Paint;

import android.graphics.Paint.Style;
import android.os.Parcel;

public class BlockQuoteSpan extends BlockSpan {
		
		private final static int dColor=0xff23D61A;
		private static final Style mStyle=Paint.Style.FILL;


		
		public BlockQuoteSpan(int color,int marginColor) {
			super(color,marginColor,mStyle,null);
		}
		
		public BlockQuoteSpan() {
			super(-1,dColor,mStyle,null);
		
		}
		
		public BlockQuoteSpan(Parcel p) {
			super(p);
		}
		
		

		
		
		
	}