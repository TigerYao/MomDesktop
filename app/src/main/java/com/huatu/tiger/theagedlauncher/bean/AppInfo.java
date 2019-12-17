package com.huatu.tiger.theagedlauncher.bean;

import android.graphics.drawable.Drawable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class AppInfo implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int _id;
    public String packageName;
    public String appName;
    public String label;
    @Ignore
    public Drawable icon;
    public int type;
    public boolean isSystem;
    @Ignore
    public boolean isDefalut;
    @Ignore
    public boolean isAppWidget;

    @Override
    public String toString() {
        return "AppInfo{" +
                "packageName='" + packageName + '\'' +
                ", appName='" + appName + '\'' +
                ", label='" + label + '\'' +
                ", icon=" + icon +
                ", isSystem=" + isSystem +
                ", isDefalut=" + isDefalut +
                ", isAppWidget=" + isAppWidget +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof AppInfo){
            AppInfo info = (AppInfo) obj;
            return packageName.equals(info.packageName) && appName.equals(info.appName);
        }
        return super.equals(obj);
    }
}
