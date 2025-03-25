package com.example.googlehealthconnectdemo.utils

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import java.time.ZonedDateTime
import kotlin.reflect.KClass

interface HealthConnectProviderContract {

    fun getHealthConnectClient(context: Context): HealthConnectClient

    suspend fun <T : Record> readSessionRecord(
        healthConnectClient: HealthConnectClient,
        recordClass: KClass<T>,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime
    ): List<T>?

    suspend fun <T : Record> writeSessionToConnect(
        healthConnectClient: HealthConnectClient,
        records: List<T>
    )

    fun buildHeartRateSeries(
        sessionStartTime: ZonedDateTime,
        sessionEndTime: ZonedDateTime,
        noOfBeats: Long
    ): HeartRateRecord
}