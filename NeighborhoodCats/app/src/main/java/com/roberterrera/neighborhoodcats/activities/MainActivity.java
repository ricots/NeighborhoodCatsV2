package com.roberterrera.neighborhoodcats.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.roberterrera.neighborhoodcats.R;
import com.roberterrera.neighborhoodcats.cardview.RecyclerViewAdapter;
import com.roberterrera.neighborhoodcats.cardview.SwipeableRecyclerViewTouchListener;
import com.roberterrera.neighborhoodcats.models.AnalyticsApplication;
import com.roberterrera.neighborhoodcats.models.Cat;
import com.roberterrera.neighborhoodcats.sqldatabase.CatsSQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private String[] locationPerms = {"android.permission.ACCESS_COURSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"};
    private String[] cameraPerms = {"android.permission.CAMERA"};
    private String[] storagePerms = {"android.permission.WRITE_EXTERNAL_STORAGE"};
    private String mCurrentPhotoPath;
    private final int locationRequestCode = 200;
    private final int cameraRequestCode = 201;
    private final int storageRequestCode = 202;

    private List<Cat> catList;
    private TextView instructions;
    private Tracker mTracker;

    //Save the FAB's active status
    //false -> fab = close
    //true -> fab = open
    private boolean FAB_Status = false;
    private FloatingActionButton fab_fromStorage;
    private FloatingActionButton fab_fromCamera;

    //Animations
    private Animation show_fab_fromStorage;
    private Animation hide_fab_fromStorage;
    private Animation show_fab_fromCamera;
    private Animation hide_fab_fromCamera;

    public Cursor mCursor;
    private CatsSQLiteOpenHelper mHelper;

    private RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.mainactivity_title));

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        catList = new ArrayList<>();
        instructions = (TextView)findViewById(R.id.textview_instructions);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab_fromStorage = (FloatingActionButton) findViewById(R.id.fab_fromStorage);
        fab_fromCamera = (FloatingActionButton) findViewById(R.id.fab_fromCamera);

        //Animations
        show_fab_fromStorage = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_fromstorage_show);
        hide_fab_fromStorage = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_fromstorage_hide);
        show_fab_fromCamera = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_fromcamera_show);
        hide_fab_fromCamera = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_fromcamera_hide);


        // Set up a linear layout manager
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.cardList);
        if (mRecyclerView != null) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        }

        // Improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        if (mRecyclerView != null) {
            mRecyclerView.setHasFixedSize(true);

            // specify the recycler view adapter
            mAdapter = new RecyclerViewAdapter(catList, MainActivity.this);
            mRecyclerView.setAdapter(mAdapter);

            // Load the user's list.
            loadCatsList();

            // Enable swipe-to-delete on cards.
            SwipeableRecyclerViewTouchListener swipeTouchListener =
                    new SwipeableRecyclerViewTouchListener(mRecyclerView,
                            new SwipeableRecyclerViewTouchListener.SwipeListener() {

                                public boolean canSwipe(int position) {
                                    return true;
                                }

                                @Override
                                public boolean canSwipeLeft(int position) {
                                    return true;
                                }

                                @Override
                                public boolean canSwipeRight(int position) {
                                    return true;
                                }

                                @Override
                                public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                    for (int position : reverseSortedPositions) {
                                        int id = catList.get(position).getId();

                                        mHelper.deleteCatByID(id);
                                        catList.remove(position);
                                        mAdapter.notifyItemRemoved(position);
                                    }
                                    mAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                    for (int position : reverseSortedPositions) {
                                        int id = catList.get(position).getId();

                                        mHelper.deleteCatByID(id);
                                        catList.remove(position);
                                        mAdapter.notifyItemRemoved(position);
                                    }
                                    mAdapter.notifyDataSetChanged();
                                    if (catList.isEmpty()){
                                        instructions.setVisibility(View.VISIBLE);
                                    }
                                }
                            });

            mRecyclerView.addOnItemTouchListener(swipeTouchListener);
        }

        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout != null) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Determine if the buttons are visible or hidden. Do the opposite of the current state.
                if (FAB_Status == false) {
                    //Display FAB menu
                    expandFAB();
                    FAB_Status = true;
                } else {
                    //Close FAB menu
                    hideFAB();
                    FAB_Status = false;
                }
            }
        });

        fab_fromStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create intent to Open Image applications like Gallery, Google Photos
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                setResult(RESULT_OK, galleryIntent);

                // Start the Intent
                startActivityForResult(galleryIntent, 2);
            }
        });

        fab_fromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCameraPermissions();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // When an Image is picked
        if (requestCode == 2 && resultCode == RESULT_OK
                && intent != null) {

            // Note: If image is an older image being selected via Google Photos, the image will not
            // be loaded because it has to be downloaded first.

            Uri selectedImageUri = null;
            selectedImageUri = intent.getData();
            mCurrentPhotoPath = getPath(selectedImageUri);

            Intent newCatIntent =  new Intent (this, NewCatActivity.class);
            newCatIntent.putExtra("ImagePath", mCurrentPhotoPath);
            startActivity(newCatIntent);
        }
    }

    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri,
                projection, null, null, null);
        if (cursor != null) {
            //HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            //THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else return null;
    }

    private void expandFAB() {

        // Floating Action Button 1
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) fab_fromStorage.getLayoutParams();
        layoutParams.rightMargin += (int) (fab_fromCamera.getWidth() * 1.5);
        layoutParams.bottomMargin += (int) (fab_fromCamera.getHeight() * 1.5);
        fab_fromStorage.setLayoutParams(layoutParams);
        fab_fromStorage.startAnimation(show_fab_fromStorage);
        fab_fromStorage.setClickable(true);

        // Floating Action Button 2
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) fab_fromCamera.getLayoutParams();
        layoutParams2.rightMargin += (int) (fab_fromCamera.getWidth() * 1.7);
        layoutParams2.bottomMargin += (int) (fab_fromCamera.getHeight() * 0.25);
        fab_fromCamera.setLayoutParams(layoutParams2);
        fab_fromCamera.startAnimation(show_fab_fromCamera);
        fab_fromCamera.setClickable(true);
    }

    private void hideFAB() {

        // Floating Action Button 1
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) fab_fromStorage.getLayoutParams();
        layoutParams.rightMargin -= (int) (fab_fromCamera.getWidth() * 1.5);
        layoutParams.bottomMargin -= (int) (fab_fromCamera.getHeight() * 1.5);
        fab_fromStorage.setLayoutParams(layoutParams);
        fab_fromStorage.startAnimation(hide_fab_fromStorage);
        fab_fromStorage.setClickable(false);

        // Floating Action Button 2
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) fab_fromCamera.getLayoutParams();
        layoutParams2.rightMargin -= (int) (fab_fromCamera.getWidth() * 1.7);
        layoutParams2.bottomMargin -= (int) (fab_fromCamera.getHeight() * 0.25);
        fab_fromCamera.setLayoutParams(layoutParams2);
        fab_fromCamera.startAnimation(hide_fab_fromCamera);
        fab_fromCamera.setClickable(false);
    }

    private void loadCatsList(){
        mHelper = new CatsSQLiteOpenHelper(MainActivity.this);
        mHelper.getWritableDatabase();
        mCursor = CatsSQLiteOpenHelper.getInstance(this).getCatsList();

        requestStoragePermissions();

        // Loop through arraylist and add database items to it.
        if (mCursor != null) {

            while (mCursor.moveToNext()) {
                int id = mCursor.getInt(mCursor.getColumnIndex(CatsSQLiteOpenHelper.CAT_ID));
                String name = mHelper.getCatNameByID(id);
                String desc = mHelper.getCatDescByID(id);
                double latitude = mHelper.getCatLatByID(id);
                double longitude = mHelper.getCatLongByID(id);
                String imagePath = mHelper.getCatPhotoByID(id);

                Cat cat = new Cat(id, name, desc, latitude, longitude, imagePath);
                catList.add(cat);
                if (!catList.isEmpty()){
                    instructions.setVisibility(View.GONE);
                }
            }
            mCursor.close();
            mHelper.close();
        }
    }

    private void requestStoragePermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(storagePerms, storageRequestCode);
        }
    }

    private void requestCameraPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(cameraPerms, cameraRequestCode);
        } else if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, cameraPerms, cameraRequestCode);

        } else {
//            requestLocationPermissions();
            Intent newCatIntent = new Intent(MainActivity.this, NewCatActivity.class);
            startActivity(newCatIntent);
        }
    }

    private void requestLocationPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(locationPerms, locationRequestCode);
        } else if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, locationPerms, locationRequestCode);
        } else {
            Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(mapIntent);
        }
    }

    // Check permissions
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (permsRequestCode) {
            case locationRequestCode:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
                    startActivity(mapIntent);
                }
                break;

            case cameraRequestCode:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Intent newCatIntent = new Intent(MainActivity.this, NewCatActivity.class);
                    startActivity(newCatIntent);

                }
                break;
            case storageRequestCode:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {}
                break;
            default:
                super.onRequestPermissionsResult(permsRequestCode, permissions, grantResults);
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

        if (id == R.id.menu_map) {


            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                requestLocationPermissions();
                Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(mapIntent);
            } else {
                Toast.makeText(MainActivity.this, "Cat Map unavailable without an internet connection.", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("Cat List");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        mAdapter.notifyDataSetChanged();
        Log.d("onResume", "Cat list refreshed");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}
