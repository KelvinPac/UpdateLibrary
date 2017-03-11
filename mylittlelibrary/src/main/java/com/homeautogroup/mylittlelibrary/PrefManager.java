package com.homeautogroup.mylittlelibrary;

import android.content.Context;
import android.content.SharedPreferences;

import java.security.PrivateKey;

/**
 * Created by Kelvin-M on 3/2/2017. at 18:50
 * for homeautogroup.co.ke (Flyboypac@gmail.com)
 * +254705419309
 * PROJECT [UpdateLibrary]
 */




public class PrefManager {

    private SharedPreferences.Editor editor;
    private SharedPreferences pref;

    PrefManager(Context context){
        String PREF_NAME = "Update";
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void setUpdateInfo(String whatsNew,String newSize,String newUrl, boolean highPriority, String newVersion){

        editor= pref.edit();
        editor.putString("whatsNewKey",whatsNew);
        editor.putString("new size",newSize);
        editor.putString("new Url", newUrl);
        editor.putBoolean("priority", highPriority);
        editor.putString("newVersion", newVersion);
        editor.commit();
    }
}
