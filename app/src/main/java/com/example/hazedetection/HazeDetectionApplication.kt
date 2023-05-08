package com.example.hazedetection

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.baidu.location.LocationClient

class HazeDetectionApplication : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
    override fun onCreate() {
        super.onCreate()
        LocationClient.setAgreePrivacy(true);
        context = applicationContext
    }
}