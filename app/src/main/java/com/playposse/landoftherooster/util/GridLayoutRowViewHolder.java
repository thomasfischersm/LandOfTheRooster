package com.playposse.landoftherooster.util;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.GridLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;

/**
 * A helper to populate a {@link GridLayout}, where each row is created by inflating a layout
 * template.
 *
 * <p>The implementing class can have Butterknife fields. They will be bound.
 */
public abstract class GridLayoutRowViewHolder {

    public GridLayoutRowViewHolder(GridLayout gridLayout, @LayoutRes int layoutResId) {
        LayoutInflater inflater = LayoutInflater.from(gridLayout.getContext());
        View rowView = inflater.inflate(layoutResId, gridLayout);

        ButterKnife.bind(this, rowView);

        copyChildViews((ViewGroup) rowView, gridLayout);
        loadData();
    }

    private static void copyChildViews(ViewGroup rowView, GridLayout gridLayout) {
        while (rowView.getChildCount() > 0) {
            View childView = rowView.getChildAt(0);
            rowView.removeView(childView);
            gridLayout.addView(childView);
        }
    }

    protected abstract void loadData();
}
