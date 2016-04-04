package com.roberterrera.neighborhoodcats.cardview;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.roberterrera.neighborhoodcats.R;
import com.roberterrera.neighborhoodcats.activities.DetailsActivity;
import com.roberterrera.neighborhoodcats.activities.MainActivity;
import com.roberterrera.neighborhoodcats.sqldatabase.CatsSQLiteOpenHelper;

/**
 * Created by Rob on 4/3/16.
 */
public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    protected TextView vName, vDesc;
    protected ImageView vThumbnail;
    private Cursor mCursor;
    private Context mContext;
    private ItemClickListener itemClickListener;

    public ViewHolder(View itemView) {
        super(itemView);

        mContext = itemView.getContext();
        vName = (TextView) itemView.findViewById(R.id.textview_catname_cardview);
        vDesc = (TextView) itemView.findViewById(R.id.textview_catdesc_cardview);
        vThumbnail = (ImageView) itemView.findViewById(R.id.imageview_cat_cardview);

        itemView.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        this.itemClickListener.onItemClick(v, getLayoutPosition());
    }

    public void setItemClickListener(ItemClickListener ic) {
        this.itemClickListener = ic;
    }
}

