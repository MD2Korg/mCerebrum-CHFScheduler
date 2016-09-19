package org.md2k.chfscheduler.logger;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.md2k.chfscheduler.Constants;
import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeJSONObject;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.source.METADATA;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.source.platform.Platform;
import org.md2k.datakitapi.source.platform.PlatformBuilder;
import org.md2k.datakitapi.source.platform.PlatformType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.utilities.Report.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Copyright (c) 2016, The University of Memphis, MD2K Center
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
public class LoggerManager {
    private static final String TAG = LoggerManager.class.getSimpleName();
    private static LoggerManager instance;
    Context context;
    DataKitAPI dataKitAPI;
    DataSourceBuilder dataSourceBuilderLogger;
    DataSourceClient dataSourceClientLogger;
    ArrayList<LogInfo> logInfos;

    private LoggerManager(Context context) {
        try {
            Log.d(TAG, "LoggerManager()...");
            this.context = context;
            dataKitAPI = DataKitAPI.getInstance(context);
            dataSourceBuilderLogger = createDataSourceBuilderLogger();
            register();
            logInfos = new ArrayList<>();
            read(DateTime.getDateTime() - 24 * 60 * 60 * 1000);
        } catch (DataKitException e) {
            Intent intent = new Intent(Constants.INTENT_STOP);
            intent.putExtra("type", "LoggerManager.java...LoggerManager()..fails");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    public static LoggerManager getInstance(Context context) {
        Log.d(TAG,"getInstance()...instance="+instance);
        if (instance == null) {
            instance = new LoggerManager(context);
        }
        return instance;
    }

    public static void clear() {
        Log.d(TAG, "clear()...");
        instance = null;
    }

    private void register() throws DataKitException {
        dataSourceClientLogger = dataKitAPI.register(dataSourceBuilderLogger);
    }

    public void insert(LogInfo logInfo) {
        try {
            logInfos.add(logInfo);
            Gson gson = new Gson();
            JsonObject sample = new JsonParser().parse(gson.toJson(logInfo)).getAsJsonObject();
            Log.d(TAG, "log=" + sample.toString());
            DataTypeJSONObject dataTypeJSONObject = new DataTypeJSONObject(DateTime.getDateTime(), sample);
            dataKitAPI.insert(dataSourceClientLogger, dataTypeJSONObject);
            logInfos.add(logInfo);
        } catch (DataKitException e) {
            Intent intent = new Intent(Constants.INTENT_STOP);
            intent.putExtra("type", "LoggerManager.java...insert()..fails");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    private void read(long startTimestamp) throws DataKitException {
            Log.d(TAG,"read...");
            long endTimestamp = DateTime.getDateTime();
            logInfos=new ArrayList<>();
            Gson gson=new Gson();
            ArrayList<DataType> dataTypes=dataKitAPI.query(dataSourceClientLogger, startTimestamp, endTimestamp);
            for(int i=0;i<dataTypes.size();i++){
                DataTypeJSONObject dataTypeJSONObject = (DataTypeJSONObject) dataTypes.get(i);
                LogInfo logInfo = gson.fromJson(dataTypeJSONObject.getSample().toString(), LogInfo.class);
                logInfos.add(logInfo);
            }
            Log.d(TAG, "read...size=" + logInfos.size());
        }

    DataSourceBuilder createDataSourceBuilderLogger() {
        Platform platform = new PlatformBuilder().setType(PlatformType.PHONE).build();
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setType(DataSourceType.LOG).setPlatform(platform);
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.NAME, "Log");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DESCRIPTION, "Represents the log of CHF Scheduler");
        dataSourceBuilder = dataSourceBuilder.setMetadata(METADATA.DATA_TYPE, DataTypeJSONObject.class.getName());
        ArrayList<HashMap<String, String>> dataDescriptors = new ArrayList<>();
        HashMap<String, String> dataDescriptor = new HashMap<>();
        dataDescriptor.put(METADATA.NAME, "Log");
        dataDescriptor.put(METADATA.UNIT, "string");
        dataDescriptor.put(METADATA.DESCRIPTION, "Contains log");
        dataDescriptor.put(METADATA.DATA_TYPE, String.class.getName());
        dataDescriptors.add(dataDescriptor);
        dataSourceBuilder = dataSourceBuilder.setDataDescriptors(dataDescriptors);
        return dataSourceBuilder;
    }

    public ArrayList<LogInfo> getLogInfos() {
        return logInfos;
    }

    public LogInfo getLast(String status) {
        for(int i=logInfos.size()-1;i>=0;i--){
            if(logInfos.get(i).getStatus().equals(status)) return logInfos.get(i);
        }
        return null;
    }
    public long getLastTime(String status) {
        for(int i=logInfos.size()-1;i>=0;i--){
            if(logInfos.get(i).getStatus().equals(status)) return logInfos.get(i).timestamp;
        }
        return -1;
    }

}
