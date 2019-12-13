package com.huatu.tiger.theagedlauncher.utils;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
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
    public static int gap = 0;
    public static int realWidh = 0;
    public static int realHeight = 0;

    public static void computeWidth(Context ctx){
        if(realWidh == 0) {
            realWidh = ((DisplayUtil.getScreenWidth(ctx)) / 20);
            Log.i("TAG", "realWidh..."+ realWidh);
            realWidh = ((DisplayUtil.getScreenWidth(ctx) -  realWidh * 3) / 16);
            Log.i("TAG", "realWidh..re..."+ realWidh);
            realHeight = ((int) Math.floor((DisplayUtil.getScreenHeight(ctx) - 55) / 11));
            Log.i("TAG", DisplayUtil.getScreenWidth(ctx) + "...realWidh..re..."+ realWidh * 18);
            gap = Math.abs(DisplayUtil.getScreenWidth(ctx) - realWidh * 18 - 40); //- realWidh * 2;
            Log.i("TAG","gap..."+gap);
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
        if(!TextUtils.isEmpty(strMacAddr))
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

}