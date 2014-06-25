package com.example.routetrack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
//import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import com.example.routetrack.R;
//import com.example.routetrack.GPS.GPSTracker;
//import com.example.routetrack.PathParser.DirectionsJSONParser;
//import com.example.routetrack.PlaceParser.FindPlaceParser;
import com.example.routetrack.PlaceParser.PlaceInfo;
import com.example.routetrack.PlaceParser.PlaceJsonParser;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.maps.model.PolylineOptions;
//import com.example.routetrack.R.layout;

//import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
//import android.content.DialogInterface;
import android.content.Intent;
//import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
//import android.util.Xml;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class PlacesActivity extends ListActivity{
	public List<PlaceInfo> result = new ArrayList<PlaceInfo>();
	public List<String> values = new ArrayList<String>();
	public ArrayAdapter<String> adapter;
	public ProgressDialog dialog;
	
	private String url_1 = "https://maps.googleapis.com/maps/api/place/search/json?location=";
	private String url_2 = "&sensor=true&key=AIzaSyD5YUfbkUbVzK_ZMu8b9sefSOCXpmN6APE";
	private String url_Full;
	private String rad;

	private String location;
	private String dest;

	private String url;

	private String types;
	private String radius;
	private boolean ranked;
	private PlaceInfo selectedp;
	
	public boolean switchOn = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);

		Bundle i = getIntent().getExtras();
		if (i == null) {
			return;
		}
		location = i.getString("Mlocation");
		types = i.getString("type");
		radius = i.getString("radius");
		ranked = i.getBoolean("ranked");
	}

	public void onStart(){
		super.onStart();
		values.clear();
		result.clear();
		dialog = new ProgressDialog(PlacesActivity.this);
        dialog.setMessage("Loading google places api...");
        dialog.show();
        
		url = getDirectionsUrl();
		DownloadTask DLT = new DownloadTask();
		DLT.execute(url);

		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, values);
		setListAdapter(adapter);
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		String item = (String) getListAdapter().getItem(position);
		for(PlaceInfo p:result){
			if (p.getName().equals(item)){
				selectedp=p;
				break;
			}			
		} 			
		dest = selectedp.getLat()+","+selectedp.getLng();
		String uri = "https://maps.google.com/maps?saddr="+location+"&daddr="+dest+"&avoid=highways&mode=bicycling";
		Intent i = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
		i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
		startActivity(i);
		finish();
	}

	private String getDirectionsUrl(){
		String category = "&keyword="+types;
		if (ranked){
			rad="&rankby=distance";
			url_Full=url_1+location+rad+category+url_2;
		}
		else {
			rad = "&radius="+radius;
			url_Full=url_1+location+rad+category+url_2;
		}
		return url_Full;
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

	private class DownloadTask extends AsyncTask<String, Void, String>{
		// Downloading data in non-ui thread
		@Override
		protected String doInBackground(String... url) {	// String... Array of String
//			System.out.println("In DownloadTask doInBackground begin:");
			// For storing data from web service
			String data = "";

			try{
				// Fetching the data from web service
//				System.out.println("Being try download data from url");
				data = downloadUrl(url[0]);
//				System.out.println("End download data from utl");
			}catch(Exception e){
				Log.d("Background Task",e.toString());
			}
//			System.out.println("Data after downloadUrl in DownloadTask background: ");
//			System.out.println("(In String): "+data);
			return data;
		}

		// Executes in UI thread, after the execution of
		// doInBackground()
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
//			System.out.println("DownloadTask execution begin");
			ParserTask parserTask = new ParserTask();

			// Invokes the thread for parsing the JSON data
			parserTask.execute(result);
//			System.out.println("DownloadTask execution finish");
		}
	}
	
	private class ParserTask extends AsyncTask<String, Integer, List<PlaceInfo>> {
		
		JSONObject jObject;
		
		@Override
		protected List<PlaceInfo> doInBackground(String... jsonData) {
			try{
				jObject = new JSONObject(jsonData[0]);
				PlaceJsonParser PJP = new PlaceJsonParser();
				result = PJP.parse(jObject);
			}catch(Exception e){
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(List<PlaceInfo> result) {
			super.onPostExecute(result);
			for (PlaceInfo placeInfo:result){
				values.add(placeInfo.getName());
			}
			setListAdapter(adapter);
			dialog.dismiss();
//			System.out.println("values: " +values);
			if(values == null){
				Toast.makeText(getApplicationContext(), "No input value", Toast.LENGTH_LONG).show();
			}
		}
	}
}