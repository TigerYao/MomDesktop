package com.huatu.tiger.theagedlauncher.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.huatu.tiger.theagedlauncher.LauncherActivity;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Created by saiyuan on 2016/10/26.
 */
public class DisplayUtil {
    private static float density = 0;
    private static float scaledDensity = 0;
    private static int screenWidth = 0;
    private static int screenHeight = 0;
    private static int navigationBarHeight;
    public static int realWidh = 0;
    public static int realHeight = 0;

    public static void computeWidth(Context ctx) {
        if (realWidh == 0) {
            realHeight = (getScreenHeight(ctx) - (int) dp2px(80, ctx)) / 3;
            realWidh = getScreenWidth(ctx) / 2;
        }
    }

    public static int getScreenWidth(Context ctx) {
        if (screenWidth <= 0) {
            screenWidth = ctx.getResources().getDisplayMetrics().widthPixels;
        }
        return screenWidth;
    }

    public static int getScreenHeight(Context ctx) {
        if (screenHeight <= 0) {
            screenHeight = ctx.getResources().getDisplayMetrics().heightPixels;
        }
        return screenHeight;
    }

    /**
     * return system bar height
     *
     * @param context
     * @return
     */
    public static int getStatuBarHeight(Context context) {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj;
            obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int width = Integer.parseInt(field.get(obj).toString());
            int height = context.getResources().getDimensionPixelSize(width);
            return height;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static float getDensity(Context ctx) {
        if (density <= 0) {
            density = ctx.getResources().getDisplayMetrics().density;
        }
        return density;
    }

    public static float getScaledDensity(Context ctx) {
        if (scaledDensity <= 0) {
            scaledDensity = ctx.getResources().getDisplayMetrics().scaledDensity;
        }
        return scaledDensity;
    }

    /*
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static float dp2px(float dpValue, Context ctx) {
        return (dpValue * getDensity(ctx) + 0.5f);
    }

    public static int px2dp(float pxValue, Context ctx) {
        return (int) (pxValue / getDensity(ctx) + 0.5f);
    }

    public static int sp2px(float pxValue, Context ctx) {
        return (int) (pxValue * getScaledDensity(ctx) + 0.5f);
    }

    public static int px2sp(float pxValue, Context ctx) {
        return (int) (pxValue / getScaledDensity(ctx) + 0.5f);
    }

    /**
     * 获取NavigationBar高度
     *
     * @param activity
     * @return
     */
    public static int getNavigationBarHeight(Context activity) {
        if (navigationBarHeight <= 0) {
            Resources resources = activity.getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            navigationBarHeight = resources.getDimensionPixelSize(resourceId);
        }
        return navigationBarHeight;
    }

    static String strMacAddr = null;

    public static String getMacAddress() {
        if (!TextUtils.isEmpty(strMacAddr))
            return strMacAddr;
        try {
            // 获得IpD地址
            InetAddress ip = getLocalInetAddress();
            byte[] b = NetworkInterface.getByInetAddress(ip)
                    .getHardwareAddress();
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < b.length; i++) {
                if (i != 0) {
                    buffer.append(':');
                }
                String str = Integer.toHexString(b[i] & 0xFF);
                buffer.append(str.length() == 1 ? 0 + str : str);
            }
            strMacAddr = buffer.toString().toUpperCase();
        } catch (Exception e) {
        }
        return strMacAddr;
    }

    /**
     * 获取移动设备本地IP
     *
     * @return
     */
    private static InetAddress getLocalInetAddress() {
        InetAddress ip = null;
        try {
            // 列举
            Enumeration<NetworkInterface> en_netInterface = NetworkInterface
                    .getNetworkInterfaces();
            while (en_netInterface.hasMoreElements()) {// 是否还有元素
                NetworkInterface ni = (NetworkInterface) en_netInterface
                        .nextElement();// 得到下一个元素
                Enumeration<InetAddress> en_ip = ni.getInetAddresses();// 得到一个ip地址的列举
                while (en_ip.hasMoreElements()) {
                    ip = en_ip.nextElement();
                    if (!ip.isLoopbackAddress()
                            && ip.getHostAddress().indexOf(":") == -1)
                        break;
                    else
                        ip = null;
                }

                if (ip != null) {
                    break;
                }
            }
        } catch (SocketException e) {

            e.printStackTrace();
        }
        return ip;
    }

    /**
     * 动态权限
     */
    public static void addPermissByPermissionList(Activity activity, String[] permissions, int request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   //Android 6.0开始的动态权限，这里进行版本判断
            ArrayList<String> mPermissionList = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(activity, permissions[i])
                        != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }
            }
            if (mPermissionList.isEmpty()) {  //非初次进入App且已授权
                Toast.makeText(activity, "已授权", Toast.LENGTH_SHORT).show();
            } else {
                //请求权限方法
                String[] permissionsNew = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
                ActivityCompat.requestPermissions(activity, permissionsNew, request); //这个触发下面onRequestPermissionsResult这个回调
            }
        }
    }

}