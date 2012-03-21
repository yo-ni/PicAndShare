package fr.enst.tpt29.picandshare;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

public class MapViewActivity extends MapActivity{
	static boolean firstPos = false;

	private LocationManager mlocManager;
	private SingleLocationListener slocListener;
	private ContinuousLocListener clocListener;
	private MapView mapView;
	private MapViewOverlay mapViewOverlay;
	List<Overlay> mapOverlays;
	
	public MapViewActivity(){
		
	}
	
	@Override public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.map_activity);
		mapView = (MapView) findViewById(R.id.mapview);
		
		mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		slocListener = new SingleLocationListener();
		clocListener = new ContinuousLocListener();
		mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(R.drawable.violet);
		mapViewOverlay = new MapViewOverlay(drawable,this);
		mapOverlays.add(mapViewOverlay);
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, clocListener);
        if (!MapViewActivity.firstPos) {
        	MapViewActivity.firstPos = true;
        	mlocManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, slocListener, null);
        }
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
    protected void onPause() {
        super.onPause();
        mlocManager.removeUpdates(clocListener);
    }

	/* Class Location Listeners */

	public class SingleLocationListener implements LocationListener {

		public void onLocationChanged(Location location) {
//			String Text = "My current location is: " + "Latitude = " + location.getLatitude() +
//					"Longitude = " + location.getLongitude();
//			Toast.makeText( getApplicationContext(),Text,Toast.LENGTH_SHORT).show();
			int latitude = (int) (location.getLatitude() * 1E6);
			int longitude = (int) (location.getLongitude() * 1E6);
			GeoPoint point = new GeoPoint(latitude,longitude);
			mapView.getController().setCenter(point);
			mapView.getController().setZoom(17);
			mapView.invalidate();
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}
	
	public class ContinuousLocListener implements LocationListener {

		public void onLocationChanged(Location location) {
			int latitude = (int) (location.getLatitude() * 1E6);
			int longitude = (int) (location.getLongitude() * 1E6);
			GeoPoint point = new GeoPoint(latitude,longitude);
			OverlayItem overlayItem  = new OverlayItem(point,"On est ici !","avec sam");
			mapViewOverlay.clearOverlay();
			mapViewOverlay.addOverlay(overlayItem);
			mapView.invalidate();
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {	
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}	
	}
}
