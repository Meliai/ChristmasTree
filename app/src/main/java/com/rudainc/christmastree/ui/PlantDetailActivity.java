package com.rudainc.christmastree.ui;

/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rudainc.christmastree.PlantWateringService;
import com.rudainc.christmastree.R;
import com.rudainc.christmastree.provider.PlantContract;
import com.rudainc.christmastree.utils.PlantUtils;

import static android.provider.BaseColumns._ID;
import static com.rudainc.christmastree.provider.PlantContract.BASE_CONTENT_URI;
import static com.rudainc.christmastree.provider.PlantContract.PATH_PLANTS;
import static com.rudainc.christmastree.provider.PlantContract.PlantEntry;


public class PlantDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int SINGLE_LOADER_ID = 200;
    public static final String EXTRA_PLANT_ID = "com.rudainc.christmastree.extra.PLANT_ID";
    private static final int CHRISTMAS_TREE = 0;
    long mPlantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_detail);
//        mPlantId = getIntent().getLongExtra(EXTRA_PLANT_ID, INVALID_PLANT_ID);
        // This activity displays single plant information that is loaded using a cursor loader
//        getSupportLoaderManager().initLoader(SINGLE_LOADER_ID, null, this);
        getSupportLoaderManager().initLoader(SINGLE_LOADER_ID, null, this);
    }

    public void onWaterButtonClick(View view) {
        PlantWateringService.startActionWaterPlant(this, mPlantId);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        Uri SINGLE_PLANT_URI = ContentUris.withAppendedId(
//                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build(), mPlantId);
//        return new CursorLoader(this, SINGLE_PLANT_URI, null,
//                null, null, null);
        Uri PLANT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build();
        return new CursorLoader(this, PLANT_URI, null,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            ((FloatingActionButton) findViewById(R.id.plant_button)).setVisibility(View.VISIBLE);
            return;
        }
        ((FloatingActionButton) findViewById(R.id.reset_button)).setVisibility(View.VISIBLE);
        ((FloatingActionButton) findViewById(R.id.water_button)).setVisibility(View.VISIBLE);
        cursor.moveToFirst();
        int createTimeIndex = cursor.getColumnIndex(PlantEntry.COLUMN_CREATION_TIME);
        int waterTimeIndex = cursor.getColumnIndex(PlantEntry.COLUMN_LAST_WATERED_TIME);
        int planTypeIndex = cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_TYPE);
        mPlantId = cursor.getLong(cursor.getColumnIndex(_ID));
        int plantType = cursor.getInt(planTypeIndex);
        long createdAt = cursor.getLong(createTimeIndex);
        long wateredAt = cursor.getLong(waterTimeIndex);
        long timeNow = System.currentTimeMillis();

        int plantImgRes = PlantUtils.getPlantImageRes(this, timeNow - createdAt, timeNow - wateredAt, plantType);

        ((ImageView) findViewById(R.id.plant_detail_image)).setImageResource(plantImgRes);

        ((TextView) findViewById(R.id.plant_age_number)).setText(
                String.valueOf(PlantUtils.getDisplayAgeInt(timeNow - createdAt))
        );
        ((TextView) findViewById(R.id.plant_age_unit)).setText(
                PlantUtils.getDisplayAgeUnit(this, timeNow - createdAt)
        );
        ((TextView) findViewById(R.id.last_watered_number)).setText(
                String.valueOf(PlantUtils.getDisplayAgeInt(timeNow - wateredAt))
        );
        ((TextView) findViewById(R.id.last_watered_unit)).setText(
                PlantUtils.getDisplayAgeUnit(this, timeNow - wateredAt)
        );
        int waterPercent = 100 - ((int) (100 * (timeNow - wateredAt) / PlantUtils.MAX_AGE_WITHOUT_WATER));
        ((WaterLevelView) findViewById(R.id.water_level)).setValue(waterPercent);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void onCutButtonClick(View view) {

        (findViewById(R.id.reset_button)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.water_button)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.plant_detail_image)).setVisibility(View.INVISIBLE);
//        Uri SINGLE_PLANT_URI = ContentUris.withAppendedId(
//                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build(), mPlantId);
        Log.i("DELETE", "plant " + mPlantId);
//        Uri PLANT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build();
        getContentResolver().delete(BASE_CONTENT_URI, null, new String[]{String.valueOf(mPlantId)});
//        getContentResolver().notifyChange(PlantContract.BASE_CONTENT_URI, null);

        PlantWateringService.startActionUpdatePlantWidgets(this);
    }


    public void onPlantButtonClick(View view) {
        // When the chosen plant type is clicked, create a new plant and set the creation time and
        // water time to now
        // Extract the plant type from the tag
//        ImageView imgView = (ImageView) view.findViewById(R.id.plant_type_image);
        ((FloatingActionButton) findViewById(R.id.plant_button)).setVisibility(View.INVISIBLE);
        int plantType = CHRISTMAS_TREE;
        getContentResolver().notifyChange(PlantContract.BASE_CONTENT_URI, null);
        long timeNow = System.currentTimeMillis();
        // Insert the new plant into DB
        ContentValues contentValues = new ContentValues();
        contentValues.put(PlantContract.PlantEntry.COLUMN_PLANT_TYPE, plantType);
        contentValues.put(PlantContract.PlantEntry.COLUMN_CREATION_TIME, timeNow);
        contentValues.put(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME, timeNow);
        getContentResolver().insert(PlantContract.PlantEntry.CONTENT_URI, contentValues);
        PlantWateringService.startActionUpdatePlantWidgets(this);
    }


}
