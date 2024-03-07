package com.example.transportrecord.ui.record.transport

import android.Manifest
import android.content.IntentSender
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.transportrecord.data.entity.TransportHistory
import com.example.transportrecord.databinding.ActivityRecordTransportBinding
import com.example.transportrecord.ui.MainViewModel
import com.example.transportrecord.ui.ViewModelFactory
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class RecordTransportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordTransportBinding
    private val viewModel: MainViewModel by viewModels { ViewModelFactory(application) }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var latLng: LatLng? = null

    private var selectedId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordTransportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations) {
                    Log.d(TAG, "onLocationResult: ${location.latitude} ${location.longitude}")
                    latLng = LatLng(location.latitude, location.longitude)
                    binding.tvLatitude.text = latLng?.latitude.toString()
                    binding.tvLongitude.text = latLng?.longitude.toString()
                }
            }
        }

        getMyLastLocation()
        createLocationRequest()
        startLocationUpdate()

        viewModel.getAllArmada().observe(this) { list ->
            val armadaId = list.toSet().map { it.armadaId }
            val armadaAdapter = ArrayAdapter(
                this, android.R.layout.simple_spinner_item, armadaId
            )
            armadaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            armadaAdapter.notifyDataSetChanged()
            binding.spIdArmada.adapter = armadaAdapter
            binding.spIdArmada.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?, view: View?, position: Int, id: Long
                    ) {
                        val selectedItem = parent?.getItemAtPosition(position).toString().toInt()
                        viewModel.getArmadaById(selectedItem)
                            .observe(this@RecordTransportActivity) { armada ->
                                binding.tvNoPol.text = armada.map { it.nopol }.first()
                                binding.tvDriver.text = armada.map { it.driver }.first()
                                binding.tvJenisArmada.text = armada.map { it.jenisArmada }.first()
                            }
                        selectedId = selectedItem
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
        }

        binding.etTanggal.setOnClickListener {
            val picker =
                MaterialDatePicker.Builder.datePicker().setTitleText("Pilih Tanggal").build()
            picker.show(this.supportFragmentManager, "TAG")
            picker.addOnPositiveButtonClickListener {
                binding.etTanggal.text = convertTimeToDate(it)
            }
            picker.addOnNegativeButtonClickListener { picker.dismiss() }
        }

        binding.etJam.setOnClickListener {
            val picker = MaterialTimePicker.Builder().setTitleText("Pilih Jam")
                .setTimeFormat(TimeFormat.CLOCK_24H).setHour(LocalDateTime.now().hour)
                .setMinute(LocalDateTime.now().minute)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK).build()
            picker.show(this.supportFragmentManager, "TAG")
            picker.addOnPositiveButtonClickListener {
                binding.etJam.text = buildString {
                    append(String.format(Locale.getDefault(), "%02d", picker.hour))
                    append(":")
                    append(String.format(Locale.getDefault(), "%02d", picker.minute))
                }
            }
        }


        binding.btRecord.setOnClickListener {
            if (binding.spIdArmada.adapter != null) {
                viewModel.recordTransport(
                    transportRecord = listOf(
                        TransportHistory(
                            refArmada = selectedId!!,
                            tgl = binding.etTanggal.text.toString().trim(),
                            jam = binding.etJam.text.toString().trim(),
                            latitude = latLng?.latitude,
                            longitude = latLng?.longitude,
                        )
                    )
                )
                Toast.makeText(this, "Perjalanan Tersimpan", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun convertTimeToDate(time: Long): String {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utc.timeInMillis = time
        val format = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        return format.format(utc.time)
    }


    override fun onPause() {
        super.onPause()
        stopLocationUpdate()
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdate()
    }


    private fun stopLocationUpdate() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun startLocationUpdate() {
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
        } catch (exception: SecurityException) {
            Log.e(TAG, "Error : " + exception.message)
        }
    }

    private val resolutionLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            RESULT_OK -> Log.i(TAG, "onActivityResult: All location settings are satisfied.")

            RESULT_CANCELED -> Toast.makeText(
                this,
                "Anda harus mengaktifkan GPS untuk menggunakan aplikasi ini!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun createLocationRequest() {
        locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, TimeUnit.SECONDS.toMillis(1))
                .setIntervalMillis(TimeUnit.SECONDS.toMillis(1))
                .setMaxUpdateDelayMillis(TimeUnit.SECONDS.toMillis(1))
                .setWaitForAccurateLocation(true).build()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        client.checkLocationSettings(builder.build()).addOnSuccessListener {
            getMyLastLocation()
        }.addOnFailureListener {
            if (it is ResolvableApiException) {
                try {
                    resolutionLauncher.launch(
                        IntentSenderRequest.Builder(it.resolution).build()
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Toast.makeText(this, sendEx.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                // Precise location access granted.
                getMyLastLocation()
                startLocationUpdate()
            }

            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                // Only approximate location access granted.
                getMyLastLocation()
                startLocationUpdate()
            }

            else -> {
                // No location access granted.
            }
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this, permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startLocation(location: Location): LatLng {
        return LatLng(location.latitude, location.longitude)
    }

    private fun getMyLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) && checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    startLocation(it)
                } else {
                    Toast.makeText(this, "Location not Found. Try Again", Toast.LENGTH_SHORT).show()
                }
            }

        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }


    companion object {
        const val TAG = "RecordTransportActivity"
    }
}