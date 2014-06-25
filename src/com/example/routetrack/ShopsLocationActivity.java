package com.example.routetrack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class ShopsLocationActivity extends Activity{

	private EditText option;

	private Spinner placesSpinner;
	private Spinner radiusSpinner;

	private String optionType;
	private String searchRadius;
	private boolean ranked;

	private String location;
	private String types;
	private String type;
	private String search_radius;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_findplaces);
		
		Bundle i = getIntent().getExtras();
		if (i == null) {
			return;
		}
		location = i.getString("Mlocation");

		setSpinner();

		option = (EditText) findViewById(R.id.searchOption);
		option.setText("");
	}

	public void onStart(){
		super.onStart();
		// When App first start from home
	}
	
	public void onResume(){
		super.onResume();
		// When App resume from home, when not exit
	}
	
	public void findplacesearch(View view){
		String temp = option.getText().toString();
		if(temp.equals("")){
			optionType = type;
		}else{
			temp = temp.replaceAll(" ", "+");
			optionType = temp;
			option.setText("");
		}			
		
		Intent i = new Intent(ShopsLocationActivity.this,PlacesActivity.class);
		i.putExtra("Mlocation", location);
		i.putExtra("type", optionType);
		i.putExtra("radius", searchRadius);
		i.putExtra("ranked", ranked);
		startActivity(i);
	}

	public class CategoriesSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			types = parent.getItemAtPosition(pos).toString();

			if (types.equalsIgnoreCase("Cafe")){
				type = "cafe";
			}else if (types.equalsIgnoreCase("Atm")){
				type = "atm";
			}else if (types.equalsIgnoreCase("Pub")){
				type = "pub";
			}else if (types.equalsIgnoreCase("Gym")){
				type = "gym";
			}else if (types.equalsIgnoreCase("Supermarket")){
				type = "supermarket";
			}else if (types.equalsIgnoreCase("Hotel")){
				type = "hotel";
			}else if (types.equalsIgnoreCase("Motel")){
				type = "motel";
			}
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	public class Search_searchRadiusSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			search_radius = parent.getItemAtPosition(pos).toString();
			if (search_radius.equalsIgnoreCase("50m")){
				searchRadius = "50";
				ranked = false;
			}else if (search_radius.equalsIgnoreCase("100m")){
				searchRadius = "100";
				ranked = false;
			}else if (search_radius.equalsIgnoreCase("500m")){
				searchRadius = "500";
				ranked = false;
			}else if (search_radius.equalsIgnoreCase("1000m")){
				searchRadius = "1000";
				ranked = false;
			}else if (search_radius.equalsIgnoreCase("2000m")){
				searchRadius = "2000";
				ranked = false;
			}else{
				ranked = true;
			}
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	public void setSpinner(){
		// Set placesSpinner to R.id.placesSpinner
		placesSpinner = (Spinner)findViewById(R.id.placesSpinner);
		placesSpinner.setOnItemSelectedListener(new CategoriesSelectedListener());
		ArrayAdapter<CharSequence>adapter1 = ArrayAdapter.createFromResource(this, R.array.types, android.R.layout.simple_spinner_item);
		adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		placesSpinner.setAdapter(adapter1);

		// Set radiusSpinner to R.id.radiusSpinner
		radiusSpinner = (Spinner)findViewById(R.id.radiusSpinner);
		radiusSpinner.setOnItemSelectedListener(new Search_searchRadiusSelectedListener());
		ArrayAdapter<CharSequence>adapter2 = ArrayAdapter.createFromResource(this, R.array.search_radius, android.R.layout.simple_spinner_item);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		radiusSpinner.setAdapter(adapter2);
	}
}
