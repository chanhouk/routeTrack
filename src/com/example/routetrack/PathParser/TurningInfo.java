package com.example.routetrack.PathParser;

public class TurningInfo {
	private String instruction;
	private double lat;
	private double lng;
	
	public TurningInfo(){
	}
	
	public String getInstruction(){
		return instruction;
	}
	public double getLat(){
		return lat;
	}
	public double getLng(){
		return lng;
	}
	public void setInstructino(String s){
		this.instruction=s;
	}
	public void setLat(double s){
		this.lat = s;
	}
	public void setLng(double s){
		this.lng = s;
	}

}
