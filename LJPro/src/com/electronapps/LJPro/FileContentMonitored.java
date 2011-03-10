package com.electronapps.LJPro;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.electronapps.LJPro.FileEntityMonitored.OutputStreamMonitored;
import com.google.api.client.http.HttpContent;

public class FileContentMonitored implements HttpContent {

	private final static int BUFFER_SIZE = 2048;
	  public String type;

	  /** Content length or less than zero if not known. Defaults to {@code -1}. */
	  public long length = -1;

	  /** Required input stream to read from. */
	  public InputStream inputStream;

	  /**
	   * Content encoding (for example {@code "gzip"}) or {@code null} for none.
	   */
	  public String encoding;
	  
	public String getEncoding() {
		// TODO Auto-generated method stub
		return encoding;
		
		  
	}

	public long getLength() throws IOException {
		// TODO Auto-generated method stub
		return length;
	}

	public String getType() {
		// TODO Auto-generated method stub
		return type;
	}

	public class OutputStreamMonitored extends FilterOutputStream {

		public OutputStreamMonitored(OutputStream out, long length) {
			super(out);
			
			m_out = out;
			m_length = length;
			m_broadcast_trigger = Math.round((double)m_length / 100.0);
			BroadcastPercentUploaded();
		}

		public void write(byte[] b, int off, int len) throws IOException {
			m_out.write(b, off, len);
			m_bytes_transferred += len;
			
			// We don't want to send a broadcast every time data is written,
			// so only do it when the amount written since the last broadcast
			// is at least 1% of the total size.
			if (m_broadcast_count < m_broadcast_trigger) {
				m_broadcast_count += len;
			}
			else {
				m_broadcast_intent.putExtra("percent", PercentUploaded());
				//Log.d("PhotoUPLOAD","percent: "+PercentUploaded());
				m_broadcast_intent.putExtra("title", m_title);
				if (m_context != null) {
					m_context.sendBroadcast(m_broadcast_intent);
				}
				m_broadcast_count = 0;
			}
		}

		public void write(int b) throws IOException {
			m_out.write(b);
			m_bytes_transferred += 1;

			// We don't want to send a broadcast every time data is written,
			// so only do it when the amount written since the last broadcast
			// is at least 1% of the total size.
			if (m_broadcast_count < m_broadcast_trigger) {
				m_broadcast_count += 1;
			}
			else {
				m_broadcast_intent.putExtra("percent", PercentUploaded());
				//Log.d("PhotoUPLOAD","percent: "+PercentUploaded());
				m_broadcast_intent.putExtra("title", m_title);
				if (m_context != null) {
					m_context.sendBroadcast(m_broadcast_intent);
				}
				m_broadcast_count = 0;
			}
		}
		
		private void BroadcastPercentUploaded() {
			if (m_broadcast_intent == null) {
				m_broadcast_intent = new Intent();
				m_broadcast_intent.setAction(PhotoAPIBase.UPLOAD_PROGRESS_UPDATE);
				m_broadcast_intent.putExtra("title", m_title);
				m_broadcast_intent.putExtra("file", m_file);
			}
			m_broadcast_intent.putExtra("percent", PercentUploaded());
			//Log.d("PhotoUPLOAD","percent: "+PercentUploaded());
			
			if (m_context != null) {
				m_context.sendBroadcast(m_broadcast_intent);
			}
			m_broadcast_count = 0;
		}
		
		private int PercentUploaded() {
			return (int)Math.round(100.0 * (double)m_bytes_transferred / (double)m_length);
		}
		
		private long m_length = 0;
		private long m_bytes_transferred = 0;
		private long m_broadcast_count = 0;
		private long m_broadcast_trigger = 0;
		private OutputStream m_out = null;
		
	}

	public FileContentMonitored(Context context, File file, String title,String ctype) {
		length=file.length();
		try {
			inputStream=new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		type=ctype;
		m_context = context;
		m_title = title;
		m_file=file.getPath();
		
		
	}

	

	

	
	public void writeTo(OutputStream outstream) throws IOException {
		   InputStream inputStream = this.inputStream;
		Log.d("UPLOAD", "Uploading data");
		if (m_outputstream == null) {
			m_outputstream = new OutputStreamMonitored(outstream, getLength());
		}
		byte[] buffer = new byte[BUFFER_SIZE];
	      try {
	        // consume no more than length
	        long remaining = getLength();
	        while (remaining > 0) {
	          int read = inputStream.read(buffer, 0, (int) Math.min(BUFFER_SIZE, remaining));
	          if (read == -1) {
	            break;
	          }
	          m_outputstream.write(buffer, 0, read);
	          remaining -= read;
	        }
	      } finally {
	        inputStream.close();
	      }
		
	}
	
	private OutputStreamMonitored m_outputstream = null;
	private Intent m_broadcast_intent = null;
	private Context m_context = null;
	private String m_title = null;
	private String m_file=null;
	

}
