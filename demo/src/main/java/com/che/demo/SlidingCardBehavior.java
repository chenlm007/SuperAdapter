package com.che.demo;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.CoordinatorLayout.Behavior;
import android.support.v4.view.ViewCompat;
import android.view.View;

public class SlidingCardBehavior extends Behavior<SlidingCardLayout> {
	
	
	private int mInitialOffset;

	@Override
	public boolean onMeasureChild(CoordinatorLayout parent,
			SlidingCardLayout child, int parentWidthMeasureSpec, int widthUsed,
			int parentHeightMeasureSpec, int heightUsed) {
		int offset = getChildMeasureOffset(parent,child);
		int heightMeasureSpec = View.MeasureSpec.getSize(parentHeightMeasureSpec) - offset;
		child.measure(parentWidthMeasureSpec, View.MeasureSpec.makeMeasureSpec(heightMeasureSpec, View.MeasureSpec.EXACTLY));
		return true;
	}
	
	@Override
	public boolean onLayoutChild(CoordinatorLayout parent,
			SlidingCardLayout child, int layoutDirection) {
		parent.onLayoutChild(child, layoutDirection);
		SlidingCardLayout previous = getPreviousChild(parent,child);
		if(previous!=null){
			int offset = previous.getTop() + previous.getHeaderHeight();
			child.offsetTopAndBottom(offset);
		}
		mInitialOffset = child.getTop();
		return true;
	}
	
	private SlidingCardLayout getPreviousChild(CoordinatorLayout parent,
			SlidingCardLayout child) {
		int cardIndex = parent.indexOfChild(child);
		for (int i = cardIndex-1; i >=0; i--) {
			View v = parent.getChildAt(i);
			if(v instanceof SlidingCardLayout){
				return (SlidingCardLayout) v;
			}
		}
		return null;
	}
	private SlidingCardLayout getNextChild(CoordinatorLayout parent,
			SlidingCardLayout child) {
		int cardIndex = parent.indexOfChild(child);
		for (int i = cardIndex+1; i<parent.getChildCount(); i++) {
			View v = parent.getChildAt(i);
			if(v instanceof SlidingCardLayout){
				return (SlidingCardLayout) v;
			}
		}
		return null;
	}

	private int getChildMeasureOffset(CoordinatorLayout parent,
			SlidingCardLayout child) {
		int offset = 0;
		for (int i = 0; i < parent.getChildCount(); i++) {
			View view = parent.getChildAt(i);
			if(view!=child&&view instanceof SlidingCardLayout){
				offset += ((SlidingCardLayout)view).getHeaderHeight();
			}
		}
		return offset;
	}

	@Override
	public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout,
			SlidingCardLayout child, View directTargetChild, View target,
			int nestedScrollAxes) {
		boolean isVertical = (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL)!=0;
		return isVertical && child==directTargetChild;
	}
	
	@Override
	public void onNestedPreScroll(CoordinatorLayout coordinatorLayout,
			SlidingCardLayout child, View target, int dx, int dy, int[] consumed) {
		if(child.getTop()>mInitialOffset){
			consumed[1] = scroll(
					child,
					dy, 
					mInitialOffset,
					mInitialOffset+child.getHeight()-child.getHeaderHeight());
			
			shiftSlidings(consumed[1],coordinatorLayout,child);
		}
	}
	
	@Override
	public void onNestedScroll(CoordinatorLayout coordinatorLayout,
			SlidingCardLayout child, View target, int dxConsumed,
			int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
		int shift = scroll(
				child,
				dyUnconsumed,
				mInitialOffset,
				mInitialOffset+child.getHeight()-child.getHeaderHeight());
		
		shiftSlidings(shift,coordinatorLayout,child);
	}
	
	private void shiftSlidings(int shift, CoordinatorLayout parent, SlidingCardLayout child) {
		if(shift==0){
			return;
		}
		if(shift>0){
			SlidingCardLayout current  = child;
			SlidingCardLayout card = getPreviousChild(parent,current);
			while(card!=null){
				int delta = getHeaderOverlap(card,current);
				if(delta>0){
					card.offsetTopAndBottom(-delta);
				}
				current = card;
				card = getPreviousChild(parent, current);
			}
		}else{
			SlidingCardLayout current  = child;
			SlidingCardLayout card = getNextChild(parent,current);
			while(card!=null){
				int delta = getHeaderOverlap(current,card);
				if(delta>0){
					card.offsetTopAndBottom(delta);
				}
				current = card;
				card = getNextChild(parent, current);
			}
		}
		
	}

	private int getHeaderOverlap(SlidingCardLayout above,
			SlidingCardLayout below) {
		return above.getTop()+above.getHeaderHeight() - below.getTop();
	}

	private int scroll(View child,int dy,int minOffset, int maxOffset){
		int initialOffset = child.getTop();
		//deltaY:[min,max]
		int offset = clamp(initialOffset-dy, minOffset, maxOffset) - initialOffset;
		child.offsetTopAndBottom(offset);
		return -offset;
	}
	
	private int clamp(int i, int minOffset, int maxOffset) {
		if(i>maxOffset){
			return maxOffset;
		}else if(i<minOffset){
			return minOffset;
		}else{
			return i;
		}
	}
	

}
