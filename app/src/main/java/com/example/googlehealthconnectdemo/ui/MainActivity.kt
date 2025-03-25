package com.example.googlehealthconnectdemo.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.StepsRecord
import com.example.googlehealthconnectdemo.R
import com.example.googlehealthconnectdemo.utils.HealthConnectProvider
import com.example.googlehealthconnectdemo.utils.HealthConnectProviderContract
import com.example.googlehealthconnectdemo.utils.hasAllPermissions
import com.example.googlehealthconnectdemo.utils.isHealthConnectSupported
import com.example.googlehealthconnectdemo.utils.permissions
import com.example.googlehealthconnectdemo.utils.requestPermissionsActivityContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var insertHeartButton: Button
    private lateinit var insertExercise: Button
    private lateinit var insertSteps: Button
    private lateinit var readExerciseButton: Button
    private lateinit var readHeartButton: Button
    private lateinit var readStepsButton: Button
    private lateinit var textView: TextView
    private lateinit var etSteps: EditText
    private lateinit var etHeart: EditText
    private lateinit var etExercise: EditText

    private var healthConnectProvider: HealthConnectProviderContract? = null
    private var healthConnectClient: HealthConnectClient? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        readExerciseButton = findViewById(R.id.readExercise)
        readHeartButton = findViewById(R.id.readHeart)
        readStepsButton = findViewById(R.id.readSteps)
        insertHeartButton = findViewById(R.id.insertHeartRate)
        insertExercise = findViewById(R.id.insertExercise)
        insertSteps = findViewById(R.id.insertSteps)
        textView = findViewById(R.id.result)
        etSteps = findViewById(R.id.edtInsertSteps)
        etExercise = findViewById(R.id.edtInsertExercise)
        etHeart = findViewById(R.id.edtInsertHeartRate)

        healthConnectProvider = HealthConnectProvider()
        healthConnectClient = healthConnectProvider?.getHealthConnectClient(this)

        readExerciseButton.setOnClickListener { readExerciseData() }

        readHeartButton.setOnClickListener { readHeartData() }

        readStepsButton.setOnClickListener { readStepsData() }

        insertHeartButton.setOnClickListener {
            val start = ZonedDateTime.now()
            val end = ZonedDateTime.now()
            if (etHeart.text.isNotEmpty()) {
                insertHeartRateRecord(start.minusMinutes(1), end)
            } else {
                Toast.makeText(this, "Please enter heartbeat count", Toast.LENGTH_SHORT).show()
            }

        }

        insertExercise.setOnClickListener {
            val start = ZonedDateTime.now()
            val end = ZonedDateTime.now()
            if (etExercise.text.isNotEmpty()) {
                insertExerciseSessionRecord(
                    start.minusMinutes(etExercise.text.toString().toLong()),
                    end
                )
            } else {
                Toast.makeText(this, "Please enter exercise time", Toast.LENGTH_SHORT).show()
            }
        }

        insertSteps.setOnClickListener {
            val start = ZonedDateTime.now()
            val end = ZonedDateTime.now()

            if (etSteps.text.isNotEmpty()) {
                insertStepsRecord(start.minusMinutes(10), end, etSteps.text.toString().toLong())
            } else {
                Toast.makeText(this, "Please enter step count", Toast.LENGTH_SHORT).show()
            }

        }


    }

    private val requestPermissions =
        registerForActivityResult(requestPermissionsActivityContract) { granted ->
            if (granted.containsAll(permissions)) {
                println("All permission granted and inserting begins")
                /*
                CoroutineScope(Dispatchers.Unconfined).launch {
                    writeSessionToConnect(
                        healthConnectClient!!,
                        getExerciseData(_start, _end.minusHours(1))
                    )
                }
                */
            } else {
                println("All permission required")
            }
        }

    private fun readHeartData() {

        if (isHealthConnectSupported()) {

            CoroutineScope(Dispatchers.Unconfined).launch {
                if (hasAllPermissions(healthConnectClient!!)) {
                    println("All permissions are available and reading begins")

                    val listOfRecords = healthConnectProvider?.readSessionRecord(
                        healthConnectClient!!,
                        HeartRateRecord::class,
                        ZonedDateTime.now().minusHours(2),
                        ZonedDateTime.now()
                    )

                    if (listOfRecords?.isNotEmpty() == true) {
                        val stringBuilder = StringBuilder()
                        listOfRecords.forEach { i: HeartRateRecord ->
                            stringBuilder.append("\n\nbeatsPerMinute:- ")
                            i.samples.forEach {
                                println("beatsPerMinute---- ${it.beatsPerMinute}")
                                stringBuilder.append("${it.beatsPerMinute}")
                            }
                        }
                        runOnUiThread { textView.text = stringBuilder }
                    } else {
                        runOnUiThread { textView.text = "No records found" }
                    }


                } else {
                    requestPermissions.launch(permissions)
                }
            }
        }

    }

    private fun readStepsData() {

        if (isHealthConnectSupported()) {

            CoroutineScope(Dispatchers.Unconfined).launch {
                if (hasAllPermissions(healthConnectClient!!)) {
                    println("All permissions are available and reading begins")

                    val listOfRecords = healthConnectProvider?.readSessionRecord(
                        healthConnectClient!!,
                        StepsRecord::class,
                        ZonedDateTime.now().minusHours(2),
                        ZonedDateTime.now()
                    )

                    if (listOfRecords?.isNotEmpty() == true) {
                        val stringBuilder = StringBuilder()
                        listOfRecords.forEach { i: StepsRecord ->
                            stringBuilder.append("${i.count}, ")
                        }
                        runOnUiThread { textView.text = "Steps count:- $stringBuilder" }
                    } else {
                        runOnUiThread { textView.text = "No records found" }
                    }


                } else {
                    requestPermissions.launch(permissions)
                }
            }
        }

    }

    private fun readExerciseData() {

        if (isHealthConnectSupported()) {

            CoroutineScope(Dispatchers.Unconfined).launch {
                if (hasAllPermissions(healthConnectClient!!)) {
                    println("All permissions are available and reading begins")

                    val listOfRecords = healthConnectProvider?.readSessionRecord(
                        healthConnectClient!!,
                        ExerciseSessionRecord::class,
                        ZonedDateTime.now().minusHours(2),
                        ZonedDateTime.now()
                    )

                    if (listOfRecords?.isNotEmpty() == true) {
                        val stringBuilder = StringBuilder()
                        listOfRecords.forEach { i: ExerciseSessionRecord ->
                            println("record------> ${i.exerciseType}")
                            val duration = Duration.between(i.startTime, i.endTime)
                            //stringBuilder.append("exercise duration:- ${duration.toHours()}.${duration.toMinutes()} hour,\nTitle:- ${i.title}, exerciseType: ${i.exerciseType}, laps: ${i.laps}, notes: ${i.notes}, exerciseRouteResult: ${i.exerciseRouteResult}\n\n")
                            stringBuilder.append(
                                "Exercise duration:- ${duration.toHours()}.${duration.toMinutes()} hour,\nStart time:- ${
                                    getTimeFromInstant(
                                        i.startTime
                                    )
                                },\nEnd time:- ${getTimeFromInstant(i.endTime)},\nTitle:- ${i.title}\n\n"
                            )
                        }
                        runOnUiThread { textView.text = stringBuilder }
                    } else {
                        runOnUiThread { textView.text = "No records found" }
                    }


                } else {
                    requestPermissions.launch(permissions)
                }
            }
        }

    }

    private fun insertHeartRateRecord(start: ZonedDateTime, end: ZonedDateTime) {

        if (isHealthConnectSupported()) {

            CoroutineScope(Dispatchers.Unconfined).launch {
                if (hasAllPermissions(healthConnectClient!!)) {
                    println("All permissions are available and inserting begins")
                    healthConnectProvider?.writeSessionToConnect(
                        healthConnectClient!!,
                        listOf(
                            healthConnectProvider!!.buildHeartRateSeries(
                                start,
                                end,
                                etHeart.text.toString().toLong()
                            )
                        )
                    )
                    runOnUiThread { textView.text = "Heart rate inserted" }
                } else {
                    requestPermissions.launch(permissions)
                }
            }
        }

    }

    private fun insertExerciseSessionRecord(start: ZonedDateTime, end: ZonedDateTime) {

        if (isHealthConnectSupported()) {

            CoroutineScope(Dispatchers.Unconfined).launch {
                if (hasAllPermissions(healthConnectClient!!)) {
                    println("All permissions are available and inserting begins")
                    healthConnectProvider?.writeSessionToConnect(
                        healthConnectClient!!,
                        listOf(
                            ExerciseSessionRecord(
                                startTime = start.toInstant(),
                                startZoneOffset = start.offset,
                                endTime = end.toInstant(),
                                endZoneOffset = end.offset,
                                exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_RUNNING,
                                title = "My Run #${Random.nextInt(0, 60)}"
                            )
                        )
                    )
                    runOnUiThread { textView.text = "Exercise Session inserted" }
                } else {
                    requestPermissions.launch(permissions)
                }
            }
        }

    }

    private fun insertStepsRecord(start: ZonedDateTime, end: ZonedDateTime, noOfSteps: Long) {

        if (isHealthConnectSupported()) {

            CoroutineScope(Dispatchers.Unconfined).launch {
                if (hasAllPermissions(healthConnectClient!!)) {
                    println("All permissions are available and inserting begins")
                    healthConnectProvider?.writeSessionToConnect(
                        healthConnectClient!!,
                        listOf(
                            StepsRecord(
                                startTime = start.toInstant(),
                                startZoneOffset = start.offset,
                                endTime = end.toInstant(),
                                endZoneOffset = end.offset,
                                count = noOfSteps
                            )
                        )
                    )
                    runOnUiThread { textView.text = "Steps Record inserted" }
                } else {
                    requestPermissions.launch(permissions)
                }
            }
        }

    }

    private fun getExerciseData(start: ZonedDateTime, end: ZonedDateTime): List<Record> {

        val records = listOf(
            ExerciseSessionRecord(
                startTime = start.toInstant(),
                startZoneOffset = start.offset,
                endTime = end.toInstant(),
                endZoneOffset = end.offset,
                exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_RUNNING,
                title = "My Run #${Random.nextInt(0, 60)}"
            ),
            StepsRecord(
                startTime = start.toInstant(),
                startZoneOffset = start.offset,
                endTime = end.toInstant(),
                endZoneOffset = end.offset,
                count = (1000 + 1000 * Random.nextInt(3)).toLong()
            )
        ) + healthConnectProvider!!.buildHeartRateSeries(start, end, 10)

        return records;

    }

    private fun getTimeFromInstant(instant: Instant): String {

        val zonedDateTime: ZonedDateTime = instant.atZone(ZoneId.systemDefault())

        val hour: Int = zonedDateTime.hour
        val minute: Int = zonedDateTime.minute
        val second: Int = zonedDateTime.second

        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss")
        val formattedTime: String = zonedDateTime.format(formatter)
        return formattedTime
    }
}