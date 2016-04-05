package com.roberterrera.neighborhoodcats.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.roberterrera.neighborhoodcats.R;
import com.roberterrera.neighborhoodcats.cardview.RecyclerViewAdapter;
import com.roberterrera.neighborhoodcats.cardview.SwipeableRecyclerViewTouchListener;
import com.roberterrera.neighborhoodcats.models.AnalyticsApplication;
import com.roberterrera.neighborhoodcats.models.Cat;
import com.roberterrera.neighborhoodcats.sqldatabase.CatsSQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
//    implements NavigationView.OnNavigationItemSelectedListener
    public Cursor mCursor;
    private List<Cat> catList;
    private ArrayList<Geofence> mGeofenceList;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private Tracker mTracker;
    private CatsSQLiteOpenHelper mHelper;
    private String[] perms = {"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.ACCESS_COURSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"};
    private int permsRequestCode = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.mainactivity_title));

        catList = new ArrayList<>();
        mGeofenceList = new ArrayList<>();

        // Set up a linear layout manager
        mRecyclerView = (RecyclerView) findViewById(R.id.cardList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // specify the recycler view adapter
        mAdapter = new RecyclerViewAdapter(catList, MainActivity.this);
        mRecyclerView.setAdapter(mAdapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newCatIntent = new Intent(MainActivity.this, NewCatActivity.class);
                startActivity(newCatIntent);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(perms, permsRequestCode);
        }

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        // Load the user's list.
        loadCatsList();

        // Enable swipe-to-delete on cards.
        SwipeableRecyclerViewTouchListener swipeTouchListener =
                new SwipeableRecyclerViewTouchListener(mRecyclerView,
                        new SwipeableRecyclerViewTouchListener.SwipeListener() {

                            public boolean canSwipe(int position) {
                                return true;
                            }

                            @Override
                            public boolean canSwipeLeft(int position) {
                                return true;
                            }

                            @Override
                            public boolean canSwipeRight(int position) {
                                return true;
                            }

                            @Override
                            public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    int id = catList.get(position).getId();

                                    mHelper.deleteCatByID(id);
                                    catList.remove(position);
                                    mAdapter.notifyItemRemoved(position);
                                }
                                mAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    int id = catList.get(position).getId();

                                    mHelper.deleteCatByID(id);
                                    catList.remove(position);
                                    mAdapter.notifyItemRemoved(position);
                                }
                                mAdapter.notifyDataSetChanged();
                            }


                        });

        mRecyclerView.addOnItemTouchListener(swipeTouchListener);

    }

    private void loadCatsList(){
        mHelper = new CatsSQLiteOpenHelper(MainActivity.this);
        mHelper.getWritableDatabase();
        mCursor = CatsSQLiteOpenHelper.getInstance(this).getCatsList();

        // Loop through arraylist and add database items to it.
        while (mCursor.moveToNext()){
            int id = mCursor.getInt(mCursor.getColumnIndex(CatsSQLiteOpenHelper.CAT_ID));
            String name = mHelper.getCatNameByID(id);
            String desc = mHelper.getCatDescByID(id);
            double latitude = mHelper.getCatLatByID(id);
            double longitude = mHelper.getCatLongByID(id);
            String imagePath = mHelper.getCatPhotoByID(id);

            Cat cat = new Cat(id, name, desc, latitude, longitude, imagePath);
            catList.add(cat);
        }
        mCursor.close();
        mHelper.close();
    }

     @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

         return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menu_map) {
            Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(mapIntent);
        }

            return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){
        switch(permsRequestCode){
            case 200:
                boolean permissionAccepted = grantResults[3]== PackageManager.PERMISSION_GRANTED;
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("Cat List");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        mAdapter.notifyDataSetChanged();
        Log.d("onResume", "Cat list refreshed");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

}
