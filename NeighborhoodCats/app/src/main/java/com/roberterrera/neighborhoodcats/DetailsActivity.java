package com.roberterrera.neighborhoodcats;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.roberterrera.neighborhoodcats.localdata.CatsSQLiteOpenHelper;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final int id = getIntent().getIntExtra("id", -1);

        final CatsSQLiteOpenHelper helper = CatsSQLiteOpenHelper.getInstance(DetailsActivity.this);

        if (helper.getCatNameByID(id) != null) {
            setTitle(helper.getCatNameByID(id));
        } else {
            setTitle("Details");
        }
    }

    //TODO: Finish setting up DetailsActivity.
}
