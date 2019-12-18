package com.huatu.tiger.theagedlauncher;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
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
    TextView mTimeTv, mDateTv;
    AllAppFragment allAppFragment;

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
        DisplayUtil.addPermissByPermissionList(this, new String[]{"android.permission.READ_CONTACTS"}, 1001);
        DisplayUtil.computeWidth(this);
        loadApps();
        register();
        LoadAppUtils.getInstance(this).getAllNewApp().subscribe();
    }

    @Override
    public void onBackPressed() {
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        if (fragmentList == null || fragmentList.isEmpty())
            return;
        for (Fragment fragment : fragmentList) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commitNow();
        }
    }

    int middleIndex = 0;

    private void loadApps() {
        List<View> views = new ArrayList<>();
        Observable.just(1).map(new Function<Integer, Map<String, List<AppInfo>>>() {
            @Override
            public Map<String, List<AppInfo>> apply(Integer integer) throws Exception {
                Map<String, List<AppInfo>> appList = new HashMap<>();
                List<AppInfo> contactList = LoadAppUtils.getInstance(getBaseContext()).getContactList();
                int index = middleIndex = (int)Math.floor(contactList.size()/6);
                for (AppInfo info : contactList) {
//                    AppInfo info = contactList.get(position);
                    List<AppInfo> infos = appList.get(index+"");
                    if (infos == null) {
                        infos = new ArrayList<>();
                    }
                    infos.add(info);
                    appList.put(index + "", infos);
                    if (infos.size() >= 6) {
                        index -= 1;
                        Log.d("Launcherss", "-ccc-->" + index);
//                        break;
                    }
                }
                middleIndex += 1;
                index = middleIndex;
                List<AppInfo> appInfos = LoadAppUtils.getInstance(getBaseContext()).getDefaultApps();
                for (AppInfo info : appInfos) {
                    List<AppInfo> infos = appList.get(index+"");
                    if (infos == null) {
                        infos = new ArrayList<>();
                    }
                    infos.add(info);
                    appList.put(index + "", infos);
                    if (index == middleIndex && infos.size() >= 4) {
                        index += 1;
                        Log.d("Launcherss", "4--->" + index);
                    } else if (infos.size() >= 6) {
                        index += 1;
                        Log.d("Launcherss", "6--->" + index);
                    }
                }
                Log.d("Launcherss", "map--->" + appList.toString());
                return appList;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).map(new Function<Map<String, List<AppInfo>>, List<View>>() {
            @Override
            public List<View> apply(Map<String, List<AppInfo>> integerListMap) throws Exception {
                for (String index : integerListMap.keySet()) {
                    views.add(createList(integerListMap.get(index)));
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
                mRecyclerView.scrollToPosition(middleIndex);
            }
        });
    }

    private View createList(List<AppInfo> appInfos) {
        View rootView = null;
        boolean isShowExpand = appInfos.size() == 4;
        if (isShowExpand) {
            rootView = getLayoutInflater().inflate(R.layout.app_widget_layout, null);
            View topView = rootView.findViewById(R.id.top);
            mTimeTv = rootView.findViewById(R.id.time_tv);
            mDateTv = rootView.findViewById(R.id.date_tv);
            topView.getLayoutParams().height = DisplayUtil.realHeight - (int) DisplayUtil.dp2px(20, LauncherActivity.this);
            updateTime();
        } else rootView = getLayoutInflater().inflate(R.layout.cell_layout, null);
        RecyclerView recyclerView = rootView.findViewById(R.id.cell_list);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(new SimpleCommonRVAdapter<AppInfo>(appInfos, R.layout.cell_item, this) {

            @Override
            public void convert(SimpleViewHolder holder, AppInfo item, int position) {
                holder.setText(R.id.name, item.label);
                holder.setImageResource(R.id.icon, item.icon);
                holder.itemView.setLayoutParams(new ViewGroup.LayoutParams(DisplayUtil.realWidh, DisplayUtil.realHeight));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (item.type == LoadAppUtils.ALL_APP_TYPE) {
                            allAppFragment = AllAppFragment.getFragment(LoadAppUtils.ALL_APP_TYPE);
                            getSupportFragmentManager().beginTransaction().add(R.id.fragment_layout, allAppFragment).commitNow();
                            addAppFragmentListener();
                        } else if (item.type == LoadAppUtils.ADD_APP_TYPE) {
                            allAppFragment = AllAppFragment.getFragment(LoadAppUtils.ADD_APP_TYPE);
                            getSupportFragmentManager().beginTransaction().add(R.id.fragment_layout, allAppFragment).commitNow();
                            addAppFragmentListener();
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

    private void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction("com.huatu.tiger.app.change");
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mReceiver, filter);
    }

    private void updateTime() {
        if (mTimeTv == null)
            return;
        String timeStr = DateTimeUtil.getInstance().getCurrentTimeHHMM();
        mTimeTv.setText(timeStr);
        String dateStr = DateTimeUtil.getInstance().getCurrentDate() + " " + DateTimeUtil.getInstance().getCurrentWeekDay(0);
        mDateTv.setText(dateStr);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        static final String SYSTEM_REASON = "reason";
        static final String SYSTEM_HOME_KEY = "homekey";// home key
        static final String SYSTEM_RECENT_APPS = "recentapps";// long home key

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("Launcherss", action);
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_REASON);
                if (SYSTEM_HOME_KEY.equals(reason)) {
                    Log.d("Launcherss", "--->" + SYSTEM_HOME_KEY); //捕获到Home键
                } else if (SYSTEM_RECENT_APPS.equals(reason)) {
                    Log.d("Launcherss", "--->" + SYSTEM_RECENT_APPS); //捕获到最近打开的Activity
                }

            } else if (Intent.ACTION_TIME_TICK.equals(action)) {
                updateTime();
            } else if ("com.huatu.tiger.app.change".equals(action)) {
                if(allAppFragment != null)
                LoadAppUtils.getInstance(context).getAllNewApp().observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<List<AppInfo>>() {
                    @Override
                    public void accept(List<AppInfo> appInfos) throws Exception {
                        allAppFragment.updateInfos(appInfos);
                    }
                });
                LoadAppUtils.getInstance(context).mDefaltInfos = null;
                loadApps();
            }
        }
    };

    private void addAppFragmentListener() {
        allAppFragment.setAppFragmentListenner(new AllAppFragment.AppFragmentListenner() {
            @Override
            public void onAppAdded(AppInfo info) {
                LoadAppUtils.getInstance(LauncherActivity.this).mDefaltInfos = null;
                loadApps();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1001 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            loadApps();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {

        }
    }

}
