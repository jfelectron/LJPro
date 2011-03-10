package com.electronapps.LJPro;

import android.database.Cursor;
import android.database.MergeCursor;

public class PostCommentsCursor extends MergeCursor {

	private PostWrapper post;
	private Cursor comments;
	Cursor[] mCursors;

	public PostCommentsCursor(Cursor[] cursors) {
		super(cursors);
		mCursors=cursors;
		post=(PostWrapper)cursors[0];
		comments=cursors[1];
	}
	
	public void changeIndex(int index) {
		post.setIndex(index);
	}
	
	public void changeComents(Cursor c) {
		mCursors[1].close();
		mCursors[1]=c;
		
	}
	
	@Override 
	public boolean requery() {
		return comments.requery();
	}

}
