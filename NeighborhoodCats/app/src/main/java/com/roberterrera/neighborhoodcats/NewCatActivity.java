package com.roberterrera.neighborhoodcats;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.roberterrera.neighborhoodcats.models.AnalyticsApplication;
import com.roberterrera.neighborhoodcats.localdata.CatsSQLiteOpenHelper;
import com.roberterrera.neighborhoodcats.localdata.DBAssetHelper;
import com.roberterrera.neighborhoodcats.models.Cat;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmConfiguration;

public class NewCatActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private ImageView mPhoto;
    private EditText mEditCatName, mEditCatDesc;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_TAKE_PHOTO = 2;
    private static final int RESULT_LOAD_IMG = 3;
    String[] perms = {"android.permission.CAMERA"};
    int permsRequestCode = 200;

    private Realm realm;
    private RealmConfiguration realmConfig;
    private long nextKey;
    public static AtomicLong primaryKeyValue;


    private String mCurrentPhotoPath;
    private String imgDecodableString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_cat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("New Cat!");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView catName = (TextView) findViewById(R.id.textView_newname);
        TextView catDesc = (TextView) findViewById(R.id.textView_newdesc);
        TextView foundAt = (TextView) findViewById(R.id.textView_found);
        TextView catLocation = (TextView) findViewById(R.id.textView_newlocation);
        mPhoto = (ImageView) findViewById(R.id.imageView_newimage);
        mEditCatDesc = (EditText) findViewById(R.id.editText_newdesc);
        mEditCatName = (EditText) findViewById(R.id.editText_newname);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(perms, permsRequestCode);
        }

        dispatchTakePictureIntent();
//        getLocation();

        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        mPhoto.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Create intent to Open Image applications like Gallery, Google Photos
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // Start the Intent
                startActivityForResult(galleryIntent, RESULT_LOAD_IMG);

                return true;
            }
        });

        Button saveButton = (Button) findViewById(R.id.button_save);
        if (saveButton != null) {
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(NewCatActivity.this, "Save button tapped", Toast.LENGTH_SHORT).show();

                            DBAssetHelper dbSetup = new DBAssetHelper(NewCatActivity.this);
                            dbSetup.getWritableDatabase();

                            // Save cat to database.
                            CatsSQLiteOpenHelper helper = CatsSQLiteOpenHelper.getInstance(NewCatActivity.this);
                            try {
                                helper.insert(
                                        mEditCatName.getText().toString(),
                                        mEditCatDesc.getText().toString(),
                                        //TODO: Get location.
//                                        String.valueOf(mTracker));
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
            });
        }
//      int permissionCheck = ContextCompat.checkSelfPermission(NewCatActivity.this,
//          Manifest.permission.CAMERA);

    }

//  public static int checkSelfPermission(Context context, String permission){
//    if () {
//      return int PERMISSION_GRANTED;
//    } else {
//      return PERMISSION_DENIED;
//    }
//  }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){
        switch(permsRequestCode){
            case 200:
                boolean cameraAccepted = grantResults[0]== PackageManager.PERMISSION_GRANTED;
                break;
        }

    }

    private void getLocation() {
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* AppCompatActivity */,
                        this /* OnConnectionFailedListener */)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .build();

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        Tracker mTracker = application.getDefaultTracker();
        // Build and send an Event.
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("category")
                .setAction("clicked button")
                .setLabel("clicker")
                .build());
//        mCatLocation.setText(String.valueOf(mTracker));
    }

    // Go to the camera.
    private void dispatchTakePictureIntent() {
        if (hasCamera() && hasPermissionInManifest(this, CAMERA_SERVICE)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                    Log.d("NEWCATACTIVITY", "Photo file created");

                } catch (IOException ex) {
                    Toast.makeText(NewCatActivity.this, "Unable to launch the camera :(", Toast.LENGTH_SHORT).show();
                    Log.d("NEWCATACTIVITY", "Error: " + String.valueOf(ex));
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                } else {
                    Toast.makeText(NewCatActivity.this, "Could not save photo :(", Toast.LENGTH_SHORT).show();
                    Log.d("NEWCATACTIVITY", "photoFile == null");
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            setPic(); // Manage memory by adjusting the photo.
            CropSquareTransformation cropSquareTransformation = new CropSquareTransformation();
//            mPhoto.setImageBitmap(cropSquareTransformation.transform(imageBitmap));
            mPhoto.setImageBitmap(imageBitmap);
        }
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                if (cursor != null) {
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imgDecodableString = cursor.getString(columnIndex);
                    cursor.close();
                }
                ImageView imgView = (ImageView) findViewById(R.id.imageView_newimage);
                // Set the Image in ImageView after decoding the String
                if (imgView != null) {
                    imgView.setImageBitmap(BitmapFactory
                            .decodeFile(imgDecodableString));
                }

                //TODO: Get GPS location from photo and save to COL_IMG.
                // If the photo has GPS EXIF data, store that in the COL_IMG column. Else use current location of device.

            } else {
                Toast.makeText(this, "You haven't picked an image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }
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

        }
        return false;
    }


    // Save image to a file in the public pictures dir.
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        galleryAddPic(); // Add image to device gallery.
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
        Log.d("GALLERYADDPIC", "File path: "+mCurrentPhotoPath);
    }

    public class CropSquareTransformation implements Transformation {
        @Override public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;
            Bitmap result = Bitmap.createBitmap(source, x, y, size, size);
            if (result != source) {
                source.recycle();
            }
            return result;
        }

        @Override public String key() { return "square()"; }
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = mPhoto.getWidth();
        int targetH = mPhoto.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mPhoto.setImageBitmap(bitmap);
    }

    private boolean hasCamera(){
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}