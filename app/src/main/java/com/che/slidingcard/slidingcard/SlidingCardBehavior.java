package com.che.slidingcard.slidingcard;

import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.view.View;

/**
 * 作者：余天然 on 16/9/13 下午11:16
 */
public class SlidingCardBehavior extends CoordinatorLayout.Behavior<SlidingCardView> {

    private int defaultOffset;//默认偏移值

    @Override
    public boolean onMeasureChild(CoordinatorLayout parent, SlidingCardView child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        int offset = getChildMeasureOffset(parent, child);
        int height = View.MeasureSpec.getSize(parentHeightMeasureSpec) - offset;
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        child.measure(parentWidthMeasureSpec, heightMeasureSpec);
        return true;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, SlidingCardView child, int layoutDirection) {
        parent.onLayoutChild(child, layoutDirection);
        SlidingCardView previous = getPreviousChild(parent, child);
        if (previous != null) {
            int offset = previous.getTop() + previous.getHeaderHeight();
            child.offsetTopAndBottom(offset);
        }
        defaultOffset = child.getTop();
        return true;
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, SlidingCardView child, View directTargetChild, View target, int nestedScrollAxes) {
        //判断监听的方向
        boolean isVertical = (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
        return isVertical && child == directTargetChild;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout parent, SlidingCardView child, View target, int dx, int dy, int[] consumed) {
        if (child.getTop() > defaultOffset) {
            //1、控制自己的滑动
            int minOffset = defaultOffset;
            int maxOffset = defaultOffset + child.getHeight() - child.getHeaderHeight();
            consumed[1] = scroll(child, dy, minOffset, maxOffset);
            //2、控制上面和下面的滑动
            shiftScroll(consumed[1], parent, child);
        }
    }

    @Override
    public void onNestedScroll(CoordinatorLayout parent, SlidingCardView child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        //1、控制自己的滑动
        int minOffset = defaultOffset;
        int maxOffset = defaultOffset + child.getHeight() - child.getHeaderHeight();
        int scrollY = scroll(child, dyUnconsumed, minOffset, maxOffset);
        //2、控制上面和下面的滑动
        shiftScroll(scrollY, parent, child);
    }

    /**
     * 处理自己的滑动
     *
     * @param child
     * @param dy
     * @param minOffset
     * @param maxOffset
     * @return
     */
    private int scroll(SlidingCardView child, int dy, int minOffset, int maxOffset) {
        int top = child.getTop();
        int offset = clamp(top - dy, minOffset, maxOffset) - top;
        child.offsetTopAndBottom(offset);
        return -offset;
    }

    /**
     * 处理其它的滑动
     *
     * @param scrollY
     * @param parent
     * @param child
     */
    private void shiftScroll(int scrollY, CoordinatorLayout parent, SlidingCardView child) {
        if (scrollY == 0) return;
        if (scrollY > 0) {//往上推
            SlidingCardView current = child;
            SlidingCardView card = getPreviousChild(parent, current);
            while (card != null) {
                int delta = calcOtherOffset(card, current);
                if (delta > 0) {
                    card.offsetTopAndBottom(-delta);
                }
                current = card;
                card = getPreviousChild(parent, current);
            }
        } else {//往下推
            SlidingCardView current = child;
            SlidingCardView card = getNextChild(parent, current);
            while (card != null) {
                int delta = calcOtherOffset(current, card);
                if (delta > 0) {
                    card.offsetTopAndBottom(delta);
                }
                current = card;
                card = getNextChild(parent, current);
            }

        }
    }


    /**
     * 获取卡片的默认偏移值
     *
     * @param parent
     * @param child
     * @return
     */
    private int getChildMeasureOffset(CoordinatorLayout parent, SlidingCardView child) {
        int offset = 0;
        for (int i = 0; i < parent.getChildCount(); i++) {
            View view = parent.getChildAt(i);
            //排除自己
            if (view != child && view instanceof SlidingCardView) {
                SlidingCardView slidingCardView = (SlidingCardView) view;
                offset += slidingCardView.getHeaderHeight();
            }
        }
        return offset;
    }

    /**
     * 计算其它卡片的偏移值
     *
     * @param above
     * @param below
     * @return
     */
    private int calcOtherOffset(SlidingCardView above, SlidingCardView below) {
        return above.getTop() + above.getHeaderHeight() - below.getTop();
    }

    /**
     * 获取上一个卡片
     *
     * @param parent
     * @param child
     * @return
     */
    private SlidingCardView getPreviousChild(CoordinatorLayout parent, SlidingCardView child) {
        int cardIndex = parent.indexOfChild(child);
        for (int i = cardIndex - 1; i >= 0; i--) {
            View view = parent.getChildAt(i);
            if (view instanceof SlidingCardView) {
                return (SlidingCardView) view;
            }
        }
        return null;
    }

    /**
     * 获取下一个卡片
     *
     * @param parent
     * @param child
     * @return
     */
    private SlidingCardView getNextChild(CoordinatorLayout parent, SlidingCardView child) {
        int cardIndex = parent.indexOfChild(child);
        for (int i = cardIndex + 1; i < parent.getChildCount(); i++) {
            View view = parent.getChildAt(i);
            if (view instanceof SlidingCardView) {
                return (SlidingCardView) view;
            }
        }
        return null;
    }

    /**
     * 取上下限之间的值
     *
     * @param i
     * @param minOffset
     * @param maxOffset
     * @return
     */
    private int clamp(int i, int minOffset, int maxOffset) {
        if (i < minOffset) {
            return minOffset;
        } else if (i > maxOffset) {
            return maxOffset;
        } else {
            return i;
        }
    }
}
