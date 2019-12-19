package com.huatu.tiger.theagedlauncher;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

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
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.huatu.tiger.theagedlauncher.utils.LoadAppUtils.ADD_CONTACT_TYPE;
import static com.huatu.tiger.theagedlauncher.utils.LoadAppUtils.ALL_APP_TYPE;

public class LauncherActivity extends BaseActivity {
    RecyclerView mRecyclerView;
    SimpleCircleIndicator mIndicatorView;
    LinearLayoutManager linearLayoutManager;
    TextView mTimeTv, mDateTv;
    AllAppFragment allAppFragment;
    AppInfo mCurrentInfo;
    int middleIndex = 0;
//    //声明AMapLocationClient类对象
//    private AMapLocationClient mLocationClient = null;
//    //声明定位回调监听器
//    private AMapLocationListener mLocationListener = new AMapLocationListener() {
//        @Override
//        public void onLocationChanged(AMapLocation aMapLocation) {
//            if (aMapLocation != null) {
//                if (aMapLocation.getErrorCode() == 0) {
//                    Log.e("Launcher", aMapLocation.getAddress());
//                } else {
//                    String info = aMapLocation.getErrorInfo();
//                    Toast.makeText(getApplicationContext(), info, Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
//    };
//    //声明AMapLocationClientOption对象
//    private AMapLocationClientOption mLocationOption = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        initView();
        initData();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
            return;
        if (requestCode == 1001)
            readContacts(ADD_CONTACT_TYPE);
        else if (requestCode == 1002)
            callNum(mCurrentInfo);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {

        }
    }

    //界面预处理
    private void initView() {
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
    }

    //预加载数据
    private void initData() {
        DisplayUtil.computeWidth(this);
        loadApps();
        register();
        Observable.create(new ObservableOnSubscribe<List<AppInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<AppInfo>> e) throws Exception {
                LoadAppUtils.getInstance(LauncherActivity.this).getAllApps(true);
                LoadAppUtils.getInstance(LauncherActivity.this).getContactList(true);
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    //处理界面
    private void addAppFragmentListener() {
        allAppFragment.setAppFragmentListenner(new AllAppFragment.AppFragmentListenner() {
            @Override
            public void onAppAdded(List<AppInfo> infos) {
                LoadAppUtils.getInstance(LauncherActivity.this).mDefaltInfos = infos;
                loadApps();
                Observable.fromIterable(infos).flatMap(new Function<AppInfo, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(AppInfo info) throws Exception {
                        if (info.type != ALL_APP_TYPE && !info.packageName.equals("all_app")) {
                            info.type = LoadAppUtils.DEFAULT_APP_TYPE;
                            LoadAppUtils.getInstance(LauncherActivity.this).getAppDao().insertApps(info);
                        }
                        return Observable.just(info);
                    }
                }).subscribeOn(Schedulers.io()).subscribe();
                onBackPressed();
            }

            @Override
            public void onContactsAdded(List<AppInfo> infos) {
                LoadAppUtils.getInstance(LauncherActivity.this).mDefaultContacts = infos;
                loadApps();
                Observable.fromIterable(infos).flatMap(new Function<AppInfo, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(AppInfo info) throws Exception {
                        if (info.type != ADD_CONTACT_TYPE && !info.packageName.equals("all_person")) {
                            info.type = LoadAppUtils.DEFAULT_CONTACT_TYPE;
                            LoadAppUtils.getInstance(LauncherActivity.this).getAppDao().insertApps(info);
                        }
                        return Observable.just(info);
                    }
                }).subscribeOn(Schedulers.io()).subscribe();
                onBackPressed();
            }
        });
    }

    //加载数据
    private void loadApps() {
        List<View> views = new ArrayList<>();
        Observable.just(1)
                .map(new Function<Integer, Map<String, List<AppInfo>>>() {
                    @Override
                    public Map<String, List<AppInfo>> apply(Integer integer) throws Exception {
                        Map<String, List<AppInfo>> appList = new HashMap<>();
                        List<AppInfo> contactList = LoadAppUtils.getInstance(getBaseContext()).getDefaultContacts();
                        int index = middleIndex = (int) Math.floor(contactList.size() / 6);
                        for (AppInfo info : contactList) {
                            List<AppInfo> infos = appList.get(index + "");
                            if (infos == null) {
                                infos = new ArrayList<>();
                            }
                            infos.add(info);
                            appList.put(index + "", infos);
                            if (infos.size() >= 6) {
                                index -= 1;
                            }
                        }
                        middleIndex += 1;
                        index = middleIndex;
                        List<AppInfo> appInfos = LoadAppUtils.getInstance(getBaseContext()).getDefaultApps();
                        for (AppInfo info : appInfos) {
                            List<AppInfo> infos = appList.get(index + "");
                            if (infos == null) {
                                infos = new ArrayList<>();
                            }
                            infos.add(info);
                            appList.put(index + "", infos);
                            if (index == middleIndex && infos.size() >= 4) {
                                index += 1;
                            } else if (infos.size() >= 6) {
                                index += 1;
                            }
                        }
                        return appList;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<Map<String, List<AppInfo>>, List<View>>() {
                    @Override
                    public List<View> apply(Map<String, List<AppInfo>> integerListMap) throws Exception {
                        for (String index : integerListMap.keySet()) {
                            views.add(createList(integerListMap.get(index), Integer.parseInt(index) == middleIndex));
                        }
                        mIndicatorView.setPageNum(views.size());
                        return views;
                    }
                })
                .subscribe(new Consumer<List<View>>() {
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

    //开启定位
    private void openLocal() {
//        //初始化定位
//        mLocationClient = new AMapLocationClient(getApplicationContext());
//        //设置定位回调监听
//        mLocationClient.setLocationListener(mLocationListener);
//        //初始化AMapLocationClientOption对象
//        mLocationOption = new AMapLocationClientOption();
//        mLocationOption.setOnceLocation(true);
//        mLocationOption.setInterval(5000);
//        mLocationOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Transport);
//        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//        mLocationClient.setLocationOption(mLocationOption);
//        mLocationClient.stopLocation();
//        mLocationClient.startLocation();
    }

    //注册广播
    private void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction("com.huatu.tiger.app.change");
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mReceiver, filter);
    }

    //更新时间
    private void updateTime() {
        if (mTimeTv == null)
            return;
        String timeStr = DateTimeUtil.getInstance().getCurrentTimeHHMM();
        mTimeTv.setText(timeStr);
        String dateStr = DateTimeUtil.getInstance().getCurrentDate() + " " + DateTimeUtil.getInstance().getCurrentWeekDay(0);
        mDateTv.setText(dateStr);
    }

    //生成列表
    private View createList(List<AppInfo> appInfos, boolean isShowExpand) {
        View rootView = null;
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
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (appInfos.size() == 1)
                    return 2;
                return 1;
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(new SimpleCommonRVAdapter<AppInfo>(appInfos, R.layout.cell_item, this) {

            @Override
            public void convert(SimpleViewHolder holder, AppInfo item, int position) {
                holder.setText(R.id.name, item.label);
                holder.setImageResource(R.id.icon, item.icon);
                int gap = getItemCount() == 1 ? 2 : 1;
                holder.itemView.setLayoutParams(new ViewGroup.LayoutParams(DisplayUtil.realWidh * gap, DisplayUtil.realHeight));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mCurrentInfo = item;
                        if (item.type == ALL_APP_TYPE) {
                            allAppFragment = AllAppFragment.getFragment(ALL_APP_TYPE);
                            getSupportFragmentManager().beginTransaction().add(R.id.fragment_layout, allAppFragment).commitNow();
                            addAppFragmentListener();
                        } else if (item.type == LoadAppUtils.ADD_APP_TYPE) {
                            allAppFragment = AllAppFragment.getFragment(LoadAppUtils.ADD_APP_TYPE);
                            getSupportFragmentManager().beginTransaction().add(R.id.fragment_layout, allAppFragment).commitNow();
                            addAppFragmentListener();
                        } else if (item.type == LoadAppUtils.DEFAULT_CONTACT_TYPE) {
                            if (DisplayUtil.addPermissByPermissionList(LauncherActivity.this, new String[]{"android.permission.CALL_PHONE"}, 1002))
                                callNum(item);
                        } else if (item.type == ADD_CONTACT_TYPE) {
                            if (DisplayUtil.addPermissByPermissionList(LauncherActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 1001)) {
                                readContacts(item.type);
                            }
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

    //读取联系人
    private void readContacts(int type) {
        allAppFragment = AllAppFragment.getFragment(type);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_layout, allAppFragment).commitNow();
        addAppFragmentListener();
    }

    //打电话
    private void callNum(AppInfo item) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        Uri data = Uri.parse("tel:" + item.packageName);
        callIntent.setData(data);
        startActivity(callIntent);
    }

    //广播接收器
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
                if (allAppFragment != null && allAppFragment.isResumed())
                    allAppFragment.refershData(true);
                LoadAppUtils.getInstance(context).mDefaltInfos = null;
                loadApps();
            }
        }
    };
}
