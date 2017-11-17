package com.rudainc.christmastree.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.rudainc.christmastree.R;
import com.rudainc.christmastree.TreeWateringService;
import com.rudainc.christmastree.provider.ChristmasTreeContract;
import com.rudainc.christmastree.utils.TreeUtils;

import io.fabric.sdk.android.Fabric;

import static android.provider.BaseColumns._ID;
import static com.rudainc.christmastree.provider.ChristmasTreeContract.BASE_CONTENT_URI;
import static com.rudainc.christmastree.provider.ChristmasTreeContract.PATH;
import static com.rudainc.christmastree.provider.ChristmasTreeContract.TreeEntry;


public class ChristmasTreeActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 1111;
    public static final String EXTRA_PLANT_ID = "com.rudainc.christmastree.extra.PLANT_ID";
    long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics(), new Answers());
        setContentView(R.layout.activity_christmas_tree);
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    public void onWaterButtonClick(View view) {
        Log.i("WATER", "plant " + id);
        TreeWateringService.startActionWaterPlant(this, id);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri PLANT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();
        return new CursorLoader(this, PLANT_URI, null,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            (findViewById(R.id.plant_button)).setVisibility(View.VISIBLE);
            return;
        }
        (findViewById(R.id.reset_button)).setVisibility(View.VISIBLE);
        (findViewById(R.id.water_button)).setVisibility(View.VISIBLE);
        cursor.moveToFirst();
        int createTimeIndex = cursor.getColumnIndex(ChristmasTreeContract.TreeEntry.COLUMN_CREATED_AT);
        int waterTimeIndex = cursor.getColumnIndex(ChristmasTreeContract.TreeEntry.COLUMN_WATERED_AT);

        id = cursor.getLong(cursor.getColumnIndex(_ID));

        long createdAt = cursor.getLong(createTimeIndex);
        long wateredAt = cursor.getLong(waterTimeIndex);
        long timeNow = System.currentTimeMillis();

        int plantImgRes = TreeUtils.getPlantImageRes(this, timeNow - createdAt, timeNow - wateredAt);

        ((ImageView) findViewById(R.id.ivTree)).setImageResource(plantImgRes);

        ((TextView) findViewById(R.id.tvAge)).setText(
                String.valueOf(TreeUtils.getDisplayAgeInt(timeNow - createdAt))
        );
        ((TextView) findViewById(R.id.tvAgeUnit)).setText(
                TreeUtils.getDisplayAgeUnit(this, timeNow - createdAt)
        );
        ((TextView) findViewById(R.id.wateredAt)).setText(
                String.valueOf(TreeUtils.getDisplayAgeInt(timeNow - wateredAt))
        );
        ((TextView) findViewById(R.id.wateredAtUnit)).setText(
                TreeUtils.getDisplayAgeUnit(this, timeNow - wateredAt)
        );
        int waterPercent = 100 - ((int) (100 * (timeNow - wateredAt) / TreeUtils.MAX_AGE_WITHOUT_WATER));
        ((WaterLevelView) findViewById(R.id.water_level)).setValue(waterPercent);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void onCutButtonClick(View view) {

        (findViewById(R.id.reset_button)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.water_button)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.ivTree)).setVisibility(View.INVISIBLE);
        Log.i("DELETE", "plant " + id);
        getContentResolver().delete(BASE_CONTENT_URI, null, new String[]{String.valueOf(id)});
        TreeWateringService.startActionUpdatePlantWidgets(this);
    }


    public void onPlantButtonClick(View view) {

        (findViewById(R.id.plant_button)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.ivTree)).setVisibility(View.VISIBLE);

        getContentResolver().notifyChange(ChristmasTreeContract.BASE_CONTENT_URI, null);
        long timeNow = System.currentTimeMillis();

        ContentValues contentValues = new ContentValues();
        contentValues.put(TreeEntry.COLUMN_CREATED_AT, timeNow);
        contentValues.put(ChristmasTreeContract.TreeEntry.COLUMN_WATERED_AT, timeNow);
        getContentResolver().insert(ChristmasTreeContract.TreeEntry.CONTENT_URI, contentValues);
        TreeWateringService.startActionUpdatePlantWidgets(this);
    }


}
