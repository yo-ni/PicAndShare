package fr.enst.tpt29.picandshare;

import com.google.android.maps.MapActivity;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

public class MapViewActivity extends MapActivity{

	private LocationManager mlocManager;
	private LocationListener mlocListener;
	public MapViewActivity(){
		
	}
	
	@Override public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.map_activity);
		
		mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

		mlocListener = new MyLocationListener();

		mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
    protected void onPause() {
        super.onPause();
        mlocManager.removeUpdates(mlocListener);
    }

	/* Class My Location Listener */

	public class MyLocationListener implements LocationListener {

		public void onLocationChanged(Location location) {
			String Text = "My current location is: " + "Latitude = " + location.getLatitude() +
					"Longitude = " + location.getLongitude();
			Toast.makeText( getApplicationContext(),Text,Toast.LENGTH_SHORT).show();
		}

		public void onProviderDisabled(String provider) {
			Toast.makeText(getApplicationContext(), "Gps Disabled",Toast.LENGTH_SHORT).show();
		}

		public void onProviderEnabled(String provider) {
			Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
		
	}
}
