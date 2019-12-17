package com.huatu.tiger.theagedlauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.huatu.tiger.theagedlauncher.adapter.SimpleCommonRVAdapter;
import com.huatu.tiger.theagedlauncher.base.BaseActivity;
import com.huatu.tiger.theagedlauncher.bean.AppInfo;
import com.huatu.tiger.theagedlauncher.page.AllAppFragment;
import com.huatu.tiger.theagedlauncher.utils.DateTimeUtil;
import com.huatu.tiger.theagedlauncher.utils.DisplayUtil;
import com.huatu.tiger.theagedlauncher.utils.LoadAppUtils;
import com.huatu.tiger.theagedlauncher.view.SimpleCircleIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class LauncherActivity extends BaseActivity {
    RecyclerView mRecyclerView;
    SimpleCircleIndicator mIndicatorView;
    LinearLayoutManager linearLayoutManager;
    int height, width;
    TextView mTimeTv,mDateTv;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        mRecyclerView = findViewById(R.id.workspace);
        mIndicatorView = findViewById(R.id.indictor_view);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int position = linearLayoutManager.findFirstVisibleItemPosition();
                mIndicatorView.onPageScrolled(position, 0.0f, dx);
            }
        });
        loadApps();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        openLocal();
    }

    @Override
    public void onBackPressed() {
        
    }

    int middleIndex = 0;

    private void loadApps() {
        List<AppInfo> appInfos = LoadAppUtils.getInstance(this).getDefaultApps();

        Observable.just(appInfos).map(new Function<List<AppInfo>, Map<Integer, List<AppInfo>>>() {
            @Override
            public Map<Integer, List<AppInfo>> apply(List<AppInfo> appInfos) throws Exception {
                Map<Integer, List<AppInfo>> appList = new HashMap<>();
                int index = 0;
                for (AppInfo info : appInfos) {
                    List<AppInfo> infos = appList.get(index);
                    if (infos == null) {
                        infos = new ArrayList<>();
                    }
                    infos.add(info);
                    appList.put(index, infos);
                    if (index == middleIndex && infos.size() >= 4) {
                        index += 4;
                    } else if (infos.size() >= 6)
                        index += 6;
                }
                return appList;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).map(new Function<Map<Integer, List<AppInfo>>, List<View>>() {
            @Override
            public List<View> apply(Map<Integer, List<AppInfo>> integerListMap) throws Exception {
                List<View> views = new ArrayList<>();
                views.add(createContactList());
                for (Integer index : integerListMap.keySet()) {
                    views.add(createList(integerListMap.get(index), index == middleIndex));
                }
                mIndicatorView.setPageNum(views.size());
                return views;
            }
        }).subscribe(new Consumer<List<View>>() {
            @Override
            public void accept(List<View> views) throws Exception {
                mRecyclerView.setAdapter(new SimpleCommonRVAdapter<View>(views, R.layout.workspace_layout, LauncherActivity.this) {
                    @Override
                    public int getItemCount() {
                        return super.getItemCount();
                    }

                    @Override
                    public void convert(SimpleViewHolder holder, View item, int position) {
                        holder.itemView.setLayoutParams(new ViewGroup.LayoutParams(DisplayUtil.getScreenWidth(LauncherActivity.this), DisplayUtil.getScreenHeight(LauncherActivity.this)));
                        ((ViewGroup) holder.itemView).removeAllViews();
                        ((ViewGroup) holder.itemView).addView(item);
                    }
                });
                mRecyclerView.scrollToPosition(1);
            }
        });
    }


    private View createContactList() {
        List<AppInfo> contactsList = LoadAppUtils.getInstance(this).getContactList();
        if (height == 0)
            height = (DisplayUtil.getScreenHeight(LauncherActivity.this) - (int) DisplayUtil.dp2px(80, this)) / 3;
        if (width == 0)
            width = DisplayUtil.getScreenWidth(LauncherActivity.this) / 2;

        View rootView = getLayoutInflater().inflate(R.layout.cell_layout, null);
        RecyclerView recyclerView = rootView.findViewById(R.id.cell_list);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(new SimpleCommonRVAdapter<AppInfo>(contactsList, R.layout.cell_item, this) {
            @Override
            public void convert(SimpleViewHolder holder, AppInfo item, int position) {
                holder.setText(R.id.name, item.label);
                holder.setImageResource(R.id.icon, item.icon);
                holder.itemView.setLayoutParams(new ViewGroup.LayoutParams(width, height));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
            }
        });
        return rootView;
    }

    private View createList(List<AppInfo> appInfos, boolean isShowExpand) {
        if (height == 0)
            height = (DisplayUtil.getScreenHeight(LauncherActivity.this) - (int) DisplayUtil.dp2px(80, this)) / 3;
        if (width == 0)
            width = DisplayUtil.getScreenWidth(LauncherActivity.this) / 2;
        View rootView = null;
        if (isShowExpand) {
            rootView = getLayoutInflater().inflate(R.layout.app_widget_layout, null);
            View topView = rootView.findViewById(R.id.top);
            mTimeTv = rootView.findViewById(R.id.time_tv);
            mDateTv = rootView.findViewById(R.id.date_tv);
            topView.getLayoutParams().height = height - (int) DisplayUtil.dp2px(20, LauncherActivity.this);
            updateTime();
            register();
        } else rootView = getLayoutInflater().inflate(R.layout.cell_layout, null);
        RecyclerView recyclerView = rootView.findViewById(R.id.cell_list);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(new SimpleCommonRVAdapter<AppInfo>(appInfos, R.layout.cell_item, this) {

            @Override
            public void convert(SimpleViewHolder holder, AppInfo item, int position) {
                holder.setText(R.id.name, item.label);
                holder.setImageResource(R.id.icon, item.icon);
                holder.itemView.setLayoutParams(new ViewGroup.LayoutParams(width, height));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (item.type == LoadAppUtils.ALL_APP_TYPE) {
                            getSupportFragmentManager().beginTransaction().add(R.id.fragment_layout, AllAppFragment.getFragment(null)).commitNow();

                        } else if (item.type == LoadAppUtils.ADD_TYPE) {

                        } else {
                            Intent intent = new Intent();
                            intent.setClassName(item.packageName, item.appName);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }
                });
            }
        });
        return rootView;
    }

    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    Log.e("Launcher", aMapLocation.getAddress());
                } else {
                    String info = aMapLocation.getErrorInfo();
                    Toast.makeText(getApplicationContext(), info, Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;

    private void openLocal() {
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        mLocationOption.setOnceLocation(true);
        mLocationOption.setInterval(5000);
        mLocationOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Transport);
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.stopLocation();
        mLocationClient.startLocation();
    }

    private void register(){
        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        registerReceiver(mReceiver, filter);
    }

    private void updateTime(){
        String timeStr = DateTimeUtil.getInstance().getCurrentTimeHHMM();
        mTimeTv.setText(timeStr);
        String dateStr = DateTimeUtil.getInstance().getCurrentDate() + " " + DateTimeUtil.getInstance().getCurrentWeekDay(0);
        mDateTv.setText(dateStr);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(Intent.ACTION_TIME_TICK.equals(action)){
                updateTime();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mReceiver);
        }catch (Exception e){

        }
    }
}
