package com.rudainc.christmastree.ui;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.ShareEvent;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
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
    private AdView mAdView;
    private InterstitialAd mInterstitialAdRecover;
    private InterstitialAd mInterstitialAd;
    private boolean isAdsLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics(), new Answers());
        MobileAds.initialize(this, getString(R.string.app_id_ads));

        setContentView(R.layout.activity_christmas_tree);
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        loadAds();
        loadInAds();
        loadInAdsRecover();
    }

    public void onWaterButtonClick(View view) {
        TreeWateringService.startActionWaterPlant(this, id);
    }

    private void loadAds() {
        mAdView = findViewById(R.id.my_ads_banner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                isAdsLoaded = true;
            }

        });
    }

    private void loadInAdsRecover() {
        mInterstitialAdRecover = new InterstitialAd(this);
        mInterstitialAdRecover.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));
        mInterstitialAdRecover.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();

            }

        });
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAdRecover.loadAd(adRequest);
        mInterstitialAdRecover.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                Uri SINGLE_PLANT_URI = ContentUris.withAppendedId(
                        BASE_CONTENT_URI.buildUpon().appendPath(PATH).build(), id);
                long timeNow = System.currentTimeMillis();
                ContentValues contentValues = new ContentValues();
                contentValues.put(ChristmasTreeContract.TreeEntry.COLUMN_WATERED_AT, timeNow - TreeUtils.DANGER_AGE_WITHOUT_WATER);
                // Update only if that plant is still alive
                getContentResolver().update(
                        SINGLE_PLANT_URI,
                        contentValues,
                        null,
                        null);
                Answers.getInstance().logCustom(new CustomEvent(getString(R.string.ce_recover_tree)));
            }
        });
    }

    private void loadInAds() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();

            }

        });
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
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
        (findViewById(R.id.reset_button)).setVisibility(isPlanted && isAdsLoaded ? View.VISIBLE : View.INVISIBLE);
        (findViewById(R.id.water_button)).setVisibility(isPlanted && isAdsLoaded ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void onCutButtonClick(View view) {
        if (TreeUtils.getStatus().equals(TreeUtils.PlantStatus.DEAD))
            showResurrectDialog();
        else
            cut();
    }

    private void showResurrectDialog() {
        AlertDialog.Builder ad = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_title))
                .setMessage(getString(R.string.dialog_message))
                .setPositiveButton(getString(R.string.btn_positive), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        if (isOnline(getApplicationContext()))
                            mInterstitialAdRecover.show();
                        else
                            Toast.makeText(getApplicationContext(), getString(R.string.internet), Toast.LENGTH_SHORT).show();
                    }
                });
        ad.setNegativeButton(getString(R.string.btn_negative), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                cut();
            }
        });
        ad.setCancelable(true);

        ad.show();
    }

    private void cut() {
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
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String app_link = "https://play.google.com/store/apps/details?id=com.rudainc.christmastree";
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + app_link);
                startActivity(Intent.createChooser(intent, "Share with"));
                break;
            case R.id.ads:
                Answers.getInstance().logCustom(new CustomEvent(getString(R.string.ce_open_ads)));
                mInterstitialAd.show();
                break;
        }
        return true;
    }

    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

}
