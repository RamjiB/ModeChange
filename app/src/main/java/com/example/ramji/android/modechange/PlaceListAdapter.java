package com.example.ramji.android.modechange;


import android.content.ContentValues;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.PlaceViewHolder> {

    private Context mContext;

    /**
     * Constructor using the context and the db cursor
     *
     * @param context the calling context/activity
     */
    public PlaceListAdapter(Context context) {
        this.mContext = context;
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
        return 0;
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
}
