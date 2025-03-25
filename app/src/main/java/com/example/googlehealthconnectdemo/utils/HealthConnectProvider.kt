package com.example.googlehealthconnectdemo.utils

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.ZonedDateTime
import kotlin.reflect.KClass

class HealthConnectProvider : HealthConnectProviderContract {

    @Volatile
    private var healthConnectClient: HealthConnectClient? = null

    override fun getHealthConnectClient(context: Context): HealthConnectClient {
        return healthConnectClient ?: synchronized(this) {
            val instance = HealthConnectClient.getOrCreate(context.applicationContext)
            healthConnectClient = instance
            instance
        }
    }

    override suspend fun <T : Record> readSessionRecord(
        healthConnectClient: HealthConnectClient,
        recordClass: KClass<T>,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime
    ): List<T>? {
        return try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordClass,
                    timeRangeFilter = TimeRangeFilter.between(
                        startTime.toInstant(),
                        endTime.toInstant()
                    )
                )
            )
            println("reading record ------- successful")
            response.records
        } catch (e: Exception) {
            println("reading record ------- failed message:  ${e.message}")
            null
        }
    }

    override suspend fun <T : Record> writeSessionToConnect(
        healthConnectClient: HealthConnectClient,
        records: List<T>
    ) {
        try {
            healthConnectClient.insertRecords(records)
            println("inserting record ------- successful")
        } catch (e: Exception) {
            println("inserting record ------- failed message:  ${e.message}")
        }
    }

    override fun buildHeartRateSeries(
        sessionStartTime: ZonedDateTime,
        sessionEndTime: ZonedDateTime,
        noOfBeats: Long
    ): HeartRateRecord {
        val samples = mutableListOf<HeartRateRecord.Sample>()
        samples.add(
            HeartRateRecord.Sample(
                time = sessionStartTime.toInstant(),
                beatsPerMinute = noOfBeats
            )
        )
        return HeartRateRecord(
            startTime = sessionStartTime.toInstant(),
            startZoneOffset = sessionStartTime.offset,
            endTime = sessionEndTime.toInstant(),
            endZoneOffset = sessionEndTime.offset,
            samples = samples
        )
    }
}