package com.example.tovisit_anmariya_c0775497_android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final int REQUEST_CODE = 1;
    private static final int RADIUS = 1500;
    private static final String TAG = "MAP";
    private static final long WAIT_TIME = 5L;
    private GoogleMap mMap;
    Location currUserLocation;
    Marker fvt_dest, startL, User;

    Boolean isEditing = false;

    AlertDialog dropdownMenu;

    String s = null;
    MyFavourites p = null;

    DatabaseHelper mDatabase;

    LocationManager locationManager;
    LocationListener locationListener;
    Geocoder geocoder;



    Spinner typeMap, nearby;

    private String place_name;
    private Object[] dataTransfer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        if (!internetCheck()){

            AlertDialog.Builder connection = new AlertDialog.Builder(MapActivity.this);
            connection.setTitle("It seems you dont have internet !! Check Connection");

            connection.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Intent i = new Intent(MapActivity.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);

                }
            });
            connection.create().show();



        }


        mDatabase = new DatabaseHelper(this);



        typeMap = findViewById(R.id.mapType);
        typeMap.setSelection(1);

        nearby = findViewById(R.id.nearByPlaces);
        nearby.setSelection(0);

        typeMap.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mMap.setMapType(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        nearby.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {



                if(position > 0){


                    Boolean isFvt = (p != null);

                    mMap.clear();
                    if(User != null){
                        User.remove();
                    }
                    User = null;

                    if (isFvt){

                        FocusLocation(new LatLng(p.getLat(), p.getLng()));
                        mMap.addMarker(new MarkerOptions().position(new LatLng(p.getLat(), p.getLng()))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                .title(p.getName())).showInfoWindow();

                        addUSerMarker(currUserLocation);


                    }else{

                        FocusHomeMarker(currUserLocation);

                    }
                    // show nearby places
                    String searchPlace = nearby.getSelectedItem().toString();

                    String url = isFvt ? getUrl(p.getLat(), p.getLng(), searchPlace) :
                            getUrl(currUserLocation.getLatitude(), currUserLocation.getLongitude(), searchPlace);


                    Log.i(TAG, "onItemSelected: "+url);
                    dataTransfer = new Object[2];
                    dataTransfer[0] = mMap;
                    dataTransfer[1] = url;
                    ShowNearby getNearByPlaceData = new ShowNearby();
                    getNearByPlaceData.execute(dataTransfer);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        geocoder = new Geocoder(this, Locale.getDefault());
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                if (location != null) {
                    currUserLocation = location;
                    addUSerMarker(currUserLocation);
                }
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
        };



    } // eof


    public boolean internetCheck() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    private String getUrl(double lat, double lng, String nearByPlace){


        StringBuilder placeUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        placeUrl.append("location="+lat+","+lng);
        placeUrl.append("&radius="+RADIUS);
        placeUrl.append("&type="+nearByPlace);
        placeUrl.append("&key="+getString(R.string.google_maps_key));
        return placeUrl.toString();
    }

    private String getDirectionUrl(){

        if(startL == null){
            System.out.println("null value detected");
        }

        Log.i(TAG, "getDirectionUrl: ");
        StringBuilder directionUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        directionUrl.append("origin=" + startL.getPosition().latitude + "," + startL.getPosition().longitude);
        directionUrl.append("&destination=" + fvt_dest.getPosition().latitude + "," + fvt_dest.getPosition().longitude);
        directionUrl.append("&key=" + getString(R.string.api_key_places));
        return directionUrl.toString();

    }


    @SuppressLint("MissingPermission")
    public void startupACtivity(){

        reqLocationUpdate();
        currUserLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        addUSerMarker(currUserLocation);


        Intent i = getIntent();
        isEditing = i.getBooleanExtra("EDIT", false);
        p = (MyFavourites) i.getSerializableExtra("selectedPlace");


        if (p != null) {
            LatLng pos = new LatLng(p.getLat(), p.getLng());

            fvt_dest = mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .title(isEditing ? "Drag To Change" : p.getName()).draggable(isEditing));

            Log.i(TAG, "onMapReady: marker added successfully");

            CameraPosition cameraPosition = CameraPosition.builder()
                    .target(pos)
                    .zoom(15)
                    .bearing(0)
                    .tilt(45)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {

            FocusHomeMarker(currUserLocation);
        }

        if (!isEditing) {
            if (fvt_dest != null) {
                fvt_dest.showInfoWindow();
            }

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    fvt_dest = marker;


                    dataTransfer = new Object[3];
                    dataTransfer[0] = mMap;
                    dataTransfer[1] = getDirectionUrl();
                    Log.i(TAG, "directionURL: " + getDirectionUrl());
                    dataTransfer[2] = fvt_dest.getPosition();

                    ShowDirections getDirectionData = new ShowDirections();
                    getDirectionData.execute(dataTransfer);


                    try {
                        s = getDirectionData.get(WAIT_TIME, TimeUnit.SECONDS);

                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    }


                    HashMap<String, String> distanceHashMap = null;
                    ParseData distanceParser = new ParseData();
                    distanceHashMap = distanceParser.parseDistance(s);

                    showMarkerClickedAlert(marker.getTitle(), distanceHashMap.get("distance"), distanceHashMap.get("duration"));
                    return true;

                }
            });


            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    MarkerOptions options = new MarkerOptions().position(latLng)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));


                    fvt_dest = mMap.addMarker(options);
                    fvt_dest.setTitle(fetchAddressLine(fvt_dest));
                    fvt_dest.showInfoWindow();
                }
            });


        }
        else {
            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {

                }

                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {

                    fvt_dest = marker;

                }
            });


            findViewById(R.id.editModeLayout).setVisibility(View.VISIBLE);

            findViewById(R.id.updateBTN).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String newPlaceName = fetchAddressLine(fvt_dest);

                    fvt_dest.setTitle(newPlaceName);
                    fvt_dest.showInfoWindow();
                    Log.i(TAG, "new data: " + fvt_dest.getPosition());
                }
            });

        }



    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {




        mMap = googleMap;




        if (!checkPermission()){
            requestPermission();}
        else {

            startupACtivity();
        }
    }


    private void showMarkerClickedAlert(String address, String distance, String duration) {

        AlertDialog.Builder alert = new AlertDialog.Builder(MapActivity.this);
        final View v = LayoutInflater.from(MapActivity.this).inflate(R.layout.dropdown, null);
        alert.setView(v);

        TextView tvPlace = v.findViewById(R.id.place_name);
        TextView tvDist = v.findViewById(R.id.distance);

        tvPlace.setText(address);
        tvDist.setText(distance);

        if (mDatabase.numberOfResults(fvt_dest.getPosition().latitude, fvt_dest.getPosition().longitude) > 0) {

            Button b = v.findViewById(R.id.addToFvtBtn);
            b.setEnabled(false);
            b.setText("Add");


        }

        dropdownMenu = alert.create();
        dropdownMenu.show();

    }


    public String fetchAddressLine(Marker m){

        try {
            List<Address> addresses = geocoder.getFromLocation(m.getPosition().latitude, m.getPosition().longitude,1);



            return addresses.get(0).getAddressLine(0);

        } catch (IOException e) {
            e.printStackTrace();
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
        String format = simpleDateFormat.format(new Date());
        Log.d("MainActivity", "Current Timestamp: " + format);

        return format;
    }

    public void addToFvt() {


        place_name = fvt_dest.getTitle();


        if (mDatabase.addPlace(place_name, false,fvt_dest.getPosition().latitude, fvt_dest.getPosition().longitude)){

            Toast.makeText(this, place_name + " added" , Toast.LENGTH_SHORT).show();

        }else{
            Toast.makeText(this, "Place NOT added", Toast.LENGTH_SHORT).show();
        }

    }


    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(0, 0, 100, 100);
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    private boolean checkPermission() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    @SuppressLint("MissingPermission")
    private void reqLocationUpdate() {

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 100, locationListener);


    }


    private void FocusHomeMarker(Location location){


        p = null;
        addUSerMarker(location);

        CameraPosition cameraPosition = CameraPosition.builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                .zoom(15)
                .bearing(0)
                .tilt(45)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


    }

    private void FocusLocation(LatLng latLng){
        CameraPosition cameraPosition = CameraPosition.builder()
                .target(latLng)
                .zoom(15)
                .bearing(0)
                .tilt(45)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    private void addUSerMarker(Location l){




        if(User != null){
            User.remove();
            User = null;
        }


        LatLng home = new LatLng(l.getLatitude(), l.getLongitude());
        Log.i(TAG, "run without error this time: ");
        startL = mMap.addMarker(new MarkerOptions()
                .position(home)
                .title("Current User Location")
                .icon(bitmapDescriptorFromVector(this, R.drawable.man))

        );
        User = startL;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                startupACtivity();

            }else{

                Toast.makeText(this, "Permission is required to access location", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void markerClickAction(View view) {

        switch (view.getId()) {

            case R.id.addToFvtBtn:
                addToFvt();
                break;



            default:
                break;

        }

        dropdownMenu.dismiss();
    }
}
