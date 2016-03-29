package com.roberterrera.neighborhoodcats;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Cursor mCursor;
    CursorAdapter mCursorAdapter;
    ListView mListView;
    TextView mCatName;
    ImageView mCatThumbnail;
    CatsSQLiteOpenHelper helper;

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

        Toast.makeText(MainActivity.this, "Cats set to listview", Toast.LENGTH_SHORT).show();

//        GetCatsListAsyncTask getCatsListAsyncTask = new GetCatsListAsyncTask();
//        getCatsListAsyncTask.execute();

        DBAssetHelper dbAssetHelper = new DBAssetHelper(MainActivity.this);
        dbAssetHelper.getReadableDatabase();

        mCursor = CatsSQLiteOpenHelper.getInstance(MainActivity.this).getCatsList();
        mCursorAdapter = new CursorAdapter(MainActivity.this, mCursor, 0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.list_item_layout, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {

                // Create helper object and make the database available to be read.
                CatsSQLiteOpenHelper helper = new CatsSQLiteOpenHelper(MainActivity.this);
                helper.getReadableDatabase();

                mCatName = (TextView) view.findViewById(R.id.textview_catname_list);
                mCatName.setText( cursor.getString(cursor.getColumnIndex(CatsSQLiteOpenHelper.COL_NAME)) );
                // Load image file path into thumbnail
                mCatThumbnail = (ImageView) view.findViewById(R.id.imageview_catthumbnail);
                Picasso.with(MainActivity.this).load(cursor.getString(cursor.getColumnIndex(CatsSQLiteOpenHelper.COL_IMG))).into(mCatThumbnail);
                // Log the filepath //TODO: Why is this logging twice?
                Log.d("CURSORADAPTER", "Name: "+cursor.getString(cursor.getColumnIndex(CatsSQLiteOpenHelper.COL_NAME))+", COL_IMG: "+cursor.getString(cursor.getColumnIndex(CatsSQLiteOpenHelper.COL_IMG)));
            }
        };

        mListView = (ListView) findViewById(R.id.listview_cats);
        mListView.setAdapter(mCursorAdapter);

        // Get item details and display them in DetailsActivity.
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Toast.makeText(MainActivity.this, position+" clicked", Toast.LENGTH_SHORT).show();
//            mCursor = mCursorAdapter.getCursor();
            Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
            mCursor.moveToPosition(position);
            intent.putExtra("id", mCursor.getInt(mCursor.getColumnIndex(CatsSQLiteOpenHelper.COL_ID)));
            startActivity(intent);
          }
        });

        // Delete a list item.
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
              Toast.makeText(MainActivity.this, position+" clicked", Toast.LENGTH_SHORT).show();
                // TODO: Run a getCatID to get the id and pass that into deleteCatByID.
              helper.deleteCatByID(mCursor.getInt(mCursor.getColumnIndex(CatsSQLiteOpenHelper.COL_ID)));
              mCursorAdapter.swapCursor(mCursor);

              return true;
            }
        });

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
        handleIntent(intent);
    }

    public void handleIntent(Intent intent){
//        Toast.makeText(MainActivity.this, "Toast from outside if statement", Toast.LENGTH_SHORT).show();

        if (Intent.ACTION_SEARCH.equals( intent.getAction() )){
            String query = intent.getStringExtra(SearchManager.QUERY);
            Cursor cursor = CatsSQLiteOpenHelper.getInstance(this).searchCats(query);
            mCursorAdapter.swapCursor(cursor);
        }
    }

    /*
    private class GetCatsListAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            DBAssetHelper dbAssetHelper = new DBAssetHelper(MainActivity.this);
            dbAssetHelper.getReadableDatabase();
            mCursor = CatsSQLiteOpenHelper.getInstance(MainActivity.this).getCatsList();

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //TODO: Set up progress bar?
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mCursorAdapter = new CursorAdapter(MainActivity.this, mCursor, 0) {
                @Override
                public View newView(Context context, Cursor cursor, ViewGroup parent) {
                    return LayoutInflater.from(context).inflate(R.layout.list_item_layout, parent, false);
                }

                @Override
                public void bindView(View view, Context context, Cursor cursor) {

                    // Create helper object and make the database available to be read.
                    CatsSQLiteOpenHelper helper = new CatsSQLiteOpenHelper(MainActivity.this);
                    helper.getReadableDatabase();

                    mCatName = (TextView) view.findViewById(R.id.textview_catname_list);
                    mCatName.setText( cursor.getString(cursor.getColumnIndex(CatsSQLiteOpenHelper.COL_NAME)) );
                    // Load image file path into thumbnail
                    mCatThumbnail = (ImageView) view.findViewById(R.id.imageview_catthumbnail);
                    Picasso.with(MainActivity.this).load(CatsSQLiteOpenHelper.COL_IMG).into(mCatThumbnail);
                }
            };

                mListView.setAdapter(mCursorAdapter);
            }
        }
*/

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

    @Override
    protected void onResume() {
        super.onResume();
        if (mCursor != null) {
            mCursorAdapter.swapCursor(mCursor);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
