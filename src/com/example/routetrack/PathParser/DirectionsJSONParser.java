package com.example.routetrack.PathParser;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

/**
 * Buffer the entire stream from Google, scan through and return Lat, lng
 * @author chanhouk
 * Google map direction: "routes" : [..... "legs" : [....."steps" : [.....   .....]   .....].....]    All in array []
 * Within steps get polyline within polyline get points
 * Decode point to get latitude and longitude and store all in arraylist
 * Then add into arraylist of path and add into list of routes
 */
public class DirectionsJSONParser {
	List<TurningInfo> listOfTurningInfo = new ArrayList<TurningInfo>();
	ArrayList<String> listOfHtml = new ArrayList<String>(); 
	List<LatLng> poly = null;
	TurningInfo currentTL = null; 

	public List<TurningInfo> getTurningPoint(){
		return listOfTurningInfo;
	}

	public List<List<HashMap<String,String>>> parse(JSONObject jObject){
		List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>() ;
		ArrayList<String> numOfPolyline = new ArrayList<String>();
		JSONArray jRoutes = null;
		JSONArray jLegs = null;
		JSONArray jSteps = null;

		try {
			jRoutes = jObject.getJSONArray("routes");

			for(int i=0;i<jRoutes.length();i++){
				jLegs = ((JSONObject)jRoutes.get(i)).getJSONArray("legs");
				List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();

				// temp
				String html = "";

				for(int j=0;j<jLegs.length();j++){
					jSteps = ((JSONObject)jLegs.get(j)).getJSONArray("steps");

					for(int k=0;k<jSteps.length();k++){
						html = (String)((JSONObject)jSteps.getJSONObject(k)).get("html_instructions");
						URLDecoder.decode(html,"ISO-8859-1");
						listOfHtml.add(html);

						String polyline = "";						
						polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
						numOfPolyline.add(polyline);

						List<LatLng> list = decodePoly(polyline);

						for(int l=0;l<list.size();l++){
							HashMap<String, String> hm = new HashMap<String, String>();
							hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude));
							hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude));
							path.add(hm);
						}
						currentTL = new TurningInfo();
						currentTL.setInstructino(listOfHtml.get(k));
						currentTL.setLat(((LatLng)list.get(0)).latitude);
						currentTL.setLng(((LatLng)list.get(0)).longitude);
						listOfTurningInfo.add(currentTL);
					}
					routes.add(path);
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}catch (Exception e){
		}
		return routes;
	}

	private List<LatLng> decodePoly(String encoded) {

		List<LatLng> poly = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng((((double) lat / 1E5)),
					(((double) lng / 1E5)));
			poly.add(p);
		}
		return poly;
	}
}