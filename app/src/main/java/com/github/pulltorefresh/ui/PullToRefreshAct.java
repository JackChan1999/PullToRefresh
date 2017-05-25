package com.github.pulltorefresh.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.pulltorefresh.R;
import com.github.pulltorefresh.view.RecycleViewDivider;
import com.github.pulltorefresh.view.RefreshRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * ============================================================
 * Copyright：JackChan和他的朋友们有限公司版权所有 (c) 2017
 * Author：   JackChan
 * Email：    815712739@qq.com
 * GitHub：   https://github.com/JackChan1999
 * GitBook：  https://www.gitbook.com/@alleniverson
 * CSDN博客： http://blog.csdn.net/axi295309066
 * 个人博客： https://jackchan1999.github.io/
 * 微博：     AndroidDeveloper
 * <p>
 * Project_Name：PullToRefresh
 * Package_Name：com.github.pulltorefresh.ui
 * Version：1.0
 * time：2017/5/25 12:11
 * des ：${TODO}
 * gitVersion：2.12.0.windows.1
 * updateAuthor：AllenIverson
 * updateDate：2017/5/25 12:11
 * updateDes：${TODO}
 * ============================================================
 */
public class PullToRefreshAct extends AppCompatActivity {

    @BindView(R.id.rv)
    RefreshRecyclerView mRecyclerView;
    List<String> mData;
    ListAdapter  adapter;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    mRecyclerView.hideHeaderView(true);
                    break;
                case 2:
                    mRecyclerView.hideFooterView();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pull_to_refresh);
        ButterKnife.bind(this);
        initData();
        initRecyclerView();
        initEvent();
    }

    private void initData() {
        mData = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            mData.add("条目" + i);
        }
    }

    private void initEvent() {
        mRecyclerView.setOnRefreshListener(new RefreshRecyclerView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SystemClock.sleep(1000);
                        List<String> data = new ArrayList<String>();
                        for (int i = 0; i < 10; i++) {
                            data.add("下拉刷新" + i);
                        }
                        mData.addAll(0, data);
                        mHandler.obtainMessage(1).sendToTarget();
                    }
                }).start();
            }
        });

        mRecyclerView.setOnLoadMoreListener(new RefreshRecyclerView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SystemClock.sleep(1000);
                        List<String> data = new ArrayList<String>();
                        for (int i = 0; i < 10; i++) {
                            data.add("上拉加载" + i);
                        }
                        mData.addAll(data);
                        mHandler.obtainMessage(2).sendToTarget();
                    }
                }).start();
            }
        });
    }

    private void initRecyclerView() {
        ImageView iv = new ImageView(this);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setImageResource(R.mipmap.holder);
        mRecyclerView.addSwitchImageView(iv);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager
                .HORIZONTAL));
        adapter = new ListAdapter(mData);
        mRecyclerView.setAdapter(adapter);
    }

    private class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {
        private List<String> datas;

        public ListAdapter(List<String> datas) {
            this.datas = datas;
        }

        @Override
        public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout
                    .simple_list_item_1, null);
            return new ListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ListViewHolder holder, int position) {
            holder.tv.setText(datas.get(position));
        }

        @Override
        public int getItemCount() {
            return datas != null ? datas.size() : 0;
        }

        class ListViewHolder extends RecyclerView.ViewHolder {
            private TextView tv;

            public ListViewHolder(View itemView) {
                super(itemView);
                tv = (TextView) itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
