
package fr.enst.tpt29.picandshare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class MainActivity extends Activity {
    
    static final private int TAKE_ID = Menu.FIRST;
    static final private int VIEW_ID = Menu.FIRST + 1;
    
    public MainActivity() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate our UI from its XML layout description.
        setContentView(R.layout.skeleton_activity);

        // Hook up button presses to the appropriate event handler.
        ((Button) findViewById(R.id.takepic)).setOnClickListener(mTakeListener);
        ((Button) findViewById(R.id.viewmap)).setOnClickListener(mViewMapListener);
        
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Called when your activity's options menu needs to be created.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // We are going to create two menus. Note that we assign them
        // unique integer IDs, labels from our string resources, and
        // given them shortcuts.
        menu.add(0, TAKE_ID, 0, R.string.takepic).setShortcut('0', 't');
        menu.add(0, VIEW_ID, 0, R.string.viewmap).setShortcut('1', 'v');

        return true;
    }

    /**
     * Called when a menu item is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case TAKE_ID:

            return true;
        case VIEW_ID:

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    OnClickListener mTakeListener = new OnClickListener() {
        public void onClick(View v) {
            finish();
        }
    };

    OnClickListener mViewMapListener = new OnClickListener() {
        public void onClick(View v) {
        	Intent i = new Intent(MainActivity.this, MapViewActivity.class);
        	
        	startActivity(i);
        }
    };
}
