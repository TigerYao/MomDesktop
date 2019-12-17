package com.huatu.tiger.theagedlauncher.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.UserManager;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.huatu.tiger.theagedlauncher.R;
import com.huatu.tiger.theagedlauncher.bean.AppDatabase;
import com.huatu.tiger.theagedlauncher.bean.AppInfo;
import com.huatu.tiger.theagedlauncher.bean.AppInfoDao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class LoadAppUtils {
    public static final int APP_TYPE = 0;
    public static final int DEFAULT_APP_TYPE = 1;
    public static final int ALL_APP_TYPE = 2;
    public static final int ALL_CONTACT_TYPE = 3;
    public static final int ADD_TYPE = 4;
    public static final int DEFAULT_SEC_APP_TYPE = 4;

    private static LoadAppUtils mInstance = null;
    private Context mCtx;
    private List<AppInfo> mInfos;
    private List<AppInfo> mDefaltInfos;
    private AppInfoDao mAppDao;
    private List<String> defaltApps;
    private PackageManager mPackageManager;

    private LoadAppUtils(Context ctx) {
        mCtx = ctx;
        AppDatabase appDatabase = Room.databaseBuilder(ctx, AppDatabase.class, "appinfo").allowMainThreadQueries().build();
        mAppDao = appDatabase.getAppDao();
        mPackageManager = mCtx.getPackageManager();
    }

    public static LoadAppUtils getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new LoadAppUtils(ctx);
        }
        return mInstance;
    }

    public List<AppInfo> getContactList() {
        List<AppInfo> contacts = mAppDao.loadAllAppByType(ALL_CONTACT_TYPE);
        if (contacts == null || contacts.size() < 6) {
            contacts = new ArrayList<>();
            int counts = (6 - contacts.size());
            for (int i = 0; i < counts; i++) {
                AppInfo appInfo = new AppInfo();
                appInfo.icon = ContextCompat.getDrawable(mCtx, R.mipmap.add_icon);
                appInfo.type = ALL_CONTACT_TYPE;
                appInfo.label = mCtx.getString(R.string.add_contact);
                contacts.add(appInfo);
            }
        } else {
            AppInfo appInfo = new AppInfo();
            appInfo.icon = ContextCompat.getDrawable(mCtx, R.mipmap.add_icon);
            appInfo.type = ALL_CONTACT_TYPE;
            appInfo.label = mCtx.getString(R.string.add_contact);
            contacts.add(appInfo);
        }

        return contacts;
    }

    public Observable<List<AppInfo>> getAllApps() {
        return Observable.just(mCtx.getPackageManager()).map(new Function<PackageManager, List<AppInfo>>() {
            @Override
            public List<AppInfo> apply(PackageManager packageManager) throws Exception {
//                mInfos = mAppDao.loadAllApp();
//                if (mInfos != null || mInfos.size() > 0)
//                    return mInfos;
                mInfos = new ArrayList<>();
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    LauncherApps launcherApps = (LauncherApps) mCtx.getSystemService(Context.LAUNCHER_APPS_SERVICE);
                    UserManager userManager = (UserManager) mCtx.getSystemService(Context.USER_SERVICE);
                    List<LauncherActivityInfo> infos = launcherApps.getActivityList(null, userManager.getUserProfiles().get(0));
                    for (LauncherActivityInfo info : infos) {
                        AppInfo appInfo = new AppInfo();
                        appInfo.packageName = info.getComponentName().getPackageName();
                        appInfo.label = info.getLabel().toString();
                        appInfo.isSystem = ((info.getApplicationInfo().flags & ApplicationInfo.FLAG_SYSTEM) != 0);
                        appInfo.appName = info.getName();
                        appInfo.icon = appInfo.isSystem && info.getIcon(1) != null ? info.getIcon(1) : info.getApplicationInfo().loadIcon(packageManager);
                        mInfos.add(appInfo);
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
                        appInfo.isSystem = ((info.activityInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
                        mInfos.add(appInfo);
                    }

                }

                return mInfos;
            }
        });
    }

    public List<AppInfo> getDefaultApps() {
        if (mDefaltInfos != null && !mDefaltInfos.isEmpty() && mDefaltInfos.size() >= 4)
            return mDefaltInfos;
        defaltApps = mAppDao.loadAppByType(DEFAULT_APP_TYPE);
        Log.e("LoadAppUtils", "default...." + defaltApps.toString());
        if (defaltApps == null || defaltApps.size() < 4)
            defaltApps = Arrays.asList(mCtx.getResources().getStringArray(R.array.default_packagename));
        mDefaltInfos = new ArrayList<>();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            LauncherApps launcherApps = (LauncherApps) mCtx.getSystemService(Context.LAUNCHER_APPS_SERVICE);
            UserManager userManager = (UserManager) mCtx.getSystemService(Context.USER_SERVICE);
            for (String packageName : defaltApps) {
                try {
                    List<LauncherActivityInfo> infos = launcherApps.getActivityList(packageName, userManager.getUserProfiles().get(0));
                    for (LauncherActivityInfo info : infos) {
                        AppInfo appInfo = new AppInfo();
                        appInfo.packageName = info.getComponentName().getPackageName();
                        appInfo.label = info.getLabel().toString();
                        appInfo.isSystem = ((info.getApplicationInfo().flags & ApplicationInfo.FLAG_SYSTEM) != 0);
                        appInfo.appName = info.getName();
                        appInfo.type = DEFAULT_APP_TYPE;
                        appInfo.icon = appInfo.isSystem && info.getIcon(1) != null ? info.getIcon(1) : info.getApplicationInfo().loadIcon(mPackageManager);
                        if (mDefaltInfos.contains(appInfo))
                            continue;
                        mDefaltInfos.add(appInfo);
                        mAppDao.insertApps(appInfo);
                    }
                    if (mDefaltInfos.size() == 4) {
                        AppInfo appInfo = new AppInfo();
                        appInfo.label = mCtx.getString(R.string.all_app);
                        appInfo.isSystem = false;
                        appInfo.type = ALL_APP_TYPE;
                        appInfo.icon = mCtx.getDrawable(R.mipmap.apps_icon);
                        mDefaltInfos.add(appInfo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            for (String packageName : defaltApps) {
                try {
                    final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    mainIntent.setPackage(packageName);
                    List<ResolveInfo> infos = mPackageManager.queryIntentActivities(mainIntent, 0);
                    for (ResolveInfo info : infos) {
                        AppInfo appInfo = new AppInfo();
                        appInfo.packageName = info.activityInfo.packageName;
                        appInfo.appName = info.activityInfo.name;
                        appInfo.label = info.loadLabel(mPackageManager).toString();
                        appInfo.icon = info.loadIcon(mPackageManager);
                        appInfo.type = DEFAULT_APP_TYPE;
                        appInfo.isSystem = ((info.activityInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
                        if (mDefaltInfos.contains(appInfo))
                            continue;
                        mDefaltInfos.add(appInfo);
                        mAppDao.insertApps(appInfo);
                    }
                    if (mDefaltInfos.size() == 4) {
                        AppInfo appInfo = new AppInfo();
                        appInfo.label = mCtx.getString(R.string.all_app);
                        appInfo.isSystem = false;
                        appInfo.type = ALL_APP_TYPE;
                        appInfo.icon = ContextCompat.getDrawable(mCtx, R.mipmap.apps_icon);
                        mDefaltInfos.add(appInfo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        AppInfo appInfo = new AppInfo();
        appInfo.label = mCtx.getString(R.string.add_app);
        appInfo.isSystem = false;
        appInfo.type = ADD_TYPE;
        appInfo.icon = mCtx.getResources().getDrawable(R.mipmap.add_icon);
        mDefaltInfos.add(appInfo);
        return mDefaltInfos;
    }
}
