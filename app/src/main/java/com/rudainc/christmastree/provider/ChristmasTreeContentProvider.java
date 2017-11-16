package com.rudainc.christmastree.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import static com.rudainc.christmastree.provider.ChristmasTreeContract.AUTHORITY;
import static com.rudainc.christmastree.provider.ChristmasTreeContract.PATH;
import static com.rudainc.christmastree.provider.ChristmasTreeContract.TreeEntry;

public class ChristmasTreeContentProvider extends ContentProvider {

    public static final int TREE = 2018;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, PATH, TREE);
        return uriMatcher;
    }

    private ChristmasTreeDbHelper mChristmasTreeDbHelper;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mChristmasTreeDbHelper = new ChristmasTreeDbHelper(context);
        return true;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mChristmasTreeDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case TREE:
                long id = db.insert(TreeEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(TreeEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mChristmasTreeDbHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match) {
            case TREE:
                retCursor = db.query(ChristmasTreeContract.TreeEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mChristmasTreeDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int deleted;

        String id = selectionArgs[0];
        switch (match) {
            case TREE:
                deleted = db.delete(ChristmasTreeContract.TreeEntry.TABLE_NAME, "_id=?", new String[]{id});
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Log.i("DELETED", "id deleted " + deleted);
        if (deleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deleted;
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        final SQLiteDatabase db = mChristmasTreeDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int updated;

        switch (match) {
            case TREE:
                updated = db.update(TreeEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (updated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updated;
    }


    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
