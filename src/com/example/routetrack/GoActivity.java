package com.example.routetrack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.example.routetrack.AutoCompleteParser.AutoCompleteInfo;
import com.example.routetrack.AutoCompleteParser.PlaceAutoCompleteJSONParser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class GoActivity extends Activity{
	public List<AutoCompleteInfo> places = new ArrayList<AutoCompleteInfo>();
	public List<String> values = new ArrayList<String>();
	public ArrayAdapter<String> adapter;

	private String location;
	private String type;
	private String travelmethod;

	private AutoCompleteTextView atvPlaces;

	// AsyncTask for Google places autocomplete
	private PlacesTask placesTask;
	private ParserTask parserTask;

	private ToggleButton car; 
	private ToggleButton bus;
	private ToggleButton walk;
	private ToggleButton cycling;

	private static boolean autoOn = true;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_go);

		travelmethod = "&dirflg=d";

		Bundle i = getIntent().getExtras();
		if (i == null) {
			return;
		}
		location = i.getString("Mlocation");

		car = (ToggleButton) findViewById(R.id.toggleCar);
		car.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{         
				travelmethod = "&dirflg=d";
				// travelmethod = "&dirflg=h";	// avoid highway
				// travelmethod = "&dirflg=t";	// avoid tolls
				bus.setChecked(false);
				walk.setChecked(false);
				cycling.setChecked(false);

			}
		});
		bus = (ToggleButton) findViewById(R.id.toggleBus);
		bus.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{     
				travelmethod = "&dirflg=r";
				car.setChecked(false);
				walk.setChecked(false);
				cycling.setChecked(false);

			}
		});
		walk = (ToggleButton) findViewById(R.id.toggleWalk);
		walk.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{               
				travelmethod = "&dirflg=w";
				bus.setChecked(false);
				car.setChecked(false);
				cycling.setChecked(false);

			}
		});
		cycling = (ToggleButton) findViewById(R.id.toggleCycling);
		cycling.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{               
				travelmethod = "&dirflg=b";
				bus.setChecked(false);
				walk.setChecked(false);
				car.setChecked(false);

			}
		});

		atvPlaces = (AutoCompleteTextView) findViewById(R.id.atv_places);
		atvPlaces.setText("");
		atvPlaces.setThreshold(1);
		// Enable when want to have google places autocomplete on
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = preferences.edit();

		if (!preferences.contains("auto")) {
			editor.putBoolean("auto", true);
			editor.commit();
		}

		autoOn = preferences.getBoolean("auto", true);

		if(autoOn){
			autoSetting();
		}
	}

	public static boolean isautoOn() {
		return autoOn;
	}

	public void autoSetting(){

		atvPlaces.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(isNetworkAvailable() == false){
					Toast.makeText(getApplicationContext(), "No network connection.", Toast.LENGTH_LONG).show();
				}else{
					if(GoActivity.autoOn){
						if(atvPlaces.length() > 3){	// Only check iff text length longer than 3
							System.out.println("AutoComplete working");
							placesTask = new PlacesTask();
							placesTask.execute(s.toString());
						}
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}
		});
	}

	public void onStart(){
		super.onStart();

	}

	public void gosearch(View view){
		type =  atvPlaces.getText().toString();
		if(type.equals("")){
			Toast.makeText(getApplicationContext(), "No input value", Toast.LENGTH_LONG).show();
		}else{
			type = type.replaceAll(" ", "+");
			String uri = "https://maps.google.com/maps?saddr="+location+"&daddr="+type+"&avoid=highways"+travelmethod;
			System.out.println(uri);
			Intent i = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
			i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
			startActivity(i);
		}
	}

	/** A method to download json data from url */
	private String downloadUrl(String strUrl) throws IOException{
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try{
			URL url = new URL(strUrl);

			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to url
			urlConnection.connect();

			// Reading data from url
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

			StringBuffer sb = new StringBuffer();

			String line = "";
			while( ( line = br.readLine()) != null){
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
		return data;
	}

	// Fetches all places from GooglePlaces AutoComplete Web Service
	private class PlacesTask extends AsyncTask<String, Void, String>{
		@Override
		protected String doInBackground(String... place) {
			// For storing data from web service 
			String data = "";

			// Obtain browser key from https://code.google.com/apis/console
			String key = "key=AIzaSyD5YUfbkUbVzK_ZMu8b9sefSOCXpmN6APE";

			String input="";

			try {
				input = "input=" + URLEncoder.encode(place[0], "utf-8");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}

			// place type to be searched
			String types = "types=(regions)";

			// Sensor enabled
			String sensor = "sensor=false";

			// Building the parameters to the web service
			String parameters = input+"&"+types+"&"+sensor+"&"+key;

			// Output format
			String output = "json";

			// Building the url to the web service
			String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;

			System.out.println("autocomplete: "+ url);

			try{
				// Fetching the data from we service
				data = downloadUrl(url);
			}catch(Exception e){
				Log.d("Background Task",e.toString());
			}
			return data;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			// Creating ParserTask
			parserTask = new ParserTask();

			// Starting Parsing the JSON string returned by Web Service
			parserTask.execute(result);
		}
	}
	/** A class to parse the Google Places in JSON format */
	private class ParserTask extends AsyncTask<String, Integer, List<AutoCompleteInfo>>{

		JSONObject jObject;

		@Override
		protected List<AutoCompleteInfo> doInBackground(String... jsonData) {
			PlaceAutoCompleteJSONParser placeJsonParser = new PlaceAutoCompleteJSONParser();

			try{
				jObject = new JSONObject(jsonData[0]);

				// Getting the parsed data as a List construct
				places = placeJsonParser.parse(jObject);


			}catch(Exception e){
				Log.d("Exception",e.toString());
			}
			return places;
		}

		@Override
		protected void onPostExecute(List<AutoCompleteInfo> result) {
			// Creating a ArrayAdapter for the AutoCompleteTextView
			for (AutoCompleteInfo autocompleteInfo:result){
				values.add(autocompleteInfo.getPlace());
			}

			adapter = new ArrayAdapter<String>(GoActivity.this, android.R.layout.simple_list_item_1, values);
			//			System.out.println("description(GoActivity): "+ result);
			// Setting the adapter
			atvPlaces.setAdapter(adapter);

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.go, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if (autoOn) {
			menu.findItem(R.id.action_auto).setTitle(R.string.setting_auto_on);
		}
		else {
			menu.findItem(R.id.action_auto).setTitle(R.string.setting_auto_off);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_auto:
			if (autoOn) {
				Toast.makeText(getApplicationContext(), "AutoComplete OFF", Toast.LENGTH_LONG).show();
				autoOn = false;
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putBoolean("auto", false);
				editor.commit();
				return true;
			}
			else {
				Toast.makeText(getApplicationContext(), "AutoComplete ON", Toast.LENGTH_LONG).show();
				autoOn = true;
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putBoolean("auto", true);
				editor.commit();
				return true;
			}
		case R.id.action_exit:
			finish();
			System.exit(0);
			return true;
		}
		return false;
	}
	// Check if network available
	private boolean isNetworkAvailable() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
}
