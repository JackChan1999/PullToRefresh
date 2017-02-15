package com.github.pulltorefresh;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.github.pulltorefresh.view.RefreshListView;

import java.util.ArrayList;
/**
 * ============================================================
 * Copyright：${TODO}有限公司版权所有 (c) 2017
 * Author：   陈冠杰
 * Email：    815712739@qq.com
 * GitHub：   https://github.com/JackChen1999
 * 博客：     http://blog.csdn.net/axi295309066
 * 微博：     AndroidDeveloper
 * <p>
 * Project_Name：PullToRefresh
 * Package_Name：com.github.pulltorefresh
 * Version：1.0
 * time：2016/2/15 17:18
 * des ：${TODO}
 * gitVersion：$Rev$
 * updateAuthor：$Author$
 * updateDate：$Date$
 * updateDes：${TODO}
 * ============================================================
 **/
public class MainActivity extends Activity {
	private RefreshListView refreshListView;
		private ArrayList<String> list = new ArrayList<String>();

	private MyAdapter adapter;
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			//更新UI
			adapter.notifyDataSetChanged();
			refreshListView.completeRefresh();
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		initData();
	}
	
	private void initView() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		refreshListView = (RefreshListView) findViewById(R.id.refreshListView);
	}
	
	private void initData() {
		for (int i = 0; i < 15; i++) {
			list.add("listview原来的数据 - "+i);
		}
		
		
//		final View headerView = View.inflate(this, R.layout.layout_header, null);
		//第一种方法
//		headerView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
//			@Override
//			public void onGlobalLayout() {
//				headerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//				int headerViewHeight = headerView.getHeight();
//				
//				
//				Log.e("MainActivity", "headerViewHeight: "+headerViewHeight);
//				headerView.setPadding(0, -headerViewHeight, 0, 0);
//				refreshListView.addHeaderView(headerView);//
//			}
//		});
		//第二种方法
//		headerView.measure(0, 0);//主动通知系统去测量
//		int headerViewHeight = headerView.getMeasuredHeight();
//		Log.e("MainActivity", "headerViewHeight: "+headerViewHeight);
//		headerView.setPadding(0, -headerViewHeight, 0, 0);
//		refreshListView.addHeaderView(headerView);//
		
		
		
		adapter = new MyAdapter();
		refreshListView.setAdapter(adapter);
		
		refreshListView.setOnRefreshListener(new RefreshListView.OnRefreshListener() {
			@Override
			public void onPullRefresh() {
				//需要联网请求服务器的数据，然后更新UI
				requestDataFromServer(false);
			}

			@Override
			public void onLoadingMore() {
				requestDataFromServer(true);
			}
		});
		
	}
	/**
	 * 模拟向服务器请求数据
	 */
	private void requestDataFromServer(final boolean isLoadingMore){
		new Thread(){
			public void run() {
				SystemClock.sleep(3000);//模拟请求服务器的一个时间长度
				
				if(isLoadingMore){
					list.add("加载更多的数据-1");
					list.add("加载更多的数据-2");
					list.add("加载更多的数据-3");
				}else {
					list.add(0, "下拉刷新的数据");
				}
				
				//在UI线程更新UI
				handler.sendEmptyMessage(0);
			};
		}.start();
	}
	
	class MyAdapter extends BaseAdapter{
		@Override
		public int getCount() {
			return list.size();
		}
		@Override
		public Object getItem(int position) {
			return null;
		}
		@Override
		public long getItemId(int position) {
			return 0;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView textView = new TextView(MainActivity.this);
			textView.setPadding(20, 20, 20, 20);
			textView.setTextSize(18);
			
			textView.setText(list.get(position));
			
			return textView;
		}
		
	}

}
