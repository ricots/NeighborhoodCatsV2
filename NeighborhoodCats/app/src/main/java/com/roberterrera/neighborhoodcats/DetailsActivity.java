package com.roberterrera.neighborhoodcats;

import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.analytics.Tracker;
import com.roberterrera.neighborhoodcats.localdata.CatsSQLiteOpenHelper;
import com.roberterrera.neighborhoodcats.models.Cat;
import com.squareup.picasso.Picasso;

import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmConfiguration;

public class DetailsActivity extends AppCompatActivity {

    private TextView mCatName, mCatDesc, mFoundAt, mCatLocation;
    private ImageView mPhoto;
    private EditText mEditCatName, mEditCatDesc;
    private CatsSQLiteOpenHelper helper;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_details);
      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);

      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      // TODO: Set up layout with text that is editable instead of EditTexts.
      mCatName = (TextView) findViewById(R.id.textView_details_newname);
      mCatDesc = (TextView) findViewById(R.id.textView_details_newdesc);
      mFoundAt = (TextView) findViewById(R.id.textView_details_found);
      mCatLocation = (TextView) findViewById(R.id.textView_details_newlocation);
      mPhoto = (ImageView) findViewById(R.id.imageView_details_newimage);
      mEditCatDesc = (EditText) findViewById(R.id.editText_details_newdesc);
      mEditCatName = (EditText) findViewById(R.id.editText_details_newname);
      Button updateButton = (Button) findViewById(R.id.button_update);

      LoadCatAsyncTask loadCatAsyncTask = new LoadCatAsyncTask();
      loadCatAsyncTask.execute();



    }

    private class LoadCatAsyncTask extends AsyncTask<Void, Void, Void>{
      @Override
      protected Void doInBackground(Void... params) {
        // Get intent from MainActivity list via the cat's id.
        id = getIntent().getIntExtra("id", -1);
        helper = CatsSQLiteOpenHelper.getInstance(DetailsActivity.this);

        return null;
      }

      @Override
      protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        // Set activity title
        if (helper.getCatNameByID(id) != null) {
          setTitle(helper.getCatNameByID(id));
        } else {
          setTitle("Cat Details");
        }

        mEditCatName.setText(helper.getCatNameByID(id));
        mEditCatDesc.setText(helper.getCatDescByID(id));
        mCatLocation.setText(helper.getCatLocByID(id));

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        //        Picasso.with(DetailsActivity.this).load(helper.getCatPhotoByID(id)).into(mPhoto);
        Picasso.with(DetailsActivity.this)
            .load(helper.getCatPhotoByID(id))
            .resize(width, height)
            .centerCrop()
            .into(mPhoto);
      }
    }




        //TODO: Finish setting up DetailsActivity.
}
