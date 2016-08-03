package com.roberterrera.neighborhoodcats;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
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
import com.roberterrera.neighborhoodcats.models.analytics.AnalyticsApplication;
import com.roberterrera.neighborhoodcats.models.Cat;
import com.roberterrera.neighborhoodcats.models.petfinderclasses.Petfinder;
import com.roberterrera.neighborhoodcats.models.petfinderclasses.Shelter;
import com.roberterrera.neighborhoodcats.models.petfinderclasses.Shelter_;
import com.roberterrera.neighborhoodcats.service.PetfinderAPI;
import com.roberterrera.neighborhoodcats.sqldatabase.CatsSQLiteOpenHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, LocationListener, GoogleMap.OnInfoWindowClickListener {

    private double mLatitude, mLongitude;
    private String provider;
    private String[] locationPerms = {"android.permission.ACCESS_COURSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"};
    private String location; // This is the zipcode query for PetfinderItem API
    private ArrayList<Cat> mCatArrayList;

    // Database
    private Cursor cursor;
    private CatsSQLiteOpenHelper helper;

    // Analytics
    private Tracker mTracker;

    // Map and location
    private GoogleMap mMap;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationManager locationManager;
    private PicassoMarker target;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        setTitle("Your Cat Map");

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
            mGoogleApiClient = new GoogleApiClient
                    .Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        mTracker.send(new HitBuilders
                .EventBuilder()
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
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

//        // Setting a custom info window adapter for the google map
//        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
//            @Override
//            public View getInfoWindow(Marker marker) {
//                return null;
//            }
//
//            @Override
//            public View getInfoContents(Marker marker) {
//
//                View v = getLayoutInflater().inflate(R.layout.info_window_layout, null);
//
//                TextView mapTitle = (TextView) v.findViewById(R.id.textview_maptitle);
//                TextView mapAddress = (TextView) v.findViewById(R.id.textview_mapaddress);
//                TextView mapPhone = (TextView) v.findViewById(R.id.textview_mapphone);
//                TextView mapEmail = (TextView) v.findViewById(R.id.textview_mapemail);
//
//                mapTitle.setText(title);
//                Log.d("INFOWINADAPTER", "title = "+title);
//                mapAddress.setText(address1);
//                Log.d("INFOWINADAPTER", "address1 = "+address1);
//                mapPhone.setText(phone);
//                Log.d("INFOWINADAPTER", "phone = "+phone);
//                mapEmail.setText(email);
//                Log.d("INFOWINADAPTER", "email = "+email);
//
//                return v;
//
//            }
//        });

        // Display cat markers on the map using the lat and lon saved to the items' database columns.
        loadCatsList();
        loadNearbyShelters();
//        mMap.setOnInfoWindowClickListener(this);

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
    }

    public class PicassoMarker implements Target {

        Marker mMarker;

        PicassoMarker(Marker marker) {
            mMarker = marker;
        }

        @Override
        public int hashCode() {
            return mMarker.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if(o instanceof PicassoMarker) {
                Marker marker = ((PicassoMarker) o).mMarker;
                return mMarker.equals(marker);
            } else {
                return false;
            }
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    }

    private void loadCatsList() {

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

            int height = 125;
            int width = 125;

            MarkerOptions markerOne = new MarkerOptions()
                    .anchor(0.0f, 1.0f)
                    .position(catLatLing)
                    .title(name);

            target = new PicassoMarker(mMap.addMarker(markerOne));
            Picasso.with(MapsActivity.this)
                    .load("file:"+imagePath)
                    .resize(width, height)
                    .into(target);
        }
        cursor.close();
    }

    private void loadNearbyShelters() {


        // Build a Retrofit object that calls the PetfinderItem API.
        String format = "json";
        getZipcode(mLatitude, mLongitude); // Returns "location" variable
        String key = getResources().getString(R.string.petfinder_key);
        PetfinderAPI.Factory.getInstance().loadShelters(format, location, key).enqueue(new Callback<Shelter>() {
            @Override
            public void onResponse(Call<Shelter> call, Response<Shelter> response) {
                final Petfinder petfinder = response.body().getPetfinder();
                List<Shelter_> results = petfinder.getShelters().getShelter();

                for (int j = 0; j <= results.size()-1; j++) {
                    //TODO: Figure out why only a singular result is populating each map item. It's something to do with the info window adapter.

                    double shelterLat = Double.parseDouble(petfinder.getShelters().getShelter().get(j).getLatitude().get$t());
                    double shelterLng = Double.parseDouble(petfinder.getShelters().getShelter().get(j).getLongitude().get$t());
                    String title = String.valueOf(petfinder.getShelters().getShelter().get(j).getName().get$t());
                    String phone = String.valueOf(petfinder.getShelters().getShelter().get(j).getPhone().get$t());
                    String email = String.valueOf(petfinder.getShelters().getShelter().get(j).getEmail().get$t());
                    String address1 = String.valueOf(petfinder.getShelters().getShelter().get(j).getAddress1().get$t());
                    String address2 = String.valueOf(petfinder.getShelters().getShelter().get(j).getAddress2());
                    LatLng shelterLatLng = new LatLng(shelterLat, shelterLng);

                    if (phone == null){
                        phone = "N/A";
                    }
                    if (email == null){
                        email = "N/A";
                    }
                    if (address1 == null){
                        address1 = address2;
                    } else address1 = "N/A";
                    if (address2 == null){
                        address2 = "N/A";
                    }

                    Marker shelterItem = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_domain))
                            .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                            .position(shelterLatLng)
                            .title(title)
                            .snippet("Phone: "+phone)
                    );

                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {

                            //TODO: "Title" needs to be accessed without setting it as final.

//                            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
//                            intent.putExtra(SearchManager.QUERY, title);
//                            if (intent.resolveActivity(getPackageManager()) != null) {
//                                startActivity(intent);
//                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<Shelter> call, Throwable t) {
                Toast.makeText(MapsActivity.this, "Unable to load shelters.", Toast.LENGTH_SHORT).show();
                Log.d("ONFAILURE", String.valueOf(t));
            }
        });
    }

    public void getZipcode(double latitude, double longitude) {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = new ArrayList<>();

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (networkInfo != null && networkInfo.isConnected()) {
            location = addresses.get(0).getPostalCode();
        } else {
            location = "";
            Toast.makeText(MapsActivity.this,
                    "Cannot show nearby shelters without an internet connection.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(locationPerms, 200);
            }
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
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
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){
        boolean permissionAccepted;

        switch(permsRequestCode){
            case 200:
                permissionAccepted = grantResults[1]== PackageManager.PERMISSION_GRANTED;
                break;
        }
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(locationPerms, 200);
            }
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(locationPerms, 200);
            }
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