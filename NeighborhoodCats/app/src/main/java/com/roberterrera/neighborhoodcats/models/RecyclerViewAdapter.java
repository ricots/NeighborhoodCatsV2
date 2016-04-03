package com.roberterrera.neighborhoodcats.models;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.roberterrera.neighborhoodcats.DetailsActivity;
import com.roberterrera.neighborhoodcats.MainActivity;
import com.roberterrera.neighborhoodcats.R;
import com.roberterrera.neighborhoodcats.localdata.CatsSQLiteOpenHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rob on 4/3/16.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.CatViewHolder> {
    private List<Cat> catList;
    private static Cursor cursor;
    private Context mContext;

    // Provide a suitable constructor
    public RecyclerViewAdapter(List<Cat> catList) {
        this.catList = catList;
    }

    // Inflate the view.
    @Override
    public CatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        cursor = CatsSQLiteOpenHelper.getInstance(parent.getContext()).getCatsList();
        View v = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.list_card_layout, parent, false);

        CatViewHolder holder = new CatViewHolder(v);
        return holder;
    }

    // Set content to the view.
    @Override
    public void onBindViewHolder(CatViewHolder holder, int position) {
        Cat cat = catList.get(position);
//        CatsSQLiteOpenHelper helper = CatsSQLiteOpenHelper.getInstance(holder).getCatsList();
//        holder.vName.setText(cursor.getString(cursor.getColumnIndex(CatsSQLiteOpenHelper.CAT_NAME)));
//        holder.vThumbnail.setImageResource(cursor.getColumnIndex(CatsSQLiteOpenHelper.CAT_IMG));
        holder.vName.setText(cat.getName());
        Picasso.with(mContext).load(cat.getPhoto())
                .placeholder(R.drawable.ic_pets_black_24dp)
                .into(holder.vThumbnail);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CatViewHolder holder = (CatViewHolder) view.getTag();
                int position = holder.getAdapterPosition();

                Cat cat = catList.get(position);
                Toast.makeText(mContext, cat.getName(), Toast.LENGTH_SHORT).show();
            }
        };

        //Handle click event on both title and image click
        holder.vName.setOnClickListener(clickListener);
        holder.vThumbnail.setOnClickListener(clickListener);

        holder.vName.setTag(holder);
        holder.vThumbnail.setTag(holder);

    }


    public static class CatViewHolder extends RecyclerView.ViewHolder {
        protected TextView vName;
        protected ImageView vThumbnail;
        public CatViewHolder(View v) {
            super(v);

            vName =  (TextView) v.findViewById(R.id.textview_catname_cardview);
            vThumbnail = (ImageView) v.findViewById(R.id.imageview_cat_cardview);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), DetailsActivity.class);
                    intent.putExtra("id", cursor.getInt(cursor.getColumnIndex(CatsSQLiteOpenHelper.CAT_ID)));
                    v.getContext().startActivity(intent);
                }
            });
        }
    }

    @Override
         public int getItemCount() {
        if (catList.size() != 0) {
            return catList.size();
        } else {
            return 0;
        }

    }
}
