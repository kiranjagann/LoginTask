package com.example.loginapp.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.example.loginapp.R
import com.example.loginapp.utility.DataParser
import com.example.loginapp.databinding.FragmentMapsBinding
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MapsFragment : Fragment() , OnMapReadyCallback {

    private lateinit var mBinding: FragmentMapsBinding

    private var mMaps : GoogleMap? = null
    lateinit var mapView: MapView
    private var mapViewBundleKey = "MapViewBundleKey"
    private val DefaultZoom = 15f
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var latitude = 0.0
    private var longitude = 0.0
    private var endLatitude = 0.0
    private var endLongitude = 0.0
    private var origin: MarkerOptions? = null
    private var destination: MarkerOptions? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentMapsBinding.inflate(inflater, container, false)
        mapView = mBinding.map1
        getBundleArguments()

        askPermissionLocation()

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null){
            mapViewBundle = savedInstanceState.getBundle(mapViewBundleKey)
        }

        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)

        selectLocation(mapViewBundle);

        return mBinding.root
    }

    /**
     * Method Name : getBundleArguments
     * Used For : to set email id
     */
    private fun getBundleArguments() {
        if( arguments != null && requireArguments().containsKey("login_id")){
            mBinding.email.text = "Welcome "+ requireArguments().getString("login_id")
        }
    }

    /**
     * Method Name : selectLocation
     * Used For : autocomplete fragment for search
     */
    private fun selectLocation(mapViewBundle: Bundle?) {
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.api_key), Locale.US);
        }
        val autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME))

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                mapView.onCreate(mapViewBundle)
                mapView.getMapAsync(this@MapsFragment)
                Log.i(ContentValues.TAG, "Place: ${place.name}, ${place.id}")
                searchArea(place.name)
            }

            override fun onError(status: Status) {
                Log.i(ContentValues.TAG, "An error occurred: $status")
            }
        })
    }

    /**
     * Method Name : searchArea
     * Used For : to search user entered location
     */
    private fun searchArea(name: String?) {
        var addressList : List<Address>? = null
        val markerOptions = MarkerOptions()

        if (name != ""){
            val geocoder = Geocoder(context!!)
            try {
                addressList = geocoder.getFromLocationName(name!!,5)

            }catch (e: IOException){
                e.printStackTrace()
            }
            if (addressList != null){
                for (i in addressList.indices){
                    val myAddress = addressList[i]
                    val latLng = LatLng(myAddress.latitude, myAddress.longitude)
                    markerOptions.position(latLng)
                    mMaps!!.addMarker(markerOptions)
                    endLatitude = myAddress.latitude
                    endLongitude = myAddress.longitude
                    mMaps!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                    val more = MarkerOptions()
                    more.title("Distance")
                    val result = FloatArray(10)
                    Location.distanceBetween(
                        latitude, longitude, endLatitude, endLongitude, result
                    )
                    val s = String.format("%.1f",result[0]/1000)

                    val estimatedTime = String.format("%.1f",result[0] / 250)

                    origin = MarkerOptions().position(LatLng(latitude, longitude))
                        .title("HSR Layout").snippet("origin")
                    destination = MarkerOptions().position(LatLng(endLatitude, endLongitude))
                        .title(name)
                        .snippet("Distance = $s Km")

                    mMaps!!.addMarker(destination)
                    mMaps!!.addMarker(origin)
                    mBinding.tvAdd!!.setText("Distance = $s Km, Time = $estimatedTime Mins")

                    val url :String = getDirectionUrl(origin!!.getPosition(), destination!!.getPosition())!!

                    val downloadTask: DownloadTask = DownloadTask()
                    downloadTask.execute(url)
                }
            }
        }
    }

    /**
     * Method Name : getDirectionUrl
     * Used For : to get url details
     */
    private fun getDirectionUrl(origin: LatLng, dest: LatLng): String? {
        val str_origin = "origin=" + origin.latitude + "," + origin.longitude
        val str_dest = "destination=" + dest.latitude + "," + dest.longitude

        val mode = "mode=driving"
        val parameter = "$str_origin&$str_dest&$mode"
        val output = "json"
        return "https://maps.googleapis.com/maps/api/directions/$output?$parameter&key=AIzaSyCEyLWz9tkomKxaPSfLdDqfv1oqDV6GaAo"
    }

    /**
     * Method Name : DownloadTask
     * Used For : to fetch user location lat long from url
     */
    inner class DownloadTask : AsyncTask<String?, Void?, String>(){
        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            val parserTask = ParserTask()
            parserTask.execute(result)
            Log.i(ContentValues.TAG, "onPostExecute: download task $result")
        }

        override fun doInBackground(vararg url: String?): String {
            var data = ""
            try {
                data = downloadUrl(url[0].toString()).toString()

            }catch (e:java.lang.Exception){
                Log.d("doInBackground:",e.toString())
            }
            return data
        }

    }

    /**
     * Method Name : downloadUrl
     * Used For : A method to download json data from url
     */
    private fun downloadUrl(strUrl: String): String? {
        var data = ""
        var iStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null
        try{
            val url = URL(strUrl)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connect()
            iStream = urlConnection!!.inputStream
            val br = BufferedReader(InputStreamReader(iStream))
            val sb = StringBuffer()
            var line: String? = ""
            while (br.readLine().also { line = it } != null){
                sb.append(line)
            }
            data = sb.toString()
            br.close()
        }catch (e:java.lang.Exception){
            Log.d("Exception",e.toString())
        } finally {
            iStream!!.close()
            urlConnection!!.disconnect()
        }
        return data
    }

    /**
     * class Name : ParserTask
     * Used For : used to access data and add polylines in map
     */
    inner class ParserTask: AsyncTask<String?, Int?, List<List<HashMap<String, String>>>?>(){

        /**
         * Method Name : doInBackground
         * Used For : Passing data in non ui thread
         */
        override fun doInBackground(vararg jsonData: String?): List<List<HashMap<String, String>>>? {
            Log.i(ContentValues.TAG, "doInBackground: Parser task $jsonData")
            val jObject: JSONObject
            var routes: List<List<HashMap<String, String>>>? = null
            try {
                jObject = JSONObject(jsonData[0])
                val parser = DataParser()
                routes = parser.parse(jObject)
            }catch (e: java.lang.Exception){
                e.printStackTrace()
            }
            return routes
        }

        /**
         * Method Name : onPostExecute
         * Used For : to mark the points of lat and long when user enter the text
         */
        override fun onPostExecute(result: List<List<HashMap<String, String>>>?) {
            val points  = ArrayList<LatLng?>()
            val lineOptions = PolylineOptions()
            for ( i in result!!.indices){
                val path = result[i]
                for ( j in path.indices ){
                    val point = path[j]
                    val lat = point["lat"]!!.toDouble()
                    val lng = point["lng"]!!.toDouble()
                    val position = LatLng(lat, lng)
                    points.add(position)
                }
                Log.i(ContentValues.TAG, "line: running$points")
                lineOptions.addAll(points)
                lineOptions.width(8f)
                lineOptions.color(Color.RED)
                lineOptions.geodesic(true)
            }

            if (points.size != 0) {
                Log.i(ContentValues.TAG, "onPostExecute: running line options")
                mMaps!!.addPolyline(lineOptions)
            }
        }

    }

    /**
     * Method Name : onMapReady
     * Used For : to resume map view and set location
     */
    override fun onMapReady(googleMap: GoogleMap?) {
        mapView.onResume()
        mMaps = googleMap

        askPermissionLocation()
        if ( ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMaps!!.isMyLocationEnabled = true

    }

    /**
     * Method Name : onSaveInstanceState
     * Used For : to save  instance using bundle
     */
    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        askPermissionLocation()
        var mapViewBundle = outState.getBundle(mapViewBundleKey)

        if (mapViewBundle == null){
            mapViewBundle = Bundle()
            outState.putBundle(mapViewBundleKey, mapViewBundle)
        }

        mapView.onSaveInstanceState(mapViewBundle)
    }

    /**
     * Method Name : askPermissionLocation
     * Used For : to ask device permission to access location
     */
    private fun askPermissionLocation(){
        if( ActivityCompat.checkSelfPermission(requireContext(),android.Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( requireContext(),android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf( android.Manifest.permission.ACCESS_FINE_LOCATION),1000)
            return
        }
        getCurrentLocation()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            1000 -> {
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    askPermissionLocation()
                }
            }
        }
    }

    /**
     * Method Name : getCurrentLocation
     * Used For : to get user location lat and long
     */
    private fun getCurrentLocation() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)
        try{
            @SuppressLint("MissingPermission") val location =
                fusedLocationProviderClient!! .getLastLocation()

            location.addOnCompleteListener(object : OnCompleteListener<Location> {
                override fun onComplete(p0: Task<Location>) {
                    if (p0.isSuccessful){
                        val currentLocation = p0.result as Location?

                        if (currentLocation != null){
                            moveCamera(
                                LatLng(currentLocation.latitude, currentLocation.longitude),
                                DefaultZoom
                            )
                            latitude = currentLocation.latitude
                            longitude = currentLocation.longitude
                        }
                    } else {
                        askPermissionLocation()
                    }
                }
            })
        } catch (se: Exception){
            Log.e(ContentValues.TAG, "getCurrentLocation: ", )
        }
    }

    /**
     * Method Name : moveCamera
     * Used For : to move camera position to lat lng
     */
    private fun moveCamera(latLng: LatLng, defaultZoom: Float) {
        mMaps?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, defaultZoom))
    }
}