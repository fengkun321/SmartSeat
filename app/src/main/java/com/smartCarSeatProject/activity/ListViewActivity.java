package com.smartCarSeatProject.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.pulltorefreshlistview.PullToRefreshBase;
import com.pulltorefreshlistview.PullToRefreshListView;
import com.smartCarSeatProject.R;

import java.util.ArrayList;
import java.util.List;


public class ListViewActivity extends Activity {

    private PullToRefreshListView pullToRefreshListView;
    //adapter的数据源
    private List<String> numList=new ArrayList<String>();
    private ArrayAdapter<String> arrayAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);

        pullToRefreshListView= findViewById(R.id.pull_to_refresh_listview);
        //初始化数据
        for(int x=0;x<18;x++){
            numList.add(""+x);
        }

        arrayAdapter = new ArrayAdapter<String>(this, R.layout.item_listview,R.id.textview,numList);
        pullToRefreshListView.setAdapter(arrayAdapter);

        //设定刷新监听
        pullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {

                String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME  | DateUtils.FORMAT_SHOW_DATE  | DateUtils.FORMAT_ABBREV_ALL);

                // 显示最后更新的时间
                refreshView.getLoadingLayoutProxy() .setLastUpdatedLabel(label);

                //代表下拉刷新
                if(refreshView.getHeaderLayout().isShown()){

                    new Thread(){
                        public void run() {
                            try {
                                sleep(1000);

                                handler.sendEmptyMessage(99);

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        };
                    }.start();
                }

                //代表下拉刷新
                if(refreshView.getFooterLayout().isShown()){
                    new Thread(){
                        public void run() {
                            try {
                                sleep(1000);

                                handler.sendEmptyMessage(98);

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        };
                    }.start();
                }

            }
        });

        pullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Toast.makeText(ListViewActivity.this,"点击："+numList.get(i-1),Toast.LENGTH_SHORT).show();
            }
        });



    }



    private Handler handler=new Handler(){
        public void handleMessage(android.os.Message msg) {

            if(msg.what==99){
                numList.add(0, "英雄联盟");
                arrayAdapter.notifyDataSetChanged();
                //关闭刷新的动画
                pullToRefreshListView.onRefreshComplete();
            }

            if(msg.what==98){
                numList.add(numList.size(), "魔兽世界");
                arrayAdapter.notifyDataSetChanged();
                //关闭刷新的动画
                pullToRefreshListView.onRefreshComplete();
            }

        };
    };

}