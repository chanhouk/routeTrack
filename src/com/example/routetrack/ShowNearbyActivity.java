package com.example.routetrack;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class ShowNearbyActivity extends Activity{

	private EditText option;

	private String uri;
	private String location;
	private String type;

	final Context context = this;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shownearby);

		Bundle i = getIntent().getExtras();
		if (i == null) {
			return;
		}
		location = i.getString("Mlocation");

		option = (EditText) findViewById(R.id.search_value);
		option.setText("");

		option.setOnKeyListener(new OnKeyListener()
		{
			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				// If the event is a key-down event on the "enter" button
				if ( (arg2.getAction() == KeyEvent.ACTION_DOWN) &&
						(arg1 == KeyEvent.KEYCODE_ENTER))
				{               
					InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(option.getWindowToken(), 0);   
					return true;
				}
				return false;
			}
		} );
	}

	public void cafe(View view) {
		type = "cafe"; // Temp

		uri = "https://maps.google.com/maps?q="+type+"+near+"+location+"&hl=en";
		Intent i = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
		i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
		startActivity(i);
	}

	public void pub(View view) {
		type = "pub"; // Temp

		uri = "https://maps.google.com/maps?q="+type+"+near+"+location+"&hl=en";
		Intent i = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
		i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
		startActivity(i);
	}

	public void hotel(View view) {
		type = "hotel"; // Temp

		uri = "https://maps.google.com/maps?q="+type+"+near+"+location+"&hl=en";
		Intent i = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
		i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
		startActivity(i);
	}

	public void motel(View view) {
		type = "motel"; // Temp

		uri = "https://maps.google.com/maps?q="+type+"+near+"+location+"&hl=en";
		Intent i = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
		i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
		startActivity(i);
	}

	public void gym(View view) {
		type = "gym"; // Temp

		uri = "https://maps.google.com/maps?q="+type+"+near+"+location+"&hl=en";
		Intent i = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
		i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
		startActivity(i);
	}

	public void nearbysearch(View view){
		// Still need to work on
		type =  option.getText().toString();
		if(type.equals("")){
			Toast.makeText(getApplicationContext(), "No input value", Toast.LENGTH_LONG).show();
		}else{
			option.setText("");
			type = type.replaceAll(" ", "+");
			uri = "https://maps.google.com/maps?q="+type+"+near+"+location+"&hl=en";
			Intent i = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
			i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
			startActivity(i);
		}
	}
}
