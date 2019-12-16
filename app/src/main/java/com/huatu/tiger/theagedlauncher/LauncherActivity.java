package com.huatu.tiger.theagedlauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.huatu.tiger.theagedlauncher.utils.DisplayUtil;
import com.huatu.tiger.theagedlauncher.view.SimpleCircleIndicator;

import java.util.ArrayList;
import java.util.Arrays;
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
        loadApps();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int position = linearLayoutManager.findFirstVisibleItemPosition();
//                if (indexNum > 1) {
//                    indicatorIndex = (position / 4) % indexNum;
//                    if (position % 4 > 0) {
//                        indicatorIndex = indicatorIndex == indexNum ? 0 : indicatorIndex + 1;
//                    }
                mIndicatorView.onPageScrolled(position, 0.0f, dx);
//                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        openLocal();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    int middleIndex = 0;

    private void loadApps() {
        List<String> defaltApps = Arrays.asList(getResources().getStringArray(R.array.defalt_apps));
        Observable.just(this.getPackageManager()).map(new Function<PackageManager, List<AppInfo>>() {
            @Override
            public List<AppInfo> apply(PackageManager packageManager) throws Exception {
                List<AppInfo> list = new ArrayList<AppInfo>();
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    LauncherApps launcherApps = (LauncherApps) getSystemService(Context.LAUNCHER_APPS_SERVICE);
                    UserManager userManager = (UserManager) getSystemService(Context.USER_SERVICE);
                    List<LauncherActivityInfo> infos = launcherApps.getActivityList(null, userManager.getUserProfiles().get(0));
                    for (LauncherActivityInfo info : infos) {
                        AppInfo appInfo = new AppInfo();
                        appInfo.packageName = info.getComponentName().getPackageName();
                        appInfo.label = info.getLabel().toString();
                        appInfo.isSystem = ((info.getApplicationInfo().flags & ApplicationInfo.FLAG_SYSTEM) != 0);
                        appInfo.appName = info.getName();
                        appInfo.icon = appInfo.isSystem && info.getIcon(1)!=null? info.getIcon(1) : info.getApplicationInfo().loadIcon(packageManager);
                        appInfo.iconId = info.getApplicationInfo().icon;
                        if (defaltApps.contains(appInfo.label)) {
                            list.add(appInfo);
                        }
                        if(list.size() >= 4)
                            break;
                        Log.i("Launcherss", appInfo.toString());
                    }
                } else {
                    final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    mainIntent.setPackage(null);
                    List<ResolveInfo> infos = packageManager.queryIntentActivities(mainIntent, 0);
                    for (ResolveInfo info : infos) {
                        AppInfo appInfo = new AppInfo();
                        appInfo.packageName = info.activityInfo.packageName;
                        appInfo.appName = info.activityInfo.name;
                        appInfo.label = info.loadLabel(packageManager).toString();
                        appInfo.icon = info.loadIcon(packageManager);
                        appInfo.iconId = info.icon;
                        appInfo.isSystem = ((info.activityInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
                        Log.i("Launcherss", appInfo.toString());
                        if (defaltApps.contains(appInfo.label))
                            list.add(appInfo);
                        if(list.size() >= 4)
                            break;
                    }

                }

                return list;
            }
        }).map(new Function<List<AppInfo>, Map<Integer, List<AppInfo>>>() {
            @Override
            public Map<Integer, List<AppInfo>> apply(List<AppInfo> appInfos) throws Exception {
                Map<Integer, List<AppInfo>> appList = new HashMap<>();
                int index = 0;
                middleIndex = (appInfos.size() / 6) / 2;
                for (AppInfo info : appInfos) {
//                    if (!info.isSystem)
//                        continue;
                    List<AppInfo> list = appList.get(index);
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    list.add(info);
                    appList.put(index, list);
                    if (list.size() == 4 && index == middleIndex)
                        index += 1;
                    else if (list.size() >= 6)
                        index += 1;

                }
                return appList;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).map(new Function<Map<Integer, List<AppInfo>>, List<View>>() {
            @Override
            public List<View> apply(Map<Integer, List<AppInfo>> integerListMap) throws Exception {
                List<View> views = new ArrayList<>();
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
                    public void convert(SimpleViewHolder holder, View item, int position) {
                        ((ViewGroup) holder.itemView).setLayoutParams(new ViewGroup.LayoutParams(DisplayUtil.getScreenWidth(LauncherActivity.this), DisplayUtil.getScreenHeight(LauncherActivity.this)));
                        ((ViewGroup) holder.itemView).removeAllViews();
                        ((ViewGroup) holder.itemView).addView(item);
                    }
                });
                mRecyclerView.scrollToPosition(middleIndex);
            }
        });
    }

    int height, width;

    private View createList(List<AppInfo> appInfos, boolean isShowExpand) {
        if (height == 0)
            height = (DisplayUtil.getScreenHeight(LauncherActivity.this) - (int) DisplayUtil.dp2px(80, this)) / 3;
        if (width == 0)
            width = DisplayUtil.getScreenWidth(LauncherActivity.this) / 2;
        View rootView = null;
        if (isShowExpand) {
            rootView = getLayoutInflater().inflate(R.layout.app_widget_layout, null);
            View topView = rootView.findViewById(R.id.top);
            topView.getLayoutParams().height = height - (int)DisplayUtil.dp2px(20, LauncherActivity.this);
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
                        Intent intent = new Intent();
                        intent.setClassName(item.packageName, item.appName);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
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

}
