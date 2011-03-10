package com.electronapps.LJPro;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class FriendsTab extends TabActivity {
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.friendtabs);
	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent=getIntent();  // Reusable Intent for each tab
	    String journalname=intent.getStringExtra("journalname");
	    Integer tab=intent.getIntExtra("tab",0);
	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, EditFriends.class);
	    intent.putExtra("journalname", journalname);
	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("friends").setIndicator("Friends",
	                      res.getDrawable(R.drawable.friends_tab))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, EditFriendGroups.class);
	    intent.putExtra("journalname", journalname);
	    spec = tabHost.newTabSpec("friendgroups").setIndicator("Friend Groups",
	                      res.getDrawable(R.drawable.friendgroups_tab))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	  

	    tabHost.setCurrentTab(tab);
	}

}
