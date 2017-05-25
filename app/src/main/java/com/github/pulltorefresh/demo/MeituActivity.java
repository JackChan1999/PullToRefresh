package com.github.pulltorefresh.demo;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.github.pulltorefresh.R;

public class MeituActivity extends AppCompatActivity {


	private RecyclerView recyclerView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_meitu);
		final RefreshLayout refreshLayout = (RefreshLayout) findViewById(R.id.refresh_layout);
		refreshLayout.setSelfHeaderViewManager(new MeiTuanSelfHeaderViewManager(this));
		refreshLayout.setOnRefreshingListener(new RefreshLayout.OnRefreshingListener() {
			@Override
			public void onRefresh() {
				//获取网络数据
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						//当获取完数据后通知RefreshLayout还原
						refreshLayout.endRefreshing();
					}
				}, 2000);
			}
		});
//		initRecyclerView();
//	}
//	private void initRecyclerView() {
//		recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
//		recyclerView.setLayoutManager(new LinearLayoutManager(this));
//		List<String> datas = new ArrayList<>();
//		for (int i = 0; i < 30; i++) {
//			datas.add("条目" + i);
//		}
//		ListAdapter adapter = new ListAdapter(datas);
//		recyclerView.setAdapter(adapter);
//	}
//
//
//
//	private class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {
//		private List<String> datas;
//
//		public ListAdapter(List<String> datas) {
//			this.datas = datas;
//		}
//
//		@Override
//		public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//			View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, null);//TODO
//			return new ListViewHolder(view);
//		}
//
//		@Override
//		public void onBindViewHolder(ListViewHolder holder, int position) {
//			holder.tv.setText(datas.get(position));
//		}
//
//		@Override
//		public int getItemCount() {
//			return datas.size();
//		}
//
//		class ListViewHolder extends RecyclerView.ViewHolder {
//			private TextView tv;
//
//			public ListViewHolder(View itemView) {
//				super(itemView);
//				tv = (TextView) itemView.findViewById(android.R.id.text1);
//			}
//		}
	}
}
