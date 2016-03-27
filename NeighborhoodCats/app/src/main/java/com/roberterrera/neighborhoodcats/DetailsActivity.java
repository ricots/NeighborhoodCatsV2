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
        String catId = getIntent().getStringExtra("cat_id");
        realm = Realm.getDefaultInstance();
        Cat cat = realm.where(Cat.class).equalTo("id", catId).findFirst();

        mCatName = (TextView) findViewById(R.id.textView_details_newname);
        mCatDesc = (TextView) findViewById(R.id.textView_details_newdesc);
        mFoundAt = (TextView) findViewById(R.id.textView_details_found);
        mCatLocation = (TextView) findViewById(R.id.textView_details_newlocation);
        mPhoto = (ImageView) findViewById(R.id.imageView_details_newimage);
        mEditCatDesc = (EditText) findViewById(R.id.editText_details_newdesc);
        mEditCatName = (EditText) findViewById(R.id.editText_details_newname);

        // Set activity title
        if (cat.getName() != null){
            setTitle(cat.getName());
        } else {
            setTitle("Cat Details");
        }

        mEditCatName.setText(cat.getName());
        mEditCatDesc.setText(cat.getDesc());
        mCatLocation.setText(cat.getLocation());
        Picasso.with(DetailsActivity.this).load(cat.getPhoto()).into(mPhoto);

        Button updateButton = (Button) findViewById(R.id.button_update);
//        if (updateButton != null) {
//            updateButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    // Create the Realm configuration
//                    realmConfig = new RealmConfiguration.Builder(DetailsActivity.this).build();
//                    // Open the Realm for the UI thread.
//                    realm = Realm.getInstance(realmConfig);
//
//                    // All writes must be wrapped in a transaction to facilitate safe multi threading
//                    realm.beginTransaction();
//
//                    transaction = realm.executeTransaction(new Realm.Transaction(){
//                        @Override
//                        public void execute(Realm bgRealm) {
//                            // Add a Cat
//                            Cat cat = realm.createObject(Cat.class);
////                            cat.setId(1);
//                            cat.setName(mEditCatName.getText().toString());
//                            cat.setDesc(mEditCatDesc.getText().toString());
//                            cat.setPhoto(mCurrentPhotoPath);
//                            cat.setLocation("*Location feature is to come!*");
//                        }
//                    }, null);
//
//                    // When the transaction is committed, all changes a synced to disk.
//                    realm.commitTransaction();

/*        final CatsSQLiteOpenHelper helper = CatsSQLiteOpenHelper.getInstance(DetailsActivity.this);

        if (helper.getCatNameByID(id) != null) {
            setTitle(helper.getCatNameByID(id));
        } else {
            setTitle("Details");
        }*/
        realm.close();
    }


    //TODO: Finish setting up DetailsActivity.
}
