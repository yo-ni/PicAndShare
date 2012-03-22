package fr.enst.tpt29.picandshare;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MapViewOverlay extends ItemizedOverlay<PhotoOverlayItem> {
	
	private ArrayList<PhotoOverlayItem> mOverlays = new ArrayList<PhotoOverlayItem>();
	private Context mContext;
	private MapViewActivity activ;
	boolean isLocation;
	
	public MapViewOverlay(Drawable defaultMarker) {
		//super(defaultMarker);
		  super(boundCenterBottom(defaultMarker));
	}
	
	public MapViewOverlay(Drawable defaultMarker, Context context, MapViewActivity act, boolean isloc) {
		  super(boundCenterBottom(defaultMarker));
		  this.populate();
		  mContext = context;
		  activ = act;
		  isLocation = isloc;
		}

	public void addOverlay(PhotoOverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
	
	public void setOverlay(PhotoOverlayItem overlay, int i) {
		mOverlays.set(i, overlay);
		populate();
	}
	
	public void clearOverlay() {
		mOverlays.clear();
	}

	@Override
	protected PhotoOverlayItem createItem(int i) {
	  return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}
	
	@Override
	protected boolean onTap(int index) {
		if (!isLocation) {
			PhotoOverlayItem item = mOverlays.get(index);
//			AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
//			dialog.setTitle(item.getTitle());
//			dialog.setMessage(item.getSnippet());
//			dialog.show();
			Dialog dialog = new Dialog(mContext);

			dialog.setContentView(R.layout.dialog_photo);
			dialog.setTitle("Test Photo");

			TextView text = (TextView) dialog.findViewById(R.id.text);
			text.setText("Quelle belle photo");
			ImageView image = (ImageView) dialog.findViewById(R.id.image);
			image.setImageBitmap(item.image);
			
			dialog.show();
		}
		return true;
	}

	public boolean onTap(final GeoPoint p, final MapView mapView) {
		boolean tapped = super.onTap(p, mapView);
        if (!tapped) {                
        	//Création de l'objet
        }                            
        return false; 
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView){
		
		if (MapViewActivity.follow) {
			MapViewActivity.follow = false; 
			((Button) activ.findViewById(R.id.follow)).setBackgroundDrawable(activ.getResources().getDrawable(R.drawable.gps_unactive));
		}
		return super.onTouchEvent(event, mapView);
	}
}
