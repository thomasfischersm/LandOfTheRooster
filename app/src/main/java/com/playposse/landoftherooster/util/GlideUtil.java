package com.playposse.landoftherooster.util;

import android.content.Context;
import android.widget.ImageView;

import com.playposse.landoftherooster.contentprovider.room.entity.BuildingType;
import com.playposse.landoftherooster.contentprovider.room.entity.BuildingWithType;
import com.playposse.landoftherooster.glide.GlideApp;

/**
 * A helper for dealing with Glide.
 */
public final class GlideUtil {

    private static final String DRAWABLE_RESOURCE_TYPE = "drawable";

    private GlideUtil() {}

    public static void loadBuildingIcon(ImageView imageView, BuildingWithType buildingWithType) {
        Context context = imageView.getContext();
        BuildingType buildingType = buildingWithType.getBuildingType();

        int drawableId = context.getResources().getIdentifier(
                buildingType.getIcon(),
                DRAWABLE_RESOURCE_TYPE,
                context.getPackageName());

        GlideApp.with(context)
                .load(drawableId)
                .into(imageView);
    }
}
