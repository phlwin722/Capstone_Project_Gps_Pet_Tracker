package com.example.gps_pet_tracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionReceiver extends BroadcastReceiver {
    // initialize listener
    public static ReceiverListener Listener;
    @Override
    public void onReceive(Context context, Intent intent) {
        // initialize connectivity manager
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // initialize network info
        NetworkInfo networkInfo =   connectivityManager.getActiveNetworkInfo();

        if (Listener != null) {
            // when connectivity receiver
            // listener  not null
            // get connection status
            boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();

            //call listener method
            Listener.onNetworkChange(isConnected);
        }
    }

    public interface ReceiverListener {
        void onNetworkChange (boolean isConnected);
    }
}
