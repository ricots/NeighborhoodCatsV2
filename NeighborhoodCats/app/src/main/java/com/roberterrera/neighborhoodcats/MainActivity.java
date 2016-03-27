package com.roberterrera.neighborhoodcats;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.roberterrera.neighborhoodcats.localdata.CatsSQLiteOpenHelper;
import com.roberterrera.neighborhoodcats.localdata.DBAssetHelper;
import com.roberterrera.neighborhoodcats.models.Cat;
import com.roberterrera.neighborhoodcats.models.CatAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Cursor mCursor;
    CursorAdapter mCursorAdapter;
    ListView mListView;
    TextView mCatName;
    ImageView mCatThumbnail;
    CatsSQLiteOpenHelper helper;
    CatAdapter mAdapter;

    private Realm realm;
    private RealmConfiguration realmConfig;
    RealmResults<Cat> results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.mainactivity_title));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newCatIntent = new Intent(MainActivity.this, NewCatActivity.class);
                startActivity(newCatIntent);
            }
        });

        /* Drawer setup */
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final Cat cat = new Cat();

        // Create the Realm configuration
        realmConfig = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(realmConfig);
        // Open the Realm for the UI thread.
        realm = Realm.getInstance(realmConfig);
        // All writes must be wrapped in a transaction to facilitate safe multi threading
        realm.beginTransaction();

//        RealmAsyncTask transaction = realm.executeTransaction(new Realm.Transaction(){
//            @Override
//            public void execute(Realm bgRealm) {
                RealmResults<Cat> catResults = realm.where(Cat.class).findAll();
        mAdapter = new CatAdapter(this, R.id.listview_cats, catResults, true);
                Toast.makeText(MainActivity.this, "Cats loaded.", Toast.LENGTH_SHORT).show();

                /*`Ω
                mCatName = (TextView) findViewById(R.id.textview_catname_list);
                String catName = cat.getName();
                // Set name of cat to list item.
                mCatName.setText(catName);
                Log.d("MAINACTIVITY", cat.getName());
                // Load image file path into thumbnail
                Picasso.with(MainActivity.this).load(cat.getPhoto()).into(mCatThumbnail);
                Log.d("MAINACTIVITY", cat.getPhoto());
                Toast.makeText(MainActivity.this, "Cats loaded.", Toast.LENGTH_SHORT).show();

            }
        }, null);
        */

        // When the transaction is committed, all changes a synced to disk.
//        realm.commitTransaction();
        mListView = (ListView) findViewById(R.id.listview_cats);
        mListView.setAdapter(mAdapter);
        Toast.makeText(MainActivity.this, "Cats set to listview", Toast.LENGTH_SHORT).show();

//        GetCatsListAsyncTask getCatsListAsyncTask = new GetCatsListAsyncTask();
//        getCatsListAsyncTask.execute();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Cursor cursor = mCursorAdapter.getCursor();
                Toast.makeText(MainActivity.this, position+" clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
//                cursor.moveToPosition(position);
//                intent.putExtra("id", cursor.getInt(cursor.getColumnIndex(CatsSQLiteOpenHelper.COL_ID)));
                intent.putExtra("cat_id", cat.getId());
                startActivity(intent);
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                helper.deleteCatByID(Integer.parseInt(CatsSQLiteOpenHelper.COL_ID));
//                mCursorAdapter.swapCursor(mCursor);
                results.remove(position);
                realm.commitTransaction();
                mAdapter.notifyDataSetChanged();

                return true;
            }
        });

    }

//    private class GetCatsListAsyncTask extends AsyncTask<Void, Void, Void> {
//
//        @Override
//        protected Void doInBackground(Void... params) {
//
//            results = realm.where(Cat.class).findAll();
//            mAdapter = new CatAdapter(MainActivity.this, R.id.listview_cats, results, true);
//
////            DBAssetHelper dbAssetHelper = new DBAssetHelper(MainActivity.this);
////            dbAssetHelper.getReadableDatabase();
//
//            return null;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            //TODO: Set up progress bar?
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//
//
///*
//            mCursor = CatsSQLiteOpenHelper.getInstance(MainActivity.this).getCatsList();
//            mCursorAdapter = new CursorAdapter(MainActivity.this, mCursor, 0) {
//                @Override
//                public View newView(Context context, Cursor cursor, ViewGroup parent) {
//                    return LayoutInflater.from(context).inflate(R.layout.list_item_layout, parent, false);
//                }
//
//                @Override
//                public void bindView(View view, Context context, Cursor cursor) {
//
//                    mCatName = (TextView) view.findViewById(R.id.textview_catname_list);
//                    String catName = cursor.getString(cursor.getColumnIndex(CatsSQLiteOpenHelper.COL_NAME));
//                    // Set name of cat to list item.
//                    mCatName.setText(catName);
//                    // Load image file path into thumbnail
//                    Picasso.with(MainActivity.this).load(CatsSQLiteOpenHelper.COL_IMG).into(mCatThumbnail);
//                }
//            };
//*/
////            if (mListView != null) {
//                mListView.setAdapter(mAdapter);
////            }
//        }
//    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //TODO: Update these values with settings.
        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

//    public void resizePhoto() {
//        // Create a thumbnail using Picasso.
//        Picasso.with(this)
//                .load(CatsSQLiteOpenHelper.COL_IMG)
//                .resize(50, 50)
//                .centerCrop()
//                .into(mCatThumbnail);
//    }

//    @Override
//    protected void onResume() {
//        super.onResume();
////        if (mCursor != null) {
////            mCursorAdapter.swapCursor(mCursor);
//        mAdapter.notifyDataSetChanged();
////        }
//    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mListView != null) {
            mAdapter.notifyDataSetChanged();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close(); // Remember to close Realm when done.
    }
}
