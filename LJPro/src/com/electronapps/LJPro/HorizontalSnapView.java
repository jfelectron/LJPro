package com.electronapps.LJPro;


import java.util.ArrayList;
import java.util.List;




import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.os.IInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.MeasureSpec;

import android.widget.AbsListView;

import android.widget.AdapterView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;


import android.widget.AbsListView.RecyclerListener;



public class HorizontalSnapView extends HorizontalScrollView {
    private static final int SWIPE_MIN_DISTANCE = 5;
    private static final int SWIPE_THRESHOLD_VELOCITY = 300;
    static final int SYNC_MAX_DURATION_MILLIS = 100;
 
    private GestureDetector mGestureDetector;
    private int mActiveFeature = 0;
    private Context mContext;

    ContainerLayout mContainer;
    ListAdapter mAdapter;
    SelectionObserver mObserver;
 
    public interface SelectionObserver {
    	void onSelectionChanged(int position);
    }
    
    private final static String TAG=HorizontalSnapView.class.getSimpleName();
    
    public HorizontalSnapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext=context;
        setHorizontalScrollBarEnabled(false);
        setupContainer();
    }
 
    public HorizontalSnapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
        setHorizontalScrollBarEnabled(false);
      
        setupContainer();
    }
 
    public HorizontalSnapView(Context context) {
        super(context);
        mContext=context;
        setHorizontalScrollBarEnabled(false);
        setupContainer();
       
        
    }
    
    protected class ContainerLayout extends LinearLayout {

		public ContainerLayout(Context context) {
			super(context);
			setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			setOrientation(LinearLayout.HORIZONTAL);
		}
		
		public void detachAllViews() {
			super.detachAllViewsFromParent();
		}
		
		public void detachView(int index) {
			super.detachViewFromParent(index);
		}
		
		public void detachView (View view) {
			super.detachViewFromParent(view);
		}
		
		public void removeView(View view) {
			super.removeViewInLayout(view);
		}
		
		public void addView(View view,int index, ViewGroup.LayoutParams params) {
			super.addViewInLayout(view, index, params);
		}
		
		public void removeDetachedView(View view, Boolean animate) {
			super.removeDetachedView(view, animate);
		}
		
		public void attachView(View child, int index, ViewGroup.LayoutParams params) {
			super.attachViewToParent(child, index, params);
		}
		
		
		
    	
    }
    
    public void registerSelectionObserver(SelectionObserver observer) {
    	mObserver=observer;
    }
    
    public void setupContainer() {
    	 mContainer= new ContainerLayout(mContext);
         addView(mContainer);
         
		   setOnTouchListener(new View.OnTouchListener() {
		         
	            public boolean onTouch(View v, MotionEvent event) {
	                //If the user swipes
	                if (mGestureDetector.onTouchEvent(event)) {
	                    return true;
	                }
	                else if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL ){
	                    computeScroll();
	                	int scrollX = getScrollX();
	                    int featureWidth = v.getMeasuredWidth();
	                    int offset=0;
	                    double shift=(scrollX-(mSelectedPosition-mFirstPosition)*featureWidth)/(featureWidth/2.0d);
	                    if (shift>1.0)
	                    {	
	                    	offset=1; 
	                    	}
	                    if (shift<-1.0) {
	                    	offset=-1;
	                    }
	                    final int selPos=mSelectedPosition;
	                    final int firstPos=mFirstPosition;
	                   
	                    mActiveFeature=selPos-firstPos;
	                    //snap back to current view if we have met offset criteria
	                    
	                    if (offset==0) smoothScrollTo(mContainer.getChildAt(mActiveFeature).getLeft(),0);
	                 
	                    else scrollListItemsBy(offset);
	                    
	                    return true;
	                }
	                else{
	                    return false;
	                }
	            }
	        });
		   mGestureDetector = new GestureDetector(new MyGestureDetector());
        
    	
    }
 
   
     
       
    
        class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            super.onFling(e1,e1,velocityX,velocityY);
            
        	try {
        		  if(Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY&&Math.abs(velocityX)>Math.abs(velocityY)) {  //right to left
                if(velocityX<0) {
                    
                    	scrollListItemsBy(1);
                    
                    return true;
                }
                //left to right
                else if (velocityX>0) {
                    
                    	scrollListItemsBy(-1);
                    
                    return true;
                }
        		  }
            } catch (Exception e) {
                    Log.e(TAG, "There was an error processing the Fling event:" + e.getMessage());
            }
            return false;
       
        }
        
    }
     
        
        boolean mDataChanged=false;
        private int mLayoutMode=LAYOUT_NORMAL;
        private int mFirstPosition=0;
       
		private boolean mBlockLayoutRequests;
		AdapterDataSetObserver mDataSetObserver;
		private boolean mAreAllItemsSelectable;
		private int mOldItemCount;
		private int mItemCount;
        private boolean mNeedSync;
		 private int mSelectedPosition;
        private long mSelectedRowId;
        private long mSyncRowId;
        private int mSyncPosition;
        private int mWindowSize=3;
        
		static final int LAYOUT_SET_SELECTION = 2;
		static final int LAYOUT_SYNC = 3;
		static final int LAYOUT_NORMAL=1;
		
		 private final static int SYNC_SELECTED_POSITION=1;
         private final static int SYNC_FIRST_POSITION=2;
        
        protected void layoutChildren() {
        	 final boolean blockLayoutRequests = mBlockLayoutRequests;
             if (!blockLayoutRequests) {
                 mBlockLayoutRequests = true;
             } else {
                 return;
             }

             try {

                 invalidate();

                 if (mAdapter == null) {
                     resetList();
                     return;
                 }

                

                 int childCount = mContainer.getChildCount();
                 int index;
                

                 View sel;
                 
                 int newSel = AdapterView.INVALID_POSITION;

                 View focusLayoutRestoreView = null;

               
                   


                 


                 boolean dataChanged = mDataChanged;
                 if (dataChanged) {
                    handleDataChanged();
                 }

                 // Handle the empty set by removing all views that are visible
                 // and calling it a day
                 if (mItemCount == 0) {
                     resetList();
                     return;
                 } else if (mItemCount != mAdapter.getCount()) {
                     throw new IllegalStateException("The content of the adapter has changed but "
                             + "ListView did not receive a notification. Make sure the content of "
                             + "your adapter is not modified from a background thread, but only "
                             + "from the UI thread. [in ListView(" + getId() + ", " + getClass() 
                             + ") with Adapter(" + mAdapter.getClass() + ")]");
                 }

                 

                 // Pull all children into the RecycleBin.
                 // These views will be reused if possible
                 final int firstPosition =mFirstPosition;
                 final RecycleBin recycleBin = mRecycler;

                 // reset the focus restoration
                 View focusLayoutRestoreDirectChild = null;


                 // Don't put header or footer views into the Recycler. Those are
                 // already cached in mHeaderViews;
                 if (dataChanged) {
                     for (int i = 0; i < childCount; i++) {
                         recycleBin.addScrapView(getChildAt(i));
                         if (ViewDebug.TRACE_RECYCLER) {
                             ViewDebug.trace(getChildAt(i),
                                     ViewDebug.RecyclerTraceType.MOVE_TO_SCRAP_HEAP, index, i);
                         }
                     }
                 } else {
                     recycleBin.fillActiveViews(childCount, firstPosition);
                 }

                 // take focus back to us temporarily to avoid the eventual
                 // call to clear focus when removing the focused child below
                 // from messing things up when ViewRoot assigns focus back
                 // to someone else
                 final View focusedChild = mContainer.getFocusedChild();
                 if (focusedChild != null) {
                     // TODO: in some cases focusedChild.getParent() == null

                     // we can remember the focused view to restore after relayout if the
                     // data hasn't changed, or if the focused position is a header or footer
                     if (!dataChanged) {
                         focusLayoutRestoreDirectChild = focusedChild;
                         // remember the specific view that had focus
                         focusLayoutRestoreView = mContainer.findFocus();
                         if (focusLayoutRestoreView != null) {
                             // tell it we are going to mess with it
                             focusLayoutRestoreView.onStartTemporaryDetach();
                         }
                     }
                     mContainer.requestFocus();
                 }

                 // Clear out old views
                 //remremoveAllViewsInLayout();
                 try{
                  mContainer.detachAllViews();
                 }
                 catch(Throwable e) {
                	 Log.e(TAG,e.getMessage(),e);
                 }
              
              
                

                 switch (mLayoutMode) {
                 case LAYOUT_SET_SELECTION:
                     if (newSel !=AdapterView.INVALID_POSITION) {
                         sel = fillSpecific(newSel);
                     } else {
                         sel = fillSpecific(mSelectedPosition);
                     }
                     break;
                 case LAYOUT_SYNC:
                     sel = fillSpecific(mSyncPosition);
                     break;
           
                 default:
                	 if (childCount == 0) {

                             sel = fillFromFirst();
                     } else {
                         if (mSelectedPosition >= 0 && mSelectedPosition < mItemCount) {
                             sel = fillSpecific(mSelectedPosition);
              
                         } else if (mFirstPosition < mItemCount) {
                             sel = fillSpecific(mFirstPosition);
                                     
                         } else {
                             sel = fillSpecific(0);
                         }
                     }
                     break;
                 }

                 // Flush any cached views that did not get reused above
                 recycleBin.scrapActiveViews();

                 if (sel != null) {
                     // the current selected item should get focus if items
                     // are focusable
                     if (mContainer.hasFocus() && !sel.hasFocus()) {
                         final boolean focusWasTaken = (sel == focusLayoutRestoreDirectChild &&
                                 focusLayoutRestoreView.requestFocus()) || sel.requestFocus();
                         if (!focusWasTaken) {
                             // selected item didn't take focus, fine, but still want
                             // to make sure something else outside of the selected view
                             // has focus
                             final View focused = getFocusedChild();
                             if (focused != null) {
                                 focused.clearFocus();
                             }
                        
                         } else {
                             sel.setSelected(false);
             
                         }
                     } 
                    
                 } else {
                   
                     // even if there is not selected position, we may need to restore
                     // focus (i.e. something focusable in touch mode)
                     if (mContainer.hasFocus() && focusLayoutRestoreView != null) {
                         focusLayoutRestoreView.requestFocus();
                     }
                 }

                 // tell focus view we are done mucking with it, if it is still in
                 // our view hierarchy.
                 if (focusLayoutRestoreView != null
                         && focusLayoutRestoreView.getWindowToken() != null) {
                     focusLayoutRestoreView.onFinishTemporaryDetach();
                 }
                 
                 setSelectedPositionInt(mSelectedPosition);
                 mLayoutMode = LAYOUT_NORMAL;
                 mDataChanged = false;
                 mNeedSync = false;
               //mContainer.scrollTo(sel.getLeft(),0);
               //scrollToChild(sel);
                
             } finally {
                 if (!blockLayoutRequests) {
                     mBlockLayoutRequests = false;
                 }
             }
        	
        	
        }
        
       
      public void setWindowSize(int size){
    	  mWindowSize=size;
      }
        
    
        

		private View fillFromFirst() {
			mFirstPosition=0;
			setSelectedPositionInt(0);
			View selectedView = null;

		      //Are we filling right from first or as part of fill from selected?
		      
		       int maxFill=mWindowSize;

		        for(int i=0;i<maxFill;i++){
		        	if(i>mItemCount) break;
		            // is this the selected item?
		            boolean selected = i == mSelectedPosition;
		            View child = makeAndAddView(i,FLOW_RIGHT,selected);

		            
		            if (selected) {
		                selectedView = child;
		            }
		          
		        }

		        return selectedView;
		}
		
		private View fillFromLast() {
			mFirstPosition=mItemCount-mWindowSize;
			mSelectedPosition=mItemCount-1;
			 View selectedView = null;

		      //Are we filling right from end or as part of fill from selected?
		      
		       int maxFill=mWindowSize;

		        for(int i=mItemCount-1;(mItemCount-i)<=maxFill;i--){
		        	if(i<0) break;
		            // is this the selected item?
		            boolean selected = i == mSelectedPosition;
		            View child = makeAndAddView(i,FLOW_LEFT,selected);

		            
		            if (selected) {
		                selectedView = child;
		            }
		          
		        }

		        return selectedView;
			
			
		}
		

		
		
		
		
		private final int FLOW_LEFT=0;
		private final int FLOW_RIGHT=1;
		private boolean mNeedLayout;
		
	
		   private View makeAndAddView(int position, int flow,boolean selected) {
		        View child = null;


		        if (!mDataChanged) {
		            // Try to use an exsiting view for this position
		            child = mRecycler.getActiveView(position);
		            if (child != null) {
		                if (ViewDebug.TRACE_RECYCLER) {
		                    ViewDebug.trace(child, ViewDebug.RecyclerTraceType.RECYCLE_FROM_ACTIVE_HEAP,
		                            position, mContainer.getChildCount());
		                }

		                // Found it -- we're using an existing child
		                // This just needs to be positioned
		                setupChild(child, position,flow,selected, true);

		                return child;
		            }
		        }

		        // Make a new view for this position, or convert an unused view if possible
		        try {
		        child = obtainView(position);
		        }
		        catch(Throwable e) {
		        	Log.e(TAG,e.getMessage(),e);
		        }
		        if (child!=null)
		        // This needs to be positioned and measured
		        {  setupChild(child, position,flow,selected, false);}

		        return child;
		    }
		   
		   private void setupChild(View child, int position,int flow,boolean selected, boolean recycled) {
		        
		       
		    
		        // Respect layout params that are already in the view. Otherwise make some up...
		        // noinspection unchecked
		        ViewGroup.LayoutParams p = (ViewGroup.LayoutParams) child.getLayoutParams();
		        final int numChildren=mContainer.getChildCount();
		        final boolean needToMeasure = !recycled || child.isLayoutRequested();
		        if (p == null) {
		            p = new ViewGroup.LayoutParams(mMeasuredWidth,mMeasuredHeight);
		        }

		        if (recycled) {
		        	
		        	try {
		        		mContainer.attachView(child,flow==FLOW_RIGHT?-1:0, p);
		        		//TODO: Do we need to measure children here?
					} catch(Throwable e) {
						Log.e(TAG,e.getMessage(),e);
					}
		        } else {
		        	mContainer.addView(child,flow==FLOW_RIGHT?-1:0, p);
		        }
		        
		        if (needToMeasure) {
		        	 int childHeightSpec;
		        	 int childWidthSpec;
		        	 
		        	 //childHeightSpec=ViewGroup.getChildMeasureSpec(MeasureSpec.EXACTLY,this.getHorizontalFadingEdgeLength(),mMeasuredHeight);
		        	childHeightSpec = MeasureSpec.makeMeasureSpec(mMeasuredHeight,MeasureSpec.EXACTLY);
		        	 childWidthSpec=ViewGroup.getChildMeasureSpec(MeasureSpec.EXACTLY,this.getHorizontalFadingEdgeLength()*2,mMeasuredWidth);

		        	 //childWidthSpec = MeasureSpec.makeMeasureSpec(mMeasuredWidth,MeasureSpec.EXACTLY);

		       
		            child.measure(childWidthSpec, childHeightSpec);
		        } else {
		            cleanupLayoutState(child);
		        }
		        
		        final int w = child.getMeasuredWidth();
		     
		        final int childrenRight=numChildren*w;
		        final int h = child.getMeasuredHeight();
		        final int childRight =flow==FLOW_RIGHT?childrenRight+w:w;
		        final int childLeft =flow==FLOW_RIGHT?childrenRight:0;
		        if (needToMeasure) {
		           
		            child.layout(childLeft, h, childRight, 0);
		        } else {
		            child.offsetLeftAndRight(flow==FLOW_RIGHT?childrenRight - child.getLeft():-child.getLeft());
		           
		        }

		        
		        
		        

		    }
		   
		   private void offsetChildrenLeftAndRight(int offset) {
		        for (int i = mContainer.getChildCount() - 1; i >= 0; i--) {
		            mContainer.getChildAt(i).offsetLeftAndRight(offset);
		        }
		    }

		private void handleDataChanged() {
			  int count = mItemCount;
		        if (count > 0) {

		            int newPos;

		            int selectablePos;

		            // Find the row we are supposed to sync to
		            if (mNeedSync) {
		                // Update this first, since setNextSelectedPositionInt inspects it
		                mNeedSync = false;

		              

		                switch (mSyncMode) {
		                case SYNC_SELECTED_POSITION:
		                    if (isInTouchMode()) {
		                        // We saved our state when not in touch mode. (We know this because
		                        // mSyncMode is SYNC_SELECTED_POSITION.) Now we are trying to
		                        // restore in touch mode. Just leave mSyncPosition as it is (possibly
		                        // adjusting if the available range changed) and return.
		                        mLayoutMode = LAYOUT_SYNC;
		                        mSyncPosition = Math.min(Math.max(0, mSyncPosition), count - 1);

		                        return;
		                    } else {
		                        // See if we can find a position in the new data with the same
		                        // id as the old selection. This will change mSyncPosition.
		                        newPos = findSyncPosition();
		                        if (newPos >= 0) {
		                            // Found it. Now verify that new selection is still selectable
		                            selectablePos =lookForSelectablePosition(newPos, true);
		                            if (selectablePos == newPos) {
		                                // Same row id is selected
		                                mSyncPosition = newPos;

		                               

		                                // Restore selection
		                                setSelectedPositionInt(newPos);
		                                return;
		                            }
		                        }
		                    }
		                    break;
		                case SYNC_FIRST_POSITION:
		                    // Leave mSyncPosition as it is -- just pin to available range
		                    mLayoutMode = LAYOUT_SYNC;
		                    mSyncPosition = Math.min(Math.max(0, mSyncPosition), count - 1);

		                    return;
		                }
		            }

		            if (!isInTouchMode()) {
		                // We couldn't find matching data -- try to use the same position
		                newPos = getSelectedItemPosition();

		                // Pin position to the available range
		                if (newPos >= count) {
		                    newPos = count - 1;
		                }
		                if (newPos < 0) {
		                    newPos = 0;
		                }

		                // Make sure we select something selectable -- first look down
		                selectablePos =lookForSelectablePosition(newPos, true);

		                if (selectablePos >= 0) {
		                    setSelectedPositionInt(selectablePos);
		                    return;
		                } else {
		                    // Looking down didn't work -- try looking up
		                    selectablePos = lookForSelectablePosition(newPos, false);
		                    if (selectablePos >= 0) {
		                        setSelectedPositionInt(selectablePos);
		                        return;
		                    }
		                }
		            }
		            }
		         
			
		}
		        
		        int lookForSelectablePosition(int position, boolean lookDown) {
		            final ListAdapter adapter = mAdapter;
		            if (adapter == null || isInTouchMode()) {
		                return AdapterView.INVALID_POSITION;
		            }

		            final int count = adapter.getCount();
		            if (!mAreAllItemsSelectable) {
		                if (lookDown) {
		                    position = Math.max(0, position);
		                    while (position < count && !adapter.isEnabled(position)) {
		                        position++;
		                    }
		                } else {
		                    position = Math.min(position, count - 1);
		                    while (position >= 0 && !adapter.isEnabled(position)) {
		                        position--;
		                    }
		                }

		                if (position < 0 || position >= count) {
		                    return AdapterView.INVALID_POSITION;
		                }
		                return position;
		            } else {
		                if (position < 0 || position >= count) {
		                    return AdapterView.INVALID_POSITION;
		                }
		                return position;
		            }
		        }

		private int findSyncPosition() {
		            int count = mItemCount;

		            if (count == 0) {
		                return AdapterView.INVALID_POSITION;
		            }

		            long idToMatch = mSyncRowId;
		            int seed = mSyncPosition;

		            // If there isn't a selection don't hunt for it
		            if (idToMatch == AdapterView.INVALID_ROW_ID) {
		                return AdapterView.INVALID_POSITION;
		            }

		            // Pin seed to reasonable values
		            seed = Math.max(0, seed);
		            seed = Math.min(count - 1, seed);

		            long endTime = SystemClock.uptimeMillis() + SYNC_MAX_DURATION_MILLIS;

		            long rowId;

		            // first position scanned so far
		            int first = seed;

		            // last position scanned so far
		            int last = seed;

		            // True if we should move down on the next iteration
		            boolean next = false;

		            // True when we have looked at the first item in the data
		            boolean hitFirst;

		            // True when we have looked at the last item in the data
		            boolean hitLast;

		            // Get the item ID locally (instead of getItemIdAtPosition), so
		            // we need the adapter
		           ListAdapter adapter=mAdapter;
		            if (adapter == null) {
		                return AdapterView.INVALID_POSITION;
		            }

		            while (SystemClock.uptimeMillis() <= endTime) {
		                rowId = adapter.getItemId(seed);
		                if (rowId == idToMatch) {
		                    // Found it!
		                    return seed;
		                }

		                hitLast = last == count - 1;
		                hitFirst = first == 0;

		                if (hitLast && hitFirst) {
		                    // Looked at everything
		                    break;
		                }

		                if (hitFirst || (next && !hitLast)) {
		                    // Either we hit the top, or we are trying to move down
		                    last++;
		                    seed = last;
		                    // Try going up next time
		                    next = false;
		                } else if (hitLast || (!next && !hitFirst)) {
		                    // Either we hit the bottom, or we are trying to move up
		                    first--;
		                    seed = first;
		                    // Try going down next time
		                    next = true;
		                }

		            }

		            return AdapterView.INVALID_POSITION;
		        }

		private View fillSpecific(int startPosition) {
			setSelectedPositionInt(startPosition);
			final int  hardLimit=(mWindowSize-1)/2;
			mFirstPosition=Math.max(0,startPosition-Math.max(hardLimit,hardLimit+(hardLimit-(mItemCount-startPosition))));
			final int count=mItemCount;
			if (startPosition==0) {
				return fillFromFirst();
			}
			else if(startPosition==mItemCount-1) {
				
				return fillFromLast();
			}
			
			else {
				
				final int leftLimit=Math.max(hardLimit,hardLimit+(hardLimit-(mItemCount-startPosition)));
				final int rightLimit=Math.max(hardLimit, hardLimit+hardLimit-startPosition);
				 	View selectedChild=null;
				 	int firstChild=Math.max(0,startPosition-leftLimit);
				 	int lastChild=Math.min(startPosition+rightLimit,mItemCount);
				 	int current=firstChild;
					while(current<=lastChild) {
						View child=makeAndAddView(current,FLOW_RIGHT,false);
						if (current==startPosition) selectedChild=child;
						current++;
					}
					
		          
				 	
				return selectedChild;
			}
			
		}
		
	
	
		
	    private void scrollListItemsBy(int amount) {

	        //final int listRight=mContainer.getRight()+mContainer.getPaddingRight();
	        //final int listLeft=mContainer.getLeft()+mContainer.getPaddingLeft();
	        if (amount!=0)
	        { 
	        	final RecycleBin recycleBin = mRecycler;
	        	int numChildren = mContainer.getChildCount();
	        	final int lastChildPosition = mFirstPosition + numChildren - 1;
	        
	        if (amount >0) {
	            // shifted items left

	            
	           //only start adding views once we are in the middle of the first window and not in the last
	           
	  
	               
	                if (mSelectedPosition>=(mWindowSize-1)/2&&lastChildPosition < mItemCount - 1) {
	                	
	               
	                    
	                	addViewRight(lastChildPosition);
	                	
	                	View first = mContainer.getChildAt(0);
	                	int childWidth=first.getWidth();
	    	            mContainer.removeViewInLayout(first);
	    	            recycleBin.addScrapView(first);
	    	            
	                	
	                	mFirstPosition++;
	                	int next=mSelectedPosition+1;
	                	setSelectedPositionInt(next);
	                	offsetChildrenLeftAndRight(-childWidth);
	                	
	                	View child=mContainer.getChildAt(mSelectedPosition-mFirstPosition);
	                	
	                	
	                	
	        			int childLeft=child.getLeft();
	        			int scrollX=getScrollX();
	        		//int childWidth=child.getMeasuredWidth();
	        		if (scrollX!=childLeft)
	        		{
	        			Log.e(TAG,"Block Layout?: "+((Boolean)mBlockLayoutRequests).toString());
	        			Log.e(TAG," scrollListItems Adjusted Scroll to: "+((Integer) childLeft).toString());
	        			
	        			smoothScrollTo(childLeft,0);
	        			
	        			
	        	    }
	                	
	                   
	                   
	             
	            

	           

	            // Put first view in scrap
	            
	            
	          
                
	                } 
	                
	                else { 
	            		
	                	 int next=mSelectedPosition+1;
		                    setSelectedPositionInt(next);
		                    View child=mContainer.getChildAt(mSelectedPosition-mFirstPosition);
		                	smoothScrollTo(child.getLeft(),0);
		                	mContainer.invalidate();
	            	}
	            
	            
	        } 
	        
	        else {
	            // shifted items right
	            //Only add more views if we aren't currently within the last window
	            	if (mSelectedPosition>(mWindowSize-1)/2&&mFirstPosition>0) {
	            		
	            	
	                    
	            		
	            		addViewLeft(mFirstPosition);
	            		int lastIndex = mContainer.getChildCount() - 1;
	            		View last = mContainer.getChildAt(lastIndex);
	            		int childWidth=last.getWidth();
	            		mContainer.removeViewInLayout(last);
	                    recycleBin.addScrapView(last);
	            		
	            		mFirstPosition--;
	            		int next=mSelectedPosition-1;
	            		setSelectedPositionInt(next);
	            		offsetChildrenLeftAndRight(childWidth);
	            		
	            		View child=mContainer.getChildAt(mSelectedPosition-mFirstPosition);
	                	
	                	
	                	
	        			int childLeft=child.getLeft();
	        			int scrollX=getScrollX();
	        		//int childWidth=child.getMeasuredWidth();
	        		if (scrollX!=childLeft)
	        		{
	        			Log.e(TAG,"Block Layout?: "+((Boolean)mBlockLayoutRequests).toString());
	        			Log.e(TAG,"scrollListItem Adjusted Scroll to: "+((Integer) childLeft).toString());
	        			
	        			smoothScrollTo(childLeft,0);
	        			mContainer.invalidate();
	        	    }
	        		
	            		
	            

	           

	            
	          

	      
	                    
	            		// View child=mContainer.getChildAt(0);
	                    // scrollTo(child.getMeasuredWidth()*(mWindowSize-1)/2,0);
	          
	        }
	            	else {
	            		int next=mSelectedPosition-1;
	            		setSelectedPositionInt(next);
	            		View child=mContainer.getChildAt(mSelectedPosition-mFirstPosition);
	                	smoothScrollTo(child.getLeft(),0);
	                	
	           
	            	}
	            	
	        
	    
	    }
	        }
	    }

	    private View addViewLeft(int position) {
	        int leftPosition = position - 1;
	        View view = obtainView(leftPosition);
	     
	        setupChild(view, leftPosition, FLOW_LEFT, false, false);
	        return view;
	    }

	    private View addViewRight(int position) {
	        int rightPosition = position + 1;
	        View view = obtainView(rightPosition);
	       
	        setupChild(view, rightPosition, FLOW_RIGHT,false, false);
	        return view;
	    }
	    
	    static class SavedState extends BaseSavedState {
	        long selectedId;
	        long firstId;
	        int position;
	     

	        /**
	         * Constructor called from {@link AbsListView#onSaveInstanceState()}
	         */
	        SavedState(Parcelable superState) {
	            super(superState);
	        }

	        /**
	         * Constructor called from {@link #CREATOR}
	         */
	        private SavedState(Parcel in) {
	            super(in);
	            selectedId = in.readLong();
	            firstId = in.readLong();
	            position = in.readInt();
	       
	        }

	        @Override
	        public void writeToParcel(Parcel out, int flags) {
	            super.writeToParcel(out, flags);
	            out.writeLong(selectedId);
	            out.writeLong(firstId);
	           
	            out.writeInt(position);
	          
	        }

	        @Override
	        public String toString() {
	            return "AbsListView.SavedState{"
	                    + Integer.toHexString(System.identityHashCode(this))
	                    + " selectedId=" + selectedId
	                    + " firstId=" + firstId
	                    + " viewTop="
	                    + " position=" + position
	                     + "}";
	        }

	        public static final Parcelable.Creator<SavedState> CREATOR
	                = new Parcelable.Creator<SavedState>() {
	            public SavedState createFromParcel(Parcel in) {
	                return new SavedState(in);
	            }

	            public SavedState[] newArray(int size) {
	                return new SavedState[size];
	            }
	        };
	    }
	    
	   public Parcelable onSaveInstanceState() {
	       
	       

	        Parcelable superState = super.onSaveInstanceState();

	        SavedState ss = new SavedState(superState);

	        long selectedId = getSelectedItemId();
	        ss.selectedId = selectedId;
	        ss.position=getSelectedItemPosition();

	      

	       

	        return ss;
	    }

	    
	    @Override
	    public void onRestoreInstanceState(Parcelable state) {
	        SavedState ss = (SavedState) state;

	        super.onRestoreInstanceState(ss.getSuperState());
	        mDataChanged = true;

	            mNeedSync = true;
	            mSyncRowId = ss.selectedId;
	            mSyncPosition = ss.position;
	            mSyncMode = SYNC_SELECTED_POSITION;
	       

	       

	        requestLayout();
	    }

		

		

		
        public void setAdapter(ListAdapter adapter,int position) {
        	
        	
            if (null != mAdapter) {
                mAdapter.unregisterDataSetObserver(mDataSetObserver);
            }

            resetList();
            mRecycler.clear();

           
                mAdapter = adapter;
           

          
            if (mAdapter != null) {
                mAreAllItemsSelectable = mAdapter.areAllItemsEnabled();
                mOldItemCount = mItemCount;
                mItemCount = mAdapter.getCount();

                mDataSetObserver = new AdapterDataSetObserver();
                mAdapter.registerDataSetObserver(mDataSetObserver);
                mLayoutMode=LAYOUT_SET_SELECTION;
                setSelectedPositionInt(position);
                
              
             mNeedLayout=true;
            requestLayout();
        }
        }
       private int mMeasuredHeight;
       private int mMeasuredWidth;
	//private boolean mAdjustScroll=false;
		
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
           //We handle adjusting children in scrollListItemsTo...no need to go through a layout pass here.
           if (changed||mNeedLayout) {
            	 mMeasuredHeight=getMeasuredHeight();
                 mMeasuredWidth=getMeasuredWidth();
                 mNeedLayout=false;
            		layoutChildren();
           }
           
            		
            		//View first=mContainer.getChildAt(0);
            		
            			View child=mContainer.getChildAt(mSelectedPosition-mFirstPosition);
            			if (child!=null) {
            			int childLeft=child.getLeft();
            			int scrollX=getScrollX();
            		//int childWidth=child.getMeasuredWidth();
            		if (scrollX!=childLeft)
            		{
            			Log.e(TAG,"Block Layout?: "+((Boolean)mBlockLayoutRequests).toString());
            			Log.e(TAG,"onLayout Adjusted Scroll to: "+((Integer) childLeft).toString());
            			scrollTo(childLeft,0);
            		}
            			}
            		
            		
          
         
            
      
         
        }
        
   

        
        static final int NO_POSITION = -1;
        
       
        
        void setSelectedPositionInt(int position) {
        	
            mSelectedPosition = position;
            if (mObserver!=null) mObserver.onSelectionChanged(position);
            mSelectedRowId = getItemIdAtPosition(position);
        }
        
        public long getSelectedItemId() {
            return mSelectedRowId;
        }
        
        public long getItemIdAtPosition(int position) {
           ListAdapter adapter=mAdapter;
            return (adapter == null || position < 0) ? AdapterView.INVALID_ROW_ID : adapter.getItemId(position);
        }
        
        public int getSelectedItemPosition() {
            return mSelectedPosition;
        }
        
        
       
        
      
      
        void resetList() {
        	mContainer.removeAllViewsInLayout();
             mFirstPosition = 0;
             mDataChanged = false;
             mNeedSync = false;
             mSelectedPosition=AdapterView.INVALID_POSITION;
           
             invalidate();
        }
        
        
       
	


		RecycleBin mRecycler=new RecycleBin();
        
        
        View obtainView(int position) {
            View scrapView;

            scrapView = mRecycler.getScrapView(position);

            View child;
            if (scrapView != null) {
                if (ViewDebug.TRACE_RECYCLER) {
                    ViewDebug.trace(scrapView, ViewDebug.RecyclerTraceType.RECYCLE_FROM_SCRAP_HEAP,
                            position, -1);
                }

                child = mAdapter.getView(position, scrapView, this);

                if (ViewDebug.TRACE_RECYCLER) {
                    ViewDebug.trace(child, ViewDebug.RecyclerTraceType.BIND_VIEW,
                            position, mContainer.getChildCount());
                }

                if (child != scrapView) {
                    mRecycler.addScrapView(scrapView);
                   
                    if (ViewDebug.TRACE_RECYCLER) {
                        ViewDebug.trace(scrapView, ViewDebug.RecyclerTraceType.MOVE_TO_SCRAP_HEAP,
                                position, -1);
                    }
                }
            } else {
                child = mAdapter.getView(position, null, this);
               
                if (ViewDebug.TRACE_RECYCLER) {
                    ViewDebug.trace(child, ViewDebug.RecyclerTraceType.NEW_VIEW,
                            position, mContainer.getChildCount());
                }
            }

            return child;
        }
        
        class RecycleBin {
            private RecyclerListener mRecyclerListener;

            /**
             * The position of the first view stored in mActiveViews.
             */
            private int mFirstActivePosition;

            /**
             * Views that were on screen at the start of layout. This array is populated at the start of
             * layout, and at the end of layout all view in mActiveViews are moved to mScrapViews.
             * Views in mActiveViews represent a contiguous range of Views, with position of the first
             * view store in mFirstActivePosition.
             */
            private View[] mActiveViews = new View[0];

            /**
             * Unsorted views that can be used by the adapter as a convert view.
             */
            private ArrayList<View> mScrapViews;

            

            
            RecycleBin() {
            	 
               
      
                 mScrapViews = new ArrayList<View>();
            	
            }
            public void setViewTypeCount(int viewTypeCount) {
                if (viewTypeCount < 1) {
                    throw new IllegalArgumentException("Can't have a viewTypeCount < 1");
                }
                //noinspection unchecked
               
            }

            public boolean shouldRecycleViewType(int viewType) {
                return viewType >= 0;
            }

            /**
             * Clears the scrap heap.
             */
            void clear() {
               
                    final ArrayList<View> scrap = mScrapViews;
                    final int scrapCount = scrap.size();
                    for (int i = 0; i < scrapCount; i++) {
                        mContainer.removeDetachedView(scrap.remove(scrapCount - 1 - i), false);
                    }
     
                
            }

            /**
             * Fill ActiveViews with all of the children of the AbsListView.
             *
             * @param childCount The minimum number of views mActiveViews should hold
             * @param firstActivePosition The position of the first view that will be stored in
             *        mActiveViews
             */
            void fillActiveViews(int childCount, int firstActivePosition) {
                if (mActiveViews.length < childCount) {
                    mActiveViews = new View[childCount];
                }
                mFirstActivePosition = firstActivePosition;

                final View[] activeViews = mActiveViews;
                for (int i = 0; i < childCount; i++) {
                    View child = mContainer.getChildAt(i);
                   
                        activeViews[i] = child;
                    
                }
            }

            /**
             * Get the view corresponding to the specified position. The view will be removed from
             * mActiveViews if it is found.
             *
             * @param position The position to look up in mActiveViews
             * @return The view if it is found, null otherwise
             */
            View getActiveView(int position) {
                int index = position - mFirstActivePosition;
                final View[] activeViews = mActiveViews;
                if (index >=0 && index < activeViews.length) {
                    final View match = activeViews[index];
                    activeViews[index] = null;
                    return match;
                }
                return null;
            }

            /**
             * @return A view from the ScrapViews collection. These are unordered.
             */
            View getScrapView(int position) {
                ArrayList<View> scrapViews;
                
                    scrapViews = mScrapViews;
                    int size = scrapViews.size();
                    if (size > 0) {
                        return scrapViews.remove(size - 1);
                    } else {
                        return null;
                    }
                 
            }

            /**
             * Put a view into the ScapViews list. These views are unordered.
             *
             * @param scrap The view to add
             */
            void addScrapView(View scrap) {
                
                if (scrap == null) {
                    return;
                }

               

               
                    mScrapViews.add(scrap);
               

                if (mRecyclerListener != null) {
                    mRecyclerListener.onMovedToScrapHeap(scrap);
                }
            }

            /**
             * Move all views remaining in mActiveViews to mScrapViews.
             */
            void scrapActiveViews() {
                final View[] activeViews = mActiveViews;
                final boolean hasListener = mRecyclerListener != null;
               

                ArrayList<View> scrapViews = mScrapViews;
                final int count = activeViews.length;
                for (int i = 0; i < count; ++i) {
                    final View victim = activeViews[i];
                    if (victim != null) {
                       

                        activeViews[i] = null;

                      

                        
                            scrapViews = mScrapViews;
                        
                        scrapViews.add(victim);

                        if (hasListener) {
                            mRecyclerListener.onMovedToScrapHeap(victim);
                        }

                        if (ViewDebug.TRACE_RECYCLER) {
                            ViewDebug.trace(victim,
                                    ViewDebug.RecyclerTraceType.MOVE_FROM_ACTIVE_TO_SCRAP_HEAP,
                                    mFirstActivePosition + i, -1);
                        }
                    }
                }

                pruneScrapViews();
            }

            /**
             * Makes sure that the size of mScrapViews does not exceed the size of mActiveViews.
             * (This can happen if an adapter does not recycle its views).
             */
            private void pruneScrapViews() {
                final int maxViews = mActiveViews.length;
                final ArrayList<View> scrapViews = mScrapViews;
              
                    final ArrayList<View> scrapPile = scrapViews;
                    int size = scrapPile.size();
                    final int extras = size - maxViews;
                    size--;
                    for (int j = 0; j < extras; j++) {
                        mContainer.removeDetachedView(scrapPile.remove(size--), false);
                    }
                
            }

            /**
             * Puts all views in the scrap heap into the supplied list.
             */
            void reclaimScrapViews(List<View> views) {
                
                    views.addAll(mScrapViews);
                
            }
        }
        
        
		
         
         
     
       
       private int mSyncMode;
       
        class AdapterDataSetObserver extends DataSetObserver {

            private Parcelable mInstanceState = null;
			
			

            @Override
            public void onChanged() {
                mDataChanged = true;
                mOldItemCount = mItemCount;
                mItemCount = mAdapter.getCount();

                // Detect the case where a cursor that was previously invalidated has
                // been repopulated with new data.
                if (mAdapter.hasStableIds() && mInstanceState != null
                        && mOldItemCount == 0 && mItemCount > 0) {
                    HorizontalSnapView.this.onRestoreInstanceState(mInstanceState);
                    mInstanceState = null;
                } else {
                    rememberSyncState();
                }
                
                layoutChildren();
            }
            
          
            
            private void rememberSyncState() {
            	   if (mContainer.getChildCount() > 0) {
                       mNeedSync = true;
            
                       if (mSelectedPosition >= 0) {
                        
                          
                           mSyncRowId = mSelectedRowId;
                           mSyncMode = SYNC_SELECTED_POSITION;
                           mSyncPosition = mSelectedPosition;
                           
                      
                       }
                   }
            	   else {
                       // Sync the based on the offset of the first view
                    
                       ListAdapter adapter = mAdapter;
                       if (mFirstPosition >= 0 && mFirstPosition < adapter.getCount()) {
                           mSyncRowId = adapter.getItemId(mFirstPosition);
                       } else {
                           mSyncRowId = NO_ID;
                       }
                       mSyncPosition = mFirstPosition;
                       
                       mSyncMode = SYNC_FIRST_POSITION;
                   }
            }
            
            @Override
            public void onInvalidated() {
               mDataChanged = true;

                if (mAdapter.hasStableIds()) {
                    // Remember the current state for the case where our hosting activity is being
                    // stopped and later restarted
                    mInstanceState = HorizontalSnapView.this.onSaveInstanceState();
                }

                // Data is invalid so we should reset our state
                mOldItemCount = mItemCount;
                mItemCount = 0;
                mSelectedPosition = AdapterView.INVALID_POSITION;
                mSelectedRowId =AdapterView.INVALID_ROW_ID;
                mNeedSync = false;

                layoutChildren();
            }

            public void clearSavedState() {
                mInstanceState = null;
            }
        }


    	
}