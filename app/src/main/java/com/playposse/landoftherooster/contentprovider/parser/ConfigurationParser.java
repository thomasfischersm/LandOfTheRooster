package com.playposse.landoftherooster.contentprovider.parser;

import android.content.Context;

import com.google.common.io.Closeables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.playposse.landoftherooster.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * A parser that reads all the configuration information from json files.
 */
final class ConfigurationParser {

    private ConfigurationParser() {}

    static List<ResourceType> readResourceTypes(Context context) throws IOException {
        InputStream inputStream = context.getResources().openRawResource(R.raw.resource_type);
        InputStreamReader reader = new InputStreamReader(inputStream);
        try {
            Gson gson = new GsonBuilder().create();
            ResourceTypeRoot root = gson.fromJson(reader, ResourceTypeRoot.class);
            return root.getResourceTypes();
        } finally {
            Closeables.close(reader, false);
        }
    }

    static List<BuildingType> readBuildingTypes(Context context) throws IOException {
        InputStream inputStream = context.getResources().openRawResource(R.raw.building_type);
        InputStreamReader reader = new InputStreamReader(inputStream);
        try {
            Gson gson = new GsonBuilder().create();
            BuildingTypeRoot root = gson.fromJson(reader, BuildingTypeRoot.class);
            return root.getBuildingTypes();
        } finally {
            Closeables.close(reader, false);
        }
    }

    static List<UnitType> readUnitTypes(Context context) throws IOException {
        InputStream inputStream = context.getResources().openRawResource(R.raw.unit_type);
        InputStreamReader reader = new InputStreamReader(inputStream);
        try {
            Gson gson = new GsonBuilder().create();
            UnitTypeRoot root = gson.fromJson(reader, UnitTypeRoot.class);
            return root.getUnitTypes();
        } finally {
            Closeables.close(reader, false);
        }
    }
}
