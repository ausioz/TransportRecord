package com.example.transportrecord.ui.history


import android.content.res.Resources
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.example.transportrecord.R
import com.example.transportrecord.data.entity.TransportHistory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.transportrecord.databinding.ActivityMapsBinding
import com.example.transportrecord.ui.MainViewModel
import com.example.transportrecord.ui.ViewModelFactory
import com.example.transportrecord.ui.history.TransportHistoryActivity.Companion.EXTRA_REF_ARMADA
import com.example.transportrecord.ui.history.TransportHistoryActivity.Companion.EXTRA_TGL_TRANSPORT
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import java.util.ArrayList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private val viewModel: MainViewModel by viewModels { ViewModelFactory(application) }

    private val transportHistoryList = mutableListOf<TransportHistory>()

    private val directionRequest = mutableListOf<DirectionsApiRequest>()
    private val paths: MutableList<LatLng> = ArrayList()
    private val boundsBuilder = LatLngBounds.Builder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setMapStyle()

        val geoApiContext =
            GeoApiContext.Builder().apiKey("AIzaSyBIZFTMnT_20Q9WZLfdJaKlZbyKbFEqgVU").build()


        val extraRefArmada = intent.getIntExtra(EXTRA_REF_ARMADA, -1)
        val extraTgl = intent.getStringExtra(EXTRA_TGL_TRANSPORT)

        Log.d(TAG, "armada: $extraRefArmada")
        Log.d(TAG, "tgl: $extraTgl")

        binding.tvArmadaId.text = buildString {
            append("ID Armada : ")
            append(extraRefArmada)
        }
        binding.tvTanggal.text = buildString {
            append("Tanggal Transport : ")
            append(extraTgl)
        }

        if (extraRefArmada != -1 && extraTgl != null) {
            viewModel.getTransportHistoryByArmadaAndDate(extraRefArmada, extraTgl)
                .observe(this) { history ->
                    transportHistoryList.addAll(history)
                    Log.d(TAG, "livedata: $transportHistoryList")


                    for (i in transportHistoryList.indices) {
                        if (i + 1 != transportHistoryList.size) {
                            directionRequest.add(
                                DirectionsApi.getDirections(
                                    geoApiContext,
                                    "${transportHistoryList[i].latitude},${transportHistoryList[i].longitude}",
                                    "${transportHistoryList[i + 1].latitude},${transportHistoryList[i + 1].longitude}"
                                )
                            )
                        }
                    }

                    directionRequest.forEach { request ->
                        try {
                            val res = request.await()
                            if (res.routes.isNullOrEmpty().not()) {
                                val route = res.routes[0]
                                if (route.legs.isNullOrEmpty().not()) {
                                    for (leg in route.legs) {
                                        if (leg.steps.isNullOrEmpty().not()) {
                                            for (step in leg.steps) {
                                                if (step.steps.isNullOrEmpty().not()) {
                                                    for (step1 in step.steps) {
                                                        step1.polyline?.let { points1 ->
                                                            val coordinates = points1.decodePath()
                                                            for (coordinate in coordinates) {
                                                                paths.add(
                                                                    LatLng(
                                                                        coordinate.lat,
                                                                        coordinate.lng
                                                                    )
                                                                )
                                                            }
                                                        }

                                                    }
                                                } else {
                                                    step.polyline?.let { points ->
                                                        val coordinates = points.decodePath()
                                                        for (coordinate in coordinates) {
                                                            paths.add(
                                                                LatLng(
                                                                    coordinate.lat, coordinate.lng
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        } catch (ex: Exception) {
                            Log.e(
                                "DirectionsApi", "DirectionsApi exception localizedMessage: $ex"
                            )
                        }
                    }

                    if (paths.isNotEmpty()) {
                        val opts = PolylineOptions().addAll(paths).color(Color.BLUE).width(10f)
                        mMap.addPolyline(opts)
                    }

                    Log.d(TAG, "transportHistoryList: $transportHistoryList")
                    transportHistoryList.forEach { historyMarker ->
                        mMap.addMarker(
                            MarkerOptions().position(
                                LatLng(
                                    historyMarker.latitude!!, historyMarker.longitude!!
                                )
                            ).title(("Transport ID : " + historyMarker.transportId)).snippet(
                                "Jam : ${historyMarker.jam}"
                            )
                        )
                        boundsBuilder.include(
                            LatLng(
                                historyMarker.latitude, historyMarker.longitude
                            )
                        )
                    }
                    val bounds: LatLngBounds = boundsBuilder.build()
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(
                            bounds,
                            resources.displayMetrics.widthPixels,
                            resources.displayMetrics.heightPixels,
                            300
                        )
                    )
                    mMap.uiSettings.isZoomControlsEnabled = true
                }
        }
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Log.e("StoryMapsActivity", "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e("StoryMapsActivity", "Can't find style. Error: ", exception)
        }
    }

    companion object {
        const val TAG = "MapsActivity"
    }
}