package com.roberterrera.neighborhoodcats;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.roberterrera.neighborhoodcats.camera.BitmapHelper;
import com.roberterrera.neighborhoodcats.camera.CameraIntentHelper;
import com.roberterrera.neighborhoodcats.camera.CameraIntentHelperCallback;
import com.roberterrera.neighborhoodcats.models.analytics.AnalyticsApplication;
import com.roberterrera.neighborhoodcats.sqldatabase.CatsSQLiteOpenHelper;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewCatActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    @BindView(R.id.imageView_newimage) ImageView mPhoto;
    @BindView(R.id.textView_newlocation) TextView mCatLocation;
    @BindView(R.id.editText_newdesc) EditText mEditCatDesc;
    @BindView(R.id.editText_newname) EditText mEditCatName;

    private String mCurrentPhotoPath;
    private String[] locationPerms = {"android.permission.ACCESS_COURSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"};
    private final int locationRequestCode = 200;
    private double latitude, longitude;

    private Bitmap photo;
    private CameraIntentHelper mCameraIntentHelper;

    private GoogleApiClient mGoogleApiClient;

    private NetworkInfo networkInfo;
    private Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_cat);
        ButterKnife.bind(this);

        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("New Cat!");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        Tracker mTracker = application.getDefaultTracker();

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connMgr.getActiveNetworkInfo();

        // Set up camera intent when activity loads
        setupCameraIntentHelper();

        // Receive photo path from gallery intent via MainActivity.
        String fromGalleryIntent = getIntent().getStringExtra("ImagePath");
        if (fromGalleryIntent != null) {
            mCurrentPhotoPath = fromGalleryIntent;

            // Get the location from the photo and display it.
            getLatLon(mCurrentPhotoPath);

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

            Picasso.with(NewCatActivity.this)
                    .load("file:" + mCurrentPhotoPath)
                    .placeholder(R.drawable.ic_pets_black_24dp)
                    .resize(width, height)
                    .centerInside()
                    .into(mPhoto);

        } else if (mCameraIntentHelper != null) {
            mCameraIntentHelper.startCameraIntent();
        }
    }

    private void setupCameraIntentHelper() {

        mCameraIntentHelper = new CameraIntentHelper(this, new CameraIntentHelperCallback() {
            @Override
            public void onPhotoUriFound(Date dateCameraIntentStarted, Uri photoUri, int rotateXDegrees) {

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;

                photo = BitmapHelper.readBitmap(NewCatActivity.this, photoUri);
                if (photo != null) {
                    photo = BitmapHelper.shrinkBitmap(photo, width);
                    mPhoto.setImageBitmap(photo);
                    mCurrentPhotoPath = photoUri.getPath();
                } else {
                    deletePhotoWithUri(photoUri);
                }

                getUserLocation();

            }


            @Override
            public void deletePhotoWithUri(Uri photoUri) {
                BitmapHelper.deleteImageWithUriIfExists(photoUri, NewCatActivity.this);
            }

            @Override
            public void onSdCardNotMounted() {
                Toast.makeText(getApplicationContext(), getString(R.string.error_sd_card_not_mounted), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCanceled() {
                // If camera is canceled, return to MainActivity (cat list).
                Intent backToMainIntent = new Intent(NewCatActivity.this, MainActivity.class);
                startActivity(backToMainIntent);
            }

            @Override
            public void onCouldNotTakePhoto() {
                Toast.makeText(getApplicationContext(), getString(R.string.error_could_not_take_photo), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPhotoUriNotFound() {
                Toast.makeText(NewCatActivity.this, R.string.activity_camera_intent_photo_uri_not_found, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void logException(Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_sth_went_wrong), Toast.LENGTH_LONG).show();
                Log.d(getClass().getName(), e.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        mCameraIntentHelper.onActivityResult(requestCode, resultCode, intent);
    }

    // Get lat & lon from a previously saved photo and display address
    private void getLatLon(String imagePath) {

        ExifInterface exif = null;

        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String LATITUDE;
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
                getUserLocation();
                Toast.makeText(NewCatActivity.this, "No location found in selected photo.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private Double convertToDegree(String location) {
        Double result;
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

    public void getUserLocation() {
        if (networkInfo != null && networkInfo.isConnected()) {
            if (mLastLocation != null) {
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
                showAddress();
            } else {
                latitude = 0.0;
                longitude = 0.0;
                Toast.makeText(NewCatActivity.this, "Location unavailable.", Toast.LENGTH_SHORT).show();
                mCatLocation.setText(R.string.location_unavailable);
            }
        }
    }

    // Check permissions
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (permsRequestCode) {
            case locationRequestCode:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    //noinspection MissingPermission
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                            mGoogleApiClient);
                }
                break;
            default:
                super.onRequestPermissionsResult(permsRequestCode, permissions, grantResults);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void showAddress() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;

        if (networkInfo != null && networkInfo.isConnected()) {
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                if (!addresses.isEmpty()) {
                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String postalCode = addresses.get(0).getPostalCode();
                    String catAddress = address + ", " + city + ", " + state + " " + postalCode;

                    mCatLocation.setText(catAddress);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.newcat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            save();
            return true;
        }
        if (id == R.id.menu_item_share) {
            save();
            shareChooser();
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareChooser() {
        // specify our test image location
        Uri url = Uri.parse(mCurrentPhotoPath);

        // set up an intent to share the image
        Intent share_intent = new Intent();
        share_intent.setAction(Intent.ACTION_SEND);
        share_intent.setType("image/jpg");
        share_intent.putExtra(Intent.EXTRA_STREAM,
                Uri.fromFile(new File(url.toString())));
        share_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        share_intent.putExtra(Intent.EXTRA_SUBJECT,
                "Share this cat!");
        share_intent.putExtra(Intent.EXTRA_TEXT,
                mEditCatName.getText().toString() + ": "
                        + mEditCatDesc.getText().toString());

        // start the intent
        try {
            startActivity(Intent.createChooser(share_intent,
                    "Sharing " + mEditCatName.getText().toString()));
        } catch (android.content.ActivityNotFoundException ex) {
            (new AlertDialog.Builder(NewCatActivity.this)
                    .setMessage("Share failed")
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                }
                            }).create()).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (networkInfo != null && networkInfo.isConnected()) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, locationPerms, locationRequestCode);
                Toast.makeText(NewCatActivity.this, "Location permission required to map your cats.", Toast.LENGTH_SHORT).show();

            } else {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);
            }
        }
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

    private void save(){

        CatsSQLiteOpenHelper helper = CatsSQLiteOpenHelper.getInstance(NewCatActivity.this);
        helper.getWritableDatabase();

        try {
            helper.insert(
                    mEditCatName.getText().toString(),
                    mEditCatDesc.getText().toString(),
                    latitude,
                    longitude,
                    mCurrentPhotoPath);
            Log.d("save", mEditCatName.getText().toString()+", "
                    + mEditCatDesc.getText().toString()+", "
                    + String.valueOf(latitude)
                    + ", "
                    +String.valueOf(longitude)
                    +", "
                    + mCurrentPhotoPath);
            Toast.makeText(NewCatActivity.this, "Cat saved.", Toast.LENGTH_SHORT).show();
        } catch (Exception e){
            Toast.makeText(NewCatActivity.this,
                    "There was a problem saving your cat data :(",
                    Toast.LENGTH_SHORT).show();
        }

        Intent backToMainIntent = new Intent(NewCatActivity.this, MainActivity.class);
        startActivity(backToMainIntent);
        helper.close();
    }

    @Override
    public void onLocationChanged(Location location) {}

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (mCameraIntentHelper != null) {
            mCameraIntentHelper.onSaveInstanceState(savedInstanceState);
            savedInstanceState.putParcelable("file_uri", photo);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCameraIntentHelper.onRestoreInstanceState(savedInstanceState);
        // get the file url
        photo = savedInstanceState.getParcelable("file_uri");    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}