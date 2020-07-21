package com.example.tovisit_anmariya_c0775497_android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class FavouriteAdapter extends ArrayAdapter {
    Context context;
    List<MyFavourites> places;
    int layoutRes;
    //SQLiteDatabase mDatabase;
    DatabaseHelper mDatabase;

    public FavouriteAdapter(@NonNull Context context, int resource, List<MyFavourites> places, DatabaseHelper mDatabase) {
        super(context, resource, places);
        this.context = context;
        this.places = places;
        this.layoutRes = resource;
        this.mDatabase = mDatabase;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(layoutRes, null);

        TextView tvName = v.findViewById(R.id.name_ID);




        final MyFavourites p = places.get(position);
        tvName.setText(p.getName());


        if (p.getVisited()){
            ImageView i = v.findViewById(R.id.placeImage);
            i.setImageResource(R.drawable.global);}

        return v;
    }




}
