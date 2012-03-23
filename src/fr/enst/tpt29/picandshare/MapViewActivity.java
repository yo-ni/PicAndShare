package fr.enst.tpt29.picandshare;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.google.android.maps.*;

import android.app.AlertDialog;
import android.content.*;
import android.database.*;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.location.*;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.view.GestureDetector.OnDoubleTapListener;

public class MapViewActivity extends MapActivity implements OnDoubleTapListener, GestureDetector.OnGestureListener{
	
	//variables globales
	static boolean firstPos = false;
	static GeoPoint lastPoint = null;
	static boolean follow = true;

	//variables d'instance
	private LocationManager mlocManager;
	//private SingleLocationListener slocListener;
	private ContinuousLocListener clocListener;
	public MapView mapView;
	private MapViewOverlay mapViewOverlay;
	private MapViewOverlay photoViewOverlay;
	List<Overlay> mapOverlays;
	private GestureDetector gestureDetector = null;
	private SQLiteDatabase myDb;
	
	//variable pour les menus
	static final private int ADD_ID = Menu.FIRST;
    static final private int SAT_ID = Menu.FIRST + 1;
    static final private int SHARE_ID = Menu.FIRST + 2;
    static final private int TAKE_ID = Menu.FIRST + 3;
    static final private int CHOOSE_ID = Menu.FIRST + 4;
    static final private int LOC_ID = Menu.FIRST + 5;
    static final int CAMERA_REQUEST = 201;
    static final int SELECT_IMAGE = 202;
    
    //variables pour la base de données
    public static final String KEY_COMM = "commentaire";
    public static final String KEY_IMG = "image";
    public static final String KEY_LAT = "latitude";
    public static final String KEY_LON = "longitude";
    public static final String PHOTO_TABLE = "photo";
    
    //string pour la requête SQL
    private static final String CREATE_PHOTO_TABLE = "create table if not exists "+PHOTO_TABLE+" ("
                                         +KEY_IMG+" blob not null, "
                                         +KEY_LAT+" int not null, "
                                         +KEY_LON+" int not null, "
                                         +KEY_COMM+" text not null);";
	
	public MapViewActivity(){
	}
	
	@Override 
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		//A la création on s'occupe déjà d'initialiser toutes les variables
		setContentView(R.layout.map_activity);
		
		mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		clocListener = new ContinuousLocListener();
		
		mapView = (MapView) findViewById(R.id.mapview);
		mapOverlays = mapView.getOverlays();
		
		//On définit les icones des différents marqueurs
		Drawable drawable = this.getResources().getDrawable(R.drawable.position);
		Drawable drawable2 = this.getResources().getDrawable(R.drawable.gps);
		
		//On crée les overlay qui contiendront les marqueurs
		//Le premier pour la position actuelle de l'utilisateur
		//Le second pour tous les marqueurs
		mapViewOverlay = new MapViewOverlay(drawable,this, this, true);
		mapOverlays.add(mapViewOverlay);
		photoViewOverlay = new MapViewOverlay(drawable2,this,this,false);
		mapOverlays.add(photoViewOverlay);
		
		//Si on a toujours la dernière position active en mémoire on l'affiche
		if(lastPoint != null){
			PhotoOverlayItem overlayItem  = new PhotoOverlayItem(lastPoint,"","",null,"");
			mapViewOverlay.addOverlay(overlayItem);
		}
		
		//Listener pour le zoom du doubletap
		gestureDetector = new GestureDetector(this);
        gestureDetector.setOnDoubleTapListener(this);

        //Ouverture ou création de la base qui contient les marqueurs
        myDb = openOrCreateDatabase(getFilesDir()+"/item.dat",MODE_WORLD_WRITEABLE, null);

        //On crée la table des marqueurs si elle n'existe pas encore
        myDb.execSQL(CREATE_PHOTO_TABLE);
        
        //On ajoute les entrées de la base dans note overlay
        int i=0;
        PhotoOverlayItem item = getItemFromDB(i); 
        while (item != null) {
        	photoViewOverlay.addOverlay(item);
        	i++;
        	item = getItemFromDB(i);
        }
        myDb.close();
       
		//On associe les listeners aux boutons
        ((Button) findViewById(R.id.addpic)).setOnClickListener(addListener);
        ((Button) findViewById(R.id.sat_street)).setOnClickListener(satListener);
        ((Button) findViewById(R.id.share)).setOnClickListener(shareListener);
        ((Button) findViewById(R.id.follow)).setOnClickListener(followListener);
        
        registerForContextMenu((Button) findViewById(R.id.addpic));
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        
        //On choisit le meilleur provider pour mettre à jour notre position
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        mlocManager.requestLocationUpdates(mlocManager.getBestProvider(criteria, true), 5, 10, clocListener);
     
      //On change le contenu des boutons en fonction de la vue
        if (mapView.isSatellite()) {
        	((Button) findViewById(R.id.sat_street)).setText(R.string.street);
        }
        if (follow){
        	((Button) findViewById(R.id.follow)).setBackgroundDrawable(getResources().getDrawable(R.drawable.gps_active));	
        }
        else ((Button) findViewById(R.id.follow)).setBackgroundDrawable(getResources().getDrawable(R.drawable.gps_unactive));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mlocManager.removeUpdates(clocListener);
	}

	public SQLiteDatabase getDb() {
		return myDb;
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, ADD_ID, 0, R.string.add);
        menu.add(0, SAT_ID, 0, R.string.sat);
        menu.add(0, SHARE_ID, 0, R.string.share);
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
			return true;
		case CHOOSE_ID:
			startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), SELECT_IMAGE);
			return true;
		case LOC_ID:
			
			return true;
		};
		
		return super.onContextItemSelected(item);
	}
	

	/* Class Listeners */
	
	public class ContinuousLocListener implements LocationListener {

		public void onLocationChanged(Location location) {
			
			//On change l'item de localisation
			int latitude = (int) (location.getLatitude() * 1E6);
			int longitude = (int) (location.getLongitude() * 1E6);
			lastPoint = new GeoPoint(latitude,longitude);
			PhotoOverlayItem overlayItem  = new PhotoOverlayItem(lastPoint,"","",null,"");
			
			mapViewOverlay.clearOverlay();
			mapViewOverlay.addOverlay(overlayItem);
			
			if(follow){
				//Si on suit l'utilisateur on bouge la carte en conséquence
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
    	//Quand on double tap on zoom et on centre où l'on a tapé
    	Projection p = mapView.getProjection();
        GeoPoint point = p.fromPixels((int) me.getX(), (int) me.getY());
        mapView.getController().animateTo(point);
        mapView.getController().zoomIn();
        return true;
    }
    //Méthodes héritées du gesturelistener
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
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (requestCode == CAMERA_REQUEST) {
			//On reçoit ou non une photo de l'appareil photo
			if(data!=null) {
				Bitmap photo1 = (Bitmap) data.getExtras().get("data");
				ByteArrayOutputStream out = new ByteArrayOutputStream();
		        photo1.compress(Bitmap.CompressFormat.JPEG, 100, out);
		        Bitmap photo = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.toByteArray().length);
		        
				if (lastPoint == null) {
					//Si on a aucune position valide on ajoute au centre de la vue
					Display display = getWindowManager().getDefaultDisplay();
					int height = display.getHeight();
					int width = display.getWidth();
					Projection p = mapView.getProjection();
					GeoPoint point = p.fromPixels(width/2, height/2);
					
					if(photoViewOverlay.testUnique(point)) {
						PhotoOverlayItem photoItem  = new PhotoOverlayItem(point,"","",photo,"test");
						photoViewOverlay.addOverlay(photoItem);
						
						//On ajoute directement l'item dans la base
						myDb = openOrCreateDatabase(getFilesDir()+"/item.dat",MODE_WORLD_WRITEABLE, null);
						createItemEntry(photoItem);
						myDb.close();
					}
					else {
						//S'il existe déjà une photo à la même position dans la base,
						//Il y a un problème à la suppression
						AlertDialog alertDial = new AlertDialog.Builder(this).create();
						alertDial.setTitle("Attention!");
						alertDial.setMessage("Il y a déjà une photo à cet endroit");
						alertDial.show();
					}
				}
				else {
					//Sinon on la place à la position de l'utilisateur
					if (photoViewOverlay.testUnique(lastPoint)) {
						PhotoOverlayItem photoItem  = new PhotoOverlayItem(lastPoint,"","",photo,"test");
						photoViewOverlay.addOverlay(photoItem);
						
						myDb = openOrCreateDatabase(getFilesDir()+"/item.dat",MODE_WORLD_WRITEABLE, null);
						createItemEntry(photoItem);
						myDb.close();
					}
					else {
						//S'il existe déjà une photo à la même position dans la base,
						//Il y a un problème à la suppression
						AlertDialog alertDial = new AlertDialog.Builder(this).create();
						alertDial.setTitle("Attention!");
						alertDial.setMessage("Il y a déjà une photo à cet endroit");
						alertDial.show();
					}
				}
			}
		}
		else if (requestCode == SELECT_IMAGE){
			//Pour l'instant on fait la même chose qu'avec la photo prise
			//On reçoit ou non une photo de l'appareil photo
			if(data!=null) {
				Uri selPhoto = data.getData();
				try {
					ExifInterface exif = new ExifInterface(getRealPathFromURI(selPhoto));

					int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
					Bitmap photo1 = BitmapFactory.decodeStream(getContentResolver().openInputStream(selPhoto));
					Bitmap photo;
					if (photo1.getHeight() > photo1.getWidth()){
						photo = getResizedBitmap(photo1, photo1.getHeight()/16, photo1.getWidth()/16, orientation);
					}
					else {
						photo = getResizedBitmap(photo1, photo1.getHeight()/16, photo1.getWidth()/16, orientation);
					}
					float[] latLong = new float[2];

					if (!exif.getLatLong(latLong)) {
						//Si on a aucune position valide exif on ajoute au centre de la vue
						Display display = getWindowManager().getDefaultDisplay();
						int height = display.getHeight();
						int width = display.getWidth();
						Projection p = mapView.getProjection();
						GeoPoint point = p.fromPixels(width/2, height/2);

						if(photoViewOverlay.testUnique(point)) {
							PhotoOverlayItem photoItem  = new PhotoOverlayItem(point,"","",photo,"test");
							photoViewOverlay.addOverlay(photoItem);

							//On ajoute directement l'item dans la base
							myDb = openOrCreateDatabase(getFilesDir()+"/item.dat",MODE_WORLD_WRITEABLE, null);
							createItemEntry(photoItem);
							myDb.close();
						}
						else {
							//S'il existe déjà une photo à la même position dans la base,
							//Il y a un problème à la suppression
							AlertDialog alertDial = new AlertDialog.Builder(this).create();
							alertDial.setTitle("Attention!");
							alertDial.setMessage("Il y a déjà une photo à cet endroit");
							alertDial.show();
						}
					}
					else {
						//Sinon on la place à la position de l'exif
						GeoPoint geoPos = new GeoPoint((int)(latLong[0]*1E6), (int)(latLong[1]*1E6));
						if (photoViewOverlay.testUnique(geoPos)) {
							PhotoOverlayItem photoItem  = new PhotoOverlayItem(geoPos,"","",photo,"test");
							photoViewOverlay.addOverlay(photoItem);

							myDb = openOrCreateDatabase(getFilesDir()+"/item.dat",MODE_WORLD_WRITEABLE, null);
							createItemEntry(photoItem);
							myDb.close();
						}
						else {
							//S'il existe déjà une photo à la même position dans la base,
							//Il y a un problème à la suppression
							AlertDialog alertDial = new AlertDialog.Builder(this).create();
							alertDial.setTitle("Attention!");
							alertDial.setMessage("Il y a déjà une photo à cet endroit");
							alertDial.show();
						}
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void createItemEntry(PhotoOverlayItem item) {
		//On crée une nouvelle entrée dans la base qui est déjà ouverte
		//On convertit l'image pour la sauvegarder en Byte
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        item.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, out);
        
        ContentValues cv = new ContentValues();
        cv.put(KEY_IMG, out.toByteArray());            
        cv.put(KEY_COMM, item.getComm());
        cv.put(KEY_LAT, item.getLat());
        cv.put(KEY_LON, item.getLong());

        myDb.insert(PHOTO_TABLE, null, cv);
    }
	
	public PhotoOverlayItem getItemFromDB(int pos) throws SQLException {
		//Permet d'obtenir le pos ième résultat des items présents dans la base
        Cursor cur = myDb.query(true,
                               PHOTO_TABLE,
                               new String[] {KEY_IMG, KEY_COMM, KEY_LAT, KEY_LON},
                               null, null,null, null, null, null);
        
        if(cur.moveToPosition(pos)) {
        	//Si l'item existe on le renvoi
            byte[] blob = cur.getBlob(cur.getColumnIndex(KEY_IMG));
            Bitmap bmp = BitmapFactory.decodeByteArray(blob, 0, blob.length);
            String comm = cur.getString(cur.getColumnIndex(KEY_COMM));
            int lat = cur.getInt(cur.getColumnIndex(KEY_LAT));
            int longi = cur.getInt(cur.getColumnIndex(KEY_LON));
            
            cur.close();
            
            GeoPoint point = new GeoPoint(lat,longi);
            return new PhotoOverlayItem(point,"","",bmp,comm);
        }
        cur.close();
        return null;
    }    
	
	public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth, int orientation) {
		
		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		
		// CREATE A MATRIX FOR THE MANIPULATION
		Matrix matrix = new Matrix();
		//Orientate
		switch (orientation) {
		case 3:
			matrix.postRotate(180);
			break;
		case 6:
			matrix.postRotate(90);
			break;
		case 8:
			matrix.postRotate(270);
		}
		
		// RESIZE THE BIT MAP
		matrix.postScale(scaleWidth, scaleHeight);
		// RECREATE THE NEW BITMAP
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
		return resizedBitmap;
	}

	private String getRealPathFromURI(Uri contentUri) {
	      String[] proj = { MediaStore.Images.Media.DATA };
	      Cursor cursor = managedQuery(contentUri, proj, null, null, null);

	      int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	      cursor.moveToFirst();
	      return cursor.getString(column_index);
	  }
}
