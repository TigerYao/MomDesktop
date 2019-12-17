package com.huatu.tiger.theagedlauncher.bean;

import androidx.room.Database;
import androidx.room.RoomDatabase;
@Database(entities = {AppInfo.class},version = 1,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AppInfoDao getAppDao();
}
