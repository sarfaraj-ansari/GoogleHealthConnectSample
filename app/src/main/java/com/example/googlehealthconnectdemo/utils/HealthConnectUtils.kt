package com.example.googlehealthconnectdemo.utils

import android.os.Build
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord

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

suspend fun hasAllPermissions(hCC: HealthConnectClient): Boolean {
    return hCC.permissionController.getGrantedPermissions()
        .containsAll(permissions)
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
