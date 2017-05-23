package com.github.pulltorefresh;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.pulltorefresh.demo.MeituActivity;
import com.github.pulltorefresh.demo.NormalActivity;
import com.github.pulltorefresh.demo52.Demo52Activity;

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
 * des ：${TODO}
 * gitVersion：2.12.0.windows.1
 * updateAuthor：AllenIverson
 * updateDate：2017/5/24 0:02
 * updateDes：${TODO}
 * ============================================================
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void click(View v) {

        switch (v.getId()) {
            case R.id.btn1:
                startActivity(new Intent(this, MeituActivity.class));
                break;

            case R.id.btn2:
                startActivity(new Intent(this, NormalActivity.class));
                break;

            case R.id.btn3:
                startActivity(new Intent(this, Demo52Activity.class));
                break;
        }

    }
}
