package com.huatu.tiger.theagedlauncher.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.net.Uri;
import android.os.Build;
import android.os.UserManager;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.huatu.tiger.theagedlauncher.R;
import com.huatu.tiger.theagedlauncher.bean.AppDatabase;
import com.huatu.tiger.theagedlauncher.bean.AppInfo;
import com.huatu.tiger.theagedlauncher.bean.AppInfoDao;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class LoadAppUtils {
    public static final int APP_TYPE = 0;
    public static final int DEFAULT_APP_TYPE = 1;
    public static final int ALL_APP_TYPE = 2;
    public static final int ALL_CONTACT_TYPE = 3;
    public static final int ADD_APP_TYPE = 4;
    public static final int ADD_CONTACT_TYPE = 5;

    private static LoadAppUtils mInstance = null;
    private Context mCtx;
    private List<AppInfo> mInfos;
    public List<AppInfo> mDefaltInfos;
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

    public Observable<List<AppInfo>> getAllNewApp() {
        mInfos = null;
        return getAllApps();
    }

    public List<AppInfo> loadNewDefault() {
        mDefaltInfos = null;
        return getDefaultApps();
    }

    public Observable<List<AppInfo>> getAllApps() {
        return Observable.just(mCtx.getPackageManager()).map(new Function<PackageManager, List<AppInfo>>() {
            @Override
            public List<AppInfo> apply(PackageManager packageManager) throws Exception {
                if (mInfos != null && mInfos.size() > 0)
                    return mInfos;
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
        }).subscribeOn(Schedulers.io());
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
                        appInfo.packageName = "all_app";
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
                        appInfo.packageName = "all_app";
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
        appInfo.type = ADD_APP_TYPE;
        appInfo.packageName = "all_app";
        appInfo.icon = mCtx.getResources().getDrawable(R.mipmap.add_icon);
        mDefaltInfos.add(appInfo);
        return mDefaltInfos;
    }

    public AppInfoDao getAppDao() {
        return mAppDao;
    }

    //调用并获取联系人信息
    public List<AppInfo> getContactList() {
        List<AppInfo> contacts = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = mCtx.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, null);
            if (cursor != null) {
                Log.e("Launcherss", "...." + cursor.getCount());
                while (cursor.moveToNext()) {
                    String displayName = cursor.getString(cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String number = cursor.getString(cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER));
                    int contactId = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                    AppInfo info = new AppInfo();
                    info.packageName = number;
                    info.appName = displayName;
                    info.label = displayName;
                    info.type = ALL_CONTACT_TYPE;
                    info.icon = getContactsIcon(contactId);//BitmapDrawable.createFromStream();
                    if(!contacts.contains(info))
                        contacts.add(info);
                    Log.e("Launcherss", "...." + "...." + contactId + "..." + number);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        AppInfo appInfo = new AppInfo();
        appInfo.packageName = "all_person";
        appInfo.icon = ContextCompat.getDrawable(mCtx, R.mipmap.add_icon);
        appInfo.type = ADD_CONTACT_TYPE;
        appInfo.label = mCtx.getString(R.string.add_contact);
        contacts.add(appInfo);
        return contacts;
//        return Observable.just(1).map(new Function<Integer, List<AppInfo>>() {
//            @Override
//            public List<AppInfo> apply(Integer integer) throws Exception {
//
//            }
//        }).subscribeOn(Schedulers.io());
    }


    public Drawable getContactsIcon(int contactsId) {
        Bitmap bitmap = null;
        try {
            // 获取内容解析者
            ContentResolver contentResolver = mCtx.getContentResolver();

            // 查头像要传的uri 参1 baseuri 参2 要拼接的部分
            Uri contactUri = Uri.withAppendedPath(
                    ContactsContract.Contacts.CONTENT_URI, contactsId + "");
            Log.e("Launcherss", "...." + "...." + contactUri.toString());
            //获取联系人头像的流
            InputStream iconIs = ContactsContract.Contacts
                    .openContactPhotoInputStream(contentResolver, contactUri);
            //把流生成bitmap对象
//         Drawable drawable = Drawable.createFromStream(iconIs, null);
            bitmap = iconIs == null ? null : BitmapFactory.decodeStream(iconIs);
        }catch (Exception e){
            e.printStackTrace();
        }
        return bitmap == null ? ContextCompat.getDrawable(mCtx, R.mipmap.girl_icon) : new BitmapDrawable(bitmap);
    }
}
