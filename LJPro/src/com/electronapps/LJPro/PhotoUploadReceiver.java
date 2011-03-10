package com.electronapps.LJPro;

import com.electronapps.LJPro.FileEntityMonitored;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.ProgressBar;

public class PhotoUploadReceiver extends BroadcastReceiver {


	private UploadStatus mCallback;
	
	public interface UploadStatus {
		public void onUpdate(String filename,int percent);
		public void onError(String filename,String title, String error);
		public void onCompleted(String filename,String link, String title,String src);
	}

	public PhotoUploadReceiver(UploadStatus callback) {
		mCallback=callback;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action=intent.getAction();
		if (action.equals(PhotoAPIBase.UPLOAD_PROGRESS_UPDATE)) {
			String file=intent.getStringExtra("file");
			int percent=intent.getIntExtra("percent",0);
			mCallback.onUpdate(file,percent);
		}
		else if (action.equals(PhotoAPIBase.UPLOAD_ERROR)) {
			String file=intent.getStringExtra("file");
			String title=intent.getStringExtra("title");
			String error=intent.getStringExtra("error");
			mCallback.onError(file,title,error);
		}
		
		else if(action.equals(PhotoAPIBase.UPLOAD_COMPLETED)) {
			String file=intent.getStringExtra("file");
			String title=intent.getStringExtra("title");
			String link=intent.getStringExtra("link");
			String source=intent.getStringExtra("source");
			mCallback.onCompleted(file,link,title,source);
		}
		
	}

}
