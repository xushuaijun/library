package com.xsj.swipelayout.library;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.Scroller;

import com.handmark.pulltorefresh.library.R;

import java.util.LinkedList;
import java.util.List;


/**
 *
 * @author xiaanming
 *
 * @blog http://blog.csdn.net/xiaanming
 *
 */
public class SwipeBackLayout extends FrameLayout {
	private static final String TAG = SwipeBackLayout.class.getSimpleName();
	private View mContentView;
	private int mTouchSlop;
	private int downX;
	private int downY;
	private int tempX;
	private Scroller mScroller;
	private int viewWidth;
	private boolean isSilding;
	private boolean isFinish;
	private Drawable mShadowDrawable;
	private Activity mActivity;
	private List<ViewPager> mViewPagers = new LinkedList<ViewPager>();
	private List<HorizontalScrollView> mHorizontalScrollViews=new LinkedList<HorizontalScrollView>();

	public SwipeBackLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SwipeBackLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		mScroller = new Scroller(context);

		mShadowDrawable = getResources().getDrawable(R.drawable.shadow_left);
	}


	public void attachToActivity(Activity activity) {
		mActivity = activity;
		TypedArray a = activity.getTheme().obtainStyledAttributes(
				new int[] { android.R.attr.windowBackground });
		int background = a.getResourceId(0, 0);
		a.recycle();

		ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();


		ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
		Log.e("decor.getChildAt(0)",  decorChild.getClass().getSimpleName()+"");
		decorChild.setBackgroundResource(background);

		decor.removeView(decorChild);
		addView(decorChild);
		setContentView(decorChild);
		decor.addView(this);


	}

	private void setContentView(View decorChild) {
		mContentView = (View) decorChild.getParent();
	}

	/**
	 * �¼�9�ز���
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		/**
		 * 查看是布局当中是否有 viewpager
		 */
		ViewPager mViewPager = getTouchViewPager(mViewPagers, ev);
		Log.i(TAG, "mViewPager = " + mViewPager);

		if(mViewPager != null && mViewPager.getCurrentItem() != 2){
			return super.onInterceptTouchEvent(ev);
		}

		/**
		 * 查看是布局当中是否有 HorizontalScrollView
		 */
		HorizontalScrollView mHorizontalScrollView = getTouchHorizontalScrollView(mHorizontalScrollViews, ev);
		Log.i(TAG, "mHorizontalScrollView = " + mHorizontalScrollView);

		if(mHorizontalScrollView != null){
			return super.onInterceptTouchEvent(ev);
		}


		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downX = tempX = (int) ev.getRawX();
			downY = (int) ev.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			int moveX = (int) ev.getRawX();
			/*if (moveX - downX <- mTouchSlop*/ // 向左滑动
			if (moveX - downX > mTouchSlop   // 向右滑动
					&& Math.abs((int) ev.getRawY() - downY) < mTouchSlop) {
				return true;
			}
			break;
		}

		return super.onInterceptTouchEvent(ev);
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			int moveX = (int) event.getRawX();
			int deltaX = tempX - moveX;
			tempX = moveX;
			/*if (moveX - downX <- mTouchSlop*/  //向左滑动
			if (moveX - downX > mTouchSlop  //向右滑动
					&& Math.abs((int) event.getRawY() - downY) < mTouchSlop) {
				isSilding = true;
			}

			/*if (moveX - downX <= 0 && isSilding) {*/   //向左滑动
			if (moveX - downX > 0 && isSilding) {    //向右滑动
				mContentView.scrollBy(deltaX, 0);
			}
			break;
		case MotionEvent.ACTION_UP:
			isSilding = false;
			//if (mContentView.getScrollX() >= viewWidth / 5) {
			/*if (mContentView.getScrollX() >=5) {*/  //向左滑动
			if (mContentView.getScrollX() <=5) {   //向右滑动
				isFinish = true;
				scrollRight();
			} else {
				scrollOrigin();
				isFinish = false;
			}
			break;
		}

		return true;
	}

	/**
	 *
	 * @param mViewPagers
	 * @param parent
	 */
	private void getAlLViewPager(List<ViewPager> mViewPagers, ViewGroup parent){
		int childCount = parent.getChildCount();
		for(int i=0; i<childCount; i++){
			View child = parent.getChildAt(i);
			if(child instanceof ViewPager){
				mViewPagers.add((ViewPager)child);
			}else if(child instanceof ViewGroup){
				getAlLViewPager(mViewPagers, (ViewGroup)child);
			}
		}
	}

	private void getAllHorizontalScrollView(List<HorizontalScrollView> mHorizontalScrollViews, ViewGroup parent){
		int childCount = parent.getChildCount();
		for(int i=0; i<childCount; i++){
			View child = parent.getChildAt(i);
			if(child instanceof HorizontalScrollView){
				mHorizontalScrollViews.add((HorizontalScrollView)child);
			}else if(child instanceof ViewGroup){
				getAllHorizontalScrollView(mHorizontalScrollViews, (ViewGroup) child);
			}
		}
	}


	/**
	 *
	 * @param mViewPagers
	 * @param ev
	 * @return
	 */
	private ViewPager getTouchViewPager(List<ViewPager> mViewPagers, MotionEvent ev){
		if(mViewPagers == null || mViewPagers.size() == 0){
			return null;
		}
		Rect mRect = new Rect();
		for(ViewPager v : mViewPagers){
			v.getHitRect(mRect);

			if(mRect.contains((int)ev.getX(), (int)ev.getY())){
				return v;
			}
		}
		return null;
	}

	/**
	 * 获取触摸到的 HorizontalScrollView
	 * @param mHorizontalScrollViews
	 * @param ev
	 * @return
	 */
	private HorizontalScrollView getTouchHorizontalScrollView(List<HorizontalScrollView> mHorizontalScrollViews, MotionEvent ev) {
		if(mHorizontalScrollViews == null || mHorizontalScrollViews.size() == 0){
			return null;
		}
		Rect mRect = new Rect();
		for(HorizontalScrollView v : mHorizontalScrollViews){
			v.getHitRect(mRect);

			if(mRect.contains((int)ev.getX(), (int)ev.getY())){
				return v;
			}
		}
		return null;
	}


	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed) {
			viewWidth = this.getWidth();

			getAlLViewPager(mViewPagers, this);
			getAllHorizontalScrollView(mHorizontalScrollViews,this);
			Log.i(TAG, "ViewPager size = " + mViewPagers.size());
		}
	}


	/**Android的view组件显示主要经过mesure, layout和draw这三个过程。在mesure阶段里调用
	 * mesure(int widthSpec, int heightSpec)方法，这个方法是final不能被重写，在这个过程里会
	 * 调用onMesure(int widthSpec, int heightSpec)方法。当组件设置好大小后，调用final layout
	 * (int l, int t, int r, int b)方法进行布局，在这个过程里会调用onLayout(boolean changed, int l,
	 *  int t, int r, int b)方法，所以处理组件的布局通常要重写onMesure和onLayout这两个方法。
	 * View组件的绘制会调用draw(Canvas canvas)方法，这个方法在源代码里看不到在哪里调用...
	 * draw过程中主要是先画Drawable背景，对drawable调用setBounds()然后是draw(Canvas c)
	 * 方法.有点注意的是背景drawable的实际大小会影响view组件的大小，drawable的实际大小通
	 * 过getIntrinsicWidth()和getIntrinsicHeight()获取，当背景比较大时view组件大小等于背景
	 * drawable的大小，不过俺没有在源代码里找到布局时调用过 getIntrinsicWidth()和getIntrinsicHeight()
	 * 方法...
       画完背景后，draw过程会调用onDraw(Canvas canvas)方法，然后就是dispatchDraw(Canvas canvas)方法,
     dispatchDraw()主要是分发给子组件进行绘制，我们通常定制组件的时候重写的是onDraw()方法。值得注意
     的是ViewGroup容器组件的绘制，当它没有背景时直接调用的是dispatchDraw()方法, 而绕过了draw()方法，
     当它有背景的时候就调用draw()方法，而draw()方法里包含了dispatchDraw()方法的调用。因此要在ViewGroup
     上绘制东西的时候往往重写的是dispatchDraw()方法而不是onDraw()方法，或者自定制一个Drawable，重写它
     的draw(Canvas c)和getIntrinsicWidth(),
     getIntrinsicHeight()方法，然后设为背景。
	 *
	 */


	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (mShadowDrawable != null && mContentView != null) {

			int left = mContentView.getLeft()
					- mShadowDrawable.getIntrinsicWidth();
			int right = left + mShadowDrawable.getIntrinsicWidth();
			int top = mContentView.getTop();
			int bottom = mContentView.getBottom();

			mShadowDrawable.setBounds(left, top, right, bottom);
			mShadowDrawable.draw(canvas);

		}

	}


	/**
	 * ������
	 */
	public void scrollRight() {
		final int delta = (viewWidth - mContentView.getScrollX());

		/*mScroller.startScroll(mContentView.getScrollX(), 0, delta + 1, 0,
				Math.abs(delta));*/  //向左滑动
		mScroller.startScroll(mContentView.getScrollX(), 0, -(delta + 1), 0,
				Math.abs(delta)); //向右滑动
		postInvalidate();
	}

	/**
	 * �����ʼλ��
	 */
	private void scrollOrigin() {
		int delta = mContentView.getScrollX();
		mScroller.startScroll(mContentView.getScrollX(), 0, -delta, 0,
				Math.abs(delta));
		postInvalidate();
	}

	@Override
	public void computeScroll() {
		Log.e("SwipeBackLayout","computeScroll");
		/**
		 * Call this when you want to know the new location.  If it returns true,
		 the animation is not yet finished.
		 */

		if (mScroller.computeScrollOffset()) {
			mContentView.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
			//Log.e("mScroller.isFinished()",mScroller.isFinished() +","+ isFinish);
		//	if (mScroller.isFinished() && isFinish) {
				mActivity.finish();
		//	}
		}
	}


}
