package com.example.nutriscan.helper;

import android.os.Handler;
import android.os.Looper;

public class MainThread {
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    public static void post(Runnable runnable) {
        HANDLER.post(runnable);
    }
}