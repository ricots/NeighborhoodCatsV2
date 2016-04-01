package com.roberterrera.neighborhoodcats;

import android.app.IntentService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.roberterrera.neighborhoodcats.localdata.CatsSQLiteOpenHelper;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewCatActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private ImageView mPhoto;
    private EditText mEditCatName, mEditCatDesc;
    private Location mLastLocation;
    private Tracker mTracker;
    private GoogleApiClient mGoogleApiClient;

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int RESULT_LOAD_IMG = 2;
    private static final String TAG = "NewCatActivity";

    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_cat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("New Cat!");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mPhoto = (ImageView) findViewById(R.id.imageView_newimage);
        mEditCatDesc = (EditText) findViewById(R.id.editText_newdesc);
        mEditCatName = (EditText) findViewById(R.id.editText_newname);

        // Open the camera when the activity is launched.
        dispatchTakePictureIntent();

      //TODO: Ask for storage permission and check that the permission is granted (check if anything Marshmallow-specific needs to be done).
        // Take a photo if you tap on the imageview
        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //TODO: Add a textview where the image view is that says "Tap to take a photo, long press to load from gallery", and set visibility to "GONE" at the start of the onActivityResult if statements.
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Picasso.with(NewCatActivity.this)
                    .load("file:"+mCurrentPhotoPath)
                    .resize(300,300)
                    .centerCrop()
                    .into(mPhoto);

            // When an Image is picked
        } else if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                && null != data) {
            // Note: If image is an older image being selected via Google Photos, the image will not
            // be loaded because it has to be downloaded first.
            Uri selectedImageUri = data.getData();
            mCurrentPhotoPath = selectedImageUri.getPath();
            Picasso.with(NewCatActivity.this)
                    .load("file:" + mCurrentPhotoPath)
                    .resize(300, 300)
                    .centerCrop()
                    .into(mPhoto);
        }
    }

    //UPDATED!
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

    private void getLocation() {

    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        setResult(RESULT_OK, takePictureIntent);

        //TODO: Fix database leak.
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            //TODO: Write code that deletes this image file if the camera is cancelled.
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d("DISPATCHTAKEPICTURE...", "Error: "+ex);
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                //TODO: Get GPS location from photo and save to COL_IMG via EXIF data.
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
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


    private String getLocationFromImage (String file){
        String exif="Exif: " + file;
        try {
            ExifInterface exifInterface = new ExifInterface(file);
// TODO: Look at this resource: http://stackoverflow.com/questions/15403797/how-to-get-the-latititude-and-longitude-of-an-image-in-sdcard-to-my-application
//            exif += "\nIMAGE_LENGTH: " + exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
//            exif += "\nIMAGE_WIDTH: " + exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
//            exif += "\n DATETIME: " + exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
//            exif += "\n TAG_MAKE: " + exifInterface.getAttribute(ExifInterface.TAG_MAKE);
//            exif += "\n TAG_MODEL: " + exifInterface.getAttribute(ExifInterface.TAG_MODEL);
//            exif += "\n TAG_ORIENTATION: " + exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);
//            exif += "\n TAG_WHITE_BALANCE: " + exifInterface.getAttribute(ExifInterface.TAG_WHITE_BALANCE);
//            exif += "\n TAG_FOCAL_LENGTH: " + exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
//            exif += "\n TAG_FLASH: " + exifInterface.getAttribute(ExifInterface.TAG_FLASH);
//            exif += "\nGPS related:";
            exif += "\n TAG_GPS_DATESTAMP: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);
            exif += "\n TAG_GPS_TIMESTAMP: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
            exif += "\n TAG_GPS_LATITUDE: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            exif += "\n TAG_GPS_LATITUDE_REF: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            exif += "\n TAG_GPS_LONGITUDE: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            exif += "\n TAG_GPS_LONGITUDE_REF: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
            exif += "\n TAG_GPS_PROCESSING_METHOD: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);


        } catch (IOException e) {
            e.printStackTrace();
            Log.d("GETLOCATIONFROMIMAGE", "Error: "+ e.toString());
        }

        return exif;
    }

    private boolean hasCamera(){
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    private void getAddressString() {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public final class Constants {
        public static final int SUCCESS_RESULT = 0;
        public static final int FAILURE_RESULT = 1;
        public static final String PACKAGE_NAME =
                "com.google.android.gms.location.sample.locationaddress";
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        public static final String RESULT_DATA_KEY = PACKAGE_NAME +
                ".RESULT_DATA_KEY";
        public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
                ".LOCATION_DATA_EXTRA";
    }


    public class FetchAddressIntentService extends IntentService {
        protected ResultReceiver mReceiver;

        /**
         * Creates an IntentService.  Invoked by your subclass's constructor.
         *
         * @param name Used to name the worker thread, important only for debugging.
         */
        public FetchAddressIntentService(String name) {
            super(name);
        }

        private void deliverResultToReceiver(int resultCode, String message) {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.RESULT_DATA_KEY, message);
            mReceiver.send(resultCode, bundle);
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            String errorMessage = "";

            // Get the location passed to this service through an extra.
            Location location = intent.getParcelableExtra(
                    Constants.LOCATION_DATA_EXTRA);

            List<Address> addresses = null;

            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                addresses = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        // In this sample, get just a single address.
                        1);
            } catch (IOException ioException) {
                // Catch network or other I/O problems.
                errorMessage = "Location services are not available";
                Log.e(TAG, errorMessage, ioException);
            } catch (IllegalArgumentException illegalArgumentException) {
                // Catch invalid latitude or longitude values.
                errorMessage = "Invalid lattitude or longitude.";
                Log.e(TAG, errorMessage + ". " +
                        "Latitude = " + location.getLatitude() +
                        ", Longitude = " +
                        location.getLongitude(), illegalArgumentException);
            }

            // Handle case where no address was found.
            if (addresses == null || addresses.size()  == 0) {
                if (errorMessage.isEmpty()) {
                    errorMessage = "No address found :(";
                    Log.e(TAG, errorMessage);
                }
                deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
            } else {
                Address address = addresses.get(0);
                ArrayList<String> addressFragments = new ArrayList<String>();

                // Fetch the address lines using getAddressLine,
                // join them, and send them to the thread.
                for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    addressFragments.add(address.getAddressLine(i));
                }
                Log.i(TAG, "Address found");
                deliverResultToReceiver(Constants.SUCCESS_RESULT,
                        TextUtils.join(System.getProperty("line.separator"),
                                addressFragments));
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
                    "Sharing "+mEditCatName.getText().toString()));
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
                    //TODO: Get location from image.
                    "*Your location here*",
                    mCurrentPhotoPath);
            Toast.makeText(NewCatActivity.this, "Cat saved.", Toast.LENGTH_SHORT).show();
        } catch (Exception e){
            Toast.makeText(NewCatActivity.this,
                    "There was a problem saving your cat data :(",
                    Toast.LENGTH_SHORT).show();
        }
        Intent backToMainIntent = new Intent(NewCatActivity.this, MainActivity.class);
        startActivity(backToMainIntent);
    }

}