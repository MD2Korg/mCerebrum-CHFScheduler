package org.md2k.chfscheduler.logger;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

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
public class LogInfo {
    public static final String STATUS_SKIP="STATUS_SKIP";
    public static final String STATUS_COMPLETED ="STATUS_COMPLETED";
    public static final String STATUS_REMIND = "STATUS_REMIND";

    public static final String STATUS_RUN_MISSED="MISSED";
    public static final String STATUS_RUN_COMPLETED="COMPLETED";
    public static final String STATUS_RUN_ABANDONED_BY_TIMEOUT ="ABANDONED_BY_TIMEOUT";
    public static final String STATUS_RUN_ABANDONED_BY_USER="ABANDONED_BY_USER";
    public static final String STATUS_RUN_START="START";

    String status;
    long timestamp;
    String message;
    String current_time;
    HashMap<String, String> data=new HashMap<>();

    public LogInfo(String status,long timestamp, String message){
        this.status=status;
        this.timestamp=timestamp;
        this.message=message;
        current_time=formatTime(timestamp);
    }

    String formatTime(long timestamp) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss a", Locale.US);
            Date currenTimeZone = calendar.getTime();
            return sdf.format(currenTimeZone);
        } catch (Exception ignored) {
        }
        return "";
    }

    public String getStatus() {
        return status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public HashMap<String, String> getData() {
        return data;
    }
}
