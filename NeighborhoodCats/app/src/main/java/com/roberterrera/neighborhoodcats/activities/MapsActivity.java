package com.roberterrera.neighborhoodcats.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.roberterrera.neighborhoodcats.R;
import com.roberterrera.neighborhoodcats.models.Cat;
import com.roberterrera.neighborhoodcats.sqldatabase.CatsSQLiteOpenHelper;
import com.roberterrera.neighborhoodcats.models.AnalyticsApplication;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, LocationListener {


    private GoogleMap mMap;
    private double mLatitude, mLongitude;
    private Location mLastLocation;
    private Tracker mTracker;
    private GoogleApiClient mGoogleApiClient;
    private LocationManager locationManager;
    private String provider;
    private ArrayList<Cat> mCatArrayList;
    private Cursor cursor;
    private CatsSQLiteOpenHelper helper;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        setTitle("Map Your Cats!");

        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), true);
        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null) {
            Log.i("Location Info", "Location achieved!");
        } else {
            Log.i("Location Info", "No location :(");
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mMap.setMyLocationEnabled(true);
            return;
        }

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Map")
                .setLabel("Map my location")
                .build());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mCatArrayList = new ArrayList<>();
        helper = new CatsSQLiteOpenHelper(MapsActivity.this);
        helper.getReadableDatabase();
        cursor = CatsSQLiteOpenHelper.getInstance(MapsActivity.this).getCatsList();

        LatLng lastLocation = new LatLng(mLatitude, mLongitude);
        Log.d("onMapReady", mLatitude+", "+mLongitude);

        mMap.setMyLocationEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.addMarker(new MarkerOptions()
                .position(lastLocation)
                .title("You"));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(lastLocation)
                .zoom(mMap.getMaxZoomLevel() * .9f)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Display cat markers on the map using the lat and lon saved to the items' database columns.
        loadCatsList();
    }

    private void loadCatsList(){

        // Loop through arraylist and add database items to it.
        while (cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndex(CatsSQLiteOpenHelper.CAT_ID));
            String name = helper.getCatNameByID(id);
            String desc = helper.getCatDescByID(id);
            double latitude = helper.getCatLatByID(id);
            double longitude = helper.getCatLongByID(id);
            String imagePath = helper.getCatPhotoByID(id);

            Cat cat = new Cat(id, name, desc, latitude, longitude, imagePath);
            mCatArrayList.add(cat);

            LatLng catLatLing = new LatLng(latitude, longitude);
            Marker catMap = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pets_black_24dp))
                    .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                    .position(catLatLing)
                    .title(name));
            Log.d("getCatLocations", String.valueOf(catLatLing));
            Log.d("mCatArrayList", "mCatArrayList size: "+mCatArrayList.size());

        }
        cursor.close();
    }


    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitude = mLastLocation.getLatitude();
            mLongitude = mLastLocation.getLongitude();
        } else {
            Toast.makeText(MapsActivity.this, "Last location is null", Toast.LENGTH_SHORT).show();
        }
        LatLng lastLocation = new LatLng(mLatitude, mLongitude);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(lastLocation)
                .zoom(mMap.getMaxZoomLevel() * .9f)
                .build();
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("MapsActivity~");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {

        double lat = location.getLatitude();
        double lng = location.getLongitude();

        Log.i("Location info: Lat", String.valueOf(lat));
        Log.i("Location info: Lng", String.valueOf(lng));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 18));
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
}
