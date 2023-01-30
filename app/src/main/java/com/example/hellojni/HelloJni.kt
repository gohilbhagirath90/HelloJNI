/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.hellojni

import android.annotation.SuppressLint
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.hellojni.databinding.ActivityHelloJniBinding

class HelloJni : AppCompatActivity(), ProbeConnectionInterface {

    val TAG = "HelloJni"
    var mUSBManager : LocalUSBManager? = null
    var binding : ActivityHelloJniBinding? = null
    var probeConnectionReceiver : ProbeConnectionReceiver? = null
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate() called with: savedInstanceState = $savedInstanceState")
        /*
         * Retrieve our TextView and set its content.
         * the text is retrieved by calling a native
         * function.
         */
        binding = ActivityHelloJniBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        binding?.helloTextview?.text = stringFromJNI()
        registerProbeConnectionReceiver()
        if(mUSBManager == null) {
            mUSBManager = LocalUSBManager(applicationContext)
            val status = mUSBManager?.isConnected();
            Log.d(TAG, "mUSBManager init isConnected() : $status")
            binding?.helloTextview?.text = "Connect Status : $status"
        }
    }

    private fun registerProbeConnectionReceiver(){
        Log.d(TAG, "registerProbeConnectionReceiver() called")
        if(probeConnectionReceiver == null) {
            probeConnectionReceiver = ProbeConnectionReceiver(this)
        }
        val filter = IntentFilter()
        filter.addAction(LocalUSBManager.ACTION_PROBE_CONNECT)
        filter.addAction(LocalUSBManager.ACTION_PROBE_DISCONNECT)
        filter.addAction(LocalUSBManager.ACTION_USB_PERMISSION_NOT_GRANTED)
        this.registerReceiver(probeConnectionReceiver, filter)
    }


    private fun deregisterProbeConnection(){
        Log.d(TAG, "deregisterProbeConnection() called")
        if(probeConnectionReceiver != null){
            this.unregisterReceiver(probeConnectionReceiver)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
        val status = mUSBManager?.isConnected();
        Log.d(TAG, "mUSBManager resume isConnected() : $status")
        binding?.helloTextview?.text = "Connect Status : $status"
    }


    override fun onPause() {
        Log.d(TAG, "onPause() called")
        super.onPause()
        //mUSBManager?.unregisterReceiver()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy() called")
        super.onDestroy()
        mUSBManager?.unregisterReceiver()
        deregisterProbeConnection()
    }

    /*
     * A native method that is implemented by the
     * 'hello-jni' native library, which is packaged
     * with this application.
     */
    external fun stringFromJNI(): String?
    external fun init(fd : Int) : Int

    /*
     * This is another native method declaration that is *not*
     * implemented by 'hello-jni'. This is simply to show that
     * you can declare as many native methods in your Java code
     * as you want, their implementation is searched in the
     * currently loaded native libraries only the first time
     * you call them.
     *
     * Trying to call this function will result in a
     * java.lang.UnsatisfiedLinkError exception !
     */
   // external fun unimplementedStringFromJNI(): String?

    companion object {
    /*
     * this is used to load the 'hello-jni' library on application
     * startup. The library has already been unpacked into
     * /data/data/com.example.hellojni/lib/libhello-jni.so
     * at the installation time by the package manager.
     */
        init {
            System.loadLibrary("hello-jni")
        }
    }

    override fun onConnect() {
        Log.d(TAG, "onConnect() called")
        binding?.helloTextview?.text = "Connect Status : true"
    }

    override fun onDisconnect() {
        Log.d(TAG, "onDisconnect() called")
        binding?.helloTextview?.text = "Connect Status : false"
    }

    override fun onPermissionDenied() {
        Log.d(TAG, "onPermissionDenied() called")
        binding?.helloTextview?.text = "Connect Status : permission denied"
    }
}

