package com.roberterrera.neighborhoodcats.cardview;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.roberterrera.neighborhoodcats.activities.DetailsActivity;
import com.roberterrera.neighborhoodcats.R;
import com.roberterrera.neighborhoodcats.models.Cat;
import com.roberterrera.neighborhoodcats.sqldatabase.CatsSQLiteOpenHelper;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Rob on 4/3/16.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder> {
    private List<Cat> catList;
    private Context mContext;
    private ItemClickListener itemClickListener;
//    private static Cursor mCursor;

    // Constructor
    public RecyclerViewAdapter(List<Cat> catList, Context mContext) {
        this.catList = catList;
        this.mContext = mContext;
    }

    // Initialize the view holder and inflate the view.
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.list_card_layout, parent, false);

        // Initialize the view holder.
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    // Bind the view to the data.
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
         //  CatsSQLiteOpenHelper helper = CatsSQLiteOpenHelper.getInstance(holder).getCatsList();
//        holder.vName.setText(mCursor.getString(mCursor.getColumnIndex(CatsSQLiteOpenHelper.CAT_NAME)));
//        holder.vDesc.setText(mCursor.getString(mCursor.getColumnIndex(CatsSQLiteOpenHelper.CAT_DESC)));
//        Log.d("RecyclerAdapter", "Name: " + holder.vName.getText().toString());
//        Log.d("RecyclerAdapter", "Desc: "+holder.vDesc.getText().toString());
//
//
////        Display display = mContext.
////        Point size = new Point();
////        display.getSize(size);
////        int width = size.x;
////        int height = size.y;

        holder.vName.setText(catList.get(position).getName());
        holder.vDesc.setText(catList.get(position).getDesc());
//        holder.vThumbnail.setImageResource(R.drawable.bond_cat);
        Picasso.with(mContext)
                .load("file:"+catList.get(position).getPhoto())
                .resize(120, 120)
                .placeholder(R.drawable.ic_pets_black_24dp)
                .into(holder.vThumbnail);

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
//                Intent intent = new Intent(v.getContext(), DetailsActivity.class);
//                intent.putExtra("id", mCursor.getInt(mCursor.getColumnIndex(CatsSQLiteOpenHelper.CAT_ID)));
//                v.getContext().startActivity(intent);
                Snackbar.make(v, catList.get(pos).getName(),Snackbar.LENGTH_SHORT).show();
            }
        });
//
//        //Handle click event on both title and image click
//        holder.vName.setOnClickListener(clickListener);
//        holder.vThumbnail.setOnClickListener(clickListener);
//
//        holder.vName.setTag(holder);
//        holder.vThumbnail.setTag(holder);

    }


//    public static class CatViewHolder extends RecyclerView.ViewHolder {
//        protected TextView vName, vDesc;
//        protected ImageView vThumbnail;
//        public CatViewHolder(View v) {
//            super(v);
//
//            vName =  (TextView) v.findViewById(R.id.textview_catname_cardview);
//            vDesc = (TextView) v.findViewById(R.id.textview_catdesc_cardview);
//            vThumbnail = (ImageView) v.findViewById(R.id.imageview_cat_cardview);
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(v.getContext(), DetailsActivity.class);
//                    intent.putExtra("id", mCursor.getInt(mCursor.getColumnIndex(CatsSQLiteOpenHelper.CAT_ID)));
//                    v.getContext().startActivity(intent);
//                }
//            });
//        }
//    }

    @Override
    public int getItemCount() {
        return  catList.size();
    }
}
