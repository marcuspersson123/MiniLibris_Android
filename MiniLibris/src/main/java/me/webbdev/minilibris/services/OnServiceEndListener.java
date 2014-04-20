package me.webbdev.minilibris.services;

import android.os.IBinder;

/**
 * Created by marcusssd on 2014-04-20.
 */
public interface OnServiceEndListener {
    //public void onServiceProgress(IBinder localBinder);
    public void onServiceEnd(IBinder localBinder);
}
