package fr.enst.tpt29.picandshare;

import android.R.string;
import android.graphics.Bitmap;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class PhotoOverlayItem extends OverlayItem {

	Bitmap image;
	String comm;
	
	public PhotoOverlayItem(GeoPoint point, String title, String snippet, Bitmap bm, String co) {
		super(point, title, snippet);
		image = bm;
		comm = co;
	}
	
	public Bitmap getBitmap() {
		return image;
	}
	
	public String getComm() {
		return comm;
	}
	
	public int getLat() {
		return mPoint.getLatitudeE6();
	}
	
	public int getLong() {
		return mPoint.getLongitudeE6();
	}

}
