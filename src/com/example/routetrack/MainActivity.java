package com.example.routetrack;

import com.example.routetrack.R;
import com.example.routetrack.GPS.GPSTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {

	private GPSTracker gps;

	private String myLatitude;
	private String myLongitude;
	private String location;
	
	private static boolean checkedConnection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);	
	}

	@Override
	public void onStart(){
		super.onStart();
		connectionDialogue();

		startGPSTrack();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		startGPSTrack();
	}
	
	private void startGPSTrack(){
		gps = new GPSTracker(MainActivity.this);
		gps.stopUsingGPS();
		if(gps.canGetLocation()){
			myLatitude = Double.toString(gps.getLatitude());
			myLongitude = Double.toString(gps.getLongitude());
			location = myLatitude +","+ myLongitude;
			System.out.println("my location: " + location);

		}else{
			gps.showSettingsAlert();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_findShop:
			changeToFind();
			return true;
		case R.id.action_map:
			changeToMap();
			return true;
		case  R.id.action_exit:
			gps.stopUsingGPS();
			finish();
			System.exit(0);
			return true;
		}
		return false;
	}

	public void go(View view){
		changeToGo();
	}

	public void find(View view){
		changeToFind();
	}

	public void shownearby(View view){
		changeToShowNearby();
	}
	
	public void mapbutton(View view){
		changeToMap();
	}

	public void changeToGo(){		
		// Switch to GoActivity
		Intent i = new Intent(MainActivity.this,GoActivity.class);
		i.putExtra("Mlocation", location);
//		gps.stopUsingGPS();	//	This switch off GPS, so do not use too much battery power
		startActivity(i);
	}

	public void changeToFind(){
		// Switch to ShopLocationActivity
		Intent i = new Intent(MainActivity.this,ShopsLocationActivity.class);
		i.putExtra("Mlocation", location);
//		gps.stopUsingGPS();	//	This switch off GPS, so do not use too much battery power
		startActivity(i);
	}

	public void changeToMap(){
		// Switch to MapActivity
		Intent i = new Intent(MainActivity.this,MapActivity.class);
		i.putExtra("Mlocation", location);
//		gps.stopUsingGPS();	//	This switch off GPS, so do not use too much battery power
		startActivity(i);
	}

	public void changeToShowNearby(){
		// Switch to ShowNearbyActivity
		Intent i = new Intent(MainActivity.this,ShowNearbyActivity.class);
		i.putExtra("Mlocation", location);
//		gps.stopUsingGPS();	//	This switch off GPS, so do not use too much battery power
		startActivity(i);
	}
	
	public boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	/**
	 * Display alert message if not online
	 */ 
	private void connectionDialogue(){
		if (!checkedConnection && !isOnline()){
			checkedConnection = true;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Change network settings now?").setTitle("No Connection.");
			builder.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// show network settings
					Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
					startActivity(intent);
				}
			});
			builder.setNegativeButton("No",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// dismiss
				}
			});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}
}
