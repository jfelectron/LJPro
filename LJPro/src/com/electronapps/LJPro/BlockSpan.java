package com.electronapps.LJPro;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.os.Parcel;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;
import android.text.style.ReplacementSpan;

public class BlockSpan {


	private int mColor;
	private int mBColor;
	private Style mStyle;
	private PathEffect mPathEffect;

	
  
	
	public BlockSpan(int blockColor,int termColor,Style style,PathEffect effect) {
		mStyle=style;
		mBColor=blockColor;
		mPathEffect=effect;
		mColor=termColor;
	}
	
	public BlockSpan(){
		 mColor = 0xffF51111;
		 mBColor=0xffCFD1CD;
	}
	
	public BlockSpan(Parcel p) {
		mBColor=p.readInt();
		mColor=p.readInt();
	
	}
	
	public void setBackgroundColor(int color) {
		mBColor=color;
	}
	
	public void setUnderLineColor(int color){
		mColor=color;
	}
	
	
	
	

	
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mBColor);
        dest.writeInt(mColor);
    }

	public void drawBlock(Canvas c, Paint p, float X,Rect curLine,int baseline, CharSequence text, int start, int end,Layout layout) {
			Spanned spanned=(Spanned)text;
			Rect rect=new Rect();
			p.getTextBounds(text.toString(), start, end,rect);
			
			float ret=p.measureText(text,start,end);
			float x=X+curLine.left;
			int bottom=curLine.bottom;
			int top=curLine.top;
			int color=p.getColor();
			PathEffect effect=p.getPathEffect();
			Style style=p.getStyle();
			
			if (mBColor!=-1) {
				p.setColor(mBColor);
				p.setStyle(Paint.Style.FILL);
				p.setPathEffect(effect);
				c.drawRect(x, rect.top, x+ret, rect.bottom, p);
			}
			p.setColor(mColor);
			if (mPathEffect!=null) {
				p.setPathEffect(mPathEffect);
			}
			
			p.setStyle(Paint.Style.STROKE);
			p.setStrokeWidth(2.0f);
			c.drawLine(x,bottom, x, top, p);
			int spanStart=spanned.getSpanStart(this);
			int spanEnd=spanned.getSpanEnd(this);
			if (start==spanStart) {
				if (spanEnd>end) {
					c.drawLine(x, top,curLine.right, top, p);
				}
				else c.drawLine(x, top,x+ret, top, p);
			}
			if (spanEnd>end) {
				c.drawLine(curLine.right,bottom,curLine.right,top,p);
			}
			else c.drawLine(x+ret, bottom, x+ret, top, p);
			if (layout.getLineForOffset(start)==layout.getLineForOffset(spanStart)+1){
				
			float spanOffset=layout.getPrimaryHorizontal(spanStart);
			if (layout.getPrimaryHorizontal(start)<spanOffset){
				c.drawLine(curLine.left,top,curLine.left+spanOffset,top,p);
			}
			}
			if (end==spanEnd) {
				
				c.drawLine(x, bottom,x+ret, bottom, p);
				if (start!=spanStart) {
				c.drawLine(x+ret, curLine.top, curLine.right, curLine.top, p);
				}
			}
			
				
			
			p.setStyle(style);
			p.setPathEffect(effect);
			p.setColor(color);
			
		
		
	}

	









}
