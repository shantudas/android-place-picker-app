package com.muvasia.android_places_demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.libraries.places.api.Places;

import java.util.ArrayList;

public class NearbyPlacesAdapter extends RecyclerView.Adapter<NearbyPlacesAdapter.ViewHolder> {

    private Context context;
    private ArrayList<NearbyPlace> nearbyPlaceArrayList;

    public NearbyPlacesAdapter(Context context, ArrayList<NearbyPlace> nearbyPlaceArrayList) {
        this.context = context;
        this.nearbyPlaceArrayList = nearbyPlaceArrayList;
    }

    @NonNull
    @Override
    public NearbyPlacesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_each_row_nearby_place, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NearbyPlacesAdapter.ViewHolder holder, int position) {
        NearbyPlace nearbyPlace=nearbyPlaceArrayList.get(position);
        holder.tvPlaceName.setText(nearbyPlace.getName());
        holder.tvPlaceAddress.setText(nearbyPlace.getAddress());
    }

    @Override
    public int getItemCount() {
        return nearbyPlaceArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvPlaceName;
        private TextView tvPlaceAddress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlaceName = itemView.findViewById(R.id.tvPlaceName);
            tvPlaceAddress = itemView.findViewById(R.id.tvPlaceAddress);

        }
    }
}
