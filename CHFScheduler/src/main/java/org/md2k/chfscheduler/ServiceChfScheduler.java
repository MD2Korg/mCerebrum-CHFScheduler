package org.md2k.chfscheduler;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.md2k.chfscheduler.day.DayManager;
import org.md2k.chfscheduler.logger.LoggerManager;
import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.messagehandler.OnConnectionListener;
import org.md2k.datakitapi.messagehandler.ResultCallback;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.utilities.Report.LogStorage;
import org.md2k.utilities.permission.PermissionInfo;

/*
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

public class ServiceChfScheduler extends Service {
    private static final String TAG = ServiceChfScheduler.class.getSimpleName();
    private DataKitAPI dataKitAPI = null;
    DayManager dayManager;
    LoggerManager loggerManager;

    @Override
    public void onCreate() {
        super.onCreate();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiverStop,
                new IntentFilter(Constants.INTENT_STOP));

        PermissionInfo permissionInfo = new PermissionInfo();
        permissionInfo.getPermissions(this, new ResultCallback<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                if (!result) {
                    Toast.makeText(getApplicationContext(), "!PERMISSION DENIED !!! Could not continue...", Toast.LENGTH_SHORT).show();
                    stopSelf();
                } else {
                    load();
                }
            }
        });
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        org.md2k.utilities.Report.Log.w(TAG, "onStartCommand()...");
        startForeground(98764, getCompatNotification());
        return START_STICKY;
    }
    private Notification getCompatNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher).setContentTitle(getResources().getString(R.string.app_name));
        return builder.build();
    }

    void load() {
        LogStorage.startLogFileStorageProcess(getApplicationContext().getPackageName());
        org.md2k.utilities.Report.Log.w(TAG, "time=" + DateTime.convertTimeStampToDateTime(DateTime.getDateTime()) + ",timestamp=" + DateTime.getDateTime() + ",service_start");
        connectDataKit();
    }

    private void connectDataKit() {
        dataKitAPI = DataKitAPI.getInstance(getApplicationContext());
        try {
            dataKitAPI.connect(new OnConnectionListener() {
                @Override
                public void onConnected() {
                    try {
                        loggerManager = LoggerManager.getInstance(ServiceChfScheduler.this);
                        dayManager=DayManager.getInstance(ServiceChfScheduler.this);
                        dayManager.start();
                    } catch (Exception e) {
                        Intent intent = new Intent(Constants.INTENT_STOP);
                        intent.putExtra("type", "ServiceMicrosoftBands.java...register error after connection");
                        LocalBroadcastManager.getInstance(ServiceChfScheduler.this).sendBroadcast(intent);
                    }
                }
            });
        } catch (DataKitException e) {
            Log.d(TAG, "onException...");
            Intent intent = new Intent(Constants.INTENT_STOP);
            intent.putExtra("type", "ServiceMicrosoftBands.java...Connection Error");
            LocalBroadcastManager.getInstance(ServiceChfScheduler.this).sendBroadcast(intent);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()...");
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiverStop);
        clear();
        org.md2k.utilities.Report.Log.w(TAG, "time=" + DateTime.convertTimeStampToDateTime(DateTime.getDateTime()) + ",timestamp=" + DateTime.getDateTime() + ",service_stop");
        super.onDestroy();
    }

    private BroadcastReceiver mMessageReceiverStop = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            org.md2k.utilities.Report.Log.w(TAG, "time=" + DateTime.convertTimeStampToDateTime(DateTime.getDateTime()) + ",timestamp=" + DateTime.getDateTime() + ",broadcast_receiver_stop_service" + ", msg=" + intent.getStringExtra("type"));
            clear();
            stopSelf();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    synchronized void clear() {
        if(dayManager!=null)
            dayManager.stop();
        if (dataKitAPI != null) {
            dataKitAPI.disconnect();
            dataKitAPI = null;
        }
    }
}
