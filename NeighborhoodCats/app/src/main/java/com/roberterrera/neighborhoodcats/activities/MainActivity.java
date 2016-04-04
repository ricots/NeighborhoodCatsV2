package com.roberterrera.neighborhoodcats.activities;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.roberterrera.neighborhoodcats.R;
import com.roberterrera.neighborhoodcats.cardview.RecyclerViewAdapter;
import com.roberterrera.neighborhoodcats.cardview.SwipeableRecyclerViewTouchListener;
import com.roberterrera.neighborhoodcats.models.AnalyticsApplication;
import com.roberterrera.neighborhoodcats.models.Cat;
import com.roberterrera.neighborhoodcats.sqldatabase.CatsSQLiteOpenHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

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

        // use a linear layout manager
        mRecyclerView = (RecyclerView) findViewById(R.id.cardList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // specify the recycler view adapter
        mAdapter = new RecyclerViewAdapter(catList, MainActivity.this);
        mAdapter.setHasStableIds(true);
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

        /* Drawer setup */
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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

        // Handle the search intent.
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    public void handleIntent(Intent intent){

        //TODO: Look into why search doesn't return the search results.
        if (Intent.ACTION_SEARCH.equals( intent.getAction() )){
//             /* this is the Seerach QuerttextListner.
//       this method filter the list data with a matching string,
//       hence provides user an easy way to find the information he needs.
//     */
//
//            SearchView.OnQueryTextListener listener = new SearchView.OnQueryTextListener() {
//                @Override
//                public boolean onQueryTextChange(String query) {
//                    query = query.toLowerCase();
//
//                    final List<String> filteredList = new ArrayList<>();
//
//                    mHelper = new CatsSQLiteOpenHelper(MainActivity.this);
//                    mHelper.getWritableDatabase();
//                    mCursor = CatsSQLiteOpenHelper.getInstance(this).getCatsList();
//
//                    // Loop through arraylist and add database items to it.
//                    while (mCursor.moveToNext()){
//                        int id = mCursor.getInt(mCursor.getColumnIndex(CatsSQLiteOpenHelper.CAT_ID));
//
//                        if (text.contains(query)) {
//
//                            filteredList.add(catList.get(i).getId());
//                        }
//                        String name = mHelper.getCatNameByID(id);
//                        String desc = mHelper.getCatDescByID(id);
//                        String latitude = String.valueOf(mHelper.getCatLatByID(id));
//                        String longitude = String.valueOf(mHelper.getCatLongByID(id));
//                        String imagePath = mHelper.getCatPhotoByID(id);
//
//                        Cat cat = new Cat(id, name, desc, latitude, longitude, imagePath);
//                        catList.add(cat);
//                    }
//
//                    mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
//                    mAdapter = new Adapter(filteredList, MainActivity.this);
//                    mRecyclerView.setAdapter(mAdapter);
//                    mAdapter.notifyDataSetChanged();  // data set changed
//                    return true;
//
//                }
//                public boolean onQueryTextSubmit(String query) {
//                    return false;
//                }
//            };
//            searchView.setOnQueryTextListener(listener); // call the QuerytextListner.

            String query = intent.getStringExtra(SearchManager.QUERY);
            mCursor = CatsSQLiteOpenHelper.getInstance(this).searchCats(query);
            mAdapter.notifyDataSetChanged();
            Toast.makeText(MainActivity.this, "Cat List refreshed by search.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
    }

     @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Setup for the search action.
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();

         SearchableInfo info = searchManager.getSearchableInfo( getComponentName() );
        searchView.setSearchableInfo(info);

         return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_search) {
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("Search")
                    .build());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //TODO: Update these values with settings.
        if (id == R.id.nav_map) {
            // Handle the map intent
            Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(mapIntent);
//        } else if (id == R.id.nav_gallery) {


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
