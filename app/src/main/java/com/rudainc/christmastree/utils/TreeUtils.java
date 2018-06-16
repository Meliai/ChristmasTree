package com.rudainc.christmastree.utils;

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

import android.content.Context;

import com.rudainc.christmastree.R;

public class TreeUtils {

    private static final long MINUTE_MILLISECONDS = 1000 * 60;
    private static final long HOUR_MILLISECONDS = MINUTE_MILLISECONDS * 60;
    private static final long DAY_MILLISECONDS = HOUR_MILLISECONDS * 24;

    public static final long MIN_AGE_BETWEEN_WATER = HOUR_MILLISECONDS * 2; // can water every 2 hours
    public static final long DANGER_AGE_WITHOUT_WATER = HOUR_MILLISECONDS * 6; // in danger after 6 hours
    public static final long MAX_AGE_WITHOUT_WATER = HOUR_MILLISECONDS * 12; // plants die after 12 hours
    private static final long TINY_AGE = 0L; // plants start tiny
    private static final long SMALL_AGE = DAY_MILLISECONDS * 2; // 2 day old
    private static final long JUVENILE_AGE = DAY_MILLISECONDS * 4; // 4 day old
    private static final long MIDDLE_AGE = DAY_MILLISECONDS * 6; // 6 day old
    private static final long FULLY_GROWN_AGE = DAY_MILLISECONDS * 8; // 8 days old
    private static final long CHRISTMAS_AGE = DAY_MILLISECONDS * 10; //10 days old

    private static final String KEY_DEF_TYPE = "drawable";

    private static PlantStatus status;

    public enum PlantStatus {ALIVE, DYING, DEAD}

    public enum PlantSize {TINY, SMALL, JUVENILE, MIDDLE, FULLY_GROWN, CHRISTMAS}

    /**
     * Returns the corresponding image resource of the plant given the plant's age and
     * time since it was last watered
     *
     * @param plantAge Time (in milliseconds) the plant has been alive
     * @param waterAge Time (in milliseconds) since it was last watered
     * @return Image Resource to the correct plant image
     */
    public static int getPlantImageRes(Context context, long plantAge, long waterAge) {
        //check if plant is dead first
        status = PlantStatus.ALIVE;
        if (waterAge > MAX_AGE_WITHOUT_WATER) status = PlantStatus.DEAD;
        else if (waterAge > DANGER_AGE_WITHOUT_WATER) status = PlantStatus.DYING;

        //Update image if old enough
        if (plantAge > CHRISTMAS_AGE) {
            return getPlantImgRes(context, status, PlantSize.CHRISTMAS);

        } else if (plantAge > FULLY_GROWN_AGE) {
            return getPlantImgRes(context, status, PlantSize.FULLY_GROWN);
        } else if (plantAge > MIDDLE_AGE) {
            return getPlantImgRes(context, status, PlantSize.MIDDLE);
        } else if (plantAge > JUVENILE_AGE) {
            return getPlantImgRes(context, status, PlantSize.JUVENILE);
        } else if (plantAge > SMALL_AGE) {
            return getPlantImgRes(context, status, PlantSize.SMALL);
        } else if (plantAge > TINY_AGE) {
            return getPlantImgRes(context, status, PlantSize.TINY);
        } else {
            return R.drawable.empty_pot;
        }
    }

    public static PlantStatus getStatus() {
        return status;
    }

    /**
     * Returns the corresponding image resource of the plant given the plant's type, status and
     * size (age category)
     *
     * @param context The context
     * @param status  The PlantStatus
     * @param size    The PlantSize
     * @return Image Resource to the correct plant image
     */
    private static int getPlantImgRes(Context context, PlantStatus status, PlantSize size) {
        String resName = "tree";
        switch (status) {
            case DYING:
                resName += "_danger";
                break;
            case DEAD:
                resName += "_dead";
                break;
        }

        switch (size) {
            case TINY:
                resName += "_1";
                break;
            case SMALL:
                resName += "_2";
                break;
            case JUVENILE:
                resName += "_3";
                break;
            case MIDDLE:
                resName += "_4";
                break;
            case FULLY_GROWN:
                resName += "_5";
                break;
            case CHRISTMAS:
                resName += "_6";
                break;
        }

        return context.getResources().
                getIdentifier(resName, KEY_DEF_TYPE, context.getPackageName());
    }


    /**
     * Converts the age in milli seconds to a displayable format (days, hours or minutes)
     *
     * @param milliSeconds The age in milli seconds
     * @return The value of either days, hours or minutes
     */
    public static int getDisplayAgeInt(long milliSeconds) {
        int days = (int) (milliSeconds / DAY_MILLISECONDS);
        if (days >= 1) return days;
        int hours = (int) (milliSeconds / HOUR_MILLISECONDS);
        if (hours >= 1) return hours;
        return (int) (milliSeconds / MINUTE_MILLISECONDS);
    }

    /**
     * Converts the age in milli seconds to a displayable format (days, hours or minutes)
     *
     * @param context      The context
     * @param milliSeconds The age in milli seconds
     * @return The unit of either days, hours or minutes
     */
    public static String getDisplayAgeUnit(Context context, long milliSeconds) {
        int days = (int) (milliSeconds / DAY_MILLISECONDS);
        if (days >= 1) return context.getString(R.string.days);
        int hours = (int) (milliSeconds / HOUR_MILLISECONDS);
        if (hours >= 1) return context.getString(R.string.hours);
        return context.getString(R.string.minutes);
    }
}
