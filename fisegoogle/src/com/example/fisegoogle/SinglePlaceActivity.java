package com.example.fisegoogle;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SinglePlaceActivity extends Activity {
	// flag for Internet connection status
	Boolean isInternetPresent = false;

	// Connection detector class
	ConnectionDetector cd;
	
	// Alert Dialog Manager
	AlertDialogManager alert = new AlertDialogManager();

	// Google Places
	GooglePlaces googlePlaces;
	
	// Place Details
	PlaceDetails placeDetails;
	
	// Progress dialog
	ProgressDialog pDialog;
	
	Button btnShowOnMap;
	
	PlacesList nearPlaces;
	GPSTracker gps;
	
	PlaceDetails nearPlace;
	
	// KEY Strings
	public static String KEY_REFERENCE = "reference"; // id of the place

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.single_place);

		
		Intent i = getIntent();
		
		// Place referece id
		String reference = i.getStringExtra(KEY_REFERENCE);
		
		// Calling a Async Background thread
		new LoadSinglePlaceDetails().execute(reference);
		
		gps = new GPSTracker(this);

		// check if GPS location can get
		if (gps.canGetLocation()) {
			Log.d("Your Location", "latitude:" + gps.getLatitude() + ", longitude: " + gps.getLongitude());
		} else {
			// Can't get user's current location
			alert.showAlertDialog(SinglePlaceActivity.this, "GPS Status",
					"Couldn't get location information. Please enable GPS",
					false);
			// stop executing code by return
			return;
		}
		
		btnShowOnMap = (Button) findViewById(R.id.btn_show_map);
		
		
		btnShowOnMap.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(),
						MapActivity.class);
				
				// Sending user current geo location
				//i.putExtra("user_latitude", Double.toString(gps.getLatitude()));
				//i.putExtra("user_longitude", Double.toString(gps.getLongitude()));

				i.putExtra("near_place_lat", Double.toString(placeDetails.result.geometry.location.lat));
				i.putExtra("near_place_long", Double.toString(placeDetails.result.geometry.location.lng));
				// staring activity
				startActivity(i);
			}
		});
	}
	
	
	/**
	 * Background Async Task to Load Google places
	 * */
	class LoadSinglePlaceDetails extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(SinglePlaceActivity.this);
			pDialog.setMessage("�������� ���������� ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}


		/**
		 * getting Profile JSON
		 * */
		protected String doInBackground(String... args) {
			String reference = args[0];
			
			// creating Places class object
			googlePlaces = new GooglePlaces();

			// Check if used is connected to Internet
			try {
				placeDetails = googlePlaces.getPlaceDetails(reference);

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {
					/**
					 * Updating parsed Places into LISTVIEW
					 * */
					if(placeDetails != null){
						String status = placeDetails.status;
						
						// check place details status
						// Check for all possible status
						if(status.equals("OK")){
							if (placeDetails.result != null) {
								String name = placeDetails.result.name;
								String address = placeDetails.result.formatted_address;
								String phone = placeDetails.result.formatted_phone_number;
								
								// Place location
								double lat2 = placeDetails.result.geometry.location.lat;
								double lng2 = placeDetails.result.geometry.location.lng;
								
								
								
								// Current location
								Criteria criteria = new Criteria();
								LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
								String provider = locationManager.getBestProvider(criteria, true);
								Location location = locationManager.getLastKnownLocation(provider);
								double lat1 = location.getLatitude();
							    double long1 = location.getLongitude();
							    
							    // Calculate distance
							    Location locationA = new Location("point A");
								locationA.setLatitude(lat1);
								locationA.setLongitude(long1);
								
								Location locationB = new Location("point B");
								locationB.setLatitude(lat2);
								locationB.setLongitude(lng2);
								
							    float dist = locationA.distanceTo(locationB);
				
								Log.d("Place ", name + address + phone);
								
								// Displaying all the details in the view
								// single_place.xml
								TextView lbl_name = (TextView) findViewById(R.id.name);
								TextView lbl_address = (TextView) findViewById(R.id.address);
								TextView lbl_phone = (TextView) findViewById(R.id.phone);
								TextView lbl_location = (TextView) findViewById(R.id.location);
								
								// Check for null data from google
								// Sometimes place details might missing
								name = name == null ? "�����������" : name; // if name is null display as "Not present"
								address = address == null ? "�����������" : address;
								phone = phone == null ? "�����������" : phone;
								
								// Output information
								lbl_name.setText(name);
								lbl_address.setText(address);
								lbl_phone.setText(Html.fromHtml("<b>�������:</b> " + phone));
								lbl_location.setText(Html.fromHtml("<b>�� �����:</b> " + dist + " �"));
							}
						}
						else if(status.equals("ZERO_RESULTS")){
							alert.showAlertDialog(SinglePlaceActivity.this, "��������� �����",
									"��������, ����� �� �������",
									false);
						}
						else if(status.equals("UNKNOWN_ERROR"))
						{
							alert.showAlertDialog(SinglePlaceActivity.this, "������",
									"��������, ����������� ������",
									false);
						}
						else if(status.equals("OVER_QUERY_LIMIT"))
						{
							alert.showAlertDialog(SinglePlaceActivity.this, "������",
									"��������, ����� �������� ��������",
									false);
						}
						else if(status.equals("REQUEST_DENIED"))
						{
							alert.showAlertDialog(SinglePlaceActivity.this, "������",
									"��������, ��������� ������",
									false);
						}
						else if(status.equals("INVALID_REQUEST"))
						{
							alert.showAlertDialog(SinglePlaceActivity.this, "������",
									"�� ����� �������� ������. ���������� �����",
									false);
						}
						else
						{
							alert.showAlertDialog(SinglePlaceActivity.this, "������",
									"��������, ��������� ������",
									false);
						}
					}else{
						alert.showAlertDialog(SinglePlaceActivity.this, "������",
								"��������, ��������� ������",
								false);
					}
					
					
				}
			});

		}

	}

}

