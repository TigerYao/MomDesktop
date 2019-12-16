package com.huatu.tiger.theagedlauncher.bean;

import android.graphics.drawable.Drawable;

public class AppInfo {
    public String packageName;
    public String appName;
    public String label;
    public Drawable icon;
    public int iconId;
    public boolean isSystem;
    public boolean isDefalut;
    public boolean isAppWidget;

    @Override
    public String toString() {
        return "AppInfo{" +
                "packageName='" + packageName + '\'' +
                ", appName='" + appName + '\'' +
                ", label='" + label + '\'' +
                ", icon=" + icon +
                ", iconId=" + iconId +
                ", isSystem=" + isSystem +
                ", isDefalut=" + isDefalut +
                ", isAppWidget=" + isAppWidget +
                '}';
    }
}
