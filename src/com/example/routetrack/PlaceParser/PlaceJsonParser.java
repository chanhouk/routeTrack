package com.example.routetrack.PlaceParser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlaceJsonParser {
	public List<PlaceInfo> parse(JSONObject jObject){

		List<PlaceInfo> result = new ArrayList<PlaceInfo>();
		PlaceInfo currentPL = null;
		JSONArray jResults = null;
		JSONObject jGeometry = null;
		JSONObject jLocation = null;

		try {
			jResults = jObject.getJSONArray("results");
			for(int i=0;i<jResults.length();i++){
				currentPL = new PlaceInfo();
				
				String name = (String)((JSONObject)jResults.getJSONObject(i)).get("name");
				currentPL.setName(name);
				
				String vicinity = (String)((JSONObject)jResults.getJSONObject(i)).get("vicinity");
				currentPL.setAdd(vicinity);
				
				jGeometry = jResults.getJSONObject(i).getJSONObject("geometry");
				jLocation = jGeometry.getJSONObject("location");
//				double lat = jLocation.getDouble("lat");
				String latS = Double.toString(jLocation.getDouble("lat"));
				currentPL.setLat(latS);
//				double lng = jLocation.getDouble("lng");
				String lngS = Double.toString(jLocation.getDouble("lng"));
				currentPL.setLng(lngS);
				
				result.add(currentPL);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}catch (Exception e){
		}
		return result;
	}
}