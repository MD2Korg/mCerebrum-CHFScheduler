package org.md2k.chfscheduler.event;

import android.content.Context;
import android.graphics.drawable.Drawable;

import org.md2k.chfscheduler.day.DayManager;
import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;

import java.util.ArrayList;

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
public abstract class Event{
    DataSourceBuilder dataSourceBuilder;
    String name;
    Drawable icon;
    String className;
    String fileName;
    Context context;

    public Event(Context context) {
        this.context=context;
    }

    public String getName() {
        return name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public String getClassName() {
        return className;
    }

    public String getFileName() {
        return fileName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setDataSourceBuilder(DataSourceBuilder dataSourceBuilder){
        this.dataSourceBuilder = dataSourceBuilder;
    }
    public String getPackageName(){
        return dataSourceBuilder.build().getApplication().getId();
    }
    public boolean isEMA(){
        return false;
    }
    public abstract String getId();

    public boolean isCompleted()
    {
        long dayStartTime = DayManager.getInstance(context).getDayStartTime();
        if(dayStartTime==-1) return true;
        try {
            ArrayList<DataSourceClient> dataSourceClients = DataKitAPI.getInstance(context).find(dataSourceBuilder);
            if (dataSourceClients.size() == 0) return false;
            ArrayList<DataType> dataTypes = DataKitAPI.getInstance(context).query(dataSourceClients.get(0), 1);
            return dataTypes.size() != 0 && dayStartTime < dataTypes.get(0).getDateTime();
        } catch (DataKitException e) {
            e.printStackTrace();
        }
        return false;
    }

};
