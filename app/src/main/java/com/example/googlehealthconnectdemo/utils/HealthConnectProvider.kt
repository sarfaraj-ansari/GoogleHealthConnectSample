package com.example.googlehealthconnectdemo.utils

import android.content.Context
import androidx.health.connect.client.HealthConnectClient

object HealthConnectProvider {

    var healthConnectClient: HealthConnectClient? = null

    fun initializeHealthConnect(context: Context) {
        synchronized(this) {
            if (healthConnectClient == null) {
                healthConnectClient = HealthConnectClient.getOrCreate(context)
            }
        }
    }

    val isHealthConnectInitialized = healthConnectClient != null

}