package fr.enst.tpt29.picandshare;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;

public class MapViewOverlay extends ItemizedOverlay<PhotoOverlayItem> {
	
	//Liste qui contiendra les marqueurs
	private ArrayList<PhotoOverlayItem> mOverlays = new ArrayList<PhotoOverlayItem>();
	private Context mContext;
	private MapViewActivity activ;
	//Booléen qui s'il est activé correspond à l'item location
	boolean isLocation;
	
	public MapViewOverlay(Drawable defaultMarker) {
		  super(boundCenterBottom(defaultMarker));
	}
	
	public MapViewOverlay(Drawable defaultMarker, Context context, MapViewActivity act, boolean isloc) {
		  super(boundCenterBottom(defaultMarker));
		  this.populate();
		  mContext = context;
		  activ = act;
		  isLocation = isloc;
		}

	//Ajout d'un item à la liste
	public void addOverlay(PhotoOverlayItem overlay) {
	    mOverlays.add(overlay);
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
			//On affiche le dialogue que si l'on a pas tapé sur la location
			PhotoOverlayItem item = mOverlays.get(index);
			
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
		//tapped vaut true si on a tapé sur un objet
		if (!tapped) {}
        return false; 
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView){
		//On retire le follow si on l'avait activé et que l'on se déplace sur la carte
		if (MapViewActivity.follow) {
			MapViewActivity.follow = false; 
			((Button) activ.findViewById(R.id.follow)).setBackgroundDrawable(activ.getResources().getDrawable(R.drawable.gps_unactive));
		}
		return super.onTouchEvent(event, mapView);
	}
}
