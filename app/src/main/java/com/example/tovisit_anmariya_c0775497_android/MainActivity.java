package com.example.tovisit_anmariya_c0775497_android;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    SwipeMenuListView LV_places;
    List<MyFavourites> placeList;
    DatabaseHelper mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mDatabase = new DatabaseHelper(this);
        LV_places = findViewById(R.id.locationList);

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {




                SwipeMenuItem update_item = new SwipeMenuItem(getApplicationContext());
                update_item.setWidth(150);
                update_item.setBackground(new ColorDrawable(Color.GREEN));
                update_item.setTitle("Update");
                update_item.setTitleSize(15);
                update_item.setTitleColor(Color.WHITE);

                menu.addMenuItem(update_item);

                SwipeMenuItem del_item = new SwipeMenuItem(getApplicationContext());
                del_item.setWidth(150);
                del_item.setBackground(new ColorDrawable(Color.RED));
                del_item.setTitle("Remove");
                del_item.setTitleSize(20);
                del_item.setTitleColor(Color.WHITE);
                menu.addMenuItem(del_item);


            }
        };
        LV_places.setMenuCreator(creator);
        LV_places.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);

        LV_places.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {

                switch(index){

                    case 0:
                        Log.i("DEBUG", "DELETE ITEM SELECTED: " + position);
                        mDatabase.removePlace(placeList.get(position).getId());
                        loadPlaces();

                        break;
                    case 1:
                        Log.i("DEBUG", "UPDATE ITEM SELECTED: " + position);
                        Intent editI = new Intent(MainActivity.this, MapActivity.class);
                        editI.putExtra("selectedPlace", placeList.get(position));
                        editI.putExtra("EDIT", true);

                        startActivity(editI);
                        break;
                    default:
                        break;
                }


                return false;
            }
        });

        LV_places.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent mapI = new Intent(MainActivity.this, MapActivity.class);
                mapI.putExtra("selectedPlace", placeList.get(position));
                startActivity(mapI);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadPlaces();
    }

    public void showMap(View view) {
        Intent mapI = new Intent(this, MapActivity.class);
        startActivity(mapI);
    }
    private void loadPlaces() {
        placeList = new ArrayList<>();
        Cursor cursor = mDatabase.getAllPlaces();
        if (cursor.moveToFirst()) {
            do {
                placeList.add(new MyFavourites(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2).equals("1"),
                        cursor.getDouble(3),
                        cursor.getDouble(4)
                ));
            } while (cursor.moveToNext());
            cursor.close();
            FavouriteAdapter adaptor = new FavouriteAdapter(this, R.layout.place_cell, placeList, mDatabase);
            LV_places.setAdapter(adaptor);

        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}