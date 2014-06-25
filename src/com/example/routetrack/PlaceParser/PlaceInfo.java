package com.example.routetrack.PlaceParser;

public class PlaceInfo {

	private String name;
	private String lat;
	private String lng;
	private String ref;
	private String add;

	public PlaceInfo (){
	}

	public String getName(){
		return name;
	}

	public String getLat(){
		return lat;
	}

	public String getLng(){
		return lng;
	}

	public String getRef(){
		return ref;
	}
	public String getAdd(){
		return add;
	}

	public void setName(String s){
		this.name=s;
	}

	public void setLat(String string){
		this.lat=string;
	}

	public void setLng(String d){
		this.lng=d;
	}

	public void setRef(String s){
		this.ref=s;
	}

	public void setAdd(String s){
		this.add=s;
	}

}
