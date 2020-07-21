package com.example.tovisit_anmariya_c0775497_android;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class ShowNearby extends AsyncTask<Object,String,String> implements GoogleMap.OnInfoWindowClickListener {
    GoogleMap googleMap;
    String placeData, url;


    @Override
    protected String doInBackground(Object... objects) {


        googleMap = (GoogleMap) objects[0];
        url = (String) objects[1];

        ReadUrl fetchURL= new ReadUrl();
        try{
            placeData = fetchURL.readURL(url);
        } catch (IOException e){
            e.printStackTrace();
        }


        return placeData;
    }

    @Override
    protected void onPostExecute(String s) {


        List<HashMap<String, String>> nearByPlaceList = null;
        ParseData parser = new ParseData();
        nearByPlaceList = parser.parseData(s);


        showNearbyPlaces(nearByPlaceList);

    }


    private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlacesList){

        for(int i=0; i<nearbyPlacesList.size(); i++){
            HashMap<String, String> place = nearbyPlacesList.get(i);

            String placeName = place.get("placeName");
            String vicinity = place.get("vicinity");
            double latitude = Double.parseDouble(place.get("lat"));
            double longitude = Double.parseDouble(place.get("lng"));
            String reference = place.get("reference");

            LatLng latLng = new LatLng(latitude, longitude);

            //marker options
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(placeName + "\n" + vicinity)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
            googleMap.addMarker(markerOptions);

        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }
}
