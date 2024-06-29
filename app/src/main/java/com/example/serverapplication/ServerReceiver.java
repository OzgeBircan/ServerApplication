package com.example.serverapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServerReceiver extends BroadcastReceiver {
    private static final String TAG = "ServerReceiver";
    public static final String ACTION_START_SERVER = "com.example.serverapplication.START_SERVER";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && ACTION_START_SERVER.equals(intent.getAction())) {
            int receivedNumber = intent.getIntExtra("number", 0);
            boolean showUI = intent.getBooleanExtra("showUI", true);

            Log.d(TAG, "Received broadcast to start server with number: " + receivedNumber + " and showUI: " + showUI);

            Intent serverIntent = new Intent(context, MainActivity.class);
            serverIntent.putExtra("number", receivedNumber);
            serverIntent.putExtra("showUI", showUI);
            serverIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(serverIntent);
            Log.d(TAG, "onReceive: start activity with number: " + receivedNumber);
        }
    }
}
