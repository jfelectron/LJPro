package com.electronapps.LJPro;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import org.panel.Panel;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;



import com.commonsware.cwac.cache.SimpleWebImageCache;
import com.commonsware.cwac.thumbnail.ThumbnailAdapter;
import com.commonsware.cwac.thumbnail.ThumbnailBus;
import com.commonsware.cwac.thumbnail.ThumbnailMessage;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.electronapps.LJPro.GetLocation.CoordsCallBack;
import com.electronapps.LJPro.GetLocation.LocationCallBack;
import com.electronapps.LJPro.PhotoUploadReceiver.UploadStatus;
import com.example.coverflow.CoverFlow;

import android.text.Editable;
import android.text.Spannable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.MultiAutoCompleteTextView.Tokenizer;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleCursorAdapter.CursorToStringConverter;

public class NewPost extends Activity implements OnAmbilWarnaListener, LocationCallBack,CoordsCallBack,UploadStatus {
	
	RichEditText mPost;

	private EditText mSubject;

	private InputMethodManager IME;

	private Context mContext;

	private MultiAutoCompleteTextView mTags;

	private AutoCompleteTextView mMood;

	private CoverFlow mUserpics;

	private SimpleWebImageCache<ThumbnailBus, ThumbnailMessage> imgCache;

	private LJDB mDB;
	private String mJournalname;

	private Spinner mSecurity;

	private Spinner mUseJournal;
	
	private Spinner mAllowComments;
	
	private Spinner mScreenComments;
	
	private Spinner mAdultContent;

	private Cursor mJournals;

	private TextView mCoords;

	private TextView mLocation;

	private CheckBox mUseLoc;

	private GetLocation mFindMe;

	private SharedPreferences appPrefs;

	private Bitmap mImagePlaceholder;

	private float mScale;

	private PhotoUploadReceiver mPhotoReceiver;
	  final static int[] to = new int[] { android.R.id.text1 };
	    final static String[] from = new String[] {LJDB.KEY_NAME};
	  
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.postcompose);
		mContext=getApplicationContext();
		mJournalname=getIntent().getStringExtra("journalname");
		
		SetupPost newpost=new SetupPost();
		newpost.execute();
		IntentFilter filter=new IntentFilter();
		filter.addAction(PhotoAPIBase.UPLOAD_PROGRESS_UPDATE);
		filter.addAction(PhotoAPIBase.UPLOAD_COMPLETED);
		filter.addAction(PhotoAPIBase.UPLOAD_ERROR);
		mPhotoReceiver=new PhotoUploadReceiver(this);
		registerReceiver(mPhotoReceiver,filter);
		Intent gettags= new Intent(LJNet.LJ_GETUSERTAGS);
		gettags.putExtra("journalname",mJournalname);
		WakefulIntentService.sendWakefulWork(getApplicationContext(),gettags);
	
		appPrefs=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		imgCache = ((LJPro) mContext).getImageCache();
		mPost=(RichEditText) findViewById(R.id.postbody);
		mTags=(MultiAutoCompleteTextView) findViewById(R.id.tags);
		mSecurity=(Spinner) findViewById(R.id.security);
		mUseJournal=(Spinner) findViewById(R.id.usejournals);
		mUseLoc=(CheckBox) findViewById(R.id.useloc);
		mUseLoc.setOnCheckedChangeListener(useLoc);
		if(appPrefs.getBoolean(mJournalname+"_useLocation", false)) {
			mUseLoc.setChecked(true);
		}
	
		mCoords=(TextView) findViewById(R.id.coordinates);
		mLocation=(TextView) findViewById(R.id.locstring);
		mFindMe=new GetLocation(getApplicationContext(),this,this);
		mAllowComments=(Spinner) findViewById(R.id.allowcomments);
		mAdultContent=(Spinner) findViewById(R.id.adultcontent);
		mScreenComments=(Spinner) findViewById(R.id.screencomments);
		mMood=(AutoCompleteTextView) findViewById(R.id.mood);
		Panel p1=(Panel) findViewById(R.id.panel1);
		Panel p2=(Panel) findViewById(R.id.panel2);
		RelativeLayout ops=(RelativeLayout) findViewById(R.id.newpost);
		p1.registerSiblings(new int[]{R.id.panel2});
		//p1.setParent(ops);
		p2.registerSiblings(new int[]{R.id.panel1});
		//p2.setParent(ops);
		mUserpics=(CoverFlow) findViewById(R.id.userpics);
		mSubject=(EditText) findViewById(R.id.postsubject);
		IME=(InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		
		//Setup stuff that doesn't depend on DB
		setupSecurity();
		setupStyleButtons();
		setupButtonHash();
		setupMiscSpinners();
	}
	
	private class SetupPost extends AsyncTask<Void,Void,Void> {

		@Override
		protected Void doInBackground(Void... params) {
			mDB=LJDB.getDB(getApplicationContext());
			mDB.open();
			return null;
		}
		
		@Override 
		protected void onPostExecute (Void result) {
			setupUseJournals();
			setupUserpics();
			setupMoods();
			setupTags();
			
			
		}
		
	}
	
	private OnCheckedChangeListener useLoc=new OnCheckedChangeListener() {

		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			mCoords.setEnabled(isChecked);
			if (isChecked) {
				mLocation.setText(R.string.finding_location_);
				mFindMe.init();
			}
			else {
				mLocation.setText("");
				mFindMe.cancel();
			}
			
		}
		
	};
	
	private void setupMiscSpinners() {
		 ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
		            this, R.array.allow_comments, android.R.layout.simple_spinner_item);
		    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		    mAllowComments.setAdapter(adapter);
		    mAllowComments.setSelection(0);
		    
		    adapter = ArrayAdapter.createFromResource(
		            this, R.array.adult_content, android.R.layout.simple_spinner_item);
		    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		    mAdultContent.setAdapter(adapter);
		    mAdultContent.setSelection(0);
		    
		    adapter = ArrayAdapter.createFromResource(
		            this, R.array.screen_comments, android.R.layout.simple_spinner_item);
		    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		    mScreenComments.setAdapter(adapter);
		    mScreenComments.setSelection(0);
		    
		    
		
	}



	private void setupSecurity() {
		 ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
		            this, R.array.security_levels, android.R.layout.simple_spinner_item);
		    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		    mSecurity.setAdapter(adapter);
		 
		    int defsec=Integer.parseInt(appPrefs.getString(mJournalname+"_"+"defaultSecurity", "1"));
		    mSecurity.setSelection(defsec);
		
	}



	private void setupUseJournals() {
		String[] columns={"_id",LJDB.KEY_JOURNALNAME};
		mJournals=mDB.getUseJournals(mJournalname,columns);
		mJournals.moveToFirst();
		int position=0;
		for (int i=0;i<mJournals.getCount();i++){
			if (mJournals.getString(1).equals(mJournalname)) {
				position=i;
				break;
			}
		}
		String[] from={LJDB.KEY_JOURNALNAME};
		int[] to={android.R.id.text1};
		SimpleCursorAdapter usej=new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, mJournals, from, to);
		usej.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mUseJournal.setAdapter(usej);
		mUseJournal.setSelection(position);
		mUseJournal.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				mJournals.moveToPosition(position);
				if (!mJournals.getString(1).equals(mJournalname)) {
					mSecurity.setSelection(0);
				}
				
			}

			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
			
		});

	}


	private void setupUserpics() {
		String[] columns={"_id",LJDB.KEY_URL};
		int[] to={R.id.userpic};
		Cursor userpics=mDB.getUserPics(mJournalname,columns);
		UserpicAdapter upics=new UserpicAdapter(this,userpics,R.layout.userpicchooser);
		mUserpics.setAdapter(new ThumbnailAdapter(this, upics, imgCache, to));
		mUserpics.setSpacing(-25);
		mUserpics.setAnimationDuration(1000);
		
	}


	
	private void setupTags() {
		SimpleCursorAdapter adapter =
            new SimpleCursorAdapter(this,
                    android.R.layout.simple_dropdown_item_1line, null,
                    from, to);
		mTags.setAdapter(adapter);
		mTags.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
	
		 adapter.setCursorToStringConverter(new CursorToStringConverter() {
	            public String convertToString(android.database.Cursor cursor) {
	                // Get the label for this row out of the "state" column
	                final int columnIndex = cursor.getColumnIndexOrThrow(LJDB.KEY_NAME);
	                final String str = cursor.getString(columnIndex);
	                return str;
	            }
	        });
	 
	        // Set the FilterQueryProvider, to run queries for choices
	        // that match the specified input.
	        adapter.setFilterQueryProvider(new FilterQueryProvider() {
	            public Cursor runQuery(CharSequence constraint) {
	                // Search for states whose names begin with the specified letters.
	                Cursor cursor = mDB.getMatchingTags(mJournalname,
	                        (constraint != null ? constraint.toString() : null));
	                return cursor;
	            }
	        }); 
		
	}



	private void setupMoods() {
		SimpleCursorAdapter adapter =
            new SimpleCursorAdapter(this,
                    android.R.layout.simple_dropdown_item_1line, null,
                    from, to);
		mMood.setAdapter(adapter);
		
		 adapter.setCursorToStringConverter(new CursorToStringConverter() {
	            public String convertToString(android.database.Cursor cursor) {
	                // Get the label for this row out of the "state" column
	                final int columnIndex = cursor.getColumnIndexOrThrow(LJDB.KEY_NAME);
	                final String str = cursor.getString(columnIndex);
	                return str;
	            }
	        });
	 
	        // Set the FilterQueryProvider, to run queries for choices
	        // that match the specified input.
	        adapter.setFilterQueryProvider(new FilterQueryProvider() {
	            public Cursor runQuery(CharSequence constraint) {
	                // Search for states whose names begin with the specified letters.
	                Cursor cursor = mDB.getMatchingMoods(mJournalname,
	                        (constraint != null ? constraint.toString() : null));
	                return cursor;
	            }
	        }); 
		
	}



	private void setupStyleButtons() {

		Button button;
		for (int i=0;i<buttonids.length;i++) {
			
			button=(Button) findViewById(buttonids[i]);
		
			button.setOnClickListener(onStyleClick);
		}
		
		mPost.registerBoldButton((Button)findViewById(R.drawable.boldbutton));
		mPost.registerItalicButton((Button)findViewById(R.drawable.italicbutton));
		mPost.registerStyleButton((Button)findViewById(R.drawable.stylebutton));
		
		
		
	}
	

	

	
	
	
	private void setupButtonHash() {
		for(int i=0;i<sizeIcons.length;i++) {
			buttonMap.put(sizeIcons[i], mRelativeSizes[i]);
			
		}
		for (int i=0;i<styleIcons.length;i++) {
			buttonMap.put(styleIcons[i], styleids[i]);
		}
		for (int i=0;i<elementIcons.length;i++) {
			buttonMap.put(elementIcons[i],elementids[i]);
		}
	}
	
	final private Integer[] buttonids={R.drawable.boldbutton,R.drawable.italicbutton,R.drawable.stylebutton,R.drawable.sizebutton,R.drawable.colorbutton,R.drawable.elementbutton};

	private final static int[] styleIcons={R.drawable.underlinebutton,R.drawable.strikethroughbutton,R.drawable.blockquotebutton,R.drawable.superscriptbutton,R.drawable.subscriptbutton};
	private final static int[] elementIcons={R.drawable.imgbutton,R.drawable.ljcutbutton,R.drawable.linkbutton};
	private final Integer[] elementids={RichEditText.IMAGE_SPAN,RichEditText.LJCUT_SPAN,RichEditText.URL_SPAN};
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
				if (mPost.isStyleSet(RichEditText.STYLE_SUPERSCRIPT)) mPost.toggleStyle(RichEditText.STYLE_SUPERSCRIPT);
			}
			if (id==R.drawable.superscriptbutton) {
				View z=((View)v.getParent().getParent()).findViewById(R.drawable.subscriptbutton);
				z.setPressed(false);
				if (mPost.isStyleSet(RichEditText.STYLE_SUBSCRIPT)) mPost.toggleStyle(RichEditText.STYLE_SUBSCRIPT);
			}
			int styleId=buttonMap.get(id);
				
			
		mQA.dismiss();
		if (imeShown&&!IME.isActive(mPost)) IME.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		mPost.toggleStyle(styleId);	
		Button b=(Button) findViewById(R.drawable.stylebutton);
		for (int style:mPost.getCurrentStyles()){
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
						mPost.toggleStyle(buttonMap.get(mCurrentSize));
					}
				}
			
			
			int styleId=-1;
			
			styleId=buttonMap.get(id);
				
			
		mQA.dismiss();
		mCurrentSize=id;
		Button b=(Button) findViewById(R.drawable.sizebutton);
		if (styleId!=RichEditText.SIZE_MEDIUM) {
			mPost.toggleStyle(styleId);	
			b.setPressed(true);
		}
		
		else {
			b.setPressed(false);}
		}
	
		
		
		
	};
	
	private Boolean mUseTag=true;
	public int mSize;
	private void showPhotoLinkDialog() {
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setTitle("Insert Photo Link");
		LayoutInflater inflater=LayoutInflater.from(this);
		View content=inflater.inflate(R.layout.photolinkdialog,null);
		final SeekBar sizeSeek=(SeekBar) content.findViewById(R.id.sizeSeek);
		RadioButton button=(RadioButton) content.findViewById(R.id.tagchoice);
		final EditText imageTag=(EditText) content.findViewById(R.id.imageTag);
		final EditText imageLink=(EditText) content.findViewById(R.id.imageLink);
		final View tagContainer=content.findViewById(R.id.tagcontainer);
		final View linkContainer=content.findViewById(R.id.linkcontainer);
		button.setChecked(true);
		RadioGroup selector=(RadioGroup) content.findViewById(R.id.selector);
		selector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

			

			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId==R.id.tagchoice) {
					
					sizeSeek.setEnabled(false);
					tagContainer.setVisibility(View.VISIBLE);
					mUseTag=true;
					linkContainer.setVisibility(View.GONE);
					((View)tagContainer.getParent()).invalidate();
				}
				else {
					mUseTag=false;
					tagContainer.setVisibility(View.GONE);
					linkContainer.setVisibility(View.VISIBLE);
					sizeSeek.setEnabled(true);
					((View)tagContainer.getParent()).invalidate();
					
				}
				
			}
			
		});
		sizeSeek.setMax(100);
		sizeSeek.setProgress(0); //Native,T,S,M,L,
		final TextView photoSize=(TextView) content.findViewById(R.id.photoSize);
		sizeSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (progress<20) photoSize.setText("75 pixels");
				else if (progress>=20&&progress<40) photoSize.setText("100 pixels");
				else if (progress>=40&&progress<60) photoSize.setText("240 pixels");
				else if (progress>=60&&progress<80) photoSize.setText("500 pixels");
				else if (progress>=80&&progress<100) photoSize.setText("Full Size");
				
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
		});
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			
			

			public void onClick(DialogInterface dialog, int which) {
				Spanned text=null;
				if (mUseTag) {
					HashMap<String, Object> image=new HashMap<String,Object>();
					image.put("tag", imageTag.getText().toString());
					image.put("size", mSize);
				    mPost.toggleStyle(RichEditText.IMAGE_SPAN,image);
					//text=Html.fromHtml(imageTag.getText().toString(), null, tagHandler);
				
				}
				else {
					int progress=sizeSeek.getProgress();
					int size;
					if (progress<20) mSize=75;
					else if (progress>=20&&progress<40) mSize=100;
					else if (progress>=40&&progress<60) mSize=240;
					else if (progress>=60&&progress<80) mSize=500;
					else if (progress>=80&&progress<100) mSize=-1;
					
					
					String link="<img src=\""+imageLink.getText().toString()+"\"\\>";
					HashMap<String, Object> image=new HashMap<String,Object>();
					image.put("tag", link);
					image.put("size", mSize);
					 mPost.toggleStyle(RichEditText.IMAGE_SPAN,image);
					//text=Html.fromHtml(link, null, tagHandler);
				}
				
				
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				
			}
		});
		builder.setView(content);
		builder.create().show();
	
		
	}
	private final static int SELECT_PHOTO=2;
	private final static int UPLOAD_PHOTO=3;
	private final static int TAKE_PHOTO=4;
	
	OnClickListener elementClick=new OnClickListener() {
		public void onClick(View v) {
			mQA.dismiss();
			switch(v.getId()) {
			case R.drawable.imgbutton:
				AlertDialog.Builder builder=new AlertDialog.Builder(NewPost.this);
				builder.setTitle("Choose Image Source");
				CharSequence[] choices={"Photo Link","Upload Photo","Take new Photo"};
				builder.setSingleChoiceItems(choices, -1, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						switch(which) {
						case 0:
							showPhotoLinkDialog();
							break;
						case 1:
							 Intent intent = new Intent();
			                    intent.setType("image/*");
			                    intent.setAction(Intent.ACTION_GET_CONTENT);
			                    startActivityForResult(Intent.createChooser(intent,
			                            "Select Picture"), SELECT_PHOTO);
							break;
						case 2:
							Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); 
							startActivityForResult(cameraIntent, TAKE_PHOTO);  
							break;
						}
						dialog.dismiss();
					}

				
				
					
				});
				builder.create().show();
				break;
			case R.drawable.ljcutbutton:
				
				int start=mPost.getSelectionStart();
				int end=mPost.getSelectionEnd();
				if (end<start) {
					int tmp=end;
					end=start;
					start=tmp;
					
				}
				LJCutSpan[] ljcuts=(LJCutSpan[]) mPost.getSpansAtSelection(LJCutSpan.class);
				int num=ljcuts.length;
			
					if (num>0) {
					if (num>1) {
						AlertDialog.Builder getcuttext=new AlertDialog.Builder(NewPost.this);
						getcuttext.setTitle("LJ Cut");
						getcuttext.setMessage("Only select one LJCut at a time");
						
						
						getcuttext.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) {
								mQA.dismiss();
								dialog.dismiss();
								
							}
						});
						getcuttext.create().show();
						
					}
					else {
						
						if (start!=end||(start==end&&end!=((Spanned)mPost.getText()).getSpanEnd(ljcuts[0]))) {
						final LJCutSpan ljcut=ljcuts[0];
						AlertDialog.Builder getcuttext=new AlertDialog.Builder(NewPost.this);
						getcuttext.setTitle("LJ Cut");
						LayoutInflater inflater=LayoutInflater.from(NewPost.this);
						View content=inflater.inflate(R.layout.ljcuteditdialog,null);
						final EditText cuttext=(EditText) content.findViewById(R.id.cuttext);
						getcuttext.setPositiveButton("Update",new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) {
								mQA.dismiss();
								ljcut.setCutText(cuttext.getText().toString());
								dialog.dismiss();
								
							}
						});
						
						cuttext.setText(ljcut.getCutText());
						getcuttext.setView(content);
						getcuttext.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) {
								mQA.dismiss();
								
								
							}
						});
						
						
						
						getcuttext.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) {
								mQA.dismiss();
								dialog.dismiss();
								mPost.toggleStyle(RichEditText.LJCUT_SPAN,cuttext.getText().toString());
								
							}
						});
						getcuttext.create().show();
						
					}
						else {
							mQA.dismiss();
							mPost.toggleStyle(RichEditText.LJCUT_SPAN);
						}
					}
					
				}
				else {
				AlertDialog.Builder getcuttext=new AlertDialog.Builder(NewPost.this);
				getcuttext.setTitle("LJ Cut");
				LayoutInflater inflater=LayoutInflater.from(NewPost.this);
				View content=inflater.inflate(R.layout.ljcutdialog,null);
				final EditText cuttext=(EditText) content.findViewById(R.id.cuttext);
				
				getcuttext.setView(content);
				getcuttext.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						mQA.dismiss();
						mPost.toggleStyle(RichEditText.LJCUT_SPAN,cuttext.getText().toString());
						
					}
				});
				
				getcuttext.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						
					}
				});
				getcuttext.create().show();
				}
				
				break;
			case R.drawable.linkbutton:
				start=mPost.getSelectionStart();
				end=mPost.getSelectionEnd();
				if (end<start) {
					int tmp=end;
					end=start;
					start=tmp;
					
				}
				final int lstart=start;
				final int lend=end;
				String linkText=null;
				if (lstart!=lend) linkText=mPost.getText().toString().substring(lstart,lend);
				URLSpan[] urls=(URLSpan[]) mPost.getSpansAtSelection(URLSpan.class);
				num=urls.length;
				
				if (num>0) {
				if (num>1) {
					AlertDialog.Builder getcuttext=new AlertDialog.Builder(NewPost.this);
					getcuttext.setTitle("Web Link");
					getcuttext.setMessage("Only select one link at a time");
					
					
					getcuttext.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							mQA.dismiss();
							dialog.dismiss();
							
						}
					});
					getcuttext.create().show();
					
				}
				else {
					
					if (lstart!=lend||(lstart==lend&&lend!=((Spanned)mPost.getText()).getSpanEnd(urls[0]))) {
					final URLSpan url=urls[0];
					AlertDialog.Builder getcuttext=new AlertDialog.Builder(NewPost.this);
					getcuttext.setTitle("Web Link");
					LayoutInflater inflater=LayoutInflater.from(NewPost.this);
					View content=inflater.inflate(R.layout.urldialog,null);
					final EditText URL=(EditText) content.findViewById(R.id.url);
					final EditText linktext=(EditText) content.findViewById(R.id.linktext);
					getcuttext.setPositiveButton("Update",new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							mQA.dismiss();
							Editable text=mPost.getEditableText();
							text.removeSpan(url);
							text.replace(lstart,lend,linktext.getText().toString());
							mPost.setCurrentURL(URL.getText().toString());
							text.setSpan(new URLSpan(URL.getText().toString()),lstart,lstart+linktext.getText().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
							dialog.dismiss();
							
						}
					});
					if (linkText!=null) {
					linktext.setText(linkText);
					}
					URL.setText(url.getURL());
					getcuttext.setView(content);
					getcuttext.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							mQA.dismiss();
							
							
						}
					});
					
					
					
					getcuttext.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							mQA.dismiss();
							dialog.dismiss();
							mPost.toggleStyle(RichEditText.URL_SPAN);
							
						}
					});
					getcuttext.create().show();
					
				}
					else {
						mQA.dismiss();
						mPost.toggleStyle(RichEditText.URL_SPAN);
					}
				
				}
				}
			
			else {
			AlertDialog.Builder getcuttext=new AlertDialog.Builder(NewPost.this);
			getcuttext.setTitle("Web Link");
			LayoutInflater inflater=LayoutInflater.from(NewPost.this);
			View content=inflater.inflate(R.layout.urldialog,null);
			final EditText URL=(EditText) content.findViewById(R.id.url);
			final EditText linktext=(EditText) content.findViewById(R.id.linktext);
			if (linkText!=null) linktext.setText(linkText);
			
			getcuttext.setView(content);
			getcuttext.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					mQA.dismiss();
					Editable text=mPost.getEditableText();
					text.replace(lstart,lend,linktext.getText());
					
					//text.setSpan(new URLSpan(URL.getText().toString()),lstart,lstart+linktext.getText().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					mPost.toggleStyle(RichEditText.URL_SPAN,URL.getText().toString());
					
				}
			});
			
			getcuttext.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					
				}
			});
			getcuttext.create().show();
			}
			
				break;
			
			
			
		}
		}
	
	};
	private QuickAction mQA;
	

	private void showStyleQuickAction(View v) {
		
		showQuickAction(v,styleIcons,styleClick);
	}
	
	private void showSizeQuickAction(View v) {
		showQuickAction(v,sizeIcons,sizeClick);
		
		
	}
	
	private void showElementQuickAction(View v){
		showQuickAction(v,elementIcons,elementClick);
	}
	
	private boolean imeShown=false;
	
	private void showQuickAction(View v, int[] what, OnClickListener listener){
		
		imeShown=IME.isActive(mPost);
		mQA= new QuickAction(v);
		boolean pressed=false;
		boolean styleSelected=false;
		for (int i=0;i<what.length;i++) {
			if (what[i]==R.drawable.btn_medium) {
				pressed=mCurrentSize==what[i]?true:false;
			}
			else pressed=mPost.isStyleSet(buttonMap.get(what[i]));
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
					
					mPost.toggleStyle(Typeface.BOLD);
					break;
				case R.drawable.italicbutton:
					mPost.toggleStyle(Typeface.ITALIC);
					break;
				case R.drawable.stylebutton:

					showStyleQuickAction(v);
					break;
				case R.drawable.sizebutton:
					showSizeQuickAction(v);
					break;
				case R.drawable.colorbutton:
					AmbilWarnaDialog colorchooser=new AmbilWarnaDialog(NewPost.this,mPost.getColor(),mPost.getBackgroundColor(),NewPost.this);
					colorchooser.show();
					break;
				case R.drawable.elementbutton:
					showElementQuickAction(v);
					break;
					
					
				
					
					
			
			}
			
		}

		
	

		};

	
		
		@Override
		protected void onActivityResult (int requestCode, int resultCode, Intent data) {
			if (resultCode==RESULT_OK) {
			switch(requestCode) {
				case SELECT_PHOTO:
				
			            Uri selectedImageUri = data.getData();
			            Cursor cursor = managedQuery(Uri.parse(data.getDataString()), null, 
                                null, null, null); 
			            cursor.moveToNext(); 
			            	// Retrieve the path and the mime type 
			            	String path = cursor.getString(cursor .getColumnIndex(MediaStore.MediaColumns.DATA)); 
			            	String mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)); 

		                //OI FILE Manager
		                //String filemanagerstring = selectedImageUri.getPath();

		                //MEDIA GALLERY
		                //String selectedImagePath = getPath(selectedImageUri);
		                  Intent uploadPhoto=new Intent(this,UploadPhoto.class);
				            uploadPhoto.putExtra("file",path);

		               // uploadPhoto.putExtra("file", selectedImagePath==null?filemanagerstring:selectedImagePath);
		                  uploadPhoto.putExtra("mime", mimeType);
		                  uploadPhoto.putExtra("journalname", mJournalname);
		                startActivityForResult(uploadPhoto,UPLOAD_PHOTO);
					break;
					
				case TAKE_PHOTO:
					break;
				case UPLOAD_PHOTO:
					String provider=data.getStringExtra("provider");
					String file=data.getStringExtra("file");
					String title=data.getStringExtra("title");
					createProgressNotify(file,title,provider);
					insertImage(data);
					
					break;
			}
			}
		}
		
		HashMap<String,Notification> notifyMap=new HashMap<String,Notification>();
		HashMap<String,Integer> notifyIdMap=new HashMap<String,Integer> ();
		private void insertImage(Intent data) {
			String filepath=data.getStringExtra("file");
			int progress=data.getIntExtra("size",0);
			int dSize=0;
			if (progress<(100/6))  dSize=75;
			else if (progress>=100/6&&progress<200/6)  dSize=100;
			else if (progress>=200/6&&progress<300/6)  dSize=240;
			else if (progress>=200/6&&progress<400/6)  dSize=500;
			else if (progress>=400/6&&progress<500/6)  dSize=1024;
			else if (progress>=500/6&&progress<600/6)  dSize=-1;
			
			mPost.insertImageStub(filepath, dSize);
			
		}
		private int UPLOAD_NOTIFY=1;
		
		
		private void createProgressNotify(String filepath,String title, String provider) {
	        // configure the notification
			
			int icon=0;
			if (provider=="ScrapBook") {
				icon=R.drawable.scrapbook;	
			}
			else if (provider=="Picasa") {
				icon=R.drawable.picasa;
			}
			else if (provider=="Flickr") {
				icon=R.drawable.flickr;
			}
			else if (provider=="PhotoBucket") {
				icon=R.drawable.photobucket;
			}
			 Intent intent = new Intent();
		        final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
	        Notification uploadNotification = new Notification(R.drawable.uploadicon, "Uploading Photo", System.currentTimeMillis());
	        notifyMap.put(filepath, uploadNotification);
	        UPLOAD_NOTIFY++;
	        notifyIdMap.put(filepath, UPLOAD_NOTIFY);
	        uploadNotification.flags = uploadNotification.flags | Notification.FLAG_ONGOING_EVENT|Notification.FLAG_AUTO_CANCEL;
	        uploadNotification.contentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.uploadprogress);
	        uploadNotification.contentIntent = pendingIntent;
	       // uploadNotification.contentView.setImageViewResource(R.id.status_icon, icon);
	        uploadNotification.contentView.setTextViewText(R.id.status_text, "Uploading Photo to "+provider);
	        uploadNotification.contentView.setTextViewText(R.id.title, title);
	        uploadNotification.contentView.setProgressBar(R.id.status_progress, 100, 0, false);
	        final NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(
	                Context.NOTIFICATION_SERVICE);

	        notificationManager.notify(UPLOAD_NOTIFY, uploadNotification);
		}
		
		private String getPath(Uri uri) {
		        String[] projection = { MediaStore.Images.Media.DATA };
		        Cursor cursor = managedQuery(uri, projection, null, null, null);
		        if(cursor!=null)
		        {
		            //HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
		            //THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
		            int column_index = cursor
		            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		            cursor.moveToFirst();
		            return cursor.getString(column_index);
		        }
		        else return null;
		    }
		

	private void addPost() {
		
	}
	
	private void loadDrafts() {
		
	}
		

	public void onCancel() {
		mPost.removeStyle(RichEditText.BACKGROUND_COLOR);
		mPost.removeStyle(RichEditText.TEXT_COLOR);
		mPost.setBackgroundColor(0xFFFFFFFF);
		mPost.setColor(0xFF000000);
		
	}



	public void onOk(int bgColor,int fgColor) {
		if(fgColor!=0xFF000000) {
			mPost.setColor(fgColor);
			mPost.toggleStyle(RichEditText.TEXT_COLOR);
		}
		if (bgColor!=0xFFFFFFFF) {
		mPost.setBackgroundColor(bgColor);
		mPost.toggleStyle(RichEditText.BACKGROUND_COLOR);
		}
		
	}



	public void onHaveAddress(String location) {
		mLocation.setText(location);
		
	}



	public void onNewLocation(Location location) {
		StringBuilder coords=new StringBuilder();
		coords.append(((Double)location.getLatitude()).toString());
		coords.append(",");
		coords.append(((Double)location.getLongitude()).toString());
		mCoords.setText(coords.toString());
		
	}
	
	public  BroadcastReceiver NewPostReceiver=new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			
			if (action.equals(LJNet.LJ_TAGSUPDATED)) {
				Toast.makeText(NewPost.this, "Tags updated", Toast.LENGTH_SHORT).show();
				
			}
			if (action.equals(LJNet.LJ_XMLERROR)) {
				Toast.makeText(NewPost.this, "Error fetching tags", Toast.LENGTH_SHORT).show();
				
			}
			
			
		}
		
	};
	
	@Override
		protected void onPause() {
		super.onPause();
		if(mUseLoc.isChecked()){
			mFindMe.cancel();
		}
		
	}
	
	@Override
	protected void onResume() {
	super.onResume();
	if(mUseLoc.isChecked()){
		mFindMe.init();
	}
	
	
	
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		unregisterReceiver(mPhotoReceiver);
	}



	public void onError(String filename, String title,String error) {
		// TODO Auto-generated method stub
		
	}



	public void onUpdate(String filename,int percent) {
		
		final NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(
                Context.NOTIFICATION_SERVICE);
		int id=notifyIdMap.get(filename);
		Notification uploadNotification=notifyMap.get(filename);
		if (percent<100) {
		
		
		 uploadNotification.contentView.setProgressBar(R.id.status_progress, 100, percent, false);
        
		}
		else {
			uploadNotification.contentView.setTextViewText(R.id.status_text, "Image Uploaded");
		}
		notificationManager.notify(id,uploadNotification);
		
	}



	public void onCompleted(String filepath,String link,String title, String src) {
		mPost.setImageDetails(filepath,link,src,title);
		notifyMap.remove(filepath);
		int id=notifyIdMap.get(filepath);
		NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(id);
		int icon=R.drawable.doneicon;
			CharSequence tickerText = "Finished Uploading";
	 		long when = System.currentTimeMillis();
	 		Notification notification = new Notification(icon, tickerText, when);
	 		notification.flags |= Notification.FLAG_AUTO_CANCEL;
	 		CharSequence contentTitle ="Finished Uploading";
	 		CharSequence contentText = title;
	 		Intent notificationIntent = new Intent();
	 		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,0);
	 		notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
	 		notificationManager.notify(id, notification);
		
		
	}
	
	
	

}
