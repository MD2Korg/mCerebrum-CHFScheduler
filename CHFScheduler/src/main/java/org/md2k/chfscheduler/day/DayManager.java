package org.md2k.chfscheduler.day;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import org.md2k.chfscheduler.Constants;
import org.md2k.chfscheduler.scheduler.DayStartScheduler;
import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeLong;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.messagehandler.OnReceiveListener;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.utilities.Report.Log;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Copyright (c) 2016, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p/>
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p/>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p/>
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
public class DayManager {
    private static final String TAG = DayManager.class.getSimpleName();
    private static DayManager instance = null;
    Context context;
    long dayStartTime;
    DataSourceClient dataSourceClientDayStart;
    DayStartScheduler dayStartScheduler;
    Handler handler;

    public static DayManager getInstance(Context context) {
        if (instance == null)
            instance = new DayManager(context);
        return instance;

    }

    private DayManager(Context context) {
        Log.d(TAG, "DayManager()...");
        this.context = context;
        dataSourceClientDayStart=null;
        dayStartScheduler = new DayStartScheduler(context);
        handler = new Handler();
    }

    public void start() {
        handler.post(runnableDay);
    }

    public void stop() {
        handler.removeCallbacks(runnableDay);
        if(dataSourceClientDayStart!=null) {
            try {
                DataKitAPI.getInstance(context).unsubscribe(dataSourceClientDayStart);
            } catch (DataKitException e) {
                e.printStackTrace();
            }
        }
        dayStartScheduler.stop();
    }

    public void subscribeDayStart() throws DataKitException {
        Log.d(TAG, "subscribeDayStart()...");
        DataKitAPI.getInstance(context).subscribe(dataSourceClientDayStart, new OnReceiveListener() {
            @Override
            public void onReceived(DataType dataType) {
                DataTypeLong dataTypeLong = (DataTypeLong) dataType;
                dayStartTime = dataTypeLong.getSample();
                Log.d(TAG, "subscribeDayStart()...received..dayStartTime=" + dayStartTime);
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        dayStartScheduler.stop();
                        dayStartScheduler.start();
                    }
                });
                t.start();
            }
        });
    }

    Runnable runnableDay = new Runnable() {
        @Override
        public void run() {
            ArrayList<DataSourceClient> dataSourceClients;
            try {
                dataSourceClients = DataKitAPI.getInstance(context).find(new DataSourceBuilder().setType(DataSourceType.DAY_START));
                Log.d(TAG, "runnableListenDayStart()...dataSourceClients.size()=" + dataSourceClients.size());
                if (dataSourceClients.size() == 0)
                    handler.postDelayed(this, 1000);
                else {
                    dataSourceClientDayStart = dataSourceClients.get(0);
                    readDayStartFromDataKit();
                    subscribeDayStart();
                    if(dayStartTime!=-1) {
                        dayStartScheduler.stop();
                        dayStartScheduler.start();
                    }
                }
            } catch (DataKitException e) {
                Log.d(TAG, "DataKitException...runnableDay");
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.INTENT_STOP));
            }
        }
    };


    private void readDayStartFromDataKit() throws DataKitException {
        Log.d(TAG, "readDayStartFromDataKit()...");
        dayStartTime = -1;
        ArrayList<DataType> dataTypes = DataKitAPI.getInstance(context).query(dataSourceClientDayStart, 1);
        if (dataTypes.size() != 0) {
            DataTypeLong dataTypeLong = (DataTypeLong) dataTypes.get(0);
            dayStartTime = dataTypeLong.getSample();
            if(!isToday(dayStartTime)) dayStartTime=-1;
            Log.d(TAG, "readDayStartFromDataKit()...dayStartTime=" + dayStartTime);
        }
    }

    public long getDayStartTime() {
        if(dayStartTime==-1) return dayStartTime;
        if(!isToday(dayStartTime)) dayStartTime=-1;
        return dayStartTime;
    }

    public boolean isToday(long time) {
        Calendar c=Calendar.getInstance();
        Calendar t=Calendar.getInstance();
        c.setTimeInMillis(time);
        if(c.get(Calendar.DATE)!=t.get(Calendar.DATE)) return false;
        if(c.get(Calendar.MONTH)!=t.get(Calendar.MONTH)) return false;
        if(c.get(Calendar.YEAR)!=t.get(Calendar.YEAR)) return false;
        return true;
    }

}
