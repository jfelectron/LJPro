package com.electronapps.LJPro;
import java.util.HashMap;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.electronapps.LJPro.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.LinearLayout.LayoutParams;


public class ReplyTo extends Activity implements OnAmbilWarnaListener{
	
	
	private String mJournalName;
	private String mAccountName;
	private int mItemId;
	private RichEditText mComment;
	private EditText mSubject;
	private PostView mPost;
	private Context mContext;
	private InputMethodManager IME;
	private int mTalkId;
	private String mPosterName;

	@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			Intent intent=getIntent();
			//final Object data=getLastNonConfigurationInstance();
			mContext=getApplicationContext();
			mJournalName=intent.getStringExtra("journalname");
			mAccountName=intent.getStringExtra("accountname");
			mTalkId=intent.getIntExtra("talkid",0);
			mItemId=intent.getIntExtra("itemid",0);
			mPosterName=intent.getStringExtra("postername");
			setContentView(R.layout.commentcompose);
			
			PostView post=(PostView) findViewById(R.id.post);
			TextView div=(TextView) findViewById(R.id.div);
			mComment=(RichEditText) findViewById(R.id.commentbody);
			mSubject=(EditText) findViewById(R.id.commentsubject);
			IME=(InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
			
			setupStyleButtons();
			setupButtonHash();
			
			//if (data!=null) mComment.setText((Editable)data);
			div.setText(getString(R.string.in_replay_to_)+mPosterName);
			post.setTag(mJournalName+((Integer)mItemId).toString()+"comment");
			//post.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
			post.setText("Loading...");
			post.setHTML(intent.getStringExtra("html"));
	}
	

	
	@Override
	public Object onRetainNonConfigurationInstance() {
		super.onRetainNonConfigurationInstance();
		Editable comment=mComment.getText();
		return comment;
	}

	
	private void setupStyleButtons() {

		Button button;
		for (int i=0;i<buttonids.length;i++) {
			
			button=(Button) findViewById(buttonids[i]);
		
			button.setOnClickListener(onStyleClick);
		}
		
		mComment.registerBoldButton((Button)findViewById(R.drawable.boldbutton));
		mComment.registerItalicButton((Button)findViewById(R.drawable.italicbutton));
		mComment.registerStyleButton((Button)findViewById(R.drawable.stylebutton));
		
		
		
	}
	

	

	
	
	
	private void setupButtonHash() {
		for(int i=0;i<sizeIcons.length;i++) {
			buttonMap.put(sizeIcons[i], mRelativeSizes[i]);
			
		}
		for (int i=0;i<styleIcons.length;i++) {
			buttonMap.put(styleIcons[i], styleids[i]);
		}
	}
	
	final private Integer[] buttonids={R.drawable.boldbutton,R.drawable.italicbutton,R.drawable.stylebutton,R.drawable.sizebutton,R.drawable.colorbutton,R.drawable.plus_button};

	private final static int[] styleIcons={R.drawable.underlinebutton,R.drawable.strikethroughbutton,R.drawable.blockquotebutton,R.drawable.superscriptbutton,R.drawable.subscriptbutton};
	final private Integer[] styleids={RichEditText.STYLE_UNDERLINE,RichEditText.STYLE_STRIKETHROUGH,RichEditText.STYLE_BLOCKQUOTE,RichEditText.STYLE_SUPERSCRIPT,RichEditText.STYLE_SUBSCRIPT};
	final private static int[] sizeIcons={R.drawable.btn_xxsmalll,R.drawable.btn_xsmalll,R.drawable.btn_smalll,R.drawable.btn_medium,R.drawable.btn_large,R.drawable.btn_xlarge,R.drawable.btn_xxlarge};
	private static int[] mRelativeSizes={RichEditText.SIZE_XXSMALL,RichEditText.SIZE_XSMALL,RichEditText.SIZE_SMALL,RichEditText.SIZE_MEDIUM,RichEditText.SIZE_LARGE,RichEditText.SIZE_XLARGE,RichEditText.SIZE_XXLARGE};

	private HashMap<Integer,Integer> buttonMap=new HashMap<Integer,Integer>();
	
	OnClickListener styleClick= new OnClickListener() {

		public void onClick(View v) {
		
			
			final int id=v.getId();
			if (id==R.drawable.subscriptbutton) {
				
				View z=((View)v.getParent().getParent()).findViewById(R.drawable.superscriptbutton);
				z.setPressed(false);
				if (mComment.isStyleSet(RichEditText.STYLE_SUPERSCRIPT)) mComment.toggleStyle(RichEditText.STYLE_SUPERSCRIPT);
			}
			if (id==R.drawable.superscriptbutton) {
				View z=((View)v.getParent().getParent()).findViewById(R.drawable.subscriptbutton);
				z.setPressed(false);
				if (mComment.isStyleSet(RichEditText.STYLE_SUBSCRIPT)) mComment.toggleStyle(RichEditText.STYLE_SUBSCRIPT);
			}
			int styleId=buttonMap.get(id);
				
			
		mQA.dismiss();
		if (imeShown&&!IME.isActive(mComment)) IME.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		mComment.toggleStyle(styleId);	
		Button b=(Button) findViewById(R.drawable.stylebutton);
		for (int style:mComment.getCurrentStyles()){
			if (style!=Typeface.BOLD&&style!=Typeface.BOLD) {
				b.setPressed(true);
				return;
			}
		}
		
		b.setPressed(false);
	
		}
		
		
	};
	
	int mCurrentSize=R.drawable.btn_medium;
	
	OnClickListener sizeClick= new OnClickListener() {

		public void onClick(View v) {
		
			
			final int id=v.getId();
			
				
				View z=((View)v.getParent().getParent()).findViewById(mCurrentSize);
				if (!(mCurrentSize==R.drawable.btn_medium&&id==mCurrentSize)) {
					z.setPressed(false);
					if (!(mCurrentSize==R.drawable.btn_medium)) {
						mComment.toggleStyle(buttonMap.get(mCurrentSize));
					}
				}
			
			
			int styleId=-1;
			
			styleId=buttonMap.get(id);
				
			
		mQA.dismiss();
		mCurrentSize=id;
		Button b=(Button) findViewById(R.drawable.sizebutton);
		if (styleId!=RichEditText.SIZE_MEDIUM) {
			mComment.toggleStyle(styleId);	
			b.setPressed(true);
		}
		
		else {
			b.setPressed(false);}
		}
	
		
		
		
	};
	private QuickAction mQA;
	

	private void showStyleQuickAction(View v) {
		
		showQuickAction(v,styleIcons,styleClick);
	}
	
	private void showSizeQuickAction(View v) {
		showQuickAction(v,sizeIcons,sizeClick);
		
		
	}
	
	private boolean imeShown=false;
	
	private void showQuickAction(View v, int[] what, OnClickListener listener){
		
		imeShown=IME.isActive(mComment);
		mQA= new QuickAction(v);
		boolean pressed=false;
		boolean styleSelected=false;
		for (int i=0;i<what.length;i++) {
			if (what[i]==R.drawable.btn_medium) {
				pressed=mCurrentSize==what[i]?true:false;
			}
			else pressed=mComment.isStyleSet(buttonMap.get(what[i]));
			if (pressed&!styleSelected) styleSelected=true;
			ActionItem ai = new ActionItem(this,what[i],pressed);
			ai.setOnClickListener(listener);
			mQA.addActionItem(ai);
		}
		mQA.show();
		if(styleSelected) {
			if (v instanceof Button) ((Button)v).setPressed(true);
			else ((ToggleButton)v).setChecked(true);
		}
	}
	

	
	private OnClickListener onStyleClick=new OnClickListener() {

		public void onClick(View v) {
		
			switch (v.getId())  {
				case R.drawable.boldbutton:
					
					mComment.toggleStyle(Typeface.BOLD);
					break;
				case R.drawable.italicbutton:
					mComment.toggleStyle(Typeface.ITALIC);
					break;
				case R.drawable.stylebutton:

					showStyleQuickAction(v);
					break;
				case R.drawable.sizebutton:
					showSizeQuickAction(v);
					break;
				case R.drawable.colorbutton:
					AmbilWarnaDialog colorchooser=new AmbilWarnaDialog(ReplyTo.this,mComment.getColor(),mComment.getBackgroundColor(),ReplyTo.this);
					colorchooser.show();
					break;
				case R.drawable.plus_button:
					
					addComment();
					
					break;
					
					
				
					
					
			
			}
			
		}

		
	

		};
		

	private void addComment() {
		String comment=Html.toHtml(mComment.getText());
		String subject=mSubject.getText().toString();
		Intent addcomment=new Intent(LJNet.LJ_ADDCOMMENT);
		addcomment.putExtra("journalname",mAccountName);
		addcomment.putExtra("postjournal", mJournalName);
		addcomment.putExtra("ditemid", mItemId);
		addcomment.putExtra("talkid", mTalkId);
		addcomment.putExtra("comment", comment);
		addcomment.putExtra("subject", subject);
		WakefulIntentService.sendWakefulWork(getApplicationContext(), addcomment);	
		Toast.makeText(this,getString(R.string.adding_comment)+" "+getString(R.string.in_reply_to)+" "+mPosterName, Toast.LENGTH_LONG).show();
		//LJPro app= (LJPro)getApplicationContext();
		//app.notifyComment(LJPro.COMMENT_ADDING, mPosterName, mJournalName);
		finish();
		}

	public void onCancel() {
		mComment.removeStyle(RichEditText.BACKGROUND_COLOR);
		mComment.removeStyle(RichEditText.TEXT_COLOR);
		mComment.setBackgroundColor(0xFFFFFFFF);
		mComment.setColor(0xFF000000);
		
	}



	public void onOk(int bgColor,int fgColor) {
		if(fgColor!=0xFF000000) {
			mComment.setColor(fgColor);
			mComment.toggleStyle(RichEditText.TEXT_COLOR);
		}
		if (bgColor!=0xFFFFFFFF) {
		mComment.setBackgroundColor(bgColor);
		mComment.toggleStyle(RichEditText.BACKGROUND_COLOR);
		}
		
	}


}
