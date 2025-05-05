package com.example.tmclone

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.util.Log

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.tmclone.databinding.ActivityMapsBinding
import kotlin.toString

private const val TAG = "MapsActivity"
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

	private lateinit var mMap: GoogleMap
	private lateinit var binding: ActivityMapsBinding

	var latitude = ""
	var longitude = ""
	var city = ""
	// provides a way to convert a physical address into geographic coordinates (latitude and longitude)
	private lateinit var geocoder: Geocoder

	// an arbitrary number request code to be used when requesting permission to access the device's location.
	private val ACCESS_LOCATION_CODE = 123
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityMapsBinding.inflate(layoutInflater)
		setContentView(binding.root)

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		val mapFragment = supportFragmentManager
			.findFragmentById(R.id.map) as SupportMapFragment
		mapFragment.getMapAsync(this)


		//show location
		longitude = intent.getStringExtra("longitude").toString()
		latitude = intent.getStringExtra("latitude").toString()
		city = intent.getStringExtra("city").toString()


		findViewById<ImageButton>(R.id.back_imageButton).setOnClickListener {
			val intent = Intent(this, MainActivity::class.java)
			startActivity(intent)
		}


	}

	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera. In this case,
	 * we just add a marker near Sydney, Australia.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	override fun onMapReady(googleMap: GoogleMap) {
		mMap = googleMap ?: return

		/*// Add a marker in Sydney and move the camera
		val sydney = LatLng(-34.0, 151.0)
		mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
		mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/

		mMap.uiSettings.isZoomControlsEnabled = true

		getLocationPermission()

		if(longitude.isNotEmpty()
			&& latitude.isNotEmpty()
			&& city.isNotEmpty()){
			showVenueLocation(longitude, latitude, city)
		}
	}

	private fun getLocationPermission(){
		if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
			== PackageManager.PERMISSION_GRANTED){
			enableUserLocation() //permission granted
		}
		else{
			//permission denied
			if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
				ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ACCESS_LOCATION_CODE)
			}
			else{
				ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ACCESS_LOCATION_CODE)
			}
		}
	}

	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<out String>,
		grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		when (requestCode) {
			ACCESS_LOCATION_CODE -> {
				// If permission is granted, enable user location
				enableUserLocation()
			}
		}
	}

	@SuppressLint("MissingPermission")
	private fun enableUserLocation() {
		// The My Location button appears in the top right corner of the screen only
		// when the My Location layer is enabled.
		mMap.isMyLocationEnabled = true
	}

	fun showVenueLocation(longitude: String, latitude: String, city: String){
		val longitude = longitude.toDouble()
		val latitude = latitude.toDouble()

		try{
			val latLng = LatLng(latitude.toDouble(), longitude.toDouble())
			//marker option
			val markerOption = MarkerOptions()
				.position(latLng)
				.title(city)

			mMap.addMarker(markerOption)
			val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
			mMap.animateCamera(cameraUpdate)
		} catch(e: Exception){
			Log.e(TAG, "${e.message}", )
		}
	}
}