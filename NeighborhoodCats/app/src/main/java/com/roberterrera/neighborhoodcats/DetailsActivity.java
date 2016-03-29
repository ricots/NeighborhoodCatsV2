package com.roberterrera.neighborhoodcats;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
    private Tracker mTracker;
    private Realm realm;
    private RealmConfiguration realmConfig;
    RealmAsyncTask transaction;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get intent from MainActivity list via the cat's id.
        final int id = getIntent().getIntExtra("id", -1);
        final CatsSQLiteOpenHelper helper = CatsSQLiteOpenHelper.getInstance(DetailsActivity.this);

        // TODO: Set up layout with text that is editable instead of EditTexts.
        mCatName = (TextView) findViewById(R.id.textView_details_newname);
        mCatDesc = (TextView) findViewById(R.id.textView_details_newdesc);
        mFoundAt = (TextView) findViewById(R.id.textView_details_found);
        mCatLocation = (TextView) findViewById(R.id.textView_details_newlocation);
        mPhoto = (ImageView) findViewById(R.id.imageView_details_newimage);
        mEditCatDesc = (EditText) findViewById(R.id.editText_details_newdesc);
        mEditCatName = (EditText) findViewById(R.id.editText_details_newname);

        // Set activity title
        if (helper.getCatNameByID(id) != null) {
            setTitle(helper.getCatNameByID(id));
        } else {
            setTitle("Cat Details");
        }

        mEditCatName.setText(helper.getCatNameByID(id));
        mEditCatDesc.setText(helper.getCatDescByID(id));
        mCatLocation.setText(helper.getCatLocByID(id));
        Picasso.with(DetailsActivity.this).load(helper.getCatPhotoByID(id)).into(mPhoto);

        Button updateButton = (Button) findViewById(R.id.button_update);


        //TODO: Finish setting up DetailsActivity.
    }
}
