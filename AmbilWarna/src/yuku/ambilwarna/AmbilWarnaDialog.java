package yuku.ambilwarna;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;


public class AmbilWarnaDialog {
	private static final String TAG = AmbilWarnaDialog.class.getSimpleName();
	
	public interface OnAmbilWarnaListener {
		void onCancel();
		void onOk(int bgColor,int fgColor);
	}
	
	Dialog dialog;
	OnAmbilWarnaListener listener;
	View viewHue;
	AmbilWarnaKotak viewKotak;
	ImageView panah;
	TextView preview;
	private int mTextColor=0xFF000000;
	private int mBgColor=0xFFFFFFFF;
	RadioButton bgSelector;
	RadioButton fgSelector;
	ImageView viewKeker;
	
	float satudp;
	int warnaLama;
	int warnaBaru;
	float hue;
	float sat;
	float val;
	float ukuranUiDp = 230.f;
	float ukuranUiPx; // diset di constructor
	private SpannableStringBuilder previewSB;
	
	public AmbilWarnaDialog(Context context, int color,int bgcolor, final OnAmbilWarnaListener listener) {
		this.listener = listener;
		this.warnaLama = color;
		this.warnaBaru = color;
		Color.colorToHSV(color, tmp01);
		hue = tmp01[0];
		sat = tmp01[1];
		val = tmp01[2];
		
		satudp = context.getResources().getDimension(R.dimen.ambilwarna_satudp);
		
		
		Log.d(TAG, "satudp = " + satudp + ", ukuranUiPx=" + ukuranUiPx);  //$NON-NLS-1$//$NON-NLS-2$
		
		View view = LayoutInflater.from(context).inflate(R.layout.ambilwarna_dialog, null);
		ukuranUiPx = ukuranUiDp * satudp;
		ukuranUiDp=view.findViewById(R.id.ambilwarna_viewKotak).getHeight();
		viewHue = view.findViewById(R.id.ambilwarna_viewHue);
		viewKotak = (AmbilWarnaKotak) view.findViewById(R.id.ambilwarna_viewKotak);
		panah = (ImageView) view.findViewById(R.id.ambilwarna_panah);
		preview = (TextView) view.findViewById(R.id.preview);
		bgSelector=(RadioButton) view.findViewById(R.id.bgcolor);
		fgSelector=(RadioButton) view.findViewById(R.id.fgcolor);
		((Button) view.findViewById(R.id.setcolor)).setOnClickListener(new OnClickListener() {

			
			public void onClick(View v) {
				listener.onOk(mBgColor,mTextColor);
				dialog.dismiss();
			}
			
		});
		((Button) view.findViewById(R.id.cancel)).setOnClickListener(new OnClickListener() {

	
			public void onClick(View v) {
				listener.onCancel();
				dialog.dismiss();
			}
			
		});
		RadioGroup rgroup=(RadioGroup) view.findViewById(R.id.rgroup);
		rgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
		
			public void onCheckedChanged(RadioGroup group, int checkedId) {
			updatePreview();
				
			}
		});
		viewKeker = (ImageView) view.findViewById(R.id.ambilwarna_keker);
		previewSB=new SpannableStringBuilder();
		previewSB.append(preview.getText());
		previewSB.setSpan(new BackgroundColorSpan(bgcolor),0,previewSB.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		previewSB.setSpan(new ForegroundColorSpan(color),0,previewSB.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		preview.setText(previewSB);
		letakkanPanah();
		letakkanKeker();
		viewKotak.setHue(hue);


		viewHue.setOnTouchListener(new View.OnTouchListener() {
		
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE 
						|| event.getAction() == MotionEvent.ACTION_DOWN
						|| event.getAction() == MotionEvent.ACTION_UP) {
					
					float y = event.getY(); // dalam px, bukan dp
					if (y < 0.f) y = 0.f;
					if (y > ukuranUiPx) y = ukuranUiPx - 0.001f;
					
					hue = 360.f - 360.f / ukuranUiPx * y;
					if (hue == 360.f) hue = 0.f;
					
					warnaBaru = hitungWarna();
					// update view
					viewKotak.setHue(hue);
					letakkanPanah();
					updatePreview();
					
					return true;
				}
				return false;
			}
		});
		viewKotak.setOnTouchListener(new View.OnTouchListener() {
		
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE 
						|| event.getAction() == MotionEvent.ACTION_DOWN
						|| event.getAction() == MotionEvent.ACTION_UP) {
					
					float x = event.getX(); // dalam px, bukan dp
					float y = event.getY(); // dalam px, bukan dp
					
					if (x < 0.f) x = 0.f;
					if (x > ukuranUiPx) x = ukuranUiPx;
					if (y < 0.f) y = 0.f;
					if (y > ukuranUiPx) y = ukuranUiPx;

					sat = (1.f / ukuranUiPx * x);
					val = 1.f - (1.f / ukuranUiPx * y);

					warnaBaru = hitungWarna();
					// update view
					letakkanKeker();
					updatePreview();
					return true;
				}
				return false;
			}
		});
		
		dialog = new Dialog(context);
		dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
		dialog.setContentView(view);
		
	}
	
	private AlertDialog Dialog(AmbilWarnaDialog ambilWarnaDialog) {
		// TODO Auto-generated method stub
		return null;
	}

	private void updatePreview() {
		if(fgSelector.isChecked()) {
			previewSB.removeSpan(previewSB.getSpans(0,previewSB.length(),ForegroundColorSpan.class)[0]);
			previewSB.setSpan(new ForegroundColorSpan(warnaBaru), 0,previewSB.length() ,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			mTextColor=warnaBaru;
		}
		
		else if(bgSelector.isChecked()){
			previewSB.removeSpan(previewSB.getSpans(0,previewSB.length(),BackgroundColorSpan.class)[0]);
			previewSB.setSpan(new BackgroundColorSpan(warnaBaru), 0,previewSB.length() ,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			mBgColor=warnaBaru;
		}
		preview.setText(previewSB);
		
	}
	
	@SuppressWarnings("deprecation")
	protected void letakkanPanah() {
		float y = ukuranUiPx - (hue * ukuranUiPx / 360.f);
		if (y == ukuranUiPx) y = 0.f;
		
		AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) panah.getLayoutParams();
		layoutParams.y = (int) (y + 4);
		panah.setLayoutParams(layoutParams);
	}
	
	

	@SuppressWarnings("deprecation")
	protected void letakkanKeker() {
		float x = sat * ukuranUiPx;
		float y = (1.f - val) * ukuranUiPx;
		
		AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) viewKeker.getLayoutParams();
		layoutParams.x = (int) (x + 3);
		layoutParams.y = (int) (y + 3);
		viewKeker.setLayoutParams(layoutParams);
	}

	float[] tmp01 = new float[3];
	private int hitungWarna() {
		tmp01[0] = hue;
		tmp01[1] = sat;
		tmp01[2] = val;
		return Color.HSVToColor(tmp01);
	}

	public void show() {
		dialog.show();
	}
}
