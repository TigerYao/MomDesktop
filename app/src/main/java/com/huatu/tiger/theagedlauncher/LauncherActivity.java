package com.huatu.tiger.theagedlauncher;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.huatu.tiger.theagedlauncher.adapter.SimpleCommonRVAdapter;
import com.huatu.tiger.theagedlauncher.base.BaseActivity;
import com.huatu.tiger.theagedlauncher.bean.AppInfo;
import com.huatu.tiger.theagedlauncher.utils.DisplayUtil;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        mRecyclerView = findViewById(R.id.workspace);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(mRecyclerView);
        loadApps();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    private void loadApps() {
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
                        appInfo.appName = info.getLabel().toString();
                        appInfo.icon = info.getApplicationInfo().loadIcon(packageManager);
                        appInfo.isSystem = ((info.getApplicationInfo().flags & ApplicationInfo.FLAG_SYSTEM) != 0);
                        list.add(appInfo);
                    }
                } else {
                    final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    mainIntent.setPackage(null);
                    List<ResolveInfo> infos = packageManager.queryIntentActivities(mainIntent, 0);
                    for (ResolveInfo info : infos) {
                        AppInfo appInfo = new AppInfo();
                        appInfo.packageName = info.activityInfo.packageName;
                        appInfo.appName = info.loadLabel(packageManager).toString();
                        appInfo.icon = info.loadIcon(packageManager);
                        appInfo.isSystem = ((info.activityInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
                        list.add(appInfo);
                    }
                }

                return list;
            }
        }).map(new Function<List<AppInfo>, Map<Integer, List<AppInfo>>>() {
            @Override
            public Map<Integer, List<AppInfo>> apply(List<AppInfo> appInfos) throws Exception {
                Map<Integer, List<AppInfo>> appList = new HashMap<>();
                for (AppInfo info : appInfos) {
                    if (info.isSystem) {
                        List<AppInfo> list = appList.get(0);
                        if (list == null)
                           list = new ArrayList<>();
                        list.add(info);
                        appList.put(0, list);
                    }else{
                        List<AppInfo> list = appList.get(1);
                        if (list == null)
                            list = new ArrayList<>();
                        list.add(info);
                        appList.put(1, list);
                    }

                }
                return appList;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).map(new Function<Map<Integer, List<AppInfo>>, List<View>>() {
            @Override
            public List<View> apply(Map<Integer, List<AppInfo>> integerListMap) throws Exception {
                List<View> views = new ArrayList<>();
                for(Integer index : integerListMap.keySet()){
                    views.add(createList(integerListMap.get(index)));
                }
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
            }
        });
    }
    int height, width;
    private View createList(List<AppInfo> appInfos){
        if(height == 0)
         height = (DisplayUtil.getScreenHeight(LauncherActivity.this) - (int)DisplayUtil.dp2px(50, this))/ 3;
        if(width == 0)
         width = DisplayUtil.getScreenWidth(LauncherActivity.this)/2;
        RecyclerView recyclerView = (RecyclerView) getLayoutInflater().inflate(R.layout.cell_layout, null);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(new SimpleCommonRVAdapter<AppInfo>(appInfos, R.layout.cell_item, this) {
            @Override
            public void convert(SimpleViewHolder holder, AppInfo item, int position) {
                holder.setText(R.id.name, item.appName);
                holder.setImageResource(R.id.icon, item.icon);
                holder.itemView.setLayoutParams(new ViewGroup.LayoutParams(width, height));
            }

            @Override
            public int getItemViewType(int position) {
                return super.getItemViewType(position);
            }
        });
        return recyclerView;
    }

    private void createAppwidget(ViewGroup viewGroup){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(LauncherActivity.this);
        List<AppWidgetProviderInfo> providerInfos = appWidgetManager.getInstalledProviders();
        AppWidgetProviderInfo appWidgetProviderInfo = providerInfos.get(0);
        //其参数hostid大意是指定该AppWidgetHost 即本Activity的标记Id， 直接设置为一个整数值吧 。
        AppWidgetHost mAppWidgetHost = new AppWidgetHost(LauncherActivity.this, 1024) ;
        int id = mAppWidgetHost.allocateAppWidgetId();
        //为了保证AppWidget的及时更新 ， 必须在Activity的onCreate/onStar方法调用该方法
        // 当然可以在onStop方法中，调用mAppWidgetHost.stopListenering() 停止AppWidget更新
        mAppWidgetHost.startListening();
        AppWidgetHostView hostView = mAppWidgetHost.createView(LauncherActivity.this, id, appWidgetProviderInfo);

        //linearLayout.addView(hostView) ;

        int widget_minWidht = appWidgetProviderInfo.minWidth ;
        int widget_minHeight = appWidgetProviderInfo.minHeight ;
        //设置长宽  appWidgetProviderInfo 对象的 minWidth 和  minHeight 属性
        ViewGroup.LayoutParams linearLayoutParams = new ViewGroup.LayoutParams(widget_minWidht, widget_minHeight);
        //添加至LinearLayout父视图中
        viewGroup.addView(hostView,linearLayoutParams) ;
    }
}
