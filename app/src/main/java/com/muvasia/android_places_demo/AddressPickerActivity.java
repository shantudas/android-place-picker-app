package com.muvasia.android_places_demo;

import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class AddressPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = AddressPickerActivity.class.getSimpleName();
    private GoogleMap mMap;
    private PlacesClient mPlacesClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(23.8103, 90.4125);
    private static final int DEFAULT_ZOOM = 15;
    private boolean mLocationPermissionGranted = true;
    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private static final String ADDRESS_REQUESTED_KEY = "address-request-pending";
    private static final String LOCATION_ADDRESS_KEY = "location-address";
    private NearbyPlace nearbyPlace;
    private ArrayList<NearbyPlace> nearbyPlaceArrayList;
    private RecyclerView recyclerViewNearByPlaces;
    private NearbyPlacesAdapter adapter;
    /**
     * Receiver registered with this activity to get the response from FetchAddressIntentService.
     */
    private AddressResultReceiver mResultReceiver;

    /**
     * Tracks whether the user has requested an address. Becomes true when the user requests an
     * address and false when the address (or an error message) is delivered.
     */
    private boolean mAddressRequested;

    private String mAddressOutput;
    private TextView tvAddress;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_picker);

        initialization();

        // Set defaults, then update using values stored in the Bundle.
        mAddressRequested = false;
        mAddressOutput="";

        updateValuesFromBundle(savedInstanceState);

        updateUIWidgets();

    }

    private void initialization() {
        nearbyPlaceArrayList = new ArrayList<>();
        Toolbar toolbar=findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        recyclerViewNearByPlaces = findViewById(R.id.recyclerViewNearByPlaces);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewNearByPlaces.setLayoutManager(layoutManager);
        adapter = new NearbyPlacesAdapter(this, nearbyPlaceArrayList);
        recyclerViewNearByPlaces.setAdapter(adapter);


        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        mPlacesClient = Places.createClient(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(AddressPickerActivity.this);

        tvAddress = findViewById(R.id.tvAddress);
        mProgressBar=findViewById(R.id.mProgressBar);
        mResultReceiver = new AddressResultReceiver(new Handler());

    }

    /**
     * Updates fields based on data stored in the bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Check savedInstanceState to see if the address was previously requested.
            if (savedInstanceState.keySet().contains(ADDRESS_REQUESTED_KEY)) {
                mAddressRequested = savedInstanceState.getBoolean(ADDRESS_REQUESTED_KEY);
            }
            // Check savedInstanceState to see if the location address string was previously found
            // and stored in the Bundle. If it was found, display the address string in the UI.
            if (savedInstanceState.keySet().contains(LOCATION_ADDRESS_KEY)) {
                mAddressOutput = savedInstanceState.getString(LOCATION_ADDRESS_KEY);
                displayAddressOutput();
            }
        }
    }

    /**
     * Toggles the visibility of the progress bar. Enables or disables the Fetch Address button.
     */
    private void updateUIWidgets() {
        if (mAddressRequested) {
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
        } else {
            mProgressBar.setVisibility(ProgressBar.GONE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getDeviceLocation();

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                setLoading(true);
                //get latlng at the center by calling
                LatLng midLatLng = mMap.getCameraPosition().target;
                Log.d(TAG, "myLocation"+ mLastKnownLocation.getLatitude()+" "+mLastKnownLocation.getLongitude());
                Log.d(TAG, "onCameraIdle"+ midLatLng.latitude+" "+midLatLng.longitude);

                if (midLatLng != null) {
                    Location targetLocation = new Location("");//provider name is unnecessary
                    targetLocation.setLatitude(midLatLng.latitude);
                    targetLocation.setLongitude(midLatLng.longitude);
                    startIntentService(targetLocation);
                    return;
                }
                // If we have not yet retrieved the user location, we process the user's request by setting
                // mAddressRequested to true. As far as the user is concerned, pressing the Fetch Address button
                // immediately kicks off the process of getting the address.
                mAddressRequested = true;
            }
        });

        /*mMap.addMarker(new MarkerOptions()
                .position(new LatLng(mLastKnownLocation.getLatitude(),
                        mLastKnownLocation.getLongitude())));*/

        mMap.setMyLocationEnabled(true);
    }


    private void setLoading(boolean loading) {
        findViewById(R.id.mProgressBar).setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(AddressPickerActivity.this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            Log.d(TAG, "Latitude: " + mLastKnownLocation.getLatitude());
                            Log.d(TAG, "Longitude: " + mLastKnownLocation.getLongitude());

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));



                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());

                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                        }

                        getCurrentPlaceLikelihoods();


                        if (mLastKnownLocation != null) {
                            startIntentService(mLastKnownLocation);
                            return;
                        }
                        // If we have not yet retrieved the user location, we process the user's request by setting
                        // mAddressRequested to true. As far as the user is concerned, pressing the Fetch Address button
                        // immediately kicks off the process of getting the address.
                        mAddressRequested = true;



                    }
                });
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void getCurrentPlaceLikelihoods() {
        // Use fields to define the data types to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

        // Get the likely places - that is, the businesses and other points of interest that
        // are the best match for the device's current location.
        @SuppressWarnings("MissingPermission") final FindCurrentPlaceRequest request =
                FindCurrentPlaceRequest.builder(placeFields).build();
        Task<FindCurrentPlaceResponse> placeResponse = mPlacesClient.findCurrentPlace(request);
        placeResponse.addOnCompleteListener(this,
                task -> {
                    if (task.isSuccessful()) {
                        FindCurrentPlaceResponse response = task.getResult();
                        // Set the count, handling cases where less than 5 entries are returned.
                        int count;
                        if (response.getPlaceLikelihoods().size() < M_MAX_ENTRIES) {
                            count = response.getPlaceLikelihoods().size();
                        } else {
                            count = M_MAX_ENTRIES;
                        }

                        int i = 0;


                        for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                            Place currPlace = placeLikelihood.getPlace();
                            nearbyPlace = new NearbyPlace();

                            nearbyPlace.setName(currPlace.getName());
                            nearbyPlace.setAddress(currPlace.getAddress());
                            nearbyPlace.setLatitude(currPlace.getLatLng().latitude);
                            nearbyPlace.setLongitude(currPlace.getLatLng().longitude);

                            nearbyPlaceArrayList.add(nearbyPlace);
                            Log.i(TAG, nearbyPlace.toString());


                            i++;
                            if (i > (count - 1)) {
                                break;
                            }
                        }

                        adapter.notifyDataSetChanged();
                        // COMMENTED OUT UNTIL WE DEFINE THE METHOD
                        // Populate the ListView
                        // fillPlacesList();
                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                        }
                    }
                }
        );
    }

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    private void startIntentService(Location location) {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(this, FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        startService(intent);
    }

    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    private class AddressResultReceiver extends ResultReceiver {
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         * Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            displayAddressOutput();

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Log.d(TAG, "address found");
            }

            mAddressRequested = false;

            setLoading(false);

        }
    }

    private void displayAddressOutput() {
        tvAddress.setText(mAddressOutput);
    }
}
