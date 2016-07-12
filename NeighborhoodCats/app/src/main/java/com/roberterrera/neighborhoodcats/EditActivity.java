package com.roberterrera.neighborhoodcats;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.roberterrera.neighborhoodcats.models.analytics.AnalyticsApplication;
import com.roberterrera.neighborhoodcats.sqldatabase.CatsSQLiteOpenHelper;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;

public class EditActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private ImageView mPhoto;
    private EditText mEditCatName, mEditCatDesc, mCatLocation;

    private String mLatLong, photoPath;
    List<Address> addresses;

    private int catId;
    private double latitude, longitude;

    private Tracker mTracker;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private CatsSQLiteOpenHelper helper;

    private static final String TAG = "EditActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);

        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPhoto = ButterKnife.findById(this, R.id.imageView_edit_image);
        mCatLocation = ButterKnife.findById(this, R.id.editText_edit_location);
        mEditCatDesc = ButterKnife.findById(this, R.id.editText_edit_desc);
        mEditCatName = ButterKnife.findById(this, R.id.editText_edit_name);
        catId = getIntent().getIntExtra("id", -2);


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

        LoadCatAsyncTask loadCatAsyncTask = new LoadCatAsyncTask();
        loadCatAsyncTask.execute();

    }

    private class LoadCatAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            // Get intent from MainActivity list via the cat's id.
            helper = CatsSQLiteOpenHelper.getInstance(EditActivity.this);
            helper.getReadableDatabase();

            photoPath = helper.getCatPhotoByID(catId);
            latitude = helper.getCatLatByID(catId);
            longitude = helper.getCatLongByID(catId);
            mLatLong = locationToString();

            helper.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // Set activity title
            if (mEditCatName != null) {
                setTitle("Update "+helper.getCatNameByID(catId));
            } else {
                setTitle("Update Cat");
            }

            mEditCatName.setText(helper.getCatNameByID(catId));
            mEditCatDesc.setText(helper.getCatDescByID(catId));

            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                showAddress();
            } else {
            mCatLocation.setText(locationToString());
            }

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

            Picasso.with(EditActivity.this)
                    .load("file:" + photoPath)
                    .resize(width, height)
                    .centerInside()
                    .placeholder(R.drawable.ic_pets_black_24dp)
                    .into(mPhoto);
        }
    }

    public String locationToString() {
        return (String.valueOf(latitude)
                + ", "
                + String.valueOf(longitude));
    }

    public void showAddress(){
        Geocoder geocoder;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = new ArrayList<>();

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String postalCode = addresses.get(0).getPostalCode();

        mCatLocation.setText(address+", "+city+", "+state+" "+postalCode);

    }

    public void getLatLongFromPlace(String place) {
        try {
            Geocoder selected_place_geocoder = new Geocoder(EditActivity.this);

            addresses = selected_place_geocoder.getFromLocationName(place, 1);

            if (addresses != null && addresses.size() > 0) {
                latitude = addresses.get(0).getLatitude();
                Log.d("LATITUDE", "latitude = "+ String.valueOf(latitude));
                longitude = addresses.get(0).getLongitude();
                Log.d("LONGITUDE", "longitude = "+ String.valueOf(longitude));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getLatLon(String imagePath) {

        ExifInterface exif = null;

        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String LATITUDE = null;
        if (exif != null) {
            LATITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);

            String LATITUDE_REF = exif
                    .getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            String LONGITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            String LONGITUDE_REF = exif
                    .getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

            if ((LATITUDE != null) && (LATITUDE_REF != null) && (LONGITUDE != null)
                    && (LONGITUDE_REF != null)) {

                if (LATITUDE_REF.equals("N")) {
                    latitude = convertToDegree(LATITUDE);
                } else {
                    latitude = 0 - convertToDegree(LATITUDE);
                }

                if (LONGITUDE_REF.equals("E")) {
                    longitude = convertToDegree(LONGITUDE);
                } else {
                    longitude = 0 - convertToDegree(LONGITUDE);
                } showAddress();

            } else {
                Toast.makeText(EditActivity.this, "No location found in selected photo.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private Double convertToDegree(String location) {
        Double result = null;
        String[] DMS = location.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = Double.valueOf(stringD[0]);
        Double D1 = Double.valueOf(stringD[1]);
        Double FloatD = D0 / D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = Double.valueOf(stringM[0]);
        Double M1 = Double.valueOf(stringM[1]);
        Double FloatM = M0 / M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = Double.valueOf(stringS[0]);
        Double S1 = Double.valueOf(stringS[1]);
        Double FloatS = S0 / S1;

        result = FloatD + (FloatM / 60) + (FloatS / 3600);

        return result;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_cat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_update) {
            update();
            return true;
        }
        if (id == R.id.action_location) {
            getLatLon(photoPath);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(EditActivity.this, "Could not connect to internet. Cat locations won't be saved.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {

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
    protected void onResume() {
        super.onResume();
    }

    private void update(){
        CatsSQLiteOpenHelper helper = CatsSQLiteOpenHelper.getInstance(EditActivity.this);
        helper.getWritableDatabase();

        try {
            // Update name and description in the database.
            helper.updateDescByID(catId, mEditCatDesc.getText().toString());
            helper.updateNameByID(catId, mEditCatName.getText().toString());

            getLatLongFromPlace(mCatLocation.getText().toString());
            helper.updateLocationByID(catId, latitude, longitude);

            Toast.makeText(EditActivity.this, "Cat updated.", Toast.LENGTH_SHORT).show();
        } catch (Exception e){
            Toast.makeText(EditActivity.this,
                    "There was a problem updating your cat data :(",
                    Toast.LENGTH_SHORT).show();
        }
        Intent backToMainIntent = new Intent(EditActivity.this, MainActivity.class);
        startActivity(backToMainIntent);
        helper.close();
    }

    @Override
    public void onLocationChanged(Location location) {

    }
}