package com.example.map;

import java.util.List;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MapTracker extends Activity implements LocationListener {

	// Google Map
	private GoogleMap googleMap;
	// // The minimum distance to change Updates in meters
	// private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10
	// meters
	//
	// // The minimum time between updates in milliseconds
	// private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1
	// minute

	TextView distanceTextView;

	protected LocationManager locationManager;
	protected LocationListener locationListener;
	protected Context context;

	Ringtone r;
	Vibrator v;
	int minDistane = 100;

	double lastDistance;
	double currentDistance;

	double latitude = 31.2521928;
	double longitude = 29.9777169;
	double lastLatitude;
	double lastLongitude;
	float results[] = new float[1];

	LatLng startPoint;
	LatLng currentPoint;
	// double latitude = 17.385044;
	// double longitude = 78.486671;
	MarkerOptions marker;
	CameraPosition cameraPosition;
	Marker m;
	Polyline line;
	List<LatLng> points;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_tracker);

		distanceTextView = (TextView) findViewById(R.id.distanceTextView);

		try {
			// Loading map
			initilizeMap();

			// Changing map type
			googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

			// Showing / hiding your current location
			googleMap.setMyLocationEnabled(true);

			// Enable / Disable zooming controls
			googleMap.getUiSettings().setZoomControlsEnabled(true);

			// Enable / Disable my location button
			googleMap.getUiSettings().setMyLocationButtonEnabled(true);

			// Enable / Disable Compass icon
			googleMap.getUiSettings().setCompassEnabled(true);

			// Enable / Disable Rotate gesture
			googleMap.getUiSettings().setRotateGesturesEnabled(true);

			// Enable / Disable zooming functionality
			googleMap.getUiSettings().setZoomGesturesEnabled(true);

			// Looper.prepare();

			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

			Criteria criteria = new Criteria();
			// criteria.setAccuracy(Criteria.ACCURACY_FINE);
			// criteria.setAccuracy(Criteria.ACCURACY_HIGH);
			criteria.setAccuracy(Criteria.POWER_LOW);
			String bestProvider = locationManager.getBestProvider(criteria,
					false);

			locationManager
					.requestLocationUpdates(bestProvider, 5000, 10, this);
			currentDistance = 0;
			lastDistance = 0;
			distanceTextView.setText(String.format("Distance: %d M",
					(int) currentDistance));

			Uri notification = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			r = RingtoneManager.getRingtone(getApplicationContext(),
					notification);
			v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

			Log.e("Location Changed---", latitude + ", " + longitude);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		initilizeMap();
	}

	/**
	 * function to load map If map is not created it will create it for you
	 * */
	private void initilizeMap() {
		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();

			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	boolean start = true;

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		// get Coordinates
		latitude = location.getLatitude();
		longitude = location.getLongitude();

		if (start) { // Start Point

			startPoint = new LatLng(latitude, longitude);

			MarkerOptions startMarker = new MarkerOptions()
					.position(startPoint)
					.title("Starting Location")
					.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_RED));

			googleMap.addMarker(startMarker);

			marker = new MarkerOptions()
					.position(startPoint)
					.title("Current Location")
					.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

			m = googleMap.addMarker(marker);

			// googleMap.addMarker(marker);

			line = googleMap.addPolyline(new PolylineOptions()
					.add(startPoint, startPoint).width(5).color(Color.RED));

			points = line.getPoints();

			cameraPosition = new CameraPosition.Builder()
					.target(new LatLng(latitude, longitude)).zoom(19).build();

			googleMap.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));

			lastLatitude = latitude;
			lastLongitude = longitude;

			start = false;
		} else {

			currentPoint = new LatLng(latitude, longitude);
			Location.distanceBetween(lastLatitude, lastLongitude, latitude,
					longitude, results);
			currentDistance += results[0];
			if (currentDistance - lastDistance >= minDistane) {
				lastDistance = currentDistance - (currentDistance % minDistane);
				Log.e("Distance", (currentDistance - lastDistance) + "");
				v.vibrate(500);
				r.play();
			}
			if (currentDistance >= 1000) {
				distanceTextView.setText(String.format("Distance: %.2f Km",
						currentDistance / 1000));
			} else {
				distanceTextView.setText(String.format("Distance: %d M",
						(int) currentDistance));
			}

			lastLatitude = latitude;
			lastLongitude = longitude;

			m.setPosition(currentPoint);

			points.add(new LatLng(latitude, longitude));

			line.setPoints(points);

			cameraPosition = new CameraPosition.Builder()
					.target(new LatLng(latitude, longitude)).zoom(19).build();
			googleMap.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));

			Log.e("Location Changed", latitude + ", " + longitude);
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		Log.e("Status Changed", latitude + ", " + longitude);

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		Log.e("Provider Enabled", latitude + ", " + longitude);

	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.e("Provider Disabled", latitude + ", " + longitude);
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map_tracker, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
