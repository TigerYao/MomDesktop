package com.huatu.tiger.theagedlauncher.base;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    public static Context fromContext(Context ctx) {
        return ctx;
    }
}
