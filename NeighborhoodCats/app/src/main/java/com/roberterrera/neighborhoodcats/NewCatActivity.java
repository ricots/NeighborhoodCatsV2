package com.roberterrera.neighborhoodcats;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.desmond.squarecamera.CameraActivity;

public class NewCatActivity extends AppCompatActivity {

    TextView mCatName, mCatDesc, mFoundAt, mCatLocation;
    ImageView mPhoto;
    EditText mEditCatName, mEditCatDesc;
    private static final int REQUEST_CAMERA = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_cat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("New Cat!");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCatName = (TextView) findViewById(R.id.textView_newname);
        mCatDesc = (TextView) findViewById(R.id.textView_newdesc);
        mFoundAt = (TextView) findViewById(R.id.textView_found);
        mCatLocation = (TextView) findViewById(R.id.textView_newlocation);
        mPhoto = (ImageView) findViewById(R.id.imageView_newimage);
        mEditCatDesc = (EditText) findViewById(R.id.editText_newdesc);
        mEditCatName = (EditText) findViewById(R.id.editText_newname);


        // Check for camera permission in Marshmallow
        public void requestForCameraPermission(View view) {
            final String permission = Manifest.permission.CAMERA;
            if (ContextCompat.checkSelfPermission(NewCatActivity.this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(NewCatActivity.this, permission)) {
                    // Show permission rationale
                } else {
                    // Handle the result in Activity#onRequestPermissionResult(int, String[], int[])
                    ActivityCompat.requestPermissions(NewCatActivity.this, new String[]{permission}, REQUEST_CAMERA_PERMISSION);
                }
            } else {
                // Start CameraActivity
            }
        }

        // Start CameraActivity
        Intent startCustomCameraIntent = new Intent(this, CameraActivity.class);
        startActivityForResult(startCustomCameraIntent, REQUEST_CAMERA);

        // Receive Uri of saved square photo
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode != RESULT_OK) return;

            if (requestCode == REQUEST_CAMERA) {
                Uri photoUri = data.getData();
            }
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

}
