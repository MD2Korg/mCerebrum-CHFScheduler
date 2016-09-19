package org.md2k.chfscheduler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.md2k.chfscheduler.event.Event;
import org.md2k.chfscheduler.event.EventEMA;
import org.md2k.chfscheduler.event.Events;
import org.md2k.mcerebrum_chfscheduler.R;

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
public class ListAdapter extends ArrayAdapter {
    private static final String TAG = ListAdapter.class.getSimpleName();
    Context context;
    boolean isChanged;

    public ListAdapter(Activity context) {
        super(context, R.layout.items, Events.getInstance(context).getEvents());
        this.context = context;
        isChanged=false;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewRow = layoutInflater.inflate(R.layout.items, null, true);
        Button button = (Button) viewRow.findViewById(R.id.button_event);
        final Event event=Events.getInstance(context).getEvents().get(i);
        button.setText(event.getName());
        button.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_teal));
        ImageView imageView_icon = (ImageView) viewRow.findViewById(R.id.imageView_icon);
        imageView_icon.setImageDrawable(event.getIcon());

        Button buttonStatus = (Button) viewRow.findViewById(R.id.button_status);
        if (event.isCompleted()) {
            buttonStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.button_teal));
            buttonStatus.setText(R.string.button_done);
            buttonStatus.setTextColor(ContextCompat.getColor(context, R.color.black));
        }
        else {
            buttonStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.button_red));
            buttonStatus.setText(R.string.button_start);
            buttonStatus.setTextColor(ContextCompat.getColor(context, R.color.white));
        }
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isChanged=true;
                try {
                    if(event.isEMA()) {
                        EventEMA eventEMA= (EventEMA) event;
                        eventEMA.start();
                        Intent intent = context.getPackageManager().getLaunchIntentForPackage(event.getPackageName());
                        intent.setAction(event.getPackageName());
                        intent.putExtra("file_name", event.getFileName());
                        intent.putExtra("id", event.getId());
                        intent.putExtra("name", event.getName());
                        intent.putExtra("timeout", Integer.MAX_VALUE);
                        context.startActivity(intent);
                    }else {
                        Intent intent = new Intent();
                        intent.setClassName(event.getPackageName(), event.getClassName());
                        context.startActivity(intent);
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error: " + event.getName() + " is not installed.", Toast.LENGTH_SHORT).show();
                }
            }
        };
        button.setOnClickListener(onClickListener);
        buttonStatus.setOnClickListener(onClickListener);

        return viewRow;
    }

    public boolean isChanged() {
        return isChanged;
    }

    public void setChanged(boolean changed) {
        isChanged = changed;
    }
}