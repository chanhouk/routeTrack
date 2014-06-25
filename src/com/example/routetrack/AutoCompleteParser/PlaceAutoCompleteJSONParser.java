package com.example.routetrack.AutoCompleteParser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlaceAutoCompleteJSONParser {
	
	public List<AutoCompleteInfo> parse(JSONObject jObject){
		List<AutoCompleteInfo> result = new ArrayList<AutoCompleteInfo>();
		AutoCompleteInfo ACInfo = null;
		JSONArray jPredictions = null;
		
		try {
			jPredictions = jObject.getJSONArray("predictions");
			for(int i=0;i<jPredictions.length();i++){
				ACInfo = new AutoCompleteInfo();
				
				String description = (String)((JSONObject)jPredictions.getJSONObject(i)).get("description");
				ACInfo.setPlace(description);
				
				result.add(ACInfo);
				System.out.println("description(Parser): "+result.get(i).getPlace());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}catch (Exception e){
		}
		return result;
	}
}