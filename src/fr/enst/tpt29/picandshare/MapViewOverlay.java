package fr.enst.tpt29.picandshare;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;

import android.database.sqlite.SQLiteDatabase;

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
		Log.i("df","avant ajout");
	    mOverlays.add(overlay);
	    setLastFocusedIndex(-1);
	    populate();

	}
	
	public boolean testUnique(GeoPoint point) {
		for( int i=0; i<mOverlays.size();i++){
		if(point.equals(mOverlays.get(i).getPoint()))
			return false;
		}
		return true;
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
	
	public void removeItem(PhotoOverlayItem item) {
		mOverlays.remove(item);
	    setLastFocusedIndex(-1);
		populate();
		activ.mapView.invalidate();
		SQLiteDatabase db = activ.openOrCreateDatabase(activ.getFilesDir()+"/item.dat",MapViewActivity.MODE_WORLD_WRITEABLE, null);
		//delete from db
		//pour l'instant inexact mais assez précis
		db.delete(MapViewActivity.PHOTO_TABLE, MapViewActivity.KEY_LAT+"="+item.getLat(), null);
		db.close();
	}
	
	@Override
	protected boolean onTap(int index) {
		if (!isLocation) {
			//On affiche le dialogue que si l'on a pas tapé sur la location
			PhotoOverlayItem item = mOverlays.get(index);
			
	        LayoutInflater factory = LayoutInflater.from(mContext);
	        final View alertDialogView = factory.inflate(R.layout.dialog_photo, null);
	 
	        //Création de l'AlertDialog
	        AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
	 
	        //On affecte la vue personnalisé que l'on a crée à notre AlertDialog
	        adb.setView(alertDialogView);
	 
	        //On donne un titre à l'AlertDialog
	        //adb.setTitle("Titre de notre boite de dialogue");
	        ImageView image = (ImageView) alertDialogView.findViewById(R.id.image);
			image.setImageBitmap(item.image);

			TextView text = (TextView) alertDialogView.findViewById(R.id.text);
			text.setText("Quelle belle photo");
	 
	        //On affecte un bouton "OK" à notre AlertDialog et on lui affecte un évènement
	        adb.setPositiveButton("OK", null);
	 
	        //On crée un bouton "Supprimer" à notre AlertDialog et on lui affecte un évènement
	        adb.setNegativeButton("Supprimer", new SupListener(item));
	        adb.show();
		}
		return true;
	}
	
	public class SupListener implements DialogInterface.OnClickListener {

		PhotoOverlayItem item;
		public SupListener(PhotoOverlayItem it) {
			item = it;
		}
		public void onClick(DialogInterface dialog, int which) {
			removeItem(item);			
		}
	}

//	public boolean onTap(final GeoPoint p, final MapView mapView) {
//		boolean tapped = super.onTap(p, mapView);
//		//tapped vaut true si on a tapé sur un objet
//		if (!tapped) {}
//        return false; 
//	}

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
