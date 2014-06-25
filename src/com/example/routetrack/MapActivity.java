package com.example.routetrack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.routetrack.GPS.GPSTracker;
import com.example.routetrack.PathParser.DirectionsJSONParser;
import com.example.routetrack.PathParser.TurningInfo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapActivity extends FragmentActivity implements LocationListener {

	private GPSTracker gps;

	private LocationManager myLocationManager;

	private GoogleMap mMap;

	private LatLng currentLocation;
	private LatLng dest;

	private Location mylocation;
	private Location midPoint;
	private Location destlocation;

	private String url;

	private MarkerOptions options;

	private double currentLatitude;
	private double currentLongitude;

	public ProgressDialog dialog;

	private Vibrator v;

	private List<TurningInfo> turningPoints;
	private ArrayList<LatLng> markerPoints;
	private List<List<HashMap<String, String>>> routes;

	private static boolean path = false;

	private int signalVrbrateMeter = 20;	// 5 meter

	private int dot = 200;      // Length of a Morse Code "dot" in milliseconds
	private int dash = 500;     // Length of a Morse Code "dash" in milliseconds
	private long[] patternL = {0,dot,0,dash,0,dot,0,dot};
	private long[] patternR = {0,dot,0,dash,0,dot};
	private long[] patternE = {0,dot};
	private long[] patternS = {0,dot,0,dot,0,dot};
	private long[] patternW = {0,dot,0,dash,0,dash};
	private long[] patternN = {0,dash,0,dot};
	private long[] one = {0,dash};
	private long[] two = {0,dash,0,dash};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		// ---------- ---------- ---------- Create location(s) ---------- ---------- ----------
		mylocation = new Location("");
		destlocation = new Location("");
		midPoint = new Location("");
		// ---------- ---------- ---------- Initializing array List ---------- ---------- ----------
		markerPoints = new ArrayList<LatLng>();
		// ---------- ---------- ---------- Create location manager ---------- ---------- ----------
		myLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		myLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		if ( !myLocationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
			buildAlertMessageNoGps();
		}
		// ---------- ---------- ---------- GPS enabled ---------- ---------- ----------  
		gps = new GPSTracker(MapActivity.this);
		// This remove the location store first
		gps.stopUsingGPS();
		if(gps.canGetLocation()){
			currentLatitude = gps.getLatitude();
			currentLongitude = gps.getLongitude();
			mylocation.setLatitude(currentLatitude);
			mylocation.setLongitude(currentLongitude);
			// Just to check if location updata correctly, can be remove after
		}else{
			gps.showSettingsAlert();
		}
		// ---------- ---------- ---------- Google Maps ---------- ---------- ----------
		// Getting reference to SupportMapFragment of the activity_main
		FragmentManager fmanager = getSupportFragmentManager();
		Fragment fragment = fmanager.findFragmentById(R.id.mapFragment1);
		SupportMapFragment supportmapfragment = (SupportMapFragment)fragment;
		// Getting Map for the SupportMapFragment
		mMap = supportmapfragment.getMap();
		// Set map type to normal
		mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		// Enable redirect to current location, only if network is available
		mMap.setMyLocationEnabled(true);
		// Store my current location in LatLng
		currentLocation = new LatLng(currentLatitude,currentLongitude);
		// Move camera to point to my current location 
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,14));
		// Click on map to add point on map
		mMap.setOnMapClickListener(new OnMapClickListener() {
			@Override
			public void onMapClick(LatLng point) {
				if(markerPoints.size()>0){
					markerPoints.clear();
					mMap.clear();
					turningPoints.clear();
				}
				path = true;
				destlocation.setLatitude(point.latitude);
				destlocation.setLongitude(point.longitude);
				markerPoints.add(point);

				options = new MarkerOptions();

				options.position(point);

				if(markerPoints.size()==1){
					options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
				}

				// Add marker on map
				mMap.addMarker(options);

				if(markerPoints.size() >= 1){

					dest = markerPoints.get(0);

					// ----------- Begin Parser ----------
					url = getDirectionsUrl(dest);
					DownloadTask downloadTask = new DownloadTask();
					downloadTask.execute(url);
				}
			}

		});
		// ---------- ---------- ---------- Android system ---------- ---------- ----------
		v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_distance:
			distance();
			return true;
		case R.id.action_refresh:
			refreshMap();
			return true;
		case R.id.action_check:
			check();
			return true;
		case R.id.action_exit:
			gps.stopUsingGPS();
			finish();
			System.exit(0);
			return true;
		}
		return false;
	}

	
	@Override
	public void onLocationChanged(Location location) {
		currentLatitude = gps.getLatitude();
		currentLongitude = gps.getLongitude();
		if(path && turningPoints.size() > 0){
			mylocation.setLatitude(currentLatitude);
			mylocation.setLongitude(currentLongitude);

			midPoint.setLatitude(turningPoints.get(0).getLat());
			midPoint.setLongitude(turningPoints.get(0).getLng());

			float StM = mylocation.distanceTo(midPoint);
			
			Toast.makeText(getApplicationContext(), "Direct distance to dest: " + StM + "m", Toast.LENGTH_LONG).show();
			
			if(StM < signalVrbrateMeter){
				System.out.println("distance is less than 20m");
				if(turningPoints.get(0).getInstruction().contains("left")){
					// 0 for repeat vibrate, -1 for vivrate once
					System.out.println("vibrate left");
					Toast.makeText(getApplicationContext(), "Vibrate left", Toast.LENGTH_LONG).show();
					v.vibrate(one,-1);
					turningPoints.remove(0);
					return;
				}
				else if(turningPoints.get(0).getInstruction().contains("right")){
					System.out.println("vibrate right");
					Toast.makeText(getApplicationContext(), "Vibrate right", Toast.LENGTH_LONG).show();
					v.vibrate(two,-1);
					turningPoints.remove(0);
					return;
				}
//				else if(turningPoints.get(0).getInstruction().contains("east")){
//					v.vibrate(patternE,-1);
//					turningPoints.remove(0);
//					return;
//				}
//				else if(turningPoints.get(0).getInstruction().contains("south")){
//					v.vibrate(patternS,-1);
//					turningPoints.remove(0);
//					return;
//				}
//				else if(turningPoints.get(0).getInstruction().contains("west")){
//					v.vibrate(patternW,-1);
//					turningPoints.remove(0);
//					return;
//				}
//				else if(turningPoints.get(0).getInstruction().contains("north")){
//					v.vibrate(patternN,-1);
//					turningPoints.remove(0);
//					return;
//				}
				else{
					turningPoints.remove(0);
					return;
				}
			}
			else
			{
				return;
			}
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
	}


	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
	}


	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}


	private void distance(){
		// This show distance 
		if(path){
			float dist = mylocation.distanceTo(destlocation);
			Toast.makeText(MapActivity.this, "Path true", Toast.LENGTH_SHORT).show();
			Toast.makeText(getApplicationContext(), "Direct distance to dest: " + dist + "m", Toast.LENGTH_LONG).show();
		}else{
			Toast.makeText(MapActivity.this, "Path false", Toast.LENGTH_SHORT).show();
			Toast.makeText(getApplicationContext(), "No destination", Toast.LENGTH_LONG).show();
		}
	}

	private void refreshMap(){
		if(path){
			dialog = new ProgressDialog(MapActivity.this);
			dialog.setMessage("Loading google places api...");
			dialog.show();

			turningPoints.clear();
			mMap.clear();
			mMap.addMarker(options);
			url = getDirectionsUrl(dest);
			DownloadTask downloadTask = new DownloadTask();
			downloadTask.execute(url);
			dialog.dismiss();
		}else{
			Toast.makeText(getApplicationContext(), "No destination", Toast.LENGTH_LONG).show();
		}
	}


	private void check(){
		Toast.makeText(getApplicationContext(), "Path: "+ path, Toast.LENGTH_LONG).show();
		if(path == true && turningPoints.size()>0){
			System.out.println("turingPoints size: "+turningPoints.size());
		}
	}


	/**
	 * If device detect GPS not enable, it will display this Alert Dialog. 
	 * And pick to jump to setting page
	 */
	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
		.setCancelable(false)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				dialog.cancel();
			}
		});
		final AlertDialog alert = builder.create();
		alert.show();
	}


	/**
	 * Pass current LatLng location and destination location to create Google direction api uri
	 * @param dest
	 * @return Url in json
	 */
	private String getDirectionsUrl(LatLng dest){

		// Origin of route (current location)
		String str_origin = "origin="+currentLatitude+","+currentLongitude;

		// Destination of route
		String str_dest = "destination="+dest.latitude+","+dest.longitude;

		// Sensor enabled
		String sensor = "sensor=true";

		// Building the parameters to the web service
		String parameters = str_origin+"&"+str_dest+"&"+sensor;

		// Output format
		String output = "json";

		String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters+"&avoid=highways&mode=bicycling";

		return url;
	}


	/**
	 * Fetches data from url passed always in AsyncTask Call downloadUrl to cache the entire Json as string
	 * Then call ParseTask to parser only the routes latitude and longitude
	 * @author takeshfanc
	 *
	 */
	private class DownloadTask extends AsyncTask<String, Void, String>{
		// Downloading data in non-ui thread
		@Override
		protected String doInBackground(String... url) {	// String... url is Array of String
			// For storing data from web service
			String data = "";

			try{
				// Fetching the data from web service
				data = downloadUrl(url[0]);
			}catch(Exception e){
				Log.d("Background Task",e.toString());
			}
			return data;
		}

		// Executes in UI thread, after the execution of
		// doInBackground()
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			ParserTask parserTask = new ParserTask();

			// Invokes the thread for parsing the JSON data
			parserTask.execute(result);
		}
	}


	private String downloadUrl(String strUrl) throws IOException{
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try{
			URL url = new URL(strUrl);

			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to url
			urlConnection.connect();

			// Reading data from url
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

			StringBuffer sb  = new StringBuffer();

			String line = "";
			while( ( line = br.readLine())  != null){
				sb.append(line);
			}

			data = sb.toString();

			br.close();

		}catch(Exception e){
			Log.d("Exception while downloading url", e.toString());
		}finally{
			iStream.close();
			urlConnection.disconnect();
		}
		// data is the cached json file
		return data;
	}


	private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>> {

		@Override
		protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
			JSONObject jObject;
			//List<List<HashMap<String, String>>> 
			routes = null;

			try{
				jObject = new JSONObject(jsonData[0]);
				DirectionsJSONParser parser = new DirectionsJSONParser();
				// Starts parsing data
				routes = parser.parse(jObject);
				turningPoints = parser.getTurningPoint(); 
				for(int i=0; i<turningPoints.size();i++){
					System.out.println("turningPoints: "+ turningPoints.get(i).getInstruction()+","+turningPoints.get(i).getLat()+","+turningPoints.get(i).getLng());
				}
			}catch(Exception e){
				e.printStackTrace();
			}

			return routes;
		}

		@Override
		protected void onPostExecute(List<List<HashMap<String, String>>> result) {
			ArrayList<LatLng> points = null;
			PolylineOptions lineOptions = null;
			//			MarkerOptions markerOptions = new MarkerOptions();

			// Traversing through all the routes
			for(int i=0;i<result.size();i++){
				points = new ArrayList<LatLng>();
				lineOptions = new PolylineOptions();

				// Fetching i-th route
				List<HashMap<String, String>> path = result.get(i);

				// Fetching all the points in i-th route
				for(int j=0;j<path.size();j++){
					HashMap<String,String> point = path.get(j);

					double lat = Double.parseDouble(point.get("lat"));
					double lng = Double.parseDouble(point.get("lng"));
					LatLng position = new LatLng(lat, lng);

					points.add(position);
				}

				// Adding all the points in the route to LineOptions
				lineOptions.addAll(points);
				lineOptions.width(5);
				lineOptions.color(Color.RED);
			}
			// Drawing polyline in the Google Map for the i-th route
			mMap.addPolyline(lineOptions);
		}
	}
}
