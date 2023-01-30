package com.example.hellojni

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ProbeConnectionReceiver(var probeConnectionInterface : ProbeConnectionInterface?) : BroadcastReceiver() {
    val TAG = "ProbeConnectionReceiver"
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            LocalUSBManager.ACTION_PROBE_CONNECT->{
                Log.d(TAG, "action ACTION_PROBE_CONNECT")
                probeConnectionInterface?.onConnect()
            }
            LocalUSBManager.ACTION_PROBE_DISCONNECT->{
                Log.d(TAG, "action ACTION_PROBE_DISCONNECT")
                probeConnectionInterface?.onDisconnect()
            }
            LocalUSBManager.ACTION_USB_PERMISSION_NOT_GRANTED->{
                Log.d(TAG, "action ACTION_USB_PERMISSION_NOT_GRANTED")
                probeConnectionInterface?.onPermissionDenied()
            }
        }
    }
}

