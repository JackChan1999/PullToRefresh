package com.github.pulltorefresh.demo62;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
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
 * des ：自定义控件62 下拉刷新
 * gitVersion：$Rev$
 * updateAuthor：$Author$
 * updateDate：$Date$
 * updateDes：${TODO}
 * ============================================================
 **/

public class RefreshListView extends ListView implements AdapterView.OnItemClickListener
{

	private View			foot;							// listview加载更多数据的尾部组件
	private LinearLayout	head;							// listview刷新数据的头部组件
	private LinearLayout	ll_refresh_head_root;
	private int				ll_refresh_head_root_Height;
	private int				ll_refresh_foot_Height;
	private float			downY			= -1;
	private final int		PULL_DOWN		= 1;			// 下拉刷新状态
	private final int		RELEASE_STATE	= 2;			// 松开刷新
	private final int		REFRESHING		= 3;			// 正在刷新
	private int				currentState	= PULL_DOWN;	// 当前的状态
	private View			lunbotu;
	private int				listViewOnScreanY;				// listview在屏幕中的y轴坐标位置
	private TextView	tv_state;
	private TextView	tv_time;
	private ImageView	iv_arrow;
	private ProgressBar	pb_loading;
	private RotateAnimation	up_ra;
	private RotateAnimation	down_ra;
	private OnRefreshDataListener listener;//刷新数据的监听回调
	
	private boolean isEnablePullRefresh;//下拉刷新是否可用
	private boolean	isLoadingMore;//是否是加载更多数据

	public RefreshListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
		initAnimation();
		initEvent();
	}

	private void initEvent() {
		//添加当前Listview的滑动事件
		setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				//状态停止，如果listview显示最后一条 加载更多数据 的显示
				// 是否最后一条数据显示
				//System.out.println(getLastVisiblePosition() + ":" + getAdapter().getCount());
				if (getLastVisiblePosition() == getAdapter().getCount() - 1 && !isLoadingMore) {
					//最后一条数据,显示加载更多的 组件
					foot.setPadding(0, 0, 0, 0);//显示加载更多
					setSelection(getAdapter().getCount());
					//加载更多数据
					
					isLoadingMore = true;
					if (listener != null) {
						listener.loadingMore();//实现该接口的组件取完成数据的加载
					}
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

			}
		});
	}

	public RefreshListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RefreshListView(Context context) {
		this(context, null);
	}

	private void initView() {
		initFoot();
		initHead();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.AbsListView#onTouchEvent(android.view.MotionEvent)
	 * 覆盖此完成自己的事件处理
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// 需要我们的功能屏蔽掉父类的touch事件
		// 下拉拖动（当listview显示第一个条数据）

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:// 按下
			downY = ev.getY();// 按下时y轴坐标
			break;
		case MotionEvent.ACTION_MOVE:// 移动

			if (!isEnablePullRefresh) {
				//没有启用下拉刷新
				break;
			}
			
			//现在是否处于刷新数据的状态
			if (currentState == REFRESHING) {
				//正在刷新
				break;
			}
			
			
			if (!isLunboFullShow()) {
				// 轮播图没有完全显示
				break;
			}

			if (downY == -1) { // 按下的时候没有获取坐标
				downY = ev.getY();
			}

			// 获取移动位置的坐标
			float moveY = ev.getY();

			// 移动的位置间距
			float dy = moveY - downY;
			// System.out.println("dy:" + dy);
			// 下拉拖动（当listview显示第一个条数据）处理自己的事件，不让listview原生的拖动事件生效
			if (dy > 0 && getFirstVisiblePosition() == 0) {

				// 当前padding top 的参数值
				float scrollYDis = -ll_refresh_head_root_Height + dy;

				if (scrollYDis < 0 && currentState != PULL_DOWN) {
					// 刷新头没有完全显示
					// 下拉刷新的状态
					currentState = PULL_DOWN;// 目的只执行一次
					refreshState();
				} else if (scrollYDis >= 0 && currentState != RELEASE_STATE) {
					currentState = RELEASE_STATE;// 记录松开刷新，只进了一次
					refreshState();
				}
				ll_refresh_head_root.setPadding(0, (int) scrollYDis, 0, 0);
				return true;
			}

			break;
		case MotionEvent.ACTION_UP:// 松开
			downY = -1;
			//判断状态
			//如果是PULL_DOWN状态,松开恢复原状
			if (currentState == PULL_DOWN) {
				ll_refresh_head_root.setPadding(0, -ll_refresh_head_root_Height, 0, 0);
			} else if (currentState == RELEASE_STATE) {
				//刷新数据
				ll_refresh_head_root.setPadding(0, 0, 0, 0);
				currentState = REFRESHING;//改变状态为正在刷新数据的状态
				refreshState();//刷新界面
				//真的刷新数据
				if (listener != null) {
					listener.refresdData();
				}
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(ev);
	}
	
	public void setOnRefreshDataListener(OnRefreshDataListener listener) {
		this.listener = listener;
	}
	public interface OnRefreshDataListener{
		void refresdData();
		void loadingMore();
	}
	
	private void initAnimation(){
		up_ra = new RotateAnimation(0, -180,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		up_ra.setDuration(500);
		up_ra.setFillAfter(true);//停留在动画结束的状态
		
		down_ra = new RotateAnimation(-180, -360,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		down_ra.setDuration(500);
		down_ra.setFillAfter(true);//停留在动画结束的状态
	}

	
	private void refreshState() {
		switch (currentState) {
		case PULL_DOWN:// 下拉刷新
			System.out.println("下拉刷新");
			//改变文件
			tv_state.setText("下拉刷新");
			iv_arrow.startAnimation(down_ra);
			break;
		case RELEASE_STATE:// 松开刷新
			System.out.println("松开刷新");
		    tv_state.setText("松开刷新");
		    iv_arrow.startAnimation(up_ra);
			break;
		case REFRESHING://正在刷新状态
			iv_arrow.clearAnimation();//清除所有动画
			iv_arrow.setVisibility(View.GONE);//隐藏箭头
			pb_loading.setVisibility(View.VISIBLE);//显示进度条
			tv_state.setText("正在刷新数据");
		default:
			break;
		}

	}
	
	/**
	 * 刷新数据成功,处理结果
	 */
	public void refreshStateFinish(){
		//下拉刷新
		if (isLoadingMore) {
			//加载更多数据
			isLoadingMore = false;
			//隐藏加载更多数据的组件
			foot.setPadding(0, -ll_refresh_foot_Height, 0, 0);
		} else {
			//改变下拉刷新
			tv_state.setText("下拉刷新");
			iv_arrow.setVisibility(View.VISIBLE);//显示箭头
			pb_loading.setVisibility(View.INVISIBLE);//隐藏进度条
			//设置刷新时间为当前时间
			tv_time.setText(getCurrentFormatDate());
			//隐藏刷新的头布局
			ll_refresh_head_root.setPadding(0, -ll_refresh_head_root_Height, 0, 0);
			
			currentState = PULL_DOWN;//初始化为下拉刷新的状态
		}
			
		
	}
	
	private String getCurrentFormatDate(){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(new Date());
	}

	/**
	 * @return 轮播图是否完全显示
	 */
	private boolean isLunboFullShow() {
		// 判断轮播图是否完全显示

		int[] location = new int[2];
		// 如果轮播图没有完全显示，相应的是Listview的事件
		// 判断轮播图是否完全显示
		// 取listview在屏幕中坐标 和 轮播图在屏幕中的坐标 判断
		// 取listview在屏幕中坐标
		if (listViewOnScreanY == 0) {
			this.getLocationOnScreen(location);
			// 获取listview在屏幕中的y轴坐标
			listViewOnScreanY = location[1];
		}

		// 轮播图在屏幕中的坐标
		lunbotu.getLocationOnScreen(location);
		// 判断
		if (location[1] < listViewOnScreanY) {
			// 轮播图没有完全显示
			// 继续相应listview的事件
			// System.out.println("没有显示");
			return false;
		}
		return true;

	}

	/**
	 * 初始化尾部组件
	 */
	private void initFoot() {
		// listview 的尾部

		foot = View.inflate(getContext(), R.layout.listview_refresh_foot, null);

		// 测量尾部组件的高度

		foot.measure(0, 0);

		// listview尾部组件的高度
		ll_refresh_foot_Height = foot.getMeasuredHeight();

		foot.setPadding(0, -ll_refresh_foot_Height, 0, 0);
		// 加载ListView中
		addFooterView(foot);
	}
	
	/**
	 * 用户自己选择是否启用下拉刷新头的功能
	 * @param isPullrefresh
	 *            true 启用下拉刷新 false 不用下拉刷新
	 * 
	 */
	public void setIsRefreshHead(boolean isPullrefresh) {
		isEnablePullRefresh = isPullrefresh;
	}

	/**
	 * @param view
	 *            轮播图view
	 */
	@Override
	public void addHeaderView(View view) {
		//判断  如果你使用下拉刷新 ，把头布局加下拉刷新的容器中，否则加载原生Listview的中
		if (isEnablePullRefresh) {
			//启用下拉刷新
			// 轮播图的组件
			lunbotu = view;
			head.addView(view);
		} else {
			//使用原生的ListView
			super.addHeaderView(view);
		}
		
	}

	/**
	 * 初始化头部组件
	 */
	private void initHead() {
		head = (LinearLayout) View.inflate(getContext(), R.layout.listview_head_container, null);
		// listview刷新头的根布局
		ll_refresh_head_root = (LinearLayout) head.findViewById(R.id.ll_listview_head_root);
		
		//获取刷新头布局的子组件
		//刷新状态的文件描述
		tv_state = (TextView) head.findViewById(R.id.tv_listview_head_state_dec);
		//最新的刷新时间
		
		tv_time = (TextView) head.findViewById(R.id.tv_listview_head_refresh_time);
		
		//下拉刷新的箭头
		
		iv_arrow = (ImageView) head.findViewById(R.id.iv_listview_head_arrow);
		
		//下拉刷新的进度
		
		pb_loading = (ProgressBar) head.findViewById(R.id.pb_listview_head_loading);
		
		// 隐藏刷新头的根布局，轮播图还要显示

		// 获取刷新头组件的高度
		ll_refresh_head_root.measure(0, 0);

		// 获取测量的高度
		ll_refresh_head_root_Height = ll_refresh_head_root.getMeasuredHeight();

		ll_refresh_head_root.setPadding(0, -ll_refresh_head_root_Height, 0, 0);

		addHeaderView(head);
	}

	OnItemClickListener mItemClickListener;

	@Override
	public void setOnItemClickListener(OnItemClickListener listener) {
		super.setOnItemClickListener(this);
		mItemClickListener = listener;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mItemClickListener != null) {
			mItemClickListener.onItemClick(parent, view, position - getHeaderViewsCount(), id);
		}
	}

}
