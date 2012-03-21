package fr.enst.tpt29.picandshare;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MapViewActivity extends MapActivity{
	static boolean firstPos = false;
	static GeoPoint lastPoint = null;
	static boolean follow = true;

	private LocationManager mlocManager;
	private SingleLocationListener slocListener;
	private ContinuousLocListener clocListener;
	private MapView mapView;
	private MapViewOverlay mapViewOverlay;
	List<Overlay> mapOverlays;
	
	static final private int ADD_ID = Menu.FIRST;
    static final private int SAT_ID = Menu.FIRST + 1;
    static final private int SHARE_ID = Menu.FIRST + 2;
	
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
		if(lastPoint != null){
			OverlayItem overlayItem  = new OverlayItem(lastPoint,"On est ici !","avec sam");
			mapViewOverlay.addOverlay(overlayItem);
		}
		
		// Hook up button presses to the appropriate event handler.
        ((Button) findViewById(R.id.addpic)).setOnClickListener(addListener);
        ((Button) findViewById(R.id.sat_street)).setOnClickListener(satListener);
        ((Button) findViewById(R.id.share)).setOnClickListener(shareListener);
        ((Button) findViewById(R.id.follow)).setOnClickListener(followListener);
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, clocListener);
        if (!MapViewActivity.firstPos) {
        	MapViewActivity.firstPos = true;
        	mlocManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, slocListener, null);
        }
        if (mapView.isSatellite()) {
        	((Button) findViewById(R.id.sat_street)).setText(R.string.street);
        }
        follow = false;
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, ADD_ID, 0, R.string.add).setShortcut('0', 'a');
        menu.add(0, SAT_ID, 0, R.string.sat).setShortcut('1', 'c');
        menu.add(0, SHARE_ID, 0, R.string.share).setShortcut('2', 's');
        if (mapView.isSatellite()) {
        	menu.getItem(SAT_ID-1).setTitle(getString(R.string.street));
        }
        return true;
    }
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu){
		if (mapView.isSatellite()) {
			menu.getItem(SAT_ID-1).setTitle(getString(R.string.street));
		}
		else {
			menu.getItem(SAT_ID-1).setTitle(getString(R.string.sat));
		}
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case ADD_ID:

            return true;
        case SAT_ID:
        	if (mapView.isSatellite()) {
        		mapView.setSatellite(false);
        		((Button) findViewById(R.id.sat_street)).setText(R.string.sat);
        	}
        	else {
        		mapView.setSatellite(true);
        		((Button) findViewById(R.id.sat_street)).setText(R.string.street);
        	}
            return true;
        case SHARE_ID:
        	
        	return true;
        };

        return super.onOptionsItemSelected(item);
    }
	
	@Override
    protected void onPause() {
        super.onPause();
        mlocManager.removeUpdates(clocListener);
    }
	
	@Override 
	public boolean onTouchEvent(MotionEvent event) {
		follow = false;
		return true;
	}

	/* Class Listeners */

	public class SingleLocationListener implements LocationListener {

		public void onLocationChanged(Location location) {
			int latitude = (int) (location.getLatitude() * 1E6);
			int longitude = (int) (location.getLongitude() * 1E6);
			GeoPoint point = new GeoPoint(latitude,longitude);
			mapView.getController().animateTo(point);
			int firstZoom = mapView.getZoomLevel();
			int targetZoom = 17;
			Handler handler= new Handler();
			long delay=0;
			
			while(firstZoom++ <targetZoom){
				handler.postDelayed(new Runnable(){
					public void run(){
						mapView.getController().zoomIn();
					}
				}, delay);
				delay+=300;
			}
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
			lastPoint = new GeoPoint(latitude,longitude);
			OverlayItem overlayItem  = new OverlayItem(lastPoint,"On est ici !","avec sam");
			mapViewOverlay.clearOverlay();
			mapViewOverlay.addOverlay(overlayItem);
			mapView.invalidate();
			if(follow){
				mapView.getController().animateTo(lastPoint);
			}
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {	
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}	
	}
	
	OnClickListener addListener = new OnClickListener() {
        public void onClick(View v) {
  
        }
    };
    
    OnClickListener satListener = new OnClickListener() {
        public void onClick(View v) {
        	if (mapView.isSatellite()) {
        		mapView.setSatellite(false);
        		((Button) findViewById(R.id.sat_street)).setText(R.string.sat);
        	}
        	else {
        		mapView.setSatellite(true);
        		((Button) findViewById(R.id.sat_street)).setText(R.string.street);
        	}
        }
    };
    
    OnClickListener shareListener = new OnClickListener() {
        public void onClick(View v) {
  
        }
    };
    
    OnClickListener followListener = new OnClickListener() {
        public void onClick(View v) {
        	follow = !follow;
        	if (follow) {
        		((Button) findViewById(R.id.follow)).setSelected(true);
        	}
        	else {
        		((Button) findViewById(R.id.follow)).setSelected(false);
        	}
        }
    };
}
