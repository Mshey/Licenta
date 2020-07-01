package com.example.licenta.view.fragment

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color.RED
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.beust.klaxon.*
import com.example.licenta.model.NavEvent
import com.example.licenta.R
import com.example.licenta.Utils.TAG
import com.example.licenta.viewmodel.TripViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.processors.PublishProcessor
import kotlinx.android.synthetic.main.fragment_map.*
import org.jetbrains.anko.async
import org.jetbrains.anko.uiThread
import java.net.URL
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback {
    @Inject
    lateinit var navEvents: PublishProcessor<NavEvent>

    private var locationPermissionGranted = false
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var locationCallback: LocationCallback
    private var mMap: GoogleMap? = null
    private lateinit var locationRequest: LocationRequest
    private var zoomIn = true

    private var tripId: String = ""
    private var isTripOrganizer: Boolean = false
    private lateinit var currentOrganizerLatLng: LatLng
    private lateinit var destinationLatLng: LatLng
    private lateinit var tripViewModel: TripViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    return
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        tripViewModel = ViewModelProvider(this).get(
            "maps",
            TripViewModel::class.java
        )
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var tripParticipantIndex: Int = -1

        setFragmentResultListener("details") { _, bundle ->
            tripId = bundle.getString(getString(R.string.trip_id))!!
            isTripOrganizer = bundle.getBoolean(getString(R.string.trip_org))

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    for (location in locationResult.locations) {
                        tripViewModel.updateTrip(
                            tripId,
                            location,
                            isTripOrganizer,
                            tripParticipantIndex
                        )
                        currentOrganizerLatLng = LatLng(location.latitude, location.longitude)

                        // Update UI with location data
                        if (zoomIn) {
                            val center: CameraUpdate =
                                CameraUpdateFactory.newLatLng(currentOrganizerLatLng)
                            val zoom: CameraUpdate = CameraUpdateFactory.zoomTo(20F)
                            mMap?.moveCamera(center)
                            mMap?.animateCamera(zoom)
                            zoomIn = false
                        }
                        if (checkSelfPermission(
                                requireContext(),
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                                requireContext(),
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return
                        }
                        mMap?.isMyLocationEnabled = true
                    }
                }
            }

            if (isTripOrganizer) {
                val innerLat = bundle.getDouble("trip lat")
                val innerLng = bundle.getDouble("trip lng")
                destinationLatLng = LatLng(innerLat, innerLng)

                stop_trip_button.visibility = View.VISIBLE
                stop_trip_button.setOnClickListener {
                    tripViewModel.stopTrip(tripId)
                    navEvents.onNext(
                        NavEvent(
                            NavEvent.Destination.FUTURE
                        )
                    )
                }

                val options = PolylineOptions()
                options.color(RED)
                options.width(5f)

                // build URL to call API
                fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
                    currentOrganizerLatLng = LatLng(location.latitude, location.longitude)
                    val url = getURL(currentOrganizerLatLng, destinationLatLng)
                    val latLngBounds = LatLngBounds.Builder()

                    async {
                        // Connect to URL, download content and convert into string asynchronously
                        val result = URL(url).readText()
                        Log.d(TAG, "result: $result")
                        uiThread {
                            // When API call is done, create parser and convert into JsonObjec
                            val parser = Parser()
                            val stringBuilder: StringBuilder = StringBuilder(result)
                            val json: JsonObject = parser.parse(stringBuilder) as JsonObject
                            // get to the correct element in JsonObject
                            val routes = json.array<JsonObject>("routes")
                            val points = routes!!["legs"]["steps"][0] as JsonArray<JsonObject>
                            // For every element in the JsonArray, decode the polyline string and pass all points to a List
                            val polypts =
                                points.flatMap {
                                    decodePoly(
                                        it.obj("polyline")?.string("points")!!
                                    )
                                }
                            // Add  points to polyline and bounds
                            options.add(currentOrganizerLatLng)
                            latLngBounds.include(currentOrganizerLatLng)
                            for (point in polypts) {
                                options.add(point)
                                latLngBounds.include(point)
                            }
                            options.add(destinationLatLng)
                            latLngBounds.include(destinationLatLng)
                            // build bounds
                            val bounds = latLngBounds.build()
                            // add polyline to the map
                            mMap!!.addPolyline(options)
                            // show map with route centered
                            mMap!!.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                        }
                    }
                }

                val markerHashMap: HashMap<String, Marker> = HashMap()
                tripViewModel.getParticipants(tripId)
                tripViewModel.tripParticipants.observe(viewLifecycleOwner, Observer {
                    it.forEach { it2 ->
                        if (it2.userLocation.latitude != 0.0 && it2.userLocation.longitude != 0.0) {
                            if (markerHashMap.containsKey(it2.username)) {
                                markerHashMap[it2.username]?.position =
                                    LatLng(it2.userLocation.latitude, it2.userLocation.longitude)
                            } else {
                                markerHashMap[it2.username] = mMap!!.addMarker(
                                    MarkerOptions().position(
                                        LatLng(
                                            it2.userLocation.latitude,
                                            it2.userLocation.longitude
                                        )
                                    )
                                        .title(it2.username)
                                )
                            }
                        }
                    }
                })
            } else {
                tripParticipantIndex = bundle.getInt("participant index")
                stop_trip_button.visibility = View.VISIBLE
                stop_trip_button.text = getString(R.string.exit_trip)
                stop_trip_button.setOnClickListener {
                    navEvents.onNext(
                        NavEvent(
                            NavEvent.Destination.FUTURE
                        )
                    )
                }

                tripViewModel.isTripActive(tripId)
                tripViewModel.isTripActiveMutableLiveData.observe(viewLifecycleOwner, Observer {
                    if (!it) {
                        Toast.makeText(requireContext(), "Your trip has ended!", Toast.LENGTH_LONG).show()
                        navEvents.onNext(
                            NavEvent(
                                NavEvent.Destination.FUTURE
                            )
                        )
                    }
                })

                var organizerMarker: Marker? = null
                tripViewModel.getOrganizerLocation(tripId)
                tripViewModel.organizerLocationMutableLiveData.observe(viewLifecycleOwner, Observer {
                    if (organizerMarker == null) {
                        organizerMarker = mMap!!.addMarker(
                            MarkerOptions().position(it).title(getString(R.string.organizer))
                        )
                    } else {
                        organizerMarker!!.position = it
                    }
                })
            }
        }

        val mMapView: MapView = map
        mMapView.onCreate(savedInstanceState)
        mMapView.onResume()
        mMapView.getMapAsync(this) //when you already implement OnMapReadyCallback in your fragment

        locationPermission
    }

    private fun checkMapServices(): Boolean {
        Log.d(TAG, "checkMapServices called")
        return if (isServicesOK) {
            isMapsEnabled
        } else false
    }

    private fun buildAlertMessageNoGps() {
        Log.d(TAG, "buildAlertmessagenoGps called")
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                val enableGpsIntent =
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(
                    enableGpsIntent,
                    PERMISSIONS_REQUEST_ENABLE_GPS
                )
            }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    private val isMapsEnabled: Boolean
        get() {
            Log.d(TAG, "isMapsEnabled called")
            val manager =
                requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps()
                return false
            }
            return true
        }//getLastKnownLocation();

    /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
    private val locationPermission: Unit
        get() {
            /*
             * Request location permission, so that we can get the location of the
             * device. The result of the permission request is handled by a callback,
             * onRequestPermissionsResult.
             */
            Log.d(TAG, "getLocationPermission called")
            if (checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationPermissionGranted = true
                //getLastKnownLocation();
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                )
            }
        }//an error occurred but we can resolve it

    //everything is fine and the user can make map requests
    private val isServicesOK: Boolean
        get() {
            Log.d(TAG, "isServicesOK: checking google services version")
            val available =
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity)
            when {
                available == ConnectionResult.SUCCESS -> {
                    //everything is fine and the user can make map requests
                    Log.d(TAG, "isServicesOK: Google Play Services is working")
                    return true
                }
                GoogleApiAvailability.getInstance().isUserResolvableError(available) -> {
                    //an error occurred but we can resolve it
                    Log.d(TAG, "isServicesOK: an error occured but we can fix it")
                    val dialog: Dialog = GoogleApiAvailability.getInstance().getErrorDialog(
                        activity,
                        available,
                        ERROR_DIALOG_REQUEST
                    )
                    dialog.show()
                }
                else -> {
                    Toast.makeText(activity, "You can't make map requests", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            return false
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionsResult called")
        locationPermissionGranted = false
        // If request is cancelled, the result arrays are empty.
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                locationPermissionGranted = true
            }
        }
    }

    override fun onResume() {
        Log.d(TAG, "onResume called")
        super.onResume()
        if (checkMapServices()) {
            if (locationPermissionGranted) {
                zoomIn = true
                startLocationUpdates()
            } else {
                locationPermission
            }
        }
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun startLocationUpdates() {
        if (checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        createLocationRequest()
        fusedLocationClient!!.requestLocationUpdates(
            locationRequest, locationCallback,
            null /* Looper */
        )
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient!!.removeLocationUpdates(locationCallback)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady called")
        mMap = googleMap
//        if (isTripOrganizer) {
//            val markerHashMap: HashMap<String, Marker> = HashMap()
//            tripViewModel.getParticipants(tripId)
//            tripViewModel.tripParticipants.observe(viewLifecycleOwner, Observer {
//                it.forEach { it2 ->
//                    if (it2.userLocation.latitude != 0.0 && it2.userLocation.longitude != 0.0) {
//                        if (markerHashMap.containsKey(it2.username)) {
//                            markerHashMap[it2.username]?.position =
//                                LatLng(it2.userLocation.latitude, it2.userLocation.longitude)
//                        } else {
//                            markerHashMap[it2.username] = googleMap.addMarker(
//                                MarkerOptions().position(
//                                    LatLng(
//                                        it2.userLocation.latitude,
//                                        it2.userLocation.longitude
//                                    )
//                                )
//                                    .title(it2.username)
//                            )
//                        }
//                    }
//                }
//            })
//        } else {
//            var organizerMarker: Marker? = null
//            tripViewModel.getOrganizerLocation(tripId)
//            tripViewModel.organizerLocationMutableLiveData.observe(viewLifecycleOwner, Observer {
//                if (organizerMarker == null) {
//                    organizerMarker = googleMap.addMarker(
//                        MarkerOptions().position(it).title(ORGANIZER)
//                    )
//                } else {
//                    organizerMarker!!.position = it
//                }
//            })
//        }
    }

    private fun getURL(from: LatLng, to: LatLng): String {
        val origin = "origin=" + from.latitude + "," + from.longitude
        val dest = "destination=" + to.latitude + "," + to.longitude
        val key = "key=" + getString(R.string.map_api_key)
        val params = "$origin&$dest&$key"
        return "https://maps.googleapis.com/maps/api/directions/json?$params"
    }

    /**
     * Method to decode polyline points
     * Courtesy : https://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(p)
        }

        return poly
    }

    companion object {
        private const val ERROR_DIALOG_REQUEST = 9001
        const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9002
        private const val PERMISSIONS_REQUEST_ENABLE_GPS = 9003
    }
}