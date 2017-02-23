package com.homeautogroup.mylittlelibrary;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Kelvin-M on 2/23/2017. at 02:24
 * for homeautogroup.co.ke (Flyboypac@gmail.com)
 * +254705419309
 * PROJECT [UpdateLibrary]
 */

public class MyDialog extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.my_dialog,null);
    }
}
