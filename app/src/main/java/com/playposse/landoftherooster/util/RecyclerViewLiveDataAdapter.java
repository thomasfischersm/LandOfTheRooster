package com.playposse.landoftherooster.util;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.support.v4.app.SupportActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.List;

/**
 * A {@link RecyclerView.Adapter} that is attached to a {@link LiveData}.
 */
public abstract class RecyclerViewLiveDataAdapter<VH extends RecyclerView.ViewHolder, D>
        extends RecyclerView.Adapter<VH> {

    private static final String LOG_TAG = RecyclerViewLiveDataAdapter.class.getSimpleName();

    private final LiveData<List<D>> liveData;

    public RecyclerViewLiveDataAdapter(SupportActivity activity, LiveData<List<D>> liveData) {
        this.liveData = liveData;

        liveData.observe(activity, new Observer<List<D>>() {
            @Override
            public void onChanged(@Nullable List<D> ds) {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        List<D> data = liveData.getValue();

        if ((data != null) && (data.size() > position)) {
            onBindViewHolder(holder, data.get(position));
        } else {
            Log.e(LOG_TAG, "onBindViewHolder: Requested position out of range!");
        }
    }

    protected abstract void onBindViewHolder(VH holder, D data);

    @Override
    public int getItemCount() {
        List<D> value = liveData.getValue();
        return (value != null) ? value.size() : 0;
    }
}
