package com.github.pulltorefresh.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

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
 * Package_Name：com.github.pulltorefresh
 * Version：1.0
 * time：2017/5/24 0:02
 * des ：包装的适配器：区分头、脚、正常的数据(返回值是0)
 * gitVersion：2.12.0.windows.1
 * updateAuthor：AllenIverson
 * updateDate：2017/5/24 0:02
 * updateDes：${TODO}
 * ============================================================
 */

public class WrapAdapter extends RecyclerView.Adapter {

    //头布局
    private View                 mHeaderView;
    //脚布局
    private View                 mFooterView;
    //正常的适配器
    private RecyclerView.Adapter mAdapter;

    public WrapAdapter(View mHeaderView, View mFooterView, RecyclerView.Adapter mAdapter) {
        this.mHeaderView = mHeaderView;
        this.mFooterView = mFooterView;
        this.mAdapter = mAdapter;
    }

    //处理条目的类型
    @Override
    public int getItemViewType(int position) {
        //头
        if (position == 0) {
            return RecyclerView.INVALID_TYPE;//-1
        }
        //正常的布局
        int adjPoistion = position - 1;
        int adapterCount = mAdapter.getItemCount();
        if (adjPoistion < adapterCount) {
            return mAdapter.getItemViewType(adjPoistion);
        }
        //脚
        return RecyclerView.INVALID_TYPE - 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == RecyclerView.INVALID_TYPE) {
            //头
            return new HeaderViewHolder(mHeaderView);
        } else if (viewType == RecyclerView.INVALID_TYPE - 1) {
            //脚
            return new FooterViewHolder(mFooterView);
        }
        //正常
        return mAdapter.onCreateViewHolder(parent, viewType);
    }

    //绑定数据
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //头
        if (position == 0) {
            return;
        }
        //正常布局
        int adjPosition = position - 1;
        int adapterCount = mAdapter.getItemCount();
        if (adjPosition < adapterCount) {
            mAdapter.onBindViewHolder(holder, adjPosition);
        }
    }

    @Override
    public int getItemCount() {
        return mAdapter.getItemCount() + 2;
    }

    //头的ViewHolder
    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    //脚的ViewHolder
    static class FooterViewHolder extends RecyclerView.ViewHolder {

        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }

}
