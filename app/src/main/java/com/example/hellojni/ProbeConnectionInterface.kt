package com.example.hellojni

interface ProbeConnectionInterface {
    fun onConnect()
    fun onDisconnect()
    fun onPermissionDenied()
}