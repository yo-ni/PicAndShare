package fr.enst.tpt29.picandshare;

import android.graphics.Bitmap;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class PhotoOverlayItem extends OverlayItem {

	Bitmap image;
	
	public PhotoOverlayItem(GeoPoint point, String title, String snippet, Bitmap bm) {
		super(point, title, snippet);
		image = bm;
	}

}
