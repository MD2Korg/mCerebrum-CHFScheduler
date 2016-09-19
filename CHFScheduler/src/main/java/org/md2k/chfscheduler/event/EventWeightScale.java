package org.md2k.chfscheduler.event;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import org.md2k.datakitapi.source.application.ApplicationBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceType;

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
public class EventWeightScale extends Event{
    public EventWeightScale(Context context) {
        super(context);
        setName("Weight");
        setIcon(ContextCompat.getDrawable(context, org.md2k.utilities.R.drawable.ic_weight_scale_48dp));
        setClassName("org.md2k.omron.ActivityWeightScale");
        setDataSourceBuilder(createDataSourceBuilder());
    }
    DataSourceBuilder createDataSourceBuilder(){
        DataSourceBuilder dataSourceBuilder=new DataSourceBuilder();
        dataSourceBuilder=dataSourceBuilder.setType(DataSourceType.WEIGHT);
        dataSourceBuilder=dataSourceBuilder.setApplication(new ApplicationBuilder().setId("org.md2k.omron").build());
        return dataSourceBuilder;
    }
    @Override
    public String getId() {
        return DataSourceType.WEIGHT;
    }
}
