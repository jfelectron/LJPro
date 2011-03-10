package com.electronapps.LJPro;



import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathDashPathEffect.Style;
import android.os.Parcel;
import android.text.DynamicLayout;

public class LJCutSpan extends BlockSpan {

	
		private String mCutText;
		
		private final static int defaultColor=0xffF51111;
	    private final static android.graphics.Paint.Style mStyle=Paint.Style.STROKE;
		private static float[] f={10.f,5.0f};
		private static DashPathEffect dashed=new DashPathEffect(f,1.0f);
		
	  
		
		public LJCutSpan(String cutText,int color,int marginColor) {
			super(color,marginColor,mStyle,dashed);
			mCutText=cutText;
			
		}
		
		public LJCutSpan(String cutText){
			super(-1,defaultColor,mStyle,dashed);
			 mCutText=cutText;
			
		}
		
		public LJCutSpan(Parcel p) {
			super(p);
			mCutText=p.readString();
		
		
		}
		
		public String getCutText() {
			return mCutText;
		}
		
		public void setCutText(String cutText) {
			mCutText=cutText;
		}
		
		
		
		

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest,flags);
			dest.writeString(mCutText);
			
	    }

		/*@Override
		public void updateDrawState(TextPaint ds) {
			ds.bgColor = mBColor;
		}*/


	


		

		
	
		
	}