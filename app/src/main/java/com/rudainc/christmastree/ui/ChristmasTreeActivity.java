package com.rudainc.christmastree.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ShareEvent;
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
            plantUI(false);
            return;
        }
        plantUI(true);
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

    private void plantUI(boolean isPlanted) {
        (findViewById(R.id.tvPlant)).setVisibility(isPlanted ? View.GONE : View.VISIBLE);
        (findViewById(R.id.plant_button)).setVisibility(isPlanted ? View.INVISIBLE : View.VISIBLE);
        (findViewById(R.id.plant_age)).setVisibility(isPlanted ? View.VISIBLE : View.INVISIBLE);
        (findViewById(R.id.water_meter)).setVisibility(isPlanted ? View.VISIBLE : View.INVISIBLE);
        (findViewById(R.id.tv_since)).setVisibility(isPlanted ? View.VISIBLE : View.INVISIBLE);
        (findViewById(R.id.tv_water)).setVisibility(isPlanted ? View.VISIBLE : View.INVISIBLE);
        (findViewById(R.id.ivTree)).setVisibility(isPlanted ? View.VISIBLE : View.INVISIBLE);
        (findViewById(R.id.reset_button)).setVisibility(isPlanted ? View.VISIBLE : View.INVISIBLE);
        (findViewById(R.id.water_button)).setVisibility(isPlanted ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void onCutButtonClick(View view) {
        getContentResolver().delete(BASE_CONTENT_URI, null, new String[]{String.valueOf(id)});
        TreeWateringService.startActionUpdatePlantWidgets(this);
        plantUI(false);
    }


    public void onPlantButtonClick(View view) {
        getContentResolver().notifyChange(ChristmasTreeContract.BASE_CONTENT_URI, null);
        long timeNow = System.currentTimeMillis();

        ContentValues contentValues = new ContentValues();
        contentValues.put(TreeEntry.COLUMN_CREATED_AT, timeNow);
        contentValues.put(ChristmasTreeContract.TreeEntry.COLUMN_WATERED_AT, timeNow);
        getContentResolver().insert(ChristmasTreeContract.TreeEntry.CONTENT_URI, contentValues);
        TreeWateringService.startActionUpdatePlantWidgets(this);

        plantUI(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                Answers.getInstance().logShare(new ShareEvent());
                Intent intent =  new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String app_link = "https://play.google.com/store/apps/details?id=com.rudainc.christmastree";
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + app_link);
                startActivity(Intent.createChooser(intent, "Share with"));
                break;
        }
        return true;
    }


}
