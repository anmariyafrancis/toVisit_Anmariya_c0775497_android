package com.example.tovisit_anmariya_c0775497_android;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

public class ShowDirections extends AsyncTask<Object, String, String> {
    String directionData, url;
    GoogleMap mMap;

    String distance, duration;

    LatLng latLng;

    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap) objects[0];
        url = (String) objects[1];
        latLng = (LatLng) objects[2];

        ReadUrl fetchURL= new ReadUrl();
        try{
            directionData = fetchURL.readURL(url);
        } catch (IOException e){
            e.printStackTrace();
        }

        return directionData;
    }

}
