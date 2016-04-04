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
import android.widget.Toast;

import com.roberterrera.neighborhoodcats.activities.DetailsActivity;
import com.roberterrera.neighborhoodcats.R;
import com.roberterrera.neighborhoodcats.models.Cat;
import com.roberterrera.neighborhoodcats.sqldatabase.CatsSQLiteOpenHelper;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

/**
 * Created by Rob on 4/3/16.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder> {
    private List<Cat> catList;
    private Context mContext;
    private Cursor mCursor;
    private ItemClickListener itemClickListener;

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

        holder.vName.setText(catList.get(position).getName());
        holder.vDesc.setText(catList.get(position).getDesc());
        Picasso.with(mContext)
                .load("file:"+catList.get(position).getPhoto())
                .resize(120, 120)
                .placeholder(R.drawable.ic_pets_black_24dp)
                .into(holder.vThumbnail);

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                //Send intent to detail activity view
                CatsSQLiteOpenHelper mHelper = new CatsSQLiteOpenHelper(mContext);
                mHelper.getWritableDatabase();
                mCursor = CatsSQLiteOpenHelper.getInstance(mContext).getCatsList();

                Toast.makeText(mContext, "Item " + String.valueOf(pos) + " clicked.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext, DetailsActivity.class);
                intent.putExtra("id", catList.get(pos).getId());
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return  catList.size();
    }

 /*   //Methods for animating removing and adding items
    public Cat removeItem(int position) {
        final Cat catObject = catList.remove(position);
        notifyItemRemoved(position);
        return catObject;
    }

    public void moveItem(int fromPosition, int toPosition) {
        final Cat catObject = catList.remove(fromPosition);
        catList.add(toPosition, catObject);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo(List<Cat> list) {
        applyAndAnimateRemovals(list);
        applyAndAnimateMovedItems(list);
    }

    private void applyAndAnimateRemovals(List<Cat> list) {
        for (int i = catList.size() - 1; i >= 0; i--) {
            final Cat catObject = catList.get(i);
            if (!list.contains(catObject)) {
                removeItem(i);
            }
        }
    }


    private void applyAndAnimateMovedItems(List<Cat> list) {
        for (int toPosition = list.size() - 1; toPosition >= 0; toPosition--) {
            final Cat catObject = list.get(toPosition);
            final int fromPosition = catList.indexOf(catObject);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }*/

    /*
    @Override
    public void onItemDismiss(int position) {
        catList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(catList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(catList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }
    */
}
