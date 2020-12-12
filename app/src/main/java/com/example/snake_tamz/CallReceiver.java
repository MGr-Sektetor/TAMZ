package com.example.snake_tamz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class CallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)){
            MainActivity.getInstance().onResume();
            Toast.makeText(context, "Call ended", Toast.LENGTH_SHORT).show();

        }
        else if(intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING)){
            MainActivity.getInstance().onPause();
            Toast.makeText(context, "Incoming Call", Toast.LENGTH_SHORT).show();
        }
    }

}
