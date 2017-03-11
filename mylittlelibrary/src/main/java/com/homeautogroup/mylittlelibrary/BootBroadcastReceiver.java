package com.homeautogroup.mylittlelibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Kelvin-M on 2/8/2017. at 23:08
 * for homeautogroup.co.ke (Flyboypac@gmail.com)
 * +254705419309
 * PROJECT [IntentService]
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(
                    "android.intent.action.BOOT_COMPLETED")) {
                Toast.makeText(context, "BOOT_COMPLETED", Toast.LENGTH_LONG).show();
                Toast.makeText(context,"RECEIVED",Toast.LENGTH_LONG).show();
                WebsiteService.setServiceAlarm(context,true);
                Toast.makeText(context,"WEBSITE SERVICE CALLED",Toast.LENGTH_LONG).show();
            }
        }
    }

