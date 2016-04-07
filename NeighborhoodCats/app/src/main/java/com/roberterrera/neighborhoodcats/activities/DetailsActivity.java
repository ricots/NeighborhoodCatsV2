package com.roberterrera.neighborhoodcats.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.roberterrera.neighborhoodcats.R;
import com.roberterrera.neighborhoodcats.sqldatabase.CatsSQLiteOpenHelper;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DetailsActivity extends AppCompatActivity {

    private TextView mCatName, mCatDesc, mFoundAt, mCatLocation, mFullCatDesc;
    private ImageView mPhoto;
    private EditText mEditCatName;
    private CatsSQLiteOpenHelper helper;
    private int id;
    private String name, desc, photoPath;
    private double latitude, longitude;
    private String mLatLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_details);
      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);

      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCatDesc = (TextView) findViewById(R.id.textView_details_newdesc);
        mFoundAt = (TextView) findViewById(R.id.textView_details_found);
        mCatLocation = (TextView) findViewById(R.id.textView_details_newlocation);
        mPhoto = (ImageView) findViewById(R.id.imageView_details_newimage);
        mFullCatDesc = (TextView) findViewById(R.id.editText_details_newdesc);

        LoadCatAsyncTask loadCatAsyncTask = new LoadCatAsyncTask();
        loadCatAsyncTask.execute();


    }

    private class LoadCatAsyncTask extends AsyncTask<Void, Void, Void>{
      @Override
      protected Void doInBackground(Void... params) {
        // Get intent from MainActivity list via the cat's id.
          id = getIntent().getIntExtra("id", -1);
          helper = CatsSQLiteOpenHelper.getInstance(DetailsActivity.this);
          helper.getReadableDatabase();
          helper.close();
          name = helper.getCatNameByID(id);
          desc = helper.getCatDescByID(id);
          photoPath = helper.getCatPhotoByID(id);
          latitude = helper.getCatLatByID(id);
          longitude = helper.getCatLongByID(id);
          mLatLong = locationToString();
        return null;
      }

      @Override
      protected void onPostExecute(Void aVoid) {
          super.onPostExecute(aVoid);

          // Set activity title
          if (name != null) {
              setTitle(name);
          } else {
              setTitle("Cat Details");
          }

          mFullCatDesc.setText(desc);

          ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
          final NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

          if (networkInfo != null && networkInfo.isConnected()) {
              showAddress();
          } else {
              mCatLocation.setText(mLatLong);
              Toast.makeText(DetailsActivity.this, "Could not show street address without a connection.", Toast.LENGTH_SHORT).show();
          }


          Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;
              Picasso.with(DetailsActivity.this)
                      .load("file:" + photoPath)
//                      .resize(width, height)
                      .placeholder(R.drawable.ic_pets_black_24dp)
//                      .centerCrop()
                      .into(mPhoto);
      }
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

    public String locationToString() {
        return (String.valueOf(latitude)
                + ", "
                + String.valueOf(longitude));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menu_item_share){
            shareChooser();
        }
        if (id == R.id.menu_edit){
            Intent editIntent = new Intent(DetailsActivity.this, EditActivity.class);
            startActivity(editIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void shareChooser() {
        // specify our test image location
        Uri url = Uri.parse(photoPath);

        // set up an intent to share the image
        Intent share_intent = new Intent();
        share_intent.setAction(Intent.ACTION_SEND);
        share_intent.setType("image/jpg");
        share_intent.putExtra(Intent.EXTRA_STREAM,
                Uri.fromFile(new File(url.toString())));
        share_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        share_intent.putExtra(Intent.EXTRA_SUBJECT,
                "Share this cat!");
        share_intent.putExtra(Intent.EXTRA_TEXT, name + ": " + desc);

        // start the intent
        try {
            startActivity(Intent.createChooser(share_intent,
                    "Sharing "+name));
        } catch (android.content.ActivityNotFoundException ex) {
            (new AlertDialog.Builder(DetailsActivity.this)
                    .setMessage("Share failed")
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                }
                            }).create()).show();
        }
    }
}
