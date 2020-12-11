package android.example.threadsverificationtask

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.BatteryManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import java.util.concurrent.atomic.AtomicReference

object PhoneData {
    val location = AtomicReference<String>()
    val batteryPercentage = AtomicReference<String>()
}

class MainActivity : AppCompatActivity() {

    companion object{
        const val A = 1
        const val B = 2
        const val C = 3
    }

    lateinit var thread1: Thread
    lateinit var thread2: Thread
    lateinit var thread3: Thread3

    var threadStarted = false

    private lateinit var batteryLevelReceiver: BroadcastReceiver
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PhoneData.location.set("")
        PhoneData.batteryPercentage.set("")

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {

            getCurrentLocation()

        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 100)
        }

        batteryLevelReceiver = object: BroadcastReceiver(){
            override fun onReceive(p0: Context?, p1: Intent?) {
                val level = p1?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                    PhoneData.batteryPercentage.set(level.toString())
            }
        }

        thread1 = initializeThread1()
        thread2 = initializeThread2()
        thread3 = Thread3(C)

        findViewById<Button>(R.id.start_button).setOnClickListener {
            if(!threadStarted){
                thread1.start()
                thread2.start()
                thread3.start()
            }

            threadStarted = true
        }

        findViewById<Button>(R.id.stop_button).setOnClickListener {
            thread1.interrupt()
            thread2.interrupt()
            thread3.interrupt()
            thread1 = initializeThread1()
            thread2 = initializeThread2()
            thread3 = Thread3(C)
            threadStarted = false
        }
    }

    private fun initializeThread1(): Thread {
        return Thread {
            while (true) {
                if(!Thread.currentThread().isInterrupted){
                    try {
                        Thread.sleep(A * 1000L)
                        thread3.addPhoneData(PhoneData.location.get())
                    } catch (e: InterruptedException){
                        break
                    }
                } else {
                    break
                }
            }
        }
    }

    private fun initializeThread2(): Thread {
            return Thread {
                while (true) {
                    if(!Thread.currentThread().isInterrupted){
                        try {
                            Thread.sleep(B * 1000L)
                            thread3.addPhoneData(PhoneData.batteryPercentage.get())
                        } catch (e: InterruptedException){
                            break
                        }
                    } else {
                        break
                    }
                }
            }
    }

    override fun onPause() {
        unregisterReceiver(batteryLevelReceiver)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(batteryLevelReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(){

        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                val locationRequest = LocationRequest()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(10000)
                        .setFastestInterval(1000)

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(p0: LocationResult?) {
                        val location = p0?.lastLocation
                        val coordinates = "${location?.latitude}, ${location?.longitude}"
                        PhoneData.location.set(coordinates)
                    }
                }

                fusedLocationProviderClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.myLooper()
                )
            }
        }
    }
}