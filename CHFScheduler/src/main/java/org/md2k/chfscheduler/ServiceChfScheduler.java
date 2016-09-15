package org.md2k.chfscheduler;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.messagehandler.OnConnectionListener;
import org.md2k.datakitapi.messagehandler.ResultCallback;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.utilities.Report.LogStorage;
import org.md2k.utilities.permission.PermissionInfo;

import java.util.ArrayList;

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
    Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiverStop,
                new IntentFilter(Constants.INTENT_STOP));
        handler=new Handler();
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
                        handler.postDelayed(runnableDay, 3000);
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
    Runnable runnableDay = new Runnable() {
        @Override
        public void run() {
            ArrayList<DataSourceClient> dataSourceClients = null;
            try {
                dataSourceClients = DataKitAPI.getInstance(ServiceChfScheduler.this).find(new DataSourceBuilder().setType(DataSourceType.DAY_START));
                org.md2k.utilities.Report.Log.d(TAG, "runnableListenDayStart()...dataSourceClients.size()=" + dataSourceClients.size());
                if (dataSourceClients.size() == 0)
                    handler.postDelayed(this, 1000);
                else {
                    Intent intent=new Intent(ServiceChfScheduler.this, ActivityShowList.class);
                    ServiceChfScheduler.this.startActivity(intent);
                }
            } catch (DataKitException e) {
                org.md2k.utilities.Report.Log.d(TAG,"DataKitException...runnableDay");
                LocalBroadcastManager.getInstance(ServiceChfScheduler.this).sendBroadcast(new Intent(Constants.INTENT_STOP));
            }
        }
    };

    synchronized void clear() {
        if (dataKitAPI != null) {
            dataKitAPI.disconnect();
            dataKitAPI = null;
        }
    }
}
