package com.nedeos.ride.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v13.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nedeos.ride.RideApplication;

import java.math.BigDecimal;
import java.util.Locale;

public class AreaService extends Service {

    private FusedLocationProviderClient mFusedLocationClient;

    private LocationRequest mLocationRequest;

    private Location mCurrentLocation;

    private SharedPreferences preferences;

    private LocationCallback mLocationCallback;

    private final IBinder mBinder = new LocationBinder();

    public class LocationBinder extends Binder {

        public AreaService getService() {
            return AreaService.this;
        }
    }

    public AreaService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        if (checkPermission(getApplicationContext()) && (mFusedLocationClient != null) && (mLocationCallback != null) && (mLocationRequest != null)) {

            try {
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

            } catch (SecurityException e) {
                // e.printStackTrace();
            }
        }

        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();

                if (mCurrentLocation != null) {
                    onLocationChanged(mCurrentLocation);
                }
            }
        };

        mLocationRequest = new LocationRequest();

        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(50000L);
        mLocationRequest.setFastestInterval(5000L);
        mLocationRequest.setSmallestDisplacement(50.f);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (checkPermission(getApplicationContext()) && (mFusedLocationClient != null) && (mLocationCallback != null) && (mLocationRequest != null)) {

            try {
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

            } catch (SecurityException e) {
                // e.printStackTrace();
            }
        }
    }

    public static boolean checkPermission(final Context context) {
        return (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, final int startId) {

        if (preferences != null) {
            final float lat = preferences.getFloat(RideApplication.ARG_LATITUDE, 0.f);
            final float lng = preferences.getFloat(RideApplication.ARG_LONGITUDE, 0.f);

            if ((lat != 0.f) && (lng != 0.f)) {
                final String topic = String.format(Locale.US, "%.2f", round(lat)) + "x" + String.format(Locale.US, "%.2f", round(lng));
                FirebaseMessaging.getInstance().subscribeToTopic(topic);
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }

        if (preferences != null) {
            final float lat = preferences.getFloat(RideApplication.ARG_LATITUDE, 0.f);
            final float lng = preferences.getFloat(RideApplication.ARG_LONGITUDE, 0.f);

            if ((lat != 0.f) && (lng != 0.f)) {
                final String topic = String.format(Locale.US, "%.2f", round(lat)) + "x" + String.format(Locale.US, "%.2f", round(lng));
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
            }
        }
    }

    public void onLocationChanged(Location location) {

        if (location != null) {
            final float newlat = (float) location.getLatitude();
            final float newlng = (float) location.getLongitude();

            if (preferences != null) {
                final float curlat = preferences.getFloat(RideApplication.ARG_LATITUDE, 0.f);
                final float curlng = preferences.getFloat(RideApplication.ARG_LONGITUDE, 0.f);

                if ((round(newlat) != round(curlat) || (round(newlng) != round(curlng)))) {

                    if ((curlat != 0.f) && (curlng != 0.f)) {
                        final String topic = String.format(Locale.US, "%.2f", round(curlat)) + "x" + String.format(Locale.US, "%.2f", round(curlng));
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
                    }

                    if ((newlat != 0.f) && (newlng != 0.f)) {
                        final String topic = String.format(Locale.US, "%.2f", round(newlat)) + "x" + String.format(Locale.US, "%.2f", round(newlng));
                        FirebaseMessaging.getInstance().subscribeToTopic(topic);

                        SharedPreferences.Editor editor = preferences.edit();

                        editor.putFloat(RideApplication.ARG_LATITUDE, newlat).apply();
                        editor.putFloat(RideApplication.ARG_LONGITUDE, newlng).apply();

                        Intent localService = new Intent(AreaService.this, UpdateLocationService.class);
                        startService(localService);
                    }
                }
            }
        }
    }

    public void launchRequest() {

        if (checkPermission(getApplicationContext()) && (mFusedLocationClient != null) && (mLocationCallback != null) && (mLocationRequest != null)) {

            try {
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

            } catch (SecurityException e) {
                // e.printStackTrace();
            }
        }
    }

    private float round(float d) {
        BigDecimal bd = new BigDecimal(Float.toString(d));

        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

}
