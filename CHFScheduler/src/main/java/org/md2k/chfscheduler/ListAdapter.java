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

import org.md2k.mcerebrum_chfscheduler.R;

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
public class ListAdapter extends ArrayAdapter {
    Context context;
    ArrayList<Events.Event> events;

    public ListAdapter(Activity context, ArrayList<Events.Event> events) {
        super(context, R.layout.items, events);
        this.events = events;
        this.context = context;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewRow = layoutInflater.inflate(R.layout.items, null,
                true);
        Button button = (Button) viewRow.findViewById(R.id.button_event);
        button.setText(events.get(i).name);
        button.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_teal));
        ImageView imageView_icon = (ImageView) viewRow.findViewById(R.id.imageView_icon);
        imageView_icon.setImageDrawable(events.get(i).icon);

        ImageView imageView = (ImageView) viewRow.findViewById(R.id.image_status);
        if (events.get(i).isCompleted())
            imageView.setImageResource(R.drawable.ic_ok_teal_50dp);
        else
            imageView.setImageResource(R.drawable.ic_error_red_50dp);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(events.get(i).type.equals("ema")) {
                        Intent intent = context.getPackageManager().getLaunchIntentForPackage(events.get(i).packageName);
                        intent.setAction(events.get(i).packageName);
                        intent.putExtra("file_name", events.get(i).fileName);
                        intent.putExtra("id", events.get(i).id);
                        intent.putExtra("name", events.get(i).name);
                        intent.putExtra("timeout", Integer.MAX_VALUE);
                        context.startActivity(intent);
                    }else {
                        Intent intent = new Intent();
                        intent.setClassName(events.get(i).packageName, events.get(i).className);
                        getContext().startActivity(intent);
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error: " + events.get(i).name + " is not installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return viewRow;
    }
}