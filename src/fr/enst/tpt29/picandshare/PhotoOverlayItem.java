package fr.enst.tpt29.picandshare;

import android.graphics.Bitmap;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class PhotoOverlayItem extends OverlayItem {

	//Marqueur qui contient la photo et le commentaire en plus
	Bitmap image;
	String comment;
	
	public PhotoOverlayItem(GeoPoint point, String title, String snippet, Bitmap bm, String com) {
		super(point, title, snippet);
		image = bm;
		comment = com;
	}
	
	public Bitmap getBitmap() {
		return image;
	}
	
	public String getComm() {
		return comment;
	}
	
	public int getLat() {
		return mPoint.getLatitudeE6();
	}
	
	public int getLong() {
		return mPoint.getLongitudeE6();
	}
}
