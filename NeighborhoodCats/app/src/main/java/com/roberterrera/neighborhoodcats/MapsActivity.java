package com.roberterrera.neighborhoodcats;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.google.gson.Gson;
import com.roberterrera.neighborhoodcats.models.AnalyticsApplication;
import com.roberterrera.neighborhoodcats.models.Cat;
import com.roberterrera.neighborhoodcats.networking.PetfinderItem;
import com.roberterrera.neighborhoodcats.networking.PetfinderService;
import com.roberterrera.neighborhoodcats.networking.SearchResults;
import com.roberterrera.neighborhoodcats.networking.ShelterItem;
import com.roberterrera.neighborhoodcats.sqldatabase.CatsSQLiteOpenHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, LocationListener {

    private int id;
    private double mLatitude, mLongitude;
    private String location; // This is the zipcode query for PetfinderItem API
    private String provider;
    private ArrayList<Cat> mCatArrayList;

    private GoogleMap mMap;
    private Location mLastLocation;
    private Tracker mTracker;
    private GoogleApiClient mGoogleApiClient;
    private LocationManager locationManager;

    private Cursor cursor;
    private CatsSQLiteOpenHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        setTitle("Map Your Cats!");

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

        String locationProvider = LocationManager.NETWORK_PROVIDER;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        mLatitude = lastKnownLocation.getLatitude();
        mLongitude = lastKnownLocation.getLongitude();
        LatLng lastLocation = new LatLng(mLatitude, mLongitude);
        Log.d("onMapReady", mLatitude + ", " + mLongitude);

        mMap.setMyLocationEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(lastLocation)
                .zoom(mMap.getMaxZoomLevel() * .9f)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Display cat markers on the map using the lat and lon saved to the items' database columns.
        loadCatsList();


        // Build a Retrofit object that calls the PetfinderItem API.
        final String shelterName = "name";
        String format = "format=json";
        String key = "key=e8736f4c0a4c61832d001b9d357055f4";
        getZipcode(mLatitude, mLongitude);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.petfinder.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        PetfinderService service = retrofit.create(PetfinderService.class);
        Call<List<PetfinderItem>> shelters = service.listShelters(format, location, key);
        shelters.enqueue(new Callback<List<PetfinderItem>>() {
            @Override
            public void onResponse(Call<List<PetfinderItem>> call, Response<List<PetfinderItem>> response) {
/*
                Gson gson = new Gson();
                SearchResults result = gson.fromJson(data, SearchResults.class);
                ArrayList<ShelterItem> sheltersArrayList = new ArrayList<ShelterItem>();
                PetfinderItem petfinderItem = new PetfinderItem(location, shelterName);
                ArrayList<PetfinderItem> petfinderItemArrayList = new ArrayList<>();

                // Loop through arraylist and add database items to it.

                // For every object in the item array, add the name to the ArrayList.
                for (int i = 0; i < sheltersArrayList.size(); i++) {
                    petfinderItemArrayList.add(petfinderItem);
                }

                LatLng shelterLatLing = new LatLng(mLatitude, mLongitude);
                Marker shelterMap = mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_domain))
                        .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                        .position(shelterLatLing)
                        .title(shelterName));

                        */
            }

            @Override
            public void onFailure(Call<List<PetfinderItem>> call, Throwable t) {
                Toast.makeText(MapsActivity.this, "Unable to load shelters.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getZipcode(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        addresses = new ArrayList<>();

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            String postalCode = addresses.get(0).getPostalCode();
            location = postalCode;
        } else {
            Toast.makeText(MapsActivity.this,
                    "Cannot show nearby shelters without an internet connection.",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private void loadCatsList() {

        // Loop through arraylist and add database items to it.
        while (cursor.moveToNext()) {
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
        }
        cursor.close();
    }


    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            return;
        }

        if (mLastLocation != null) {
            mLatitude = mLastLocation.getLatitude();
            mLongitude = mLastLocation.getLongitude();
        } else {
            Toast.makeText(MapsActivity.this, "Last location is null", Toast.LENGTH_SHORT).show();
        }
        LatLng location = new LatLng(54.5260, 105.2551);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(location)
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
}
