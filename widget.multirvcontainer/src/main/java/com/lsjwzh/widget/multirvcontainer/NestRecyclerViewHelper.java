package com.lsjwzh.widget.multirvcontainer;


import android.support.v7.widget.RVScrollViewUtils;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

class NestRecyclerViewHelper {
  private static final String TAG = NestRecyclerViewHelper.class.getSimpleName();
  final MultiRVScrollView mHostScrollView;
  RecyclerView mNestedRecyclerView;
  private View mChildContainsRecyclerView;
  RecyclerView.OnScrollListener mOnScrollListener;
  View.OnLayoutChangeListener mNestRecyclerViewLayoutChangeListener =
      new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                                   int oldTop, int oldRight, int oldBottom) {
          if (mNestedRecyclerView.getHeight() > mHostScrollView.getHeight()) {
            mNestedRecyclerView.getLayoutParams().height = mHostScrollView.getHeight();
            mNestedRecyclerView.requestLayout();
          }
        }
      };

  NestRecyclerViewHelper(RecyclerView recyclerView,
                         MultiRVScrollView scrollView) {
    mNestedRecyclerView = recyclerView;
    mHostScrollView = scrollView;

    // 由于在嵌套RecyclerView时会自动Focus到RecyclerView上，而RecyclerView调用clearFocus无效
    // 因此需要禁用Focus
    mNestedRecyclerView.setFocusable(false);
    mNestedRecyclerView.setNestedScrollingEnabled(true);
    mChildContainsRecyclerView = findDirectChildContainsRecyclerView();
  }

  void removeLayoutChangeRelationship() {
    mNestedRecyclerView.removeOnLayoutChangeListener(mNestRecyclerViewLayoutChangeListener);
  }

  void fitRecyclerViewHeight() {
    if (mNestedRecyclerView.getLayoutManager().isAutoMeasureEnabled()) {
      if (mNestedRecyclerView.getHeight() > mHostScrollView.getHeight()) {
        // 如果RecyclerView是automeasurable且自动高度大于ScrollView高度，则需要对齐高度做限制
        // 所以如果RecyclerView的Adapter一开始就有很多数据，最好禁用automeasurable
        mNestedRecyclerView.getLayoutParams().height = mHostScrollView.getHeight();
        mNestedRecyclerView.requestLayout();
      } else if (mNestedRecyclerView.getLayoutParams().height < 0) {
        // 如果是自适应高度，则需要监听其高度变化
        mNestedRecyclerView.addOnLayoutChangeListener(mNestRecyclerViewLayoutChangeListener);
      }
    } else {
      // 如果RecyclerView不是automeasurable的则需指定其高度
      mNestedRecyclerView.getLayoutParams().height = mHostScrollView.getHeight();
      mNestedRecyclerView.requestLayout();
    }
  }

  boolean onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed,
                         int dyUnconsumed) {
    if (isRecyclerViewNestedScrollingEnabled(target)) {
      if (!shouldHandleByRecyclerView()) {
        RVScrollViewUtils.scrollVerticallyBy(mNestedRecyclerView, -dyConsumed);
        Log.d(TAG, "scrollBy " + dyConsumed);
        mHostScrollView.scrollBy(0, dyConsumed);
        return true;
      }
    }
    return false;
  }

  boolean onNestedPreFling(View target, float velocityX, final float velocityY) {
    if (isRecyclerViewNestedScrollingEnabled(target)) {
      if (shouldHandleByRecyclerView()) {
        if (mOnScrollListener != null) {
          mNestedRecyclerView.removeOnScrollListener(mOnScrollListener);
        }
        mOnScrollListener = new RecyclerView.OnScrollListener() {
          @Override
          public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
              recyclerView.removeOnScrollListener(this);
              if ((RVScrollViewUtils.isTopOverScrolled(recyclerView)
                  && velocityY < 0 && mHostScrollView.getScrollY() == getRecyclerViewPartTop()
                  && mHostScrollView.getScrollY() > 0
                  && !mHostScrollView.mScrollerCompat.isOverScrolled())
                  || (RVScrollViewUtils.isBottomOverScrolled(recyclerView)
                  && velocityY > 0
                  && mHostScrollView.getScrollY() == getRecyclerViewPartTop()
                  && mHostScrollView.getScrollY() < mHostScrollView.getChildAt(0).getHeight()
                  - mHostScrollView.getHeight()
                  && !mHostScrollView.mScrollerCompat.isOverScrolled())) {
                float currentVelocityY = RVScrollViewUtils.getCurrentVelocityY(recyclerView);
                mHostScrollView.fling(currentVelocityY == 0 ?
                    (int) (velocityY / 2) : (int) currentVelocityY);
                Log.d(TAG, mNestedRecyclerView.getId() +
                    " fling onScrollStateChanged:" + velocityY +
                    " recyclerView velocityY:" + currentVelocityY);
              }
            }
          }
        };
        mNestedRecyclerView.addOnScrollListener(mOnScrollListener);
      } else {
        mHostScrollView.fling((int) velocityY);
        Log.d(TAG, "fling onNestedPreFling:" + velocityY);
        return true;
      }
    }
    return false;
  }

  boolean onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
    float currVelocity = mHostScrollView.mScrollerCompat.getCurrVelocity();
    if (clampedY && shouldHandleByRecyclerView()
        && mNestedRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE
        && !Float.isNaN(currVelocity)) {
      currVelocity = scrollY == 0 ? -currVelocity : currVelocity;
      mNestedRecyclerView.stopScroll();
      mNestedRecyclerView.fling(0, (int) currVelocity);
      Log.d(TAG, mNestedRecyclerView.getId() + "fling onOverScrolled" + currVelocity);
      return true;
    }
    return false;
  }

  boolean isRecyclerViewNestedScrollingEnabled(View target) {
    return mNestedRecyclerView == target && mNestedRecyclerView.isNestedScrollingEnabled();
  }

  boolean shouldHandleByRecyclerView() {
    return mHostScrollView.getScrollY() == getRecyclerViewPartTop();
  }

  private int getRecyclerViewPartTop() {
    return mChildContainsRecyclerView == null
        ? mNestedRecyclerView.getTop()
        : mChildContainsRecyclerView.getTop() + mNestedRecyclerView.getTop();
  }

  private int getRecyclerViewPartBottom() {
    return mChildContainsRecyclerView == null
        ? mNestedRecyclerView.getBottom()
        : mNestedRecyclerView.getBottom() + mChildContainsRecyclerView.getTop();
  }

  private View findDirectChildContainsRecyclerView() {
    View parent = (View) mNestedRecyclerView.getParent();
    if (parent == mHostScrollView.getChildAt(0)) {
      return null;
    }
    while (parent.getParent() != mHostScrollView.getChildAt(0)) {
      parent = (View) parent.getParent();
    }
    return parent;
  }
}
