package fr.enst.tpt29.picandshare;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;

public class MapViewActivity extends MapActivity implements OnDoubleTapListener, GestureDetector.OnGestureListener{
	static boolean firstPos = false;
	static GeoPoint lastPoint = null;
	static boolean follow = true;
	static boolean addPic = false;

	private LocationManager mlocManager;
	private SingleLocationListener slocListener;
	private ContinuousLocListener clocListener;
	private MapView mapView;
	private MapViewOverlay mapViewOverlay;
	private MapViewOverlay photoViewOverlay;
	List<Overlay> mapOverlays;
	private GestureDetector gestureDetector = null;
	
	static final private int ADD_ID = Menu.FIRST;
    static final private int SAT_ID = Menu.FIRST + 1;
    static final private int SHARE_ID = Menu.FIRST + 2;
    static final private int TAKE_ID = Menu.FIRST + 3;
    static final private int CHOOSE_ID = Menu.FIRST + 4;
    static final private int LOC_ID = Menu.FIRST + 5;
    static final int CAMERA_REQUEST = 201;
	
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
		Drawable drawable = this.getResources().getDrawable(R.drawable.position);
		Drawable drawable2 = this.getResources().getDrawable(R.drawable.gps);
		mapViewOverlay = new MapViewOverlay(drawable,this, this, true);
		mapOverlays.add(mapViewOverlay);
		photoViewOverlay = new MapViewOverlay(drawable2,this,this,false);
		mapOverlays.add(photoViewOverlay);
		if(lastPoint != null){
			PhotoOverlayItem overlayItem  = new PhotoOverlayItem(lastPoint,"On est ici !","avec sam",null);
			mapViewOverlay.addOverlay(overlayItem);
		}
		gestureDetector = new GestureDetector(this);
        gestureDetector.setOnDoubleTapListener(this);
		
		// Hook up button presses to the appropriate event handler.
        ((Button) findViewById(R.id.addpic)).setOnClickListener(addListener);
        ((Button) findViewById(R.id.sat_street)).setOnClickListener(satListener);
        ((Button) findViewById(R.id.share)).setOnClickListener(shareListener);
        ((Button) findViewById(R.id.follow)).setOnClickListener(followListener);
        
        registerForContextMenu((Button) findViewById(R.id.addpic));
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        mlocManager.requestLocationUpdates(mlocManager.getBestProvider(criteria, true), 5, 10, clocListener);
        if (!MapViewActivity.firstPos) {
        	MapViewActivity.firstPos = true;
        	mlocManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, slocListener, null);
        }
        if (mapView.isSatellite()) {
        	((Button) findViewById(R.id.sat_street)).setText(R.string.street);
        }
        follow = false;
        ((Button) findViewById(R.id.follow)).setBackgroundDrawable(getResources().getDrawable(R.drawable.gps_unactive));
        addPic = false;
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
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuinfo) {
		super.onCreateContextMenu(menu, v, menuinfo);
		
		menu.add(0, TAKE_ID, 0, R.string.take);
		menu.add(0, CHOOSE_ID, 0, R.string.choose);
		menu.add(0, LOC_ID, 0, R.string.loc);
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
        	openContextMenu((Button) findViewById(R.id.addpic));
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
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case TAKE_ID:
			Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(cameraIntent, CAMERA_REQUEST);
			takeSelected();
			return true;
		case CHOOSE_ID:
			chooseSelected();
			return true;
		case LOC_ID:
			locSelected();
			return true;
		};
		
		return super.onContextItemSelected(item);
	}
	
	@Override
    protected void onPause() {
        super.onPause();
        mlocManager.removeUpdates(clocListener);
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
			PhotoOverlayItem overlayItem  = new PhotoOverlayItem(lastPoint,"On est ici !","et pas là bas",null);
			
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
        	openContextMenu(v);
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
        		if (lastPoint != null) {
        			mapView.getController().animateTo(lastPoint);
        		}
        		((Button) findViewById(R.id.follow)).setBackgroundDrawable(getResources().getDrawable(R.drawable.gps_active));
        	}
        	else {
        		((Button) findViewById(R.id.follow)).setBackgroundDrawable(getResources().getDrawable(R.drawable.gps_unactive));        	}
        }
    };
    
    public boolean onDoubleTap(MotionEvent me) {
    	Projection p = mapView.getProjection();
        GeoPoint point = p.fromPixels((int) me.getX(), (int) me.getY());
        mapView.getController().animateTo(point);
        mapView.getController().zoomIn();
        return true;
    }

	public boolean onDoubleTapEvent(MotionEvent e) {
		return false;
	}
	
	public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

	public boolean onSingleTapConfirmed(MotionEvent e) {
		return false;
	}

	public boolean onDown(MotionEvent e) {
		return false;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return false;
	}

	public void onLongPress(MotionEvent e) {}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	public void onShowPress(MotionEvent e) {}

	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
	
	public void takeSelected() {
		
	}
	
	public void chooseSelected() {
		
	}
	
	public void locSelected() {

	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMERA_REQUEST) {
			if(data!=null) {
				Bitmap photo = (Bitmap) data.getExtras().get("data");
				if (lastPoint == null) {
					Display display = getWindowManager().getDefaultDisplay();
					int height = display.getHeight();
					int width = display.getWidth();
					Projection p = mapView.getProjection();
					GeoPoint point = p.fromPixels(width/2, height/2);
					PhotoOverlayItem photoItem  = new PhotoOverlayItem(point,"En voilà une belle photo","et pas là bas",photo);
					photoViewOverlay.addOverlay(photoItem);
				}
				else {
					PhotoOverlayItem photoItem  = new PhotoOverlayItem(lastPoint,"En voilà une belle photo","et pas là bas",photo);
					photoViewOverlay.addOverlay(photoItem);
				}
			}
			//else Toast.makeText(getApplicationContext(), "Photo non réussie!", Toast.LENGTH_SHORT).show();
		}
	}
}
