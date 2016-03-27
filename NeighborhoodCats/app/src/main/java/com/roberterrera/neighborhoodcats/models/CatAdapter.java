package com.roberterrera.neighborhoodcats.models;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.roberterrera.neighborhoodcats.R;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by Rob on 3/26/16.
 */
public class CatAdapter extends RealmBaseAdapter<Cat> implements ListAdapter {

    private static class ViewHolder {
        TextView catName;
        ImageView catPhoto;
    }

    public CatAdapter(Context context, int resId, RealmResults<Cat> realmResults, boolean automaticUpdate) {
        super(context, realmResults, automaticUpdate);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.catName = (TextView) convertView.findViewById(R.id.textview_catname_list);
            viewHolder.catPhoto = (ImageView) convertView.findViewById(R.id.imageview_catthumbnail);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Cat cat = realmResults.get(position);
        viewHolder.catName.setText(cat.getName());
        return convertView;
    }

    public RealmResults<Cat> getRealmResults() {
        return realmResults;
    }
}