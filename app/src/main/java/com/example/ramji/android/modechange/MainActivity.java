package com.example.ramji.android.modechange;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.ramji.android.modechange.provider.PlaceContract;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "MainActivity";
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 111;
    private static final int PLACE_PICKER_REQUEST = 1111;

    // Member variables
    private PlaceListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private CheckBox locationPermissionCB;
    private Button addButton;
    private GoogleApiClient mClient;

    /**
     * Called when the activity is starting
     *
     * @param savedInstanceState The Bundle that contains the data supplied in onSaveInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.places_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new PlaceListAdapter(this,null);
        mRecyclerView.setAdapter(mAdapter);

        //Creating GoogleApiClient with the location services API and GEO_DATA_API

        mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this,this)
                .build();
    }

    /**
     * when API gets connected
     * @param bundle
     */

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        refreshPlacesData();
        Log.i(TAG,"API Client Connection Successful");
    }

    /**
     * When API gets suspended
     * @param i
     */

    @Override
    public void onConnectionSuspended(int i) {

        Log.i(TAG,"API Client Connection Suspended");

    }

    /**
     * When API Client connection failed
     * @param connectionResult
     */

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG,"API Client Connection Failed");

    }

    public void refreshPlacesData(){

        Log.i(TAG,"refreshPlacesData");

        Uri uri = PlaceContract.PlaceEntry.CONTENT_URI;
        Log.i(TAG,"uri: " + uri);
        Cursor data = getContentResolver().query(uri,null,null,null,null);
        if (data == null || data.getCount() == 0) {
            Log.i(TAG,"Data == null ");
            return;
        }
        List<String> guids = new ArrayList<String>();
        while (data.moveToNext()){
            guids.add(data.getString(data.getColumnIndex(PlaceContract.PlaceEntry.COLUMN_PLACE_ID)));
        }

        //Modify the adapter to use placebuffer as the data source
        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mClient,
                guids.toArray(new String[guids.size()]));
        Log.i(TAG,"placeResult2");
        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                mAdapter.swapPlaces(places);
            }
        });
    }

    /**
     * Button click event handler to handle clicking the "Add new location" button
     * @param view
     */

    public void onAddPlaceButtonClicked(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, getString(R.string.need_location_permission_message), Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, getString(R.string.location_permissions_granted_message), Toast.LENGTH_SHORT).show();


        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            Intent intent = builder.build(this);
            startActivityForResult(intent,PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            Log.e(TAG,String.format("GooglePlay Sevices not available: %s", e.getMessage()));
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e(TAG,String.format("GooglePlay Sevices not available: %s", e.getMessage()));
        }catch (Exception e){
            Log.e(TAG,String.format("PlacePicker Exception: %s", e.getMessage()));
        }

    }

    /**
     * Called when the place picker activity returns back with a selected place(or after cancelling)
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK){
            Place place = PlacePicker.getPlace(this,data);
            if (place == null){
                Log.i(TAG,"No place selected");
                return;
            }

            //Extract the information from the API
            String placeName = place.getName().toString();
            String placeAddress = place.getAddress().toString();
            String placeID = place.getId();

            //Insert the new place into database
            ContentValues contentValues = new ContentValues();
            contentValues.put(PlaceContract.PlaceEntry.COLUMN_PLACE_ID,placeID);
            getContentResolver().insert(PlaceContract.PlaceEntry.CONTENT_URI,contentValues);

            // Get live data information
            refreshPlacesData();
        }
}

    @Override
    protected void onResume() {
        super.onResume();

        //Initiliasing location permission checkbox

        locationPermissionCB = (CheckBox) findViewById(R.id.location_permission_checkbox);
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            locationPermissionCB.setChecked(false);
        }else{
            locationPermissionCB.setChecked(true);
            locationPermissionCB.setEnabled(false);
        }

    }

    public void onLocationPermissionClicked(View view) {

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_REQUEST_FINE_LOCATION);

    }
}
