package fr.enst.tpt29.picandshare;

import com.google.android.maps.MapActivity;

import android.os.Bundle;

public class MapViewActivity extends MapActivity{

	public MapViewActivity(){
		
	}
	
	@Override public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.map_activity);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	
}
