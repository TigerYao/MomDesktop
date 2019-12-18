package com.huatu.tiger.theagedlauncher;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class LauncherApp extends Application {
    AppInstallBroadcaset broadcaset;

    @Override
    public void onCreate() {
        super.onCreate();
        register();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        try {
            if (broadcaset != null)
                unregisterReceiver(broadcaset);
        } catch (Exception e) {
        }
    }

    private void register() {
        broadcaset = new AppInstallBroadcaset();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        registerReceiver(broadcaset, filter);
    }

    class AppInstallBroadcaset extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("Launcherss", action);
            Intent ss = new Intent("com.huatu.tiger.app.change");
            context.sendBroadcast(ss);
        }
    }
}
