package com.roberterrera.neighborhoodcats.cardview;

/**
 * Created by Rob on 4/3/16.
 */
public interface ItemTouchHelperAdapter {

    boolean onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);
}