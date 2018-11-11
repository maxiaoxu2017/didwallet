package com.ela.wallet.sdk.didlibrary.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ela.wallet.sdk.didlibrary.http.HttpServer;

import java.io.IOException;

public class DidService extends Service {

    private HttpServer mHttpServer;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHttpServer = new HttpServer(8080);
        try {
            mHttpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (mHttpServer != null) {
            mHttpServer.stop();
        }
        super.onDestroy();
    }
}
