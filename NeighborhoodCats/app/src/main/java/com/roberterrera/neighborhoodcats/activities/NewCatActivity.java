package com.roberterrera.neighborhoodcats.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.roberterrera.neighborhoodcats.R;
import com.roberterrera.neighborhoodcats.models.AnalyticsApplication;
import com.roberterrera.neighborhoodcats.camera.BitmapHelper;
import com.roberterrera.neighborhoodcats.camera.CameraIntentHelper;
import com.roberterrera.neighborhoodcats.camera.CameraIntentHelperCallback;
import com.roberterrera.neighborhoodcats.sqldatabase.CatsSQLiteOpenHelper;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewCatActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private String mLatLong;
    public String mCurrentPhotoPath;
    private double latitude, longitude;

    private ImageView mPhoto;
    private EditText mEditCatName, mEditCatDesc;
    private TextView mCatLocation;

    private Tracker mTracker;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private NetworkInfo networkInfo;
    private ConnectivityManager connMgr;
    private CameraIntentHelper mCameraIntentHelper;

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int RESULT_LOAD_IMG = 2;
    private static final String TAG = "NewCatActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_cat);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("New Cat!");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPhoto = (ImageView) findViewById(R.id.imageView_newimage);
        mCatLocation = (TextView) findViewById(R.id.textView_newlocation);
        mEditCatDesc = (EditText) findViewById(R.id.editText_newdesc);
        mEditCatName = (EditText) findViewById(R.id.editText_newname);

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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
        } else {
            Toast.makeText(NewCatActivity.this, "Location unavailable", Toast.LENGTH_SHORT).show();
        }
        setupCameraIntentHelper();

        // Open the camera when the activity is launched.
        if (mCameraIntentHelper != null) {
            mCameraIntentHelper.startCameraIntent();
        }


        // Take a photo if you tap on the imageview
        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCameraIntentHelper != null) {
                    mCameraIntentHelper.startCameraIntent();
                }
            }
        });

        // Use an image from device.
        mPhoto.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
              // Create intent to Open Image applications like Gallery, Google Photos
              Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                  MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                setResult(RESULT_OK, galleryIntent);

                // Start the Intent
              startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
              return true;
            }
        });
    }


    private void setupCameraIntentHelper() {
        mCameraIntentHelper = new CameraIntentHelper(this, new CameraIntentHelperCallback() {
            @Override
            public void onPhotoUriFound(Date dateCameraIntentStarted, Uri photoUri, int rotateXDegrees) {
//                Toast.makeText(NewCatActivity.this, R.string.activity_camera_intent_photo_uri_found, Toast.LENGTH_SHORT).show();

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                int height = size.y;


                Bitmap photo = BitmapHelper.readBitmap(NewCatActivity.this, photoUri);
                if (photo != null) {
                    photo = BitmapHelper.shrinkBitmap(photo, width);
                    mPhoto.setImageBitmap(photo);
                    mCurrentPhotoPath = photoUri.getPath();
                    Log.d("SETUPCAMERAINTENT", mCurrentPhotoPath);
                } else {
                    deletePhotoWithUri(photoUri);
                }
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
//                Toast.makeText(getApplicationContext(), getString(R.string.warning_camera_intent_canceled), Toast.LENGTH_LONG).show();
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
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

            mCameraIntentHelper.onActivityResult(requestCode, resultCode, intent);

//                if (networkInfo != null && networkInfo.isConnected()) {
//                    showAddress();
//                } else {
                    mLatLong = locationToString();
//                }
                mCatLocation.setText(mLatLong);

            // When an Image is picked
        if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                && null != intent) {
            // Note: If image is an older image being selected via Google Photos, the image will not
            // be loaded because it has to be downloaded first.
            Uri selectedImageUri = intent.getData();
            mCurrentPhotoPath = getPath(selectedImageUri);

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

            Picasso.with(NewCatActivity.this)
                    .load("file:" + mCurrentPhotoPath)
                    .resize(width, height)
                    .placeholder(R.drawable.ic_pets)
                    .centerCrop()
                    .into(mPhoto);

            if (networkInfo != null && networkInfo.isConnected()) {
                showAddress();
            } else {
                mLatLong = locationToString();
            }
            mCatLocation.setText(mLatLong);

        }
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri,
                projection, null, null, null);
        if(cursor!=null)
        {
            //HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            //THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        else return null;
    }


    // Check permissions
    public boolean hasPermissionInManifest(Context context, String permissionName) {
        final String packageName = context.getPackageName();
        try {
            final PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            final String[] declaredPermissisons = packageInfo.requestedPermissions;
            if (declaredPermissisons != null && declaredPermissisons.length > 0) {
                for (String p : declaredPermissisons) {
                    if (p.equals(permissionName)) {
                        return true;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("HAS_PERMISSION", "Catch: "+String.valueOf(e));
        }
        return false;
    }

    // Save image to a file in the public pictures dir.
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "CAT_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();

        Log.d("CREATEIMAGEFILE", mCurrentPhotoPath);

        galleryAddPic(); // Add image to device gallery.
        return image;
    }


  private void galleryAddPic() {
      Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
      File f = new File(mCurrentPhotoPath);
      Uri contentUri = Uri.fromFile(f);
      mediaScanIntent.setData(contentUri);
      this.sendBroadcast(mediaScanIntent);
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

    private boolean hasCamera(){
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.newcat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_save) {
            save();
            return true;
        }
        if (id == R.id.menu_item_share){
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
                mEditCatName.getText().toString()+": "
                        +mEditCatDesc.getText().toString());

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
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(NewCatActivity.this, "Could not connect to internet. Cat locations won't be saved.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        // Check if a connection is available.
        networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
            }
        } else {
            latitude = 0.0;
            longitude = 0.0;
            Toast.makeText(NewCatActivity.this, "Location unavailable.", Toast.LENGTH_SHORT).show();
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
    public void onLocationChanged(Location location) {

        // Check if a connection is available.
        networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        } else {
            latitude = 0.0;
            longitude = 0.0;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        mCameraIntentHelper.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCameraIntentHelper.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}