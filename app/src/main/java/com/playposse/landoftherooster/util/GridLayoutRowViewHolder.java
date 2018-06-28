package com.playposse.landoftherooster.util;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.GridLayout;
import android.util.Log;
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
public abstract class GridLayoutRowViewHolder<T> {

    private static final String LOG_TAG = GridLayoutRowViewHolder.class.getSimpleName();

    private final GridLayout gridLayout;
    private final int layoutResId;

    protected GridLayoutRowViewHolder(GridLayout gridLayout, @LayoutRes int layoutResId) {
        this.gridLayout = gridLayout;
        this.layoutResId = layoutResId;
    }

    public void apply(T data) {
        LayoutInflater inflater = LayoutInflater.from(gridLayout.getContext());
        View rowView = inflater.inflate(layoutResId, null);

        ButterKnife.bind(this, rowView);

        copyChildViews((ViewGroup) rowView, gridLayout);
        populate(data);
    }

    private static void copyChildViews(ViewGroup rowView, GridLayout gridLayout) {
        while (rowView.getChildCount() > 0) {
            Log.d(LOG_TAG, "copyChildViews: Copying child " + rowView.getChildCount());
            View childView = rowView.getChildAt(0);
            rowView.removeView(childView);
            gridLayout.addView(childView);
        }

        gridLayout.invalidate();
    }

    protected abstract void populate(T data);
}
