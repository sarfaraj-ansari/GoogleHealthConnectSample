package com.example.googlehealthconnectdemo.utils

import android.os.Build
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.googlehealthconnectdemo.utils.HealthConnectProvider.healthConnectClient
import java.time.ZonedDateTime

// The minimum android level that can use Health Connect
private const val MIN_SUPPORTED_SDK = Build.VERSION_CODES.O_MR1

val permissions = setOf<String>(
    HealthPermission.getReadPermission(HeartRateRecord::class),
    HealthPermission.getWritePermission(HeartRateRecord::class),
    HealthPermission.getReadPermission(StepsRecord::class),
    HealthPermission.getWritePermission(StepsRecord::class),
    HealthPermission.getReadPermission(ExerciseSessionRecord::class),
    HealthPermission.getWritePermission(ExerciseSessionRecord::class),
    HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
    HealthPermission.getWritePermission(TotalCaloriesBurnedRecord::class),
    HealthPermission.getReadPermission(WeightRecord::class),
    HealthPermission.getWritePermission(WeightRecord::class)
)

val requestPermissionsActivityContract: ActivityResultContract<Set<String>, Set<String>> =
    PermissionController.createRequestPermissionResultContract()

private fun isSupported() = Build.VERSION.SDK_INT >= MIN_SUPPORTED_SDK

fun isHealthConnectSupported(): Boolean {
    return isSupported()
}

suspend fun hasAllPermissions(): Boolean {
    return healthConnectClient!!.permissionController.getGrantedPermissions()
        .containsAll(permissions)
}

suspend inline fun <reified T : Record> readSessionRecord(
    healthConnectClient: HealthConnectClient,
    startTime: ZonedDateTime,
    endTime: ZonedDateTime
): List<T>? {

    return try {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                T::class,
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

suspend inline fun <reified T : Record> writeSessionToConnect(
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

fun buildHeartRateSeries(
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

/*
fun buildHeartRateSeries(
    sessionStartTime: ZonedDateTime,
    sessionEndTime: ZonedDateTime,
): HeartRateRecord {
    val samples = mutableListOf<HeartRateRecord.Sample>()
    var time = sessionStartTime
    while (time.isBefore(sessionEndTime)) {
        samples.add(
            HeartRateRecord.Sample(
                time = time.toInstant(),
                beatsPerMinute = (80 + Random.nextInt(80)).toLong()
            )
        )
        time = time.plusSeconds(30)
    }
    return HeartRateRecord(
        startTime = sessionStartTime.toInstant(),
        startZoneOffset = sessionStartTime.offset,
        endTime = sessionEndTime.toInstant(),
        endZoneOffset = sessionEndTime.offset,
        samples = samples
    )
}
*/
