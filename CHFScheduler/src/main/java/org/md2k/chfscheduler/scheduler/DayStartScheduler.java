package org.md2k.chfscheduler.scheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import org.md2k.chfscheduler.ActivityShowList;
import org.md2k.chfscheduler.Constants;
import org.md2k.chfscheduler.config.ConfigManager;
import org.md2k.chfscheduler.day.DayManager;
import org.md2k.chfscheduler.logger.LogInfo;
import org.md2k.chfscheduler.logger.LoggerManager;
import org.md2k.chfscheduler.notification.Callback;
import org.md2k.chfscheduler.notification.NotifierManager;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.notification.NotificationRequests;

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
public class DayStartScheduler {
    private static final String TAG = DayStartScheduler.class.getSimpleName();
    Handler handler;
    Context context;
    LoggerManager loggerManager;
    NotifierManager notifierManager;
    boolean isLaunched = false;

    private static final long REMIND_TIMESTAMP = 1 * 60 * 1000; // 1 hour

    public DayStartScheduler(Context context) {
        this.context = context;
        loggerManager = LoggerManager.getInstance(context);
        handler = new Handler();
        notifierManager = new NotifierManager(context);
        notifierManager.set();
    }

    public void start() {
        Log.d(TAG,"start");
        long dayStartTime = DayManager.getInstance(context).getDayStartTime();
        handler.removeCallbacks(runnableReminder);
        if (dayStartTime == -1) return;
        if (dayStartTime < loggerManager.getLastTime(LogInfo.STATUS_SKIP)) return;
        if (dayStartTime < loggerManager.getLastTime(LogInfo.STATUS_COMPLETED)) return;
        long reminderTime = loggerManager.getLastTime(LogInfo.STATUS_REMIND);
        LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiverResponse,
                new IntentFilter(Constants.INTENT_NAME));
        if (dayStartTime < reminderTime) {
            long diff = reminderTime + REMIND_TIMESTAMP - DateTime.getDateTime();
            if (diff > 0) handler.postDelayed(runnableReminder, diff);
            else handler.post(runnableReminder);
        } else {
            launch(true);
            handler.postDelayed(runnableReminder, REMIND_TIMESTAMP);
        }
    }

    private Runnable runnableReminder = new Runnable() {
        @Override
        public void run() {
            NotificationRequests notificationRequests = new NotificationRequests();
            ConfigManager configManager = new ConfigManager(context);

            for (int j = 0; j < configManager.getNotificationRequests().getNotification_option().size(); j++)
                notificationRequests.getNotification_option().add(configManager.getNotificationRequests().getNotification_option().get(j));

            notifierManager.clear();
            if (notificationRequests.getNotification_option().size() == 0) return;
            notifierManager.trigger(new Callback() {
                @Override
                public void onResponse(String response) {
                    launch(false);
                }
            }, notificationRequests);
        }
    };

    public void stop() {
        Log.d(TAG,"stop");
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mMessageReceiverResponse);
        handler.removeCallbacks(runnableReminder);
        if (notifierManager != null)
            notifierManager.clear();
    }

    private void launch(boolean remind) {
        if (isLaunched) return;
        isLaunched = true;
        Intent intent = new Intent(context, ActivityShowList.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("remind", remind);
        context.startActivity(intent);

    }
/*
    private long getTimestamp(String status) {
        LogInfo logInfoDayStart = loggerManager.getLast(status);
        if (logInfoDayStart == null) return -1;
        return logInfoDayStart.getTimestamp();
    }


        String getState() {
            long dayStartTime = getTimestamp(LogInfo.STATUS_DAY_START);
            long startTime = getTimestamp(LogInfo.STATUS_START);
            long skipTime = getTimestamp(LogInfo.STATUS_SKIP);
            long remindTime = getTimestamp(LogInfo.STATUS_REMIND);
            long completedTime = getTimestamp(LogInfo.STATUS_COMPLETED);
            if (dayStartTime == -1) return LogInfo.STATUS_DO_NOTHING;
            if (dayStartTime < skipTime || dayStartTime < completedTime)
                return LogInfo.STATUS_DO_NOTHING;
            if (remindTime + 60 * 60 * 1000 >= DateTime.getDateTime()) return LogInfo.STATUS_REMIND;
            if (dayStartTime + 60 * 60 * 1000 >= DateTime.getDateTime())
                return LogInfo.STATUS_REMIND;
            return LogInfo.STATUS_DO_NOTHING;
        }
    */
    public BroadcastReceiver mMessageReceiverResponse = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getStringExtra("action");
            Log.d(TAG,"broadcastReceiver...action="+action);
            LogInfo logInfo = new LogInfo(action, DateTime.getDateTime(), action);
            LoggerManager.getInstance(context).insert(logInfo);
            isLaunched=false;
            stop();
            start();
        }
    };

}