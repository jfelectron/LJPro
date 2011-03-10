package com.electronapps.LJPro;

import android.app.Activity;
import android.content.Intent;



import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

	public class Navigation extends Activity {
		 
		  String journalname;
		  @Override
		  public void onCreate(Bundle savedInstanceState) {
			    super.onCreate(savedInstanceState);
			    setContentView(R.layout.navigation);
			    Intent intent=getIntent();
			    journalname=intent.getStringExtra("journalname");
			    TextView header=(TextView) findViewById(R.id.journal_name);
			    header.setText(journalname);
			    ImageButton friendspage=(ImageButton) findViewById(R.id.friends_button);
			    friendspage.setOnClickListener(new OnClickListener(){

					public void onClick(View v) {
						Intent fp=new Intent(getApplicationContext(),FriendsPage.class);
						fp.putExtra("journalname",journalname);
						startActivity(fp);
					}
			    	
			    });
			    ImageButton newpost=(ImageButton) findViewById(R.id.write_button);
			    newpost.setOnClickListener(new OnClickListener(){

					public void onClick(View v) {
						Intent np=new Intent(getApplicationContext(),NewPost.class);
						np.putExtra("journalname",journalname);
						startActivity(np);
					}
			    	
			    });
			    
			    
			}
	}

