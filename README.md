# **PullToRefresh 下拉刷新 上拉加载**

添加了通用的下拉刷新，使用于任何View，具体看代码，2017-05-24

- 掌握自定义的具有下拉刷新和上拉加载功能的 ListView
- 掌握自定义的侧边栏 SlidingMenu

在日常开发工作中，应用界面常常都是用ListView进行数据展示的，并且界面可以实现下拉刷新和下拉加载功能，本文从根本上来自定义一个具有下拉刷新和上拉加载的 ListView。另外，侧边栏 SlidingMenu的应用场景也很多，这里我们也自定义一个具有侧栏栏效果的 SlidingMenu。

## **自定义控件之 ListView**

### **项目概述**

这里我们将使用前面所学的自定义控件的知识来进行自定义一个具有下拉刷新和上拉加载的ListView如图所示。

<img src="https://github.com/JackChen1999/PullToRefresh/blob/master/art/PullToRefresh-1.jpg" width="300" /> <img src="https://github.com/JackChen1999/PullToRefresh/blob/master/art/PullToRefresh-2.jpg" width="300" /> 

<img src="https://github.com/JackChen1999/PullToRefresh/blob/master/art/PullToRefresh-3.jpg" width="300" /> <img src="https://github.com/JackChen1999/PullToRefresh/blob/master/art/PullToRefresh-4.jpg" width="300" />

<img src="https://github.com/JackChen1999/PullToRefresh/blob/master/art/PullToRefresh-5.jpg" width="300" />

## **布局界面 UI**

在本章中，主界面为 MainActivity.java，具体代码如文件【2-1】所示：【文件 2-1】 activity_main.xml

```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
                xmlns:tools="http://schemas.android.com/tools"
                android:layout_width="match_parent"
                android:layout_height="match_parent">

    <com.itheima.refreshlistview.view.RefreshListView
        android:id="@+id/refreshLv"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
    </com.itheima.refreshlistview.view.RefreshListView>
</RelativeLayout>
```

另外，头布局 listview_header.xml 的代码如下所示。【文件 2-2】 listview_header.xml


```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
              android:layout_width="match_parent"
              android:layout_height="wrap_content"
              android:orientation="horizontal" >

    <FrameLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" >
        <ImageView
            android:id="@+id/header_iv"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:src="@drawable/common_listview_headview_red_arrow" />
        <ProgressBar
            android:id="@+id/header_pb"
            android:visibility="invisible"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:indeterminateDrawable="@drawable/custom_progressbar"
            android:layout_gravity="center" />
    </FrameLayout>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:gravity="center" >
        <TextView

            android:id="@+id/tv_state"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="下拉刷新"
            android:textColor="#ff0000" />
        <TextView
            android:id="@+id/tv_time"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="最近更新时间： 1999-9-9 9:9:9"
            android:textColor="#ff0000" />
    </LinearLayout>
</LinearLayout>
```

根布局 listview_footer.xml 的代码如下所示 【文件 2-3】 listview_header.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
              android:layout_width="match_parent"
              android:layout_height="wrap_content"
              android:gravity="center"
              android:orientation="horizontal" >

    <ProgressBar
        android:id="@+id/header_pb"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:indeterminateDrawable="@drawable/custom_progressbar" />

    <TextView
        android:id="@+id/tv_state"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginLeft="20dp"
        android:text="加载更多..."
        android:textColor="#ff0000" />
</LinearLayout>
```

运行程序，效果图如图所示。

<img src="http://img.blog.csdn.net/20170217134105775?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYXhpMjk1MzA5MDY2/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast" width="250" /> <img src="http://img.blog.csdn.net/20170217134122854?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYXhpMjk1MzA5MDY2/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast" width="250" /> <img src="http://img.blog.csdn.net/20170217134155089?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYXhpMjk1MzA5MDY2/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast" width="250" />

### **主界面业务逻辑**

观察市场上手机应用项目的功能界面，发现几乎都具有下拉刷新和上拉加载的功能效果，这里我们就将要实现该功能，主界面 MainActivity.java 的业务逻辑如下所示：【文件 2-4】 MainActivity.java

```java
public class MainActivity extends Activity {
        private List<String> datas;
        private Handler handler = new Handler();
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            final RefreshListView refreshLv = (RefreshListView) findViewById(R.id.refreshLv);
            initData();
            final MyAdapter adapter = new MyAdapter();
            refreshLv.setAdapter(adapter);
            refreshLv.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onFresh() {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            datas.add(0, "这是下拉刷新的新数据");
                            adapter.notifyDataSetChanged();
                            refreshLv.onFinish();
                        }

                    }, 3000);
                }
                @Override
                public void onLoadMore() {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            datas.add("这是加载更多的数据 1");
                            datas.add("这是加载更多的数据 2");
                            datas.add("这是加载更多的数据 3");
                            adapter.notifyDataSetChanged();
                            refreshLv.onFinish();
                        }
                    }, 3000);
                }
            });
        }
        private void initData() {
            datas = new ArrayList<String>();
            for (int i = 0; i < 30; i++) {
                datas.add("这是 listview 的数据" + i);
            }
        }
        private class MyAdapter extends BaseAdapter {
            @Override
            public int getCount() {
                return datas.size();
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
                TextView tv = new TextView(getApplicationContext());
                tv.setText(datas.get(position));
                tv.setTextSize(15);

                tv.setTextColor(Color.BLACK);
                tv.setPadding(5, 5, 5, 5);
                return tv;
            }
        }
    }

```

### **自定义 ListView 的业务逻辑**

下面我们将实现自定义 ListView 的主逻辑代码，自定义的 RefreshListView 通过继承 ListView 并进行相应的逻辑修改达到我们需要的效果。【文件 2-5】 RefreshListView.java

```java
    public class RefreshListView extends ListView implements OnScrollListener {
        private int downY;
        private View headerView;//头布局
        private int headerViewdHeight;//头布局的高度
        private final int DOWN_PULL = 0;//下拉刷新状态
        private final int RELEASE_REFRESH = 1;//松开刷新状态
        private final int REFRESHING = 2;//正在刷新状态
        private int currentState = DOWN_PULL;//记录当前状态，默认为下拉刷新
        private ImageView         header_iv;
        private ProgressBar       header_pb;
        private TextView          tv_state;
        private TextView          tv_time;
        private RotateAnimation   upAnimation;//向上的动画
        private RotateAnimation   downAnimation;//向下的动画
        private OnRefreshListener mOnRefreshListener;//刷新的回调接口对象
        private boolean isLoadingMore = false;//记录加载更多的状态，默认值为 false
        private View footerView;
        private int footerViewHeight;
        public RefreshListView(Context context) {
            this(context, null);
        }
        public RefreshListView(Context context, AttributeSet attrs) {
            super(context, attrs);
            initHeaderView();
            initFooterView();
            init();
            setOnScrollListener(this);
        }
        //初始化，就添加一个脚布局
        private void initFooterView() {
            footerView = View.inflate(getContext(), R.layout.listview_footer, null);

            //获取脚布局的高度
            footerView.measure(0, 0);
            footerViewHeight = footerView.getMeasuredHeight();
            //设置脚布局的 paddingtop
            footerView.setPadding(0, -footerViewHeight, 0, 0);
            this.addFooterView(footerView);
        }
        //初始化方法，这里是设置下拉刷新布局中的箭头动画
        private void init() {
            upAnimation = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            upAnimation.setDuration(500);
            upAnimation.setFillAfter(true);
            downAnimation = new RotateAnimation(-180, -360,Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            downAnimation.setDuration(500);
            downAnimation.setFillAfter(true);
        }
        //初始化头布局
        private void initHeaderView() {
            headerView = View.inflate(getContext(), R.layout.listview_header, null);
            header_iv = (ImageView) headerView.findViewById(R.id.header_iv);
            header_pb = (ProgressBar) headerView.findViewById(R.id.header_pb);
            tv_state = (TextView) headerView.findViewById(R.id.tv_state);
            tv_time = (TextView) headerView.findViewById(R.id.tv_time);
            //获得 headerview 的高度
            headerView.measure(0, 0);//让系统去测量控件的宽高
            headerViewdHeight = headerView.getMeasuredHeight();
            // headerView.getHeight();//这里获得值永远为 0，它还没经过测量
            //给 headerview 设置 paddingtop
            headerView.setPadding(0, -headerViewdHeight, 0, 0);
            //添加头布局
            this.addHeaderView(headerView);
        }
        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downY = (int) ev.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(currentState == REFRESHING){
                        break;
                    }

                    int moveY = (int) ev.getY();
                    int diff = moveY -downY;
                    int paddingTop = -headerViewdHeight + diff;
                    //获得当前列表显示的第一个条目的索引
                    int firstVisiablePosition = getFirstVisiblePosition();
                    //只有当 paddingTop 大于头部高度的负数时才进行处理
                    if(paddingTop > -headerViewdHeight&&firstVisiablePosition == 0){
                        System.out.println(currentState+"");
                        //当前头布局完全显示，为松开刷新状态,下拉刷新变成松开刷新的时候
                        if(paddingTop > 0&&currentState == DOWN_PULL){
                            System.out.println("松开刷新");
                            currentState = RELEASE_REFRESH;
                            switchViewOnStateChange();
                            //当前头布局补完全显示，为下拉刷新状态,松开刷新变成下拉刷新的时候
                        }else if((paddingTop < 0&&currentState == RELEASE_REFRESH)){
                            System.out.println("下拉刷新");
                            currentState = DOWN_PULL;
                            switchViewOnStateChange();
                        }
                        // System.out.println("paddingTop = "+ paddingTop);
                        headerView.setPadding(0, paddingTop, 0, 0);
                        return true;//自己处理触摸事件
                    }
                    // break;
                case MotionEvent.ACTION_UP:
                    if(currentState == DOWN_PULL){//当前状态为下拉刷新，隐藏头布局
                        headerView.setPadding(0, -headerViewdHeight, 0, 0);
                    }else if(currentState == RELEASE_REFRESH){//当前状态为松开刷新,改变状态
                        currentState = REFRESHING;
                        switchViewOnStateChange();
                        if(mOnRefreshListener != null){//正在刷新时，调用回调方法
                            mOnRefreshListener.onFresh();
                        }
                    }
                    break;
                default:
                    break;
            }
            return super.onTouchEvent(ev); //listview 自己处理触摸事件，
        }
        //根据当前的状态来改变头布局的内容
        private void switchViewOnStateChange(){
            switch (currentState) {

                case DOWN_PULL://下拉刷新
                    header_iv.startAnimation(downAnimation);
                    tv_state.setText("下拉刷新");
                    break;
                case RELEASE_REFRESH://松开刷新
                    header_iv.startAnimation(upAnimation);
                    tv_state.setText("松开刷新");
                    break;
                case REFRESHING://正在刷新
                    header_iv.clearAnimation();
                    header_iv.setVisibility(View.INVISIBLE);
                    header_pb.setVisibility(View.VISIBLE);
                    tv_state.setText("正在刷新...");
                    headerView.setPadding(0, 0, 0, 0);
                    break;
                default:
                    break;
            }
        }
        public void setOnRefreshListener(OnRefreshListener listener){
            this.mOnRefreshListener = listener;
        }
        //刷新的回调接口
        public interface OnRefreshListener{
            //下拉刷新回调方法
            void onFresh();
            //加载更多的回调方法
            void onLoadMore();
        }
        //当刷新完毕过后，调用的回调方法
        public void onFinish() {
            if(isLoadingMore){//加载更多
                //隐藏脚布局
                footerView.setPadding(0, -footerViewHeight, 0, 0);
                //改变状态
                isLoadingMore = false;
            }else{//加载更多
                //箭头图片显示
                header_iv.setVisibility(View.VISIBLE);
                //进度圈隐藏
                header_pb.setVisibility(View.INVISIBLE);
                //文字状态改变
                tv_state.setText("下拉刷新");
                //头布局隐藏

                headerView.setPadding(0, -headerViewdHeight, 0, 0);
                //状态值改变
                currentState = DOWN_PULL;
                //修改更新时间
                tv_time.setText("最近刷新时间： "+getTime());
            }
        }
        //获取当前刷新后的时间
        private String getTime() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(new Date());
        }
        //当滚动发生改变时，调用该方法
        // OnScrollListener.SCROLL_STATE_FLING;2 手指用力滑动一下，离开屏幕，listview 有一个
        惯性的滑动状态
        // OnScrollListener.SCROLL_STATE_IDLE;0 listview 列表处于停滞状态，手指没有触摸屏幕
        // OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;1 手指触摸着屏幕，上下滑动的状态
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            System.out.println("scrollState" + scrollState);
            //手指离开屏幕，并且列表显示到最后一条数据的时候
            int lastVisiablePosition = getLastVisiblePosition();
            if(scrollState !=OnScrollListener.SCROLL_STATE_TOUCH_SCROLL&&lastVisiablePosition
                    == (getCount()-1)&&!isLoadingMore){
                System.out.println("加载更多");
                isLoadingMore = true;
                footerView.setPadding(0, 0, 0, 0);
                setSelection(getCount()-1);
                if(mOnRefreshListener != null){
                    mOnRefreshListener.onLoadMore();
                }
            }
        }
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
        }
    }

```

自定义 ListView 之后，在主界面布局中使用改写好的 ListView 类的全路径引入，这样定义好之后，运行程序，效果图如图所示

<img src="http://img.blog.csdn.net/20170217134637216?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYXhpMjk1MzA5MDY2/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast" width="300" /> 

<img src="http://img.blog.csdn.net/20170217134334294?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYXhpMjk1MzA5MDY2/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast" width="300" />

[打造通用的Android下拉刷新组件](blog/打造通用的Android下拉刷新组件.md)
[Android打造通用的下拉刷新、上拉自动加载的组件](blog/Android打造通用的下拉刷新、上拉自动加载的组件.md)
[android_my_pull_refresh_view](blog/android_my_pull_refresh_view.md)

# 关于我

- Email：<815712739@qq.com>
- CSDN博客：[Allen Iverson](http://blog.csdn.net/axi295309066)
- 新浪微博：[AndroidDeveloper](http://weibo.com/u/1848214604?topnav=1&amp;wvr=6&amp;topsug=1&amp;is_all=1)

# License

    Copyright 2015 AllenIverson

    Copyright 2012 Jake Wharton
    Copyright 2011 Patrik Åkerfeldt
    Copyright 2011 Francisco Figueiredo Jr.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
