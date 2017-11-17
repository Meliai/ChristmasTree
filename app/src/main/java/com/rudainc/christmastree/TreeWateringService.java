package com.rudainc.christmastree;

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

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.rudainc.christmastree.provider.ChristmasTreeContract;
import com.rudainc.christmastree.utils.TreeUtils;

import static com.rudainc.christmastree.provider.ChristmasTreeContract.BASE_CONTENT_URI;
import static com.rudainc.christmastree.provider.ChristmasTreeContract.INVALID_PLANT_ID;
import static com.rudainc.christmastree.provider.ChristmasTreeContract.PATH;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class TreeWateringService extends IntentService {

    public static final String ACTION_WATER_PLANT = "com.rudainc.christmastree.action.water_plant";
    public static final String ACTION_UPDATE_PLANT_WIDGETS = "com.rudainc.christmastree.action.update_plant_widgets";
    public static final String EXTRA_PLANT_ID = "com.rudainc.christmastree .extra.PLANT_ID";
    ;

    public TreeWateringService() {
        super("TreeWateringService");
    }

    /**
     * Starts this service to perform WaterPlant action with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionWaterPlant(Context context, long plantId) {
        Intent intent = new Intent(context, TreeWateringService.class);
        intent.setAction(ACTION_WATER_PLANT);
        intent.putExtra(EXTRA_PLANT_ID, plantId);
        context.startService(intent);
    }

    /**
     * Starts this service to perform UpdatePlantWidgets action with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdatePlantWidgets(Context context) {
        Intent intent = new Intent(context, TreeWateringService.class);
        intent.setAction(ACTION_UPDATE_PLANT_WIDGETS);
        context.startService(intent);
    }

    /**
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_WATER_PLANT.equals(action)) {
                final long plantId = intent.getLongExtra(EXTRA_PLANT_ID,
                        INVALID_PLANT_ID);
                handleActionWaterPlant(plantId);
            } else if (ACTION_UPDATE_PLANT_WIDGETS.equals(action)) {
                handleActionUpdatePlantWidgets();
            }
        }
    }

    /**
     * Handle action WaterPlant in the provided background thread with the provided
     * parameters.
     */
    private void handleActionWaterPlant(long plantId) {
        Uri SINGLE_PLANT_URI = ContentUris.withAppendedId(
                BASE_CONTENT_URI.buildUpon().appendPath(PATH).build(), plantId);
        ContentValues contentValues = new ContentValues();
        long timeNow = System.currentTimeMillis();
        contentValues.put(ChristmasTreeContract.TreeEntry.COLUMN_WATERED_AT, timeNow);
        // Update only if that plant is still alive
        getContentResolver().update(
                SINGLE_PLANT_URI,
                contentValues,
                ChristmasTreeContract.TreeEntry.COLUMN_WATERED_AT + ">?",
                new String[]{String.valueOf(timeNow - TreeUtils.MAX_AGE_WITHOUT_WATER)});
        // Always update widgets after watering plants
        startActionUpdatePlantWidgets(this);
    }


    /**
     * Handle action UpdatePlantWidgets in the provided background thread
     */
    private void handleActionUpdatePlantWidgets() {
        //Query to get the plant that's most in need for water (last watered)
        Uri PLANT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();
        Cursor cursor = getContentResolver().query(
                PLANT_URI,
                null,
                null,
                null,
                ChristmasTreeContract.TreeEntry.COLUMN_WATERED_AT
        );
        // Extract the plant details
        int imgRes = R.drawable.grass; // Default image in case our garden is empty
        boolean canWater = false; // Default to hide the water drop button
        long plantId = INVALID_PLANT_ID;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int idIndex = cursor.getColumnIndex(ChristmasTreeContract.TreeEntry._ID);
            int createTimeIndex = cursor.getColumnIndex(ChristmasTreeContract.TreeEntry.COLUMN_CREATED_AT);
            int waterTimeIndex = cursor.getColumnIndex(ChristmasTreeContract.TreeEntry.COLUMN_WATERED_AT);
            plantId = cursor.getLong(idIndex);
            long timeNow = System.currentTimeMillis();
            long wateredAt = cursor.getLong(waterTimeIndex);
            long createdAt = cursor.getLong(createTimeIndex);

            cursor.close();
            canWater = (timeNow - wateredAt) > TreeUtils.MIN_AGE_BETWEEN_WATER &&
                    (timeNow - wateredAt) < TreeUtils.MAX_AGE_WITHOUT_WATER;
            imgRes = TreeUtils.getPlantImageRes(this, timeNow - createdAt, timeNow - wateredAt);
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, TreeWidgetProvider.class));
        //Trigger data update to handle the GridView widgets and force a data refresh
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_grid_view);
        //Now update all widgets
        TreeWidgetProvider.updatePlantWidgets(this, appWidgetManager, imgRes, plantId, canWater, appWidgetIds);
    }
}
