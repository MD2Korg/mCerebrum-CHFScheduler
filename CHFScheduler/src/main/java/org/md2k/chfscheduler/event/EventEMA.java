package org.md2k.chfscheduler.event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.md2k.chfscheduler.Constants;
import org.md2k.chfscheduler.logger.LogInfo;
import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataTypeJSONObject;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.utilities.Report.Log;

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
public class EventEMA extends Event {
    public static final String EMA_MEDICATION = "MEDICATION";
    public static final String EMA_SURVEY = "SURVEY";
    private static final String TAG = EventEMA.class.getSimpleName();
    private MyBroadcastReceiver myReceiver;
    EMA ema;

    public EventEMA(Context context, String id) {
        super(context);
        setDataSourceBuilder(createDataSourceBuilder(id));
        if (id.equals(EMA_MEDICATION)) {
            setName("Medication");
            setIcon(ContextCompat.getDrawable(context, org.md2k.utilities.R.drawable.ic_medication_teal_48dp));
            setClassName("org.md2k.ema.ActivityMain");
            setFileName("medication.json");
        } else {
            setName("Survey");
            setIcon(ContextCompat.getDrawable(context, org.md2k.utilities.R.drawable.ic_survey_teal_48dp));
            setClassName("org.md2k.ema.ActivityMain");
            setFileName("questionnaire.json");
        }
    }

    DataSourceBuilder createDataSourceBuilder(String id) {
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder();
        dataSourceBuilder=dataSourceBuilder.setType(DataSourceType.EMA).setId(id);
        return dataSourceBuilder;
    }
    public String getPackageName(){
        return "org.md2k.ema";
    }
    public boolean isEMA() {
        return true;
    }

    @Override
    public String getId() {
        return dataSourceBuilder.build().getId();
    }

    public void start() {
        ema=new EMA();
        ema.id=getId();
        ema.name=getName();
        ema.start_timestamp=DateTime.getDateTime();
        ema.trigger_type="USER";
        myReceiver = new MyBroadcastReceiver();
        context.registerReceiver(myReceiver, new IntentFilter("org.md2k.ema_scheduler.response"));

    }

    public void saveData(JsonArray answer, String status) throws DataKitException {
        ema.end_timestamp = DateTime.getDateTime();
        ema.question_answers = answer;
        Log.d(TAG, "status=" + status);
        if (status == null) ema.status = LogInfo.STATUS_RUN_ABANDONED_BY_USER;
        else
            ema.status = status;
        saveToDataKit(ema);
        if (myReceiver != null)
            context.unregisterReceiver(myReceiver);

    }
    void saveToDataKit(EMA ema) throws DataKitException {
        Gson gson = new Gson();
        JsonObject sample = new JsonParser().parse(gson.toJson(ema)).getAsJsonObject();
        DataTypeJSONObject dataTypeJSONObject = new DataTypeJSONObject(DateTime.getDateTime(), sample);
        DataSourceClient dataSourceClient = DataKitAPI.getInstance(context).register(createDataSourceBuilder(ema.id));
        DataKitAPI.getInstance(context).insert(dataSourceClient, dataTypeJSONObject);
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String type = intent.getStringExtra("TYPE");
                if (type.equals("RESULT")) {
                    Log.d(TAG,"data received...result");
                    String answer = intent.getStringExtra("ANSWER");
                    String status = intent.getStringExtra("STATUS");
                    JsonParser parser = new JsonParser();
                    JsonElement tradeElement = parser.parse(answer);
                    JsonArray question_answer = tradeElement.getAsJsonArray();
                    saveData(question_answer, status);
                } else if (type.equals("STATUS_MESSAGE")) {
//                lastResponseTime = intent.getLongExtra("TIMESTAMP", -1);
//                message = intent.getStringExtra("MESSAGE");
                    Log.d(TAG, "data received... lastResponseTime=" + intent.getLongExtra("TIMESTAMP", -1) + " message=" + intent.getStringExtra("MESSAGE"));
                }
            } catch (DataKitException e) {
                Log.d(TAG, "DataKitException...savedata..");
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.INTENT_NAME));
            }
        }
    }
}
