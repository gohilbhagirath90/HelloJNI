package com.example.hellojni

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.util.Log

class LocalUSBManager(val mContext: Context) {
    private val TAG = "LocalUSBManager"
    private val mUsbReceiver: BroadcastReceiver
    var fd = 0
    private val mUsbManager = mContext.getSystemService(Context.USB_SERVICE) as UsbManager
    val PENDING_INTENT_FLAG_MUTABLE = 33554432

    private var mPermissionIntent : PendingIntent? = null
    private var mUsbDevice: UsbDevice? = null

    companion object{
        val ACTION_USB_PERMISSION = "com.example.hellojni.USB_PERMISSION"
        val ACTION_PROBE_CONNECT = "com.example.hellojni.ACTION_PROBE_CONNECT"
        val ACTION_PROBE_DISCONNECT = "com.example.hellojni.ACTION_PROBE_DISCONNECT"
        val ACTION_USB_PERMISSION_NOT_GRANTED = "com.example.hellojni.USB_PERMISSION_NOT_GRANTED"

    }

    init {
        Log.d("USB", "init() called")
        mPermissionIntent = PendingIntent.getBroadcast(
            mContext,
            0,
            Intent(ACTION_USB_PERMISSION),
            PENDING_INTENT_FLAG_MUTABLE
        )
        mUsbReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (UsbManager.ACTION_USB_DEVICE_ATTACHED == action) {
                    Log.d("USB", " action ACTION_USB_DEVICE_ATTACHED")
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    if (mUsbManager.hasPermission(device)) {
                        fd = device!!.deviceId
                        Log.d("USB1", " device fd :$fd")
                        Log.d("USB1", " device init :${HelloJni().init(fd)}")

                    } else {
                        //mUsbManager.requestPermission(device, mPermissionIntent)
                    }
                } else if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                    Log.d("USB", " action ACTION_USB_DEVICE_DETACHED")
                    sendDisconnectEvent()
                }
                else if (ACTION_USB_PERMISSION == action) {
                    Log.d("USB", " action ACTION_USB_PERMISSION")
                    synchronized(this) {
                        val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                        val permissionGranted =
                            intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                        if (permissionGranted) {
                            if (device != null) {
                                Log.d("USB", " action ACTION_USB_PERMISSION Granted")
                                mUsbManager.openDevice(device)
                                fd = device.deviceId
                                Log.d("USB", " device fd :$fd")
                                Log.d("USB", " device init :${HelloJni().init(fd)}")
                                sendConnectEvent()
                            } else {

                            }
                        } else {
                            Log.d(
                                "USB",
                                "permission denied for device $device permissionGranted : $permissionGranted"
                            )
                            sendPermissionDeniedEvent()
                        }
                    }
                }
            }
        }

        val filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        filter.addAction(ACTION_USB_PERMISSION)
        mContext.registerReceiver(mUsbReceiver, filter)
    }

    fun sendConnectEvent(){
        val connectIntent =
            Intent(ACTION_PROBE_CONNECT)
        mContext.sendBroadcast(connectIntent)
    }

    fun sendDisconnectEvent(){
        val connectIntent =
            Intent(ACTION_PROBE_DISCONNECT)
        mContext.sendBroadcast(connectIntent)
    }

    fun sendPermissionDeniedEvent(){
        val connectIntent =
            Intent(ACTION_USB_PERMISSION_NOT_GRANTED)
        mContext.sendBroadcast(connectIntent)
    }
    fun isConnected() : Boolean {
        Log.d(TAG, "isConnected() called")
        val devices: HashMap<String, UsbDevice> = mUsbManager.deviceList
        if (devices.isEmpty()) {
            return false
        }

        for (device in devices.values) {
            Log.d(
                TAG, "Usb Device 0x" +
                        Integer.toHexString(device.vendorId) +
                        ":0x" +
                        Integer.toHexString(device.productId)
            )

            val isGranted: Boolean = mUsbManager.hasPermission(device)
            if (isGranted) {
                Log.d(TAG,
                    "USB Device Found Permission Granted "
                )
                mUsbDevice = device
                val usbDeviceConnection: UsbDeviceConnection = mUsbManager.openDevice(device)
                val fdId = usbDeviceConnection.fileDescriptor
                //fd = device.deviceId
                Log.d("USB", " isConnected device fd :$fdId")
                Log.d("USB", " isConnected device init :${HelloJni().init(fdId)}")
                sendConnectEvent()
            } else {
                Log.d(
                    TAG,
                    "Application is not granted"
                )
                mUsbManager.requestPermission(device, mPermissionIntent)
                break
            }
        }
        return (null != mUsbDevice);

    }

    // unregister the broadcast receiver
    fun unregisterReceiver() {
        Log.d("USB", "unregisterReceiver() called")
        mContext.unregisterReceiver(mUsbReceiver)
    }
}