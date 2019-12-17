package com.huatu.tiger.theagedlauncher.bean;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AppInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertApps(List<AppInfo> apps);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertApps(AppInfo...apps);
    @Update
    public void updateApps(AppInfo...apps);

    @Delete
    public void deleteApps(AppInfo...apps);

    @Query("SELECT * FROM appinfo")
    public List<AppInfo> loadAllApp();

    @Query("SELECT * FROM appinfo WHERE type = :appType")
    public List<AppInfo> loadAllAppByType(int appType);

    @Query("SELECT packageName FROM appinfo WHERE type = :appType")
    public List<String> loadAppByType(int appType);
}
