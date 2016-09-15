package org.md2k.chfscheduler;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

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
public class Events {
    private ArrayList<Event> events;
    Context context;
    public Events(Context context){
        this.context=context;
        events=new ArrayList<>();
        events.add(new Event("weight","Measure Weight", "event",ContextCompat.getDrawable(context, org.md2k.utilities.R.drawable.ic_weight_scale_48dp),"org.md2k.omron","org.md2k.omron.ActivityWeightScale",null));
        events.add(new Event("blood_pressure","Measure Blood Pressure","event",ContextCompat.getDrawable(context, org.md2k.utilities.R.drawable.ic_blood_pressure_teal_48dp),"org.md2k.omron","org.md2k.omron.ActivityBloodPressure",null));
        events.add(new Event("easy_sense","Measure Heart Physiology","event",ContextCompat.getDrawable(context, org.md2k.utilities.R.drawable.ic_easysense_teal_48dp),"org.md2k.easysense","org.md2k.easysense.ActivityEasySense",null));
        events.add(new Event("medication","Medication Questionnaire","ema",ContextCompat.getDrawable(context, org.md2k.utilities.R.drawable.ic_medication_teal_48dp),"org.md2k.ema","org.md2k.ema.ActivityMain","medication.json"));
        events.add(new Event("ema","Survey","ema",ContextCompat.getDrawable(context, org.md2k.utilities.R.drawable.ic_survey_teal_48dp),"org.md2k.ema","org.md2k.ema.ActivityMain","questionnaire.json"));
    }

    public ArrayList<Event> getEvents() {
        return events;
    }
    public String[] getId(){
        String ids[]=new String[events.size()];
        for(int i=0;i<events.size();i++)
            ids[i]=events.get(i).id;
        return ids;
    }

    public boolean isCompleted() {
        for(int i=0;i<events.size();i++)
            if(!events.get(i).isCompleted())
                return false;
        return true;
    }

    public class Event{
        String id;
        String type;
        String name;
        Drawable icon;
        String packageName;
        String className;
        String fileName;

        public Event(String id,String name, String type, Drawable icon, String packageName, String className, String fileName) {
            this.id = id;
            this.name = name;
            this.type=type;
            this.icon = icon;
            this.packageName = packageName;
            this.className = className;
            this.fileName = fileName;
        }
        boolean isCompleted(){
            return false;
        }
    };
}
