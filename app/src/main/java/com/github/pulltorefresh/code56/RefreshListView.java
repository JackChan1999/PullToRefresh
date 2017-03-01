package com.github.pulltorefresh.code56;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.pulltorefresh.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ============================================================
 * Copyright：${TODO}有限公司版权所有 (c) 2017
 * Author：   AllenIverson
 * Email：    815712739@qq.com
 * GitHub：   https://github.com/JackChen1999
 * 博客：     http://blog.csdn.net/axi295309066
 * 微博：     AndroidDeveloper
 * <p>
 * Project_Name：PullToRefresh
 * Package_Name：com.github.pulltorefresh
 * Version：1.0
 * time：2016/2/15 17:18
 * des ：自定义控件56 下拉刷新
 * gitVersion：$Rev$
 * updateAuthor：$Author$
 * updateDate：$Date$
 * updateDes：${TODO}
 * ============================================================
 **/
public class RefreshListView extends ListView implements OnScrollListener
{
	private static final String	TAG						= "RefreshListView";

	private static final int	STATE_PULL_DOWN_REFRESH	= 0;						// 下拉刷新状态
	private static final int	STATE_RELEASE_REFRESH	= 1;						// 松开刷新状态
	private static final int	STATE_REFRESHING		= 2;						// 正在刷新状态

	private int					mCurrentState			= STATE_PULL_DOWN_REFRESH;	// 默认为下拉刷新状态

	private LinearLayout		mHeaderLayout;										// 头布局(刷新部分
																					// +
																					// 自定义部分)
	private View				mCustomHeaderView;									// 头布局中
																					// 自定义部分
	private View				mRefreshView;										// 头布局中刷新的部分

	private ImageView			mIvArrow;											// 刷新部分的箭头
	private ProgressBar			mProgressBar;										// 刷新部分的进度条
	private TextView			mTvState;											// 刷新部分的状态
	private TextView			mTvTime;											// 记录上次刷新的时间

	private int					mDownX;
	private int					mDownY;
	private int					mRefreshHeight;

	private RotateAnimation		down2UpAnimation;
	private RotateAnimation		up2DownAnimation;

	private int					mCurrentPaddingTop;

	private OnRefreshListener	mListener;

	private View				mFooterLayout;

	private int					mFooterHeight;

	private boolean				isLoadMore;										// 用来标记是否是正在加载更多

	private boolean				noMore;

	private int					diffY;

	public RefreshListView(Context context) {
		super(context);

		initHeaderLayout();
		initFooterLayout();
		initAnimation();
	}

	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);

		initHeaderLayout();
		initFooterLayout();
		initAnimation();
	}

	private void initAnimation()
	{
		// 由下往上的动画
		down2UpAnimation = new RotateAnimation(0, 180,
												Animation.RELATIVE_TO_SELF, 0.5f,
												Animation.RELATIVE_TO_SELF, 0.5f);
		down2UpAnimation.setDuration(300);
		down2UpAnimation.setFillAfter(true);

		up2DownAnimation = new RotateAnimation(-180, 0,
												Animation.RELATIVE_TO_SELF, 0.5f,
												Animation.RELATIVE_TO_SELF, 0.5f);
		up2DownAnimation.setDuration(300);
		up2DownAnimation.setFillAfter(true);
	}

	// 初始化头布局
	private void initHeaderLayout()
	{
		// 给ListView添加头布局
		// 加载头布局
		mHeaderLayout = (LinearLayout) View.inflate(getContext(), R.layout.refresh_header_layout, null);
		mRefreshView = mHeaderLayout.findViewById(R.id.refresh_header_refresh);
		mIvArrow = (ImageView) mHeaderLayout.findViewById(R.id.refresh_header_arrow);
		mProgressBar = (ProgressBar) mHeaderLayout.findViewById(R.id.refresh_header_progress);
		mTvState = (TextView) mHeaderLayout.findViewById(R.id.refresh_header_tv_state);
		mTvTime = (TextView) mHeaderLayout.findViewById(R.id.refresh_header_tv_time);

		// 添加到ListView的头布局中
		this.addHeaderView(mHeaderLayout);

		// 隐藏 刷新部分,设置头布局的paddingTop为刷新部分的高度的负数
		mRefreshView.measure(0, 0);
		mRefreshHeight = mRefreshView.getMeasuredHeight();
		Log.d(TAG, "刷新部分的高度:" + mRefreshHeight);
		mHeaderLayout.setPadding(0, -mRefreshHeight, 0, 0);
	}

	private void initFooterLayout()
	{
		// 底部加载更多的view
		mFooterLayout = View.inflate(getContext(), R.layout.refresh_footer_layout, null);

		// 加载到listView的footer中
		this.addFooterView(mFooterLayout);

		// 隐藏footview
		mFooterLayout.measure(0, 0);
		mFooterHeight = mFooterLayout.getMeasuredHeight();
		mFooterLayout.setPadding(0, -mFooterHeight, 0, 0);

		// 设置listView滚动的监听
		this.setOnScrollListener(this);
	}

	public void addCustomHeaderView(View headerView)
	{
		this.mCustomHeaderView = headerView;
		mHeaderLayout.addView(headerView);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		int action = ev.getAction();
		switch (action)
		{
			case MotionEvent.ACTION_DOWN:
				mDownX = (int) (ev.getX() + 0.5f);
				mDownY = (int) (ev.getY() + 0.5f);
				break;
			case MotionEvent.ACTION_MOVE:
				int moveX = (int) (ev.getX() + 0.5f);
				int moveY = (int) (ev.getY() + 0.5f);

				int diffX = moveX - mDownX;
				diffY = moveY - mDownY;

				// 如果当前的状态为正在刷新，就不去响应touch
				if (mCurrentState == STATE_REFRESHING)
				{
					break;
				}

				// 如果第一个View是可见的情况下，并且headerView完全可见的情况下
				if (mCustomHeaderView != null)
				{
					// 如果CustomHeaderView没有完全露出来，不去响应下拉刷新

					// 取出listView的左上角的点
					int[] lliw = new int[2];
					this.getLocationInWindow(lliw);
					Log.d(TAG, "listView Y : " + lliw[1]);

					// 取出customheaderView左上角的点
					int[] hliw = new int[2];
					mCustomHeaderView.getLocationInWindow(hliw);
					Log.d(TAG, "customHeader Y : " + hliw[1]);

					if (hliw[1] < lliw[1])
					{
						// 不响应下拉刷新
						return super.onTouchEvent(ev);
					}
				}

				// 如果第一个View是可见的情况下
				if (getFirstVisiblePosition() == 0)
				{
					if (diffY > 0)
					{
						Log.d(TAG, "第一个View可见");
						// 希望看到刷新的View
						// 改变头布局的PaddingTop
						mCurrentPaddingTop = diffY - mRefreshHeight;
						mHeaderLayout.setPadding(0, mCurrentPaddingTop, 0, 0);

						// 如果paddingTop是负数值的时候，说明刷新部分没有完全露出来，现在的状态为 下拉刷新
						if (mCurrentPaddingTop < 0 && mCurrentState != STATE_PULL_DOWN_REFRESH)
						{
							// 说明刷新部分没有完全露出来，现在的状态为 下拉刷新
							mCurrentState = STATE_PULL_DOWN_REFRESH;
							Log.d(TAG, "当前状态为 : 下拉刷新");
							// UI需要刷新
							refreshUI();
						}
						else if (mCurrentPaddingTop >= 0 && mCurrentState != STATE_RELEASE_REFRESH)
						{
							// 如果刷新部分完全露出来说明PaddingTop>=0,现在的状态为 释放刷新
							mCurrentState = STATE_RELEASE_REFRESH;
							Log.d(TAG, "当前状态为 : 释放刷新");
							// UI需要刷新
							refreshUI();
						}
						// 消费掉
						return true;
					}
				}

				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mDownX = 0;// 清空数据
				mDownY = 0;// 清空数据

				// 是否是下拉的操作
				if (diffY <= 0)
				{
					break;
				}

				diffY = 0;

				// 松开时的逻辑

				// 如果现在是 松开刷新的状态
				if (mCurrentState == STATE_RELEASE_REFRESH)
				{
					Log.d(TAG, "up后，正在刷新");
					// 1. paddingTop应该到 0
					// mHeaderLayout.setPadding(0, 0, 0, 0);//太突然

					// ObjectAnimator.ofInt(mHeaderLayout, "scrollX", 0, 1,2,1);
					int start = mCurrentPaddingTop;
					int end = 0;
					doHeaderAnimation(start, end);

					// 2. 刷新的状态应该变为 正在刷新
					mCurrentState = STATE_REFRESHING;
					// 3. UI刷新
					refreshUI();

					// 调用接口，通知正在刷新
					if (mListener != null)
					{
						mListener.onRefreshing();
					}
				}

				// 如果 现在的状态是 下拉刷新
				if (mCurrentState == STATE_PULL_DOWN_REFRESH)
				{
					Log.d(TAG, "up后，下拉刷新");

					// 1. paddingTop = -refreshHeight
					// mHeaderLayout.setPadding(0, -mRefreshHeight, 0, 0);// 太突然

					int start = mCurrentPaddingTop;
					int end = -mRefreshHeight;
					doHeaderAnimation(start, end);

				}

				break;
			default:
				break;
		}

		return super.onTouchEvent(ev);
	}

	public void setRefreshFinish()
	{
		// 默认有更多
		setRefreshFinish(false);
	}

	public void setRereshTime(long date)
	{
		mTvTime.setText(getDateString(date));
	}

	/**
	 * 设置下拉刷新结束
	 */
	public void setRefreshFinish(boolean noMore)
	{
		long currentTimeMillis = System.currentTimeMillis();
		// 设置更新的时间
		mTvTime.setText(getDateString(currentTimeMillis));

		if (isLoadMore)
		{
			isLoadMore = false;

			Log.d(TAG, "加载更多完成");
			// 标记改变

			this.noMore = noMore;

			// 如果加载更多完成了
			mFooterLayout.setPadding(0, -mFooterHeight, 0, 0);
		}
		else
		{
			this.noMore = false;

			// 针对下拉刷新
			mCurrentState = STATE_PULL_DOWN_REFRESH;
			// UI更新
			refreshUI();

			// 做动画
			int start = 0;
			int end = -mRefreshHeight;
			doHeaderAnimation(start, end);
		}
	}

	private String getDateString(long time)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return sdf.format(new Date(time));
	}

	private void doHeaderAnimation(int start, int end)
	{
		// 模拟数据的变化 100-->0 100,90,80
		ValueAnimator animator = ValueAnimator.ofInt(start, end);
		animator.setDuration(300);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation)
			{
				int animatedValue = (Integer) animation.getAnimatedValue();

				mHeaderLayout.setPadding(0, animatedValue, 0, 0);
			}
		});
		animator.start();
	}

	// 更新UI
	private void refreshUI()
	{
		switch (mCurrentState)
		{
			case STATE_PULL_DOWN_REFRESH:
				// 下拉刷新
				// 1.箭头显示，进度不显示
				mIvArrow.setVisibility(View.VISIBLE);
				mProgressBar.setVisibility(View.INVISIBLE);
				// 2.箭头由上往下 :动画操作
				mIvArrow.startAnimation(up2DownAnimation);
				// 3.文本变为 下拉刷新
				mTvState.setText("下拉刷新");
				break;
			case STATE_RELEASE_REFRESH:
				// 释放刷新
				// 1.箭头显示，进度不显示
				mIvArrow.setVisibility(View.VISIBLE);
				mProgressBar.setVisibility(View.INVISIBLE);

				// 2.箭头由下往上 :动画操作
				mIvArrow.startAnimation(down2UpAnimation);

				// 3.文本变为 松开刷新
				mTvState.setText("松开刷新");

				break;
			case STATE_REFRESHING:
				// 正在刷新
				// 清空动画
				mIvArrow.clearAnimation();
				// 1.箭头不显示，进度显示
				mIvArrow.setVisibility(View.INVISIBLE);
				mProgressBar.setVisibility(View.VISIBLE);

				// 2.文本变为 正在刷新
				mTvState.setText("正在刷新");
				break;

			default:
				break;
		}
	}

	/**
	 * 设置监听
	 * 
	 * @param listener
	 */
	public void setOnRefreshListener(OnRefreshListener listener)
	{
		this.mListener = listener;
	}

	public interface OnRefreshListener
	{

		// 正在刷新时的回调
		void onRefreshing();

		// 正在加载更多时的回调
		void onLoadMore();
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState)
	{
		// 最后一个是footerView
		// ListView的position起始是从headerView算起，以footerView结束

		// 最后一个view可见的时候
		int lastVisiblePosition = getLastVisiblePosition();// adatper
		int count = getAdapter().getCount();

		Log.d(TAG, "lastVisiblePosition : " + lastVisiblePosition + " : " + count);
		// count - 1
		if (lastVisiblePosition == count - 1
			&& (scrollState == OnScrollListener.SCROLL_STATE_FLING
			|| scrollState == OnScrollListener.SCROLL_STATE_IDLE))
		{
			// 有更多并且不是加载更多
			if (!isLoadMore && !noMore)
			{
				// 改变标记
				isLoadMore = true;

				// 可以看到最后一个
				Log.d(TAG, "显示最后一个");
				mFooterLayout.setPadding(0, 0, 0, 0);// 完全显示

				setSelection(count + 2);

				if (mListener != null)
				{
					mListener.onLoadMore();
				}
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
	{

	}
}
