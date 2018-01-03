package com.playposse.landoftherooster.contentprovider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * A contract for {@link RoosterContentProvider}.
 */
public class RoosterContentContract {

    public static final String AUTHORITY = "com.playposse.landoftherooster.provider";

    private static final String CONTENT_SCHEME = "content";

    private RoosterContentContract() {
    }

    private static Uri createContentUri(String path) {
        return new Uri.Builder()
                .scheme(CONTENT_SCHEME)
                .encodedAuthority(AUTHORITY)
                .appendPath(path)
                .build();
    }

    /**
     * Stores resource types
     */
    public static class ResourceTypeTable implements BaseColumns {

        public static final String PATH = "recourceType";
        public static final Uri CONTENT_URI = createContentUri(PATH);
        public static final String TABLE_NAME = "resource_type";

        public static final String ID_COLUMN = _ID;
        public static final String NAME_COLUMN = "name";
        public static final String PRECURSOR_ID_COLUMN = "precursor_id";

        public static final String[] COLUMN_NAMES = new String[]{
                ID_COLUMN,
                NAME_COLUMN,
                PRECURSOR_ID_COLUMN};

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE resource_type "
                        + "(_id INTEGER PRIMARY KEY, "
                        + "name TEXT NOT NULL UNIQUE, "
                        + "precursor_id INTEGER, "
                        + "FOREIGN KEY(precursor_id) REFERENCES resource_type(_id))";
    }

    /**
     * Stores resources
     */
    public static final class ResourceTable implements BaseColumns {

        public static final String PATH = "resource";
        public static final Uri CONTENT_URI = createContentUri(PATH);
        public static final String TABLE_NAME = "resource";

        public static final String ID_COLUMN = _ID;
        public static final String RESOURCE_TYPE_ID_COLUMN = "resource_type_id";
        public static final String AMOUNT_COLUMN = "amount";

        public static final String[] COLUMN_NAMES = new String[]{
                ID_COLUMN,
                RESOURCE_TYPE_ID_COLUMN,
                AMOUNT_COLUMN};

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE resource "
                        + "(_id INTEGER PRIMARY KEY, "
                        + "resource_type_id INTEGER NOT NULL, "
                        + "amount INTEGER NOT NULL, "
                        + "FOREIGN KEY(resource_type_id) REFERENCES resource_type(_id))";
    }

    /**
     * Stores building types
     */
    public static class BuildingTypeTable implements BaseColumns {

        public static final String PATH = "buildingType";
        public static final Uri CONTENT_URI = createContentUri(PATH);
        public static final String TABLE_NAME = "building_type";

        public static final String ID_COLUMN = _ID;
        public static final String NAME_COLUMN = "name";
        public static final String ICON_COLUMN = "icon";
        public static final String PRODUCED_RESOURCE_TYPE_ID_COLUMN = "produced_resource_type_id";

        public static final String[] COLUMN_NAMES = new String[]{
                ID_COLUMN,
                NAME_COLUMN,
                ICON_COLUMN,
                PRODUCED_RESOURCE_TYPE_ID_COLUMN};

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE building_type "
                        + "(_id INTEGER PRIMARY KEY, "
                        + "name TEXT NOT NULL UNIQUE, "
                        + "icon TEXT NOT NULL UNIQUE, "
                        + "produced_resource_type_id INTEGER, "
                        + "FOREIGN KEY(produced_resource_type_id) REFERENCES resource_type(_id))";
    }

    /**
     * Stores building types
     */
    public static class BuildingTable implements BaseColumns {

        public static final String PATH = "building";
        public static final Uri CONTENT_URI = createContentUri(PATH);
        public static final String TABLE_NAME = "building";

        public static final String ID_COLUMN = _ID;
        public static final String BUILDING_TYPE_ID_COLUMN = "building_type_id";
        public static final String LATITUDE_COLUMN = "latitude";
        public static final String LONGITUDE_COLUMN = "longitude";

        public static final String[] COLUMN_NAMES = new String[]{
                ID_COLUMN,
                BUILDING_TYPE_ID_COLUMN,
                LATITUDE_COLUMN,
                LONGITUDE_COLUMN};

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE building_type "
                        + "(_id INTEGER PRIMARY KEY, "
                        + "building_type_id INTEGER NOT NULL, "
                        + "latitude REAL NOT NULL, "
                        + "longitude REAL NOT NULL, "
                        + "FOREIGN KEY(building_type_id) REFERENCES building_type(_id))";
    }
}
