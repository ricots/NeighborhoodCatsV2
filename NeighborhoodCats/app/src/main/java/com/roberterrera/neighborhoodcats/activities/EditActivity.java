package com.roberterrera.neighborhoodcats.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.roberterrera.neighborhoodcats.R;
import com.roberterrera.neighborhoodcats.models.AnalyticsApplication;
import com.roberterrera.neighborhoodcats.sqldatabase.CatsSQLiteOpenHelper;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private ImageView mPhoto;
    private EditText mEditCatName, mEditCatDesc;
    private TextView mCatLocation;

    private String mLatLong, photoPath;
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

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            mPhoto = (ImageView) findViewById(R.id.imageView_edit_image);
            mCatLocation = (TextView) findViewById(R.id.textView_edit_location);
            mEditCatDesc = (EditText) findViewById(R.id.editText_edit_desc);
            mEditCatName = (EditText) findViewById(R.id.editText_edit_name);
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
//                      .resize(width, height)
                    .placeholder(R.drawable.ic_pets)
//                      .centerCrop()
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
        List<Address> addresses;
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