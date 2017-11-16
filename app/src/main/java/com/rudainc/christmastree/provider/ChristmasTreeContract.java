package com.rudainc.christmastree.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class ChristmasTreeContract {

    public static final String AUTHORITY = "com.rudainc.christmastree";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final long INVALID_PLANT_ID = -1;

    public static final String PATH = "tree";

    public static final class TreeEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        public static final String TABLE_NAME = "christmas_tree";
        public static final String COLUMN_CREATED_AT = "createdAt";
        public static final String COLUMN_WATERED_AT = "wateredAt";
    }
}
