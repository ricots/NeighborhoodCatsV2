package com.roberterrera.neighborhoodcats.activities;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.roberterrera.neighborhoodcats.R;
import com.roberterrera.neighborhoodcats.sqldatabase.CatsSQLiteOpenHelper;
import com.roberterrera.neighborhoodcats.models.AnalyticsApplication;
import com.roberterrera.neighborhoodcats.models.Cat;
import com.roberterrera.neighborhoodcats.cardview.RecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public Cursor mCursor;
//    private CursorAdapter mCursorAdapter;
//    private ListView mListView;
    private List<Cat> catList;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView mCatName;
    private ImageView mCatThumbnail;
    private Tracker mTracker;
    private CatsSQLiteOpenHelper mHelper;
    private String[] perms = {"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.ACCESS_COURSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"};
    private int permsRequestCode = 200;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(perms, permsRequestCode);
        }

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();


        catList = new ArrayList<>();

        // use a linear layout manager
        mRecyclerView = (RecyclerView) findViewById(R.id.cardList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(false);

        // specify the recycler view adapter
        mAdapter = new RecyclerViewAdapter(catList, MainActivity.this);

        // Load the user's list.
        loadCatsList();

        // Get item details and display them in DetailsActivity.
//        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//          @Override
//          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//              Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
//              mCursor.moveToPosition(position);
//              mHelper.getReadableDatabase();
//              intent.putExtra("id", mCursor.getInt(mCursor.getColumnIndex(CatsSQLiteOpenHelper.CAT_ID)));
//              mTracker.send(new HitBuilders.EventBuilder()
//                      .setCategory("Action")
//                      .setAction("View cat details from list")
//                      .build());
//            startActivity(intent);
//          }
//        });
//
//        // Delete a list item.
//        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                mTracker.send(new HitBuilders.EventBuilder()
//                        .setCategory("Action")
//                        .setAction("Delete Cat")
//                        .build());
//                mHelper.deleteCatByID(mCursor.getInt(mCursor.getColumnIndex(CatsSQLiteOpenHelper.CAT_ID)));
//                mCursor = CatsSQLiteOpenHelper.getInstance(MainActivity.this).getCatsList();
//                mCursorAdapter.swapCursor(mCursor);
//              return true;
//            }
//        });

//        mCursor = CatsSQLiteOpenHelper.getInstance(MainActivity.this).getCatsList();
//        mCursorAdapter = new CursorAdapter(MainActivity.this, mCursor, 0) {
//            @Override
//            public View newView(Context context, Cursor cursor, ViewGroup parent) {
//                return LayoutInflater.from(context).inflate(R.layout.list_item_layout, parent, false);
//            }
//
//            @Override
//            public void bindView(View view, Context context, Cursor cursor) {
//
//                // Create helper object and make the database available to be read.
//                mHelper = new CatsSQLiteOpenHelper(MainActivity.this);
//                mHelper.getReadableDatabase();
//
//                mCatName = (TextView) view.findViewById(R.id.textview_catname_list);
//                mCatName.setText( cursor.getString(cursor.getColumnIndex(CatsSQLiteOpenHelper.CAT_NAME)) );
//
//                // Load image file path into thumbnail
//                mCatThumbnail = (ImageView) view.findViewById(R.id.imageview_catthumbnail);
//                Display display = getWindowManager().getDefaultDisplay();
//                Point size = new Point();
//                display.getSize(size);
//                int width = size.x;
//                int height = size.y;
//                Picasso.with(MainActivity.this)
//                    .load("file:"+cursor.getString(cursor.getColumnIndex(CatsSQLiteOpenHelper.CAT_IMG)))
//                    .resize(width, height)
//                    .centerCrop()
//                    .into(mCatThumbnail);
//
//                // Log the filepath
//                Log.d("CURSORADAPTER", "Name: "+cursor.getString(cursor.getColumnIndex(CatsSQLiteOpenHelper.CAT_NAME))+", CAT_IMG: "+cursor.getString(cursor.getColumnIndex(CatsSQLiteOpenHelper.CAT_IMG)));
//                Log.d("MAINACTIVITY", "Cat list loaded.");
//            }
//        };

//        mListView = (ListView) findViewById(R.id.listview_cats);
//        if (mListView != null) {
//            mListView.setAdapter(mCursorAdapter);
//        }
        handleIntent(getIntent());

        // Get item details and display them in DetailsActivity.
//        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//          @Override
//          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//              Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
//              mCursor.moveToPosition(position);
//              mHelper.getReadableDatabase();
//              intent.putExtra("id", mCursor.getInt(mCursor.getColumnIndex(CatsSQLiteOpenHelper.CAT_ID)));
//              mTracker.send(new HitBuilders.EventBuilder()
//                      .setCategory("Action")
//                      .setAction("View cat details from list")
//                      .build());
//            startActivity(intent);
//          }
//        });
//
//        // Delete a list item.
//        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                mTracker.send(new HitBuilders.EventBuilder()
//                        .setCategory("Action")
//                        .setAction("Delete Cat")
//                        .build());
//                mHelper.deleteCatByID(mCursor.getInt(mCursor.getColumnIndex(CatsSQLiteOpenHelper.CAT_ID)));
//                mCursor = CatsSQLiteOpenHelper.getInstance(MainActivity.this).getCatsList();
//                mCursorAdapter.swapCursor(mCursor);
//              return true;
//            }
//        });
//
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    public void handleIntent(Intent intent){

        if (Intent.ACTION_SEARCH.equals( intent.getAction() )){
            String query = intent.getStringExtra(SearchManager.QUERY);
            mCursor = CatsSQLiteOpenHelper.getInstance(this).searchCats(query);
//            mCursorAdapter.swapCursor(mCursor);
            mAdapter.notifyDataSetChanged();
            Toast.makeText(MainActivity.this, "Cat List refreshed", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void loadCatsList(){
        mHelper = new CatsSQLiteOpenHelper(MainActivity.this);
        mHelper.getReadableDatabase();
        mCursor = CatsSQLiteOpenHelper.getInstance(this).getCatsList();

        // Loop through arraylist and add database items to it.
        while (mCursor.moveToNext()){
            int id = mCursor.getInt(mCursor.getColumnIndex(CatsSQLiteOpenHelper.CAT_ID));
            String name = mHelper.getCatNameByID(id);
            String desc = mHelper.getCatDescByID(id);
            String latitude = String.valueOf(mHelper.getCatLatByID(id));
            String longitude = String.valueOf(mHelper.getCatLatByID(id));
            String imagePath = mHelper.getCatPhotoByID(id);

            Cat cat = new Cat(id, name, desc, latitude, longitude, imagePath);
            catList.add(cat);
        }

        // Check that arraylist isn't empty.
        if (!(catList.size()<1)){
            mRecyclerView.setAdapter(mAdapter);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Setup for the search action.
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();

        SearchableInfo info = searchManager.getSearchableInfo( getComponentName() );
        searchView.setSearchableInfo(info);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_search) {
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("Search")
                    .build());
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
        if (id == R.id.nav_map) {
            // Handle the map intent
            Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(mapIntent);
//        } else if (id == R.id.nav_gallery) {


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){
        switch(permsRequestCode){
            case 200:
                boolean permissionAccepted = grantResults[3]== PackageManager.PERMISSION_GRANTED;
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("Cat List");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
//        if (mCursor != null) {
//            mCursorAdapter.swapCursor(mCursor);
//        }
        mAdapter.notifyDataSetChanged();
        Log.d("onResume", "Cat list refreshed");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
