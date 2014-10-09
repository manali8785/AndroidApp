package com.example.carparkinglocator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends FragmentActivity implements LocationListener,OnClickListener,OnMenuItemClickListener {

	GoogleMap googleMap;
	Button parkButton;
	Button unparkButton;
	Button findButton;
	ImageButton currentLocButton;
	TextView lottlong;
	TextView distTime;
	double currentLocLattitude;
	double currentLocLongitude;
	double[] parkLocation=new double[2];
	MarkerOptions currentLocMarker=null;
	MarkerOptions parkedCarmarker=null;
	Marker currMarker;
	Marker parkingMarker;
	Polyline polyline;
	String mode;
	
	protected void onCreate(Bundle savedInstanceState) {
		System.out.println("---------------- MainActivity.onCreate() -------------------");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		parkButton=(Button)findViewById(R.id.parkButton);
		unparkButton=(Button)findViewById(R.id.unparkButton);
		findButton=(Button)findViewById(R.id.findButton);
		currentLocButton=(ImageButton)findViewById(R.id.currentLocButton);
		parkButton.setOnClickListener(this);
		unparkButton.setOnClickListener(this);
		findButton.setOnClickListener(this);
		currentLocButton.setOnClickListener(this);
		getCurrentLocation();
	}
	public void getCurrentLocation(){
		if(!haveNetworkConnection()){
			System.out.println("******************* NOT CONNECTED TO INTERNET!!! **********************");
			Toast.makeText(this, "Not connected to internet!!!", Toast.LENGTH_LONG).show();
		}else{
			System.out.println("******************* CONNECTED TO INTERNET!!! **********************");
			int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
			if (status != ConnectionResult.SUCCESS) { 
				int requestCode = 10;
				Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this,requestCode);
				dialog.show();
			} else { 
				SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
				googleMap = fm.getMap();
				googleMap.setMyLocationEnabled(true);
		
				LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
				boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
					if (isGPSEnabled) {
						Location location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
						if (location != null) {
							onLocationChanged(location);
						}else{
							System.out.println("--------------Current Location is Null!-------------");
						}
						locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 20000, 0, this);
	
						
					} else {
						System.out.println("----------------------GPS is OFF!!!--------------------");
						Toast.makeText(this, "GPS is turned OFF!!!", Toast.LENGTH_LONG).show();
						Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivity(intent);
					}
				} 
		}	
	}

	private boolean haveNetworkConnection() {
	    boolean haveConnectedWifi = false;
	    boolean haveConnectedMobile = false;

	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
	    for (NetworkInfo ni : netInfo) {
	        if (ni.getTypeName().equalsIgnoreCase("WIFI"))
	            if (ni.isConnected())
	                haveConnectedWifi = true;
	        if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
	            if (ni.isConnected())
	                haveConnectedMobile = true;
	    }
	    return haveConnectedWifi || haveConnectedMobile;
	}


	@Override
	public void onLocationChanged(Location location) {
		System.out.println("MainActivity.onLocationChanged()");
		lottlong = (TextView) findViewById(R.id.location);
		distTime = (TextView) findViewById(R.id.disttime);
		
		currentLocLattitude = location.getLatitude();
		currentLocLongitude = location.getLongitude();

		System.out.println("Current Location Latitude: " + currentLocLattitude + "\nCurrent Location Longitude: " + currentLocLongitude);
		LatLng latLng = new LatLng(currentLocLattitude, currentLocLongitude);
		
		currentLocMarker = new MarkerOptions().position(new LatLng(currentLocLattitude, currentLocLongitude)).title("You are here!");
		if(currMarker!=null){
			currMarker.remove();
		}
		currMarker=googleMap.addMarker(currentLocMarker);
		googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
		googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
		lottlong.setText("Latitude:" + currentLocLattitude + ", Longitude:" + currentLocLongitude);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.parkButton){
			saveCarLocation();
		}else if (v.getId()==R.id.unparkButton) {
			unSaveCarLocation();
		}else if (v.getId()==R.id.findButton) {
			findCar(v);
		}else if (v.getId()==R.id.currentLocButton) {
			getCurrentLocation();
		}
		
	}
	
	//Saving park car location
	public void saveCarLocation(){
		System.out.println("MainActivity.saveCarLocation()");
		System.out.println("Current Location Latitude: " + currentLocLattitude + "\nCurrent Location Longitude: " + currentLocLongitude);
		parkLocation[0]=currentLocLattitude;
		parkLocation[1]=currentLocLongitude;
		parkedCarmarker = new MarkerOptions().position(new LatLng(parkLocation[0], parkLocation[1])).title("You parked car here!");
		parkedCarmarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.car));
		parkingMarker=googleMap.addMarker(parkedCarmarker);
		currMarker.setVisible(false);
		parkButton.setVisibility(View.INVISIBLE);
		unparkButton.setVisibility(View.VISIBLE);
		findButton.setVisibility(View.VISIBLE);
	
	}
	
	public void unSaveCarLocation(){
		Arrays.fill(parkLocation, 0);
		parkingMarker.remove();
		currMarker.setVisible(true);
		parkButton.setVisibility(View.VISIBLE);
		unparkButton.setVisibility(View.INVISIBLE);
		findButton.setVisibility(View.INVISIBLE);
		distTime.setVisibility(View.GONE);
		if(polyline!=null){
			polyline.remove();
		}
	}
	
	public void findCar(View v){
		System.out.println("MainActivity.findCar()");
		System.out.println("Current Location Lattitude: "+currentLocLattitude+"\nCurrent Location Lattitude"+currentLocLongitude);
		System.out.println("Parked Location Lattitude: "+parkLocation[0]+"\nParked Location Longitude: "+parkLocation[1]);
	    PopupMenu popup = new PopupMenu(this, v);  
	    MenuInflater inflater = popup.getMenuInflater();  
	    popup.setOnMenuItemClickListener((OnMenuItemClickListener) this);  
	    inflater.inflate(R.menu.main, popup.getMenu());  
	    popup.show();  
	
	}
	
	private String getMapsApiDirectionsUrl() {
		String waypoints = "waypoints=optimize:true|"+ currentLocLattitude + "," + currentLocLongitude + "|" + "|" + parkLocation[0] + ","+ parkLocation[1];
		String sensor = "sensor=false";
		mode="mode="+mode;
		String params = waypoints + "&" + sensor + "&" + mode;
		String output = "json";
		String url = "https://maps.googleapis.com/maps/api/directions/"+ output + "?" + params;
		System.out.println("URL: "+url);
		return url;
	}

	private class ReadTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... url) {
			System.out.println("PathGoogleMapActivity.ReadTask.doInBackground()");
			String data = "";
			try {
				HttpConnection http = new HttpConnection();
				data = http.readUrl(url[0]);
			} catch (Exception e) {
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		@Override
		protected void onPostExecute(String result) {
			System.out.println("PathGoogleMapActivity.ReadTask.onPostExecute()");
			super.onPostExecute(result);
			new ParserTask().execute(result);
		}
	}

	private class ParserTask extends
			AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

		@Override
		protected List<List<HashMap<String, String>>> doInBackground(
				String... jsonData) {
			System.out.println("PathGoogleMapActivity.ParserTask.doInBackground()");
			JSONObject jObject;
			List<List<HashMap<String, String>>> routes = null;

			try {
				jObject = new JSONObject(jsonData[0]);
				PathJSONParser parser = new PathJSONParser();
				routes = parser.parse(jObject);
				System.out.println("Routes:"+routes.size());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return routes;
		}

		@Override
		protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
			System.out.println("PathGoogleMapActivity.ParserTask.onPostExecute()");
			ArrayList<LatLng> points = null;
			PolylineOptions polyLineOptions = null;
			String distance = "";
	        String duration = "";
	            
			System.out.println("Routes:"+routes.size());
			// traversing through routes
			for (int i = 0; i < routes.size(); i++) {
				points = new ArrayList<LatLng>();
				polyLineOptions = new PolylineOptions();
				List<HashMap<String, String>> path = routes.get(i);

				for (int j = 0; j < path.size(); j++) {
					HashMap<String, String> point = path.get(j);
				   if(j==0){    // Get distance from the list
                        distance = (String)point.get("distance");
                        continue;
                    }else if(j==1){ // Get duration from the list
                        duration = (String)point.get("duration");
                        continue;
                    }
					double lat = Double.parseDouble(point.get("lat"));
					double lng = Double.parseDouble(point.get("lng"));
					LatLng position = new LatLng(lat, lng);
					points.add(position);
				}

				polyLineOptions.addAll(points);
				polyLineOptions.width(12);
				polyLineOptions.color(Color.BLUE);
				
			}
			
			if(polyline!=null){
				polyline.remove();
			}
			polyline=googleMap.addPolyline(polyLineOptions);
			System.out.println("Distance: "+distance+"\t Duration: "+duration);
			distTime.setVisibility(View.VISIBLE);
			distTime.setText("Distance:" + distance + ",Duration:" + duration);
		}

	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
	System.out.println("MainActivity.onMenuItemClick()");
		 switch (item.getItemId()) {  
		     case R.id.transit:  
		 //   	 	Toast.makeText(this,"TRANSIT MODE",Toast.LENGTH_SHORT).show(); 
		    	 	mode="transit";
		    	 	drawRoute();
		     return true;  
		     case R.id.driving:  
		 //        Toast.makeText(this,"DRIVING MODE",Toast.LENGTH_SHORT).show();  
		           mode="driving";
		           drawRoute();
		     return true;  
		     case R.id.bicycling:  
		 //        Toast.makeText(this,"BICYCLING MODE",Toast.LENGTH_SHORT).show();  
		           mode="bicycling";
		           drawRoute();
		     return true;  
		     default:   
		  //  	   Toast.makeText(this,"WALKING MODE",Toast.LENGTH_SHORT).show();
		    	   mode="walking";
		    	   drawRoute();
		     return true;  
		} 
	}
	
	public void drawRoute(){
		if(haveNetworkConnection()){
		    String url = getMapsApiDirectionsUrl();
		    ReadTask downloadTask = new ReadTask();
		    downloadTask.execute(url);
		}else{
			System.out.println("******************* NOT CONNECTED TO INTERNET!!! **********************");
			Toast.makeText(this, "Not connected to internet!!!", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onBackPressed() {
	    Log.d("CDA", "onBackPressed Called");
	    Intent intent = new Intent();
	    intent.setAction(Intent.ACTION_MAIN);
	    intent.addCategory(Intent.CATEGORY_HOME);
	    startActivity(intent);
	}
}
