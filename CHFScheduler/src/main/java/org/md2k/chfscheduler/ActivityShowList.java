package org.md2k.chfscheduler;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.md2k.chfscheduler.event.Events;
import org.md2k.chfscheduler.logger.LogInfo;
import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.messagehandler.ResultCallback;
import org.md2k.mcerebrum_chfscheduler.R;
import org.md2k.utilities.UI.AlertDialogs;
import org.md2k.utilities.permission.PermissionInfo;

/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
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

public class ActivityShowList extends AppCompatActivity {
    Events events;
    boolean isRemindShow;
    ListAdapter listAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        isRemindShow = getIntent().getBooleanExtra("remind",false);
        if (!DataKitAPI.getInstance(this).isConnected()) {
            Toast.makeText(getApplicationContext(), "!!! Error !!! Datakit connection failed...", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        PermissionInfo permissionInfo = new PermissionInfo();
        permissionInfo.getPermissions(this, new ResultCallback<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                if (!result) {
                    Toast.makeText(getApplicationContext(), "!PERMISSION DENIED !!! Could not continue...", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    if(isRemindShow) {
                        AlertDialogs.AlertDialog(ActivityShowList.this, "CHF Protocol", "It's time to collect Sensor Data and answer some questions", R.drawable.ic_info_teal_48dp, "Ok", null, null, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                    }
                    events = Events.getInstance(ActivityShowList.this);
                    listAdapter = new ListAdapter(ActivityShowList.this);
                    ListView androidListView = (ListView) findViewById(R.id.custom_listview_example);
                    androidListView.setAdapter(listAdapter);
                    setButtons();
                }
            }
        });
    }

    @Override
    public void onResume() {
//        if(listAdapter!=null && listAdapter.isChanged) {
            listAdapter.notifyDataSetChanged();
//            listAdapter.setChanged(false);
//        }
        setButtons();
        super.onResume();
    }
    @Override
    public void onBackPressed(){

    }

    void setButtons() {
        Button button1 = (Button) findViewById(R.id.button_1);
        if (events.isCompleted())
            button1.setText(R.string.button_done);
        else button1.setText(R.string.button_skip);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!events.isCompleted()) {
                    AlertDialogs.AlertDialog(ActivityShowList.this, "Skip", "Do you want to skip?", R.drawable.ic_error_red_50dp, "Yes", "Cancel", null, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == Dialog.BUTTON_POSITIVE) {
                                Intent intent=new Intent(Constants.INTENT_NAME);
                                intent.putExtra("action",LogInfo.STATUS_SKIP);
                                LocalBroadcastManager.getInstance(ActivityShowList.this).sendBroadcast(intent);
                                finish();
                            }
                        }
                    });
                } else {
                    Intent intent=new Intent(Constants.INTENT_NAME);
                    intent.putExtra("action",LogInfo.STATUS_COMPLETED);
                    LocalBroadcastManager.getInstance(ActivityShowList.this).sendBroadcast(intent);
                    finish();
                }
            }
        });
        Button button2 = (Button) findViewById(R.id.button_2);
        button2.setText(R.string.button_remind);
        button2.setEnabled(isRemindShow);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogs.AlertDialog(ActivityShowList.this, "Remind me after 1 hour", "Do you want a reminder after 1 hour?", R.drawable.ic_error_red_50dp, "Yes", "Cancel", null, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == Dialog.BUTTON_POSITIVE) {
                            Intent intent=new Intent(Constants.INTENT_NAME);
                            intent.putExtra("action",LogInfo.STATUS_REMIND);
                            LocalBroadcastManager.getInstance(ActivityShowList.this).sendBroadcast(intent);
                            finish();
                        }
                    }
                });
            }
        });
    }
}
