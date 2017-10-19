package com.example.ramji.android.modechange;


import android.content.ContentValues;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.PlaceViewHolder> {

    private Context mContext;
    private PlaceBuffer mPlaces;

    /**
     * Constructor using the context and the db cursor
     *
     * @param context the calling context/activity
     */
    public PlaceListAdapter(Context context,PlaceBuffer places) {
        this.mContext = context;
        this.mPlaces = places;
    }

    @Override
    public PlaceListAdapter.PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //Get the recycler view item layout
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_place_card,parent,false);
        return new PlaceViewHolder(view);

    }

    @Override
    public void onBindViewHolder(PlaceListAdapter.PlaceViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        if (mPlaces == null) return 0;
        return mPlaces.getCount();
    }

    public class PlaceViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;
        TextView addressTextView;

        public PlaceViewHolder(View itemView) {
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.name_text_view);
            addressTextView = (TextView) itemView.findViewById(R.id.address_text_view);

        }
    }

    public void swapPlaces(PlaceBuffer newPlaces){

        mPlaces = newPlaces;
        if (mPlaces != null){
            // Force the recycler view to refresh
            this.notifyDataSetChanged();
        }

    }
}
