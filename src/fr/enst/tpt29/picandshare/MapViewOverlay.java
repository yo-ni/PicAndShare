package fr.enst.tpt29.picandshare;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MapViewOverlay extends ItemizedOverlay<OverlayItem> {
	
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;
	
	public MapViewOverlay(Drawable defaultMarker) {
		//super(defaultMarker);
		  super(boundCenterBottom(defaultMarker));
	}
	
	public MapViewOverlay(Drawable defaultMarker, Context context) {
		  super(boundCenterBottom(defaultMarker));
		  this.populate();
		  mContext = context;
		}

	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
	
	public void setOverlay(OverlayItem overlay, int i) {
		mOverlays.set(i, overlay);
		populate();
	}
	
	public void clearOverlay() {
		mOverlays.clear();
	}

	@Override
	protected OverlayItem createItem(int i) {
	  return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}
	
	@Override
	protected boolean onTap(int index) {
		OverlayItem item = mOverlays.get(index);
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
		dialog.show();
		return true;
	}

	public boolean onTap(final GeoPoint p, final MapView mapView) {
		boolean tapped = super.onTap(p, mapView);
        if (!tapped) {                
                  
        }                            
        return false; 
	}
}
