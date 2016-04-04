package com.roberterrera.neighborhoodcats.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
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

import com.roberterrera.neighborhoodcats.R;
import com.roberterrera.neighborhoodcats.sqldatabase.CatsSQLiteOpenHelper;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.File;

public class DetailsActivity extends AppCompatActivity {

    private TextView mCatName, mCatDesc, mFoundAt, mCatLocation, mFullCatDesc;
    private ImageView mPhoto;
    private EditText mEditCatName;
    private CatsSQLiteOpenHelper helper;
    private int id;
    private String name, desc, photoPath;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_details);
      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);

      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // TODO: Set up layout with text that is editable instead of EditTexts.
//        mCatName = (TextView) findViewById(R.id.textView_details_newname);
        mCatDesc = (TextView) findViewById(R.id.textView_details_newdesc);
        mFoundAt = (TextView) findViewById(R.id.textView_details_found);
        mCatLocation = (TextView) findViewById(R.id.textView_details_newlocation);
        mPhoto = (ImageView) findViewById(R.id.imageView_details_newimage);
        mFullCatDesc = (TextView) findViewById(R.id.editText_details_newdesc);
//        mEditCatName = (EditText) findViewById(R.id.editText_details_newname);

        LoadCatAsyncTask loadCatAsyncTask = new LoadCatAsyncTask();
        loadCatAsyncTask.execute();


    }

    private class LoadCatAsyncTask extends AsyncTask<Void, Void, Void>{
      @Override
      protected Void doInBackground(Void... params) {
        // Get intent from MainActivity list via the cat's id.
          id = getIntent().getIntExtra("id", -1);
          helper = CatsSQLiteOpenHelper.getInstance(DetailsActivity.this);
          name = helper.getCatNameByID(id);
          desc = helper.getCatDescByID(id);
          photoPath = helper.getCatPhotoByID(id);
          latitude = helper.getCatLatByID(id);
          longitude = helper.getCatLongByID(id);
          Log.d("DetailsActivity", "latitude: "+latitude);
          Log.d("DetailsActivity", "longitude: "+longitude);
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

//              mEditCatName.setText(name);
              mFullCatDesc.setText(desc);
              mCatLocation.setText(latitude + ", " + longitude);

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;
              Picasso.with(DetailsActivity.this)
                      .load("file:" + photoPath)
                      .resize(width, height)
                      .placeholder(R.drawable.ic_pets_black_24dp)
                      .centerCrop()
                      .into(mPhoto);
      }
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
        share_intent.putExtra(Intent.EXTRA_TEXT, name+": "+desc);

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
