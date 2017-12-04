package com.nedeos.ride;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.nedeos.ride.accounts.AccountFragment;
import com.nedeos.ride.messages.RideDBHelper;
import com.nedeos.ride.messages.Subscription;
import com.nedeos.ride.messages.ViewFragment;
import com.nedeos.ride.purchases.PurchaseFragment;
import com.nedeos.ride.services.AreaService;
import com.nedeos.ride.services.UpdateLocationService;
import com.nedeos.ride.services.UpdateSubscriptionService;
import com.nedeos.ride.settings.LocationDialogFragment;
import com.nedeos.ride.settings.SettingsFragment;
import com.nedeos.ride.users.UserFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class RideActivity extends AppCompatActivity implements ViewFragment.OnViewListener, PurchaseFragment.OnPurchaseListener,
        AccountFragment.OnAccountListener, UserFragment.OnUserListener {

    private final static int MULTIPLE_ACCESS = 60;

    private AreaService locationService;

    private String identify = "";
    private String password = "";

    private int layoutPosition = 0;

    BottomNavigationView navigation;

    SharedPreferences preferences;

    Toolbar toolbar;

    private MessageReceiver messageReceiver;

    private SearchView searchView;

    private SearchView.OnQueryTextListener queryTextListener;

    FragmentManager mainFragmentManager;

    FragmentManager.OnBackStackChangedListener onBackStackChangedListener;

    private ServiceConnection userServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AreaService.LocationBinder b = (AreaService.LocationBinder) service;

            if (b != null) {
                locationService = b.getService();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ride);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (toolbar != null) {
            toolbar.setNavigationIcon(R.mipmap.ic_launcher);
        }

        navigation = findViewById(R.id.navigation);

        if (navigation != null) {
            navigation.inflateMenu(R.menu.navigation);

            BottomNavigationViewHelper.removeShiftMode(navigation);

            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        messageReceiver = new MessageReceiver();

        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(RideApplication.ARG_IMAGE_UPLOAD_FAILED);

        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, intentFilter);

        mainFragmentManager = getFragmentManager();

        if (mainFragmentManager != null) {

            onBackStackChangedListener = new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    Fragment fragment = mainFragmentManager.findFragmentById(R.id.content_ride);

                    if (fragment instanceof ViewFragment) {

                        if (navigation != null) {
                            navigation.getMenu().getItem(0).setChecked(true);
                        }

                    } else if (fragment instanceof PurchaseFragment) {

                        if (navigation != null) {
                            navigation.getMenu().getItem(1).setChecked(true);
                        }

                    } else if (fragment instanceof UserFragment) {

                        if (navigation != null) {
                            navigation.getMenu().getItem(2).setChecked(true);
                        }

                    } else if (fragment instanceof AccountFragment) {

                        if (navigation != null) {
                            navigation.getMenu().getItem(3).setChecked(true);
                        }
                    }
                }
            };

            mainFragmentManager.addOnBackStackChangedListener(onBackStackChangedListener);

            mainFragmentManager.beginTransaction()
                    .replace(R.id.content_ride, ViewFragment.newInstance(layoutPosition), "ViewFragment")
                    .addToBackStack("ViewFragment")
                    .commit();
        }

        if (savedInstanceState != null) {
            identify = savedInstanceState.getString(RideApplication.ARG_IDENTIFY, "");
            password = savedInstanceState.getString(RideApplication.ARG_PASSWORD, "");
        }

        Intent localService = new Intent(RideActivity.this, AreaService.class);

        if (!runningLocationService()) {
            startService(localService);
        }

        Intent subscriptionService = new Intent(this, UpdateSubscriptionService.class);
        startService(subscriptionService);

        if (userServiceConnection != null) {
            bindService(localService, userServiceConnection, Context.BIND_AUTO_CREATE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(this, CAMERA) +
                    ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) +
                    ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{CAMERA,
                        ACCESS_FINE_LOCATION, WRITE_EXTERNAL_STORAGE}, MULTIPLE_ACCESS);
            }
        }

        if (preferences != null) {
            identify = preferences.getString(RideApplication.ARG_IDENTIFY, "");
            password = preferences.getString(RideApplication.ARG_PASSWORD, "");

            String ca = "A41";
            String co = "X9c";
            String ci = "0F2";

            identify = "RC-" + Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            password = "CL-" + Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID) + ca + co + ci;

            SharedPreferences.Editor editor = preferences.edit();

            editor.putString(RideApplication.ARG_IDENTIFY, identify).apply();
            editor.putString(RideApplication.ARG_PASSWORD, password).apply();

            Intent updateService = new Intent(this, UpdateLocationService.class);
            startService(updateService);
        }

        if (savedInstanceState == null) {
            LocationManager lm = (LocationManager) getSystemService(RideActivity.LOCATION_SERVICE);

            if (lm != null) {
                boolean gps_enabled = false;
                boolean net_enabled = false;

                try {
                    gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

                } catch (Exception e) {
                    // e.printStackTrace();
                }

                try {
                    net_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                } catch (Exception e) {
                    // e.printStackTrace();
                }

                if (!gps_enabled && !net_enabled) {
                    ActivateLocation();
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString(RideApplication.ARG_IDENTIFY, identify);
        savedInstanceState.putString(RideApplication.ARG_PASSWORD, password);

        savedInstanceState.putInt(RideApplication.ARG_LAYOUT_POSITION, layoutPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            identify = savedInstanceState.getString(RideApplication.ARG_IDENTIFY, "");
            password = savedInstanceState.getString(RideApplication.ARG_PASSWORD, "");

            layoutPosition = savedInstanceState.getInt(RideApplication.ARG_LAYOUT_POSITION, 0);
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getFragmentManager();

        if (fragmentManager != null) {
            Fragment fragment = fragmentManager.findFragmentById(R.id.content_ride);

            if (fragmentManager.getBackStackEntryCount() == 1) {
                finish();

            } else if (fragmentManager.getBackStackEntryCount() == 0) {
                super.onBackPressed();

            } else {

                if (fragment != null) {

                    if (fragment instanceof ViewFragment) {
                        finish();

                    } else {
                        fragmentManager.popBackStack();

                        if (fragment instanceof PurchaseFragment) {

                            if (navigation != null) {
                                navigation.getMenu().getItem(0).setChecked(true);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);

        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();

            searchItem.collapseActionView();

            searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {

                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    InputMethodManager methodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                    if ((methodManager != null) && (searchView != null)) {
                        methodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                    }

                    return true;
                }
            });
        }

        if ((searchManager != null) && (searchView != null)) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

            final TextView searchText = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);

            if (searchText != null) {
                Typeface searchFont = Typeface.create("sans-serif-condensed", Typeface.NORMAL);

                if (searchFont != null) {
                    searchText.setTypeface(searchFont);
                }

                searchText.setText("");
            }

            queryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(final String query) {

                    InputMethodManager methodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                    if ((methodManager != null) && (searchView != null)) {
                        methodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                    }

                    Handler handler = new Handler(Looper.getMainLooper());

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (searchItem != null) {
                                searchItem.collapseActionView();

                                searchItem.setIcon(R.mipmap.ic_magnify_white);
                            }
                        }
                    }, 100);

                    RideDBHelper.delAllFromDB(RideApplication.DB_FOUNDSUBS_TABLE);

                    Fragment fragment = getFragmentManager().findFragmentById(R.id.content_ride);

                    if (fragment != null) {

                        if (fragment instanceof PurchaseFragment) {
                            // Log.d("Frag", "purchase");

                        } else if (fragment instanceof ViewFragment) {
                            // Log.d("Frag", "view");
                        }
                    }

                    if ((query != null) && query.trim().length() > 2) {

                        try {
                            final JSONObject jsonObject = new JSONObject();

                            jsonObject.put(RideApplication.ARG_IDENTIFY, preferences.getString(RideApplication.ARG_IDENTIFY, ""));
                            jsonObject.put(RideApplication.ARG_PASSWORD, preferences.getString(RideApplication.ARG_PASSWORD, ""));

                            jsonObject.put(RideApplication.ARG_SINGLE, "no");

                            jsonObject.put(RideApplication.ARG_QUERY, query);

                            final OkHttpClient client = RideApplication.getOkHttpClient();

                            if (client != null) {
                                RequestBody body = RequestBody.create(RideApplication.JSON, jsonObject.toString());

                                Request request = new Request.Builder()
                                        .url("https://ride.barubox.com/queryallSubs")
                                        .post(body)
                                        .build();

                                client.newCall(request).enqueue(new Callback() {

                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {

                                        if (response.isSuccessful()) {

                                            try {
                                                final JSONObject jsonObject = new JSONObject(response.body().string());

                                                if (jsonObject.length() > 0) {
                                                    JSONArray jsonSubs = jsonObject.getJSONArray("subs");

                                                    if ((jsonSubs != null) && (jsonSubs.length() > 0)) {

                                                        for (int i = 0; i < jsonSubs.length(); i++) {
                                                            JSONObject object = jsonSubs.getJSONObject(i);

                                                            if (object != null) {
                                                                Subscription subscription = new Subscription();

                                                                subscription.setProd(object.optString(RideApplication.ARG_PROD));
                                                                subscription.setHead(object.optString(RideApplication.ARG_HEAD));
                                                                subscription.setText(object.optString(RideApplication.ARG_TEXT));
                                                                subscription.setIcon(object.optString(RideApplication.ARG_ICON));
                                                                subscription.setShot(object.optString(RideApplication.ARG_SHOT));
                                                                subscription.setCost(object.optString(RideApplication.ARG_COST));
                                                                subscription.setUsed(object.optString(RideApplication.ARG_USED));
                                                                subscription.setStat(object.optInt(RideApplication.ARG_STAT));
                                                                subscription.setTime(object.optInt(RideApplication.ARG_TIME));
                                                                subscription.setUsid(object.optInt(RideApplication.ARG_USID));

                                                                RideDBHelper.addSubToDB(subscription, RideApplication.DB_FOUNDSUBS_TABLE);
                                                            }
                                                        }
                                                    }

                                                    Handler handler = new Handler(Looper.getMainLooper());

                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {

                                                            FragmentManager fragmentManager = getFragmentManager();

                                                            if ((fragmentManager != null) && (query.length() > 0)) {

                                                                if (navigation != null) {
                                                                    navigation.setSelectedItemId(R.id.navigation_shop);
                                                                }

                                                                fragmentManager.beginTransaction()
                                                                        .replace(R.id.content_ride, PurchaseFragment.newInstance(0, query), "PurchaseFragment")
                                                                        .addToBackStack("PurchaseFragment")
                                                                        .commit();
                                                            }

                                                        }
                                                    });
                                                }

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        response.body().close();
                                    }
                                });
                            }

                        } catch (Exception e) {
                            // e.printStackTrace();
                        }

                    } else {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ViewGroup container = (CoordinatorLayout) getWindow().getDecorView().findViewById(R.id.ride_coordinatorlayout);

                                if (container != null) {
                                    Snackbar.make(container, R.string.too_short, Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            };

            searchView.setOnQueryTextListener(queryTextListener);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(R.string.news);
        }

        Fragment fragment = getFragmentManager().findFragmentById(R.id.content_ride);

        if (fragment != null) {

            if (fragment instanceof SettingsFragment) {

                if (navigation != null) {
                    navigation.setVisibility(View.GONE);
                }

            } else if (navigation != null) {
                navigation.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_search:

                if (searchView != null) {
                    searchView.setOnQueryTextListener(queryTextListener);
                }

                return true;

            case R.id.action_settings:

                InputMethodManager methodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                if ((methodManager != null) && (searchView != null)) {
                    methodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                }

                FragmentManager fragmentManager = getFragmentManager();

                if (fragmentManager != null) {
                    Fragment fragment = fragmentManager.findFragmentById(R.id.content_ride);

                    if (fragment != null) {

                        if (!(fragment instanceof SettingsFragment)) {
                            Intent updateService = new Intent(this, UpdateLocationService.class);
                            startService(updateService);

                            fragmentManager.beginTransaction()
                                    .replace(R.id.content_ride, SettingsFragment.newInstance(), "SettingsFragment")
                                    .addToBackStack("SettingsFragment")
                                    .commit();

                        } else {
                            fragmentManager.popBackStack();
                        }
                    }
                }

                return true;
        }

        return false;
    }

    @Override
    public void onStop() {
        super.onStop();

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager != null) {
            manager.cancelAll();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (messageReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
            messageReceiver = null;
        }

        if (locationService != null) {
            unbindService(userServiceConnection);
            locationService = null;
        }

        if (mainFragmentManager != null) {
            mainFragmentManager.removeOnBackStackChangedListener(onBackStackChangedListener);
            onBackStackChangedListener = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        switch (requestCode) {

            case MULTIPLE_ACCESS:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                    if (locationService != null) {
                        locationService.launchRequest();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "No permissions", Toast.LENGTH_SHORT).show();
                }

                return;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void ActivateLocation() {
        FragmentManager fragmentManager = getFragmentManager();

        if (fragmentManager != null) {
            LocationDialogFragment locationDialogFragment = new LocationDialogFragment();

            fragmentManager.beginTransaction()
                    .add(locationDialogFragment, "LocationDialogFragment")
                    .commit();
        }
    }

    private boolean runningLocationService() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (AreaService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void onViewFragment(int mLayoutPosition, final int errorCode) {

        if (mLayoutPosition != -1) {
            layoutPosition = mLayoutPosition;
        }

        if (errorCode != -1) {
            Handler handler = new Handler(Looper.getMainLooper());

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ViewGroup container = (CoordinatorLayout) getWindow().getDecorView().findViewById(R.id.ride_coordinatorlayout);

                    if (container != null) {
                        String sResult = "";

                        if (errorCode == 0) {
                            sResult = getString(R.string.successfully_deleted);
                        }

                        if (sResult.length() > 0) {
                            Snackbar snackbar = Snackbar.make(container, sResult, Snackbar.LENGTH_SHORT);

                            snackbar.show();
                        }
                    }
                }
            }, 100);
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            FragmentManager fragmentManager = getFragmentManager();

            if (fragmentManager != null) {
                Fragment fragment = fragmentManager.findFragmentById(R.id.content_ride);

                switch (item.getItemId()) {

                    case R.id.navigation_home:

                        if ((fragment != null) && !(fragment instanceof ViewFragment)) {
                            fragmentManager.beginTransaction()
                                    .replace(R.id.content_ride, ViewFragment.newInstance(layoutPosition), "ViewFragment")
                                    .addToBackStack("ViewFragment")
                                    .commit();
                        }

                        return true;

                    case R.id.navigation_shop:

                        if (fragment != null) {
                            fragmentManager.beginTransaction()
                                    .replace(R.id.content_ride, PurchaseFragment.newInstance(layoutPosition, ""), "PurchaseFragment")
                                    .addToBackStack("PurchaseFragment")
                                    .commit();
                        }

                        return true;

                    case R.id.navigation_locals:

                        if (fragment != null) {
                            fragmentManager.beginTransaction()
                                    .replace(R.id.content_ride, UserFragment.newInstance(), "UserFragment")
                                    .addToBackStack("UserFragment")
                                    .commit();
                        }

                        return true;

                    case R.id.navigation_account:

                        if (fragment != null) {
                            fragmentManager.beginTransaction()
                                    .replace(R.id.content_ride, AccountFragment.newInstance(), "AccountFragment")
                                    .addToBackStack("AccountFragment")
                                    .commit();
                        }

                        return true;
                }
            }

            return false;
        }
    };

    @Override
    public void onPurchaseFragment(final int subscriptionStatus) {

        Handler handler = new Handler(Looper.getMainLooper());

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewGroup container = (CoordinatorLayout) getWindow().getDecorView().findViewById(R.id.ride_coordinatorlayout);

                if (container != null) {
                    String sResult = "";

                    if (subscriptionStatus == 0) {
                        sResult = getString(R.string.successfully_subscribed);

                        Intent subscriptionService = new Intent(RideActivity.this, UpdateSubscriptionService.class);
                        startService(subscriptionService);

                    } else if (subscriptionStatus == 1) {
                        sResult = getString(R.string.failed_to_subscribe);

                    } else if (subscriptionStatus == 2) {
                        sResult = getString(R.string.successfully_unsubscribed);

                        Intent subscriptionService = new Intent(RideActivity.this, UpdateSubscriptionService.class);
                        startService(subscriptionService);

                    } else if (subscriptionStatus == 3) {
                        sResult = getString(R.string.failed_to_unsubscribe);

                    } else if (subscriptionStatus == 4) {
                        sResult = getString(R.string.successfully_updated);

                        Intent subscriptionService = new Intent(RideActivity.this, UpdateSubscriptionService.class);
                        startService(subscriptionService);

                    } else if (subscriptionStatus == 5) {
                        sResult = getString(R.string.not_subscribed);

                    } else {
                        sResult = getString(R.string.general_error);
                    }

                    if (sResult.length() > 0) {
                        Snackbar snackbar = Snackbar.make(container, sResult, Snackbar.LENGTH_SHORT);

                        snackbar.show();
                    }
                }
            }
        }, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean handled = false;

        Fragment fragment = getFragmentManager().findFragmentById(R.id.content_ride);

        if (fragment != null) {

            if (fragment instanceof PurchaseFragment) {
                handled = ((PurchaseFragment) fragment).checkIabHelperHandleActivityResult(requestCode, resultCode, data);
            }
        }

        if (!handled) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onAccountFragment(Uri uri) {

    }

    @Override
    public void onUserFragment(final int userStatus) {

        Handler handler = new Handler(Looper.getMainLooper());

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewGroup container = (CoordinatorLayout) getWindow().getDecorView().findViewById(R.id.ride_coordinatorlayout);

                if (container != null) {
                    String sResult = "";

                    if (userStatus == 0) {
                        sResult = getString(R.string.added_to_favorites);

                    } else if (userStatus == 1) {
                        sResult = getString(R.string.failed_to_add_to_favorites);

                    } else if (userStatus == 2) {
                        sResult = getString(R.string.user_blocked);

                    } else if (userStatus == 3) {
                        sResult = getString(R.string.failed_to_block_user);

                    } else if (userStatus == 5) {
                        sResult = getString(R.string.already_saved);

                    } else {
                        sResult = getString(R.string.general_error);
                    }

                    if (sResult.length() > 0) {
                        Snackbar snackbar = Snackbar.make(container, sResult, Snackbar.LENGTH_SHORT);

                        snackbar.show();
                    }
                }
            }
        }, 100);
    }

    private class MessageReceiver extends BroadcastReceiver {

        public MessageReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (action != null) {

                if (action.equals(RideApplication.ARG_IMAGE_UPLOAD_FAILED)) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ViewGroup container = (CoordinatorLayout) getWindow().getDecorView().findViewById(R.id.ride_coordinatorlayout);

                            if (container != null) {
                                Snackbar.make(container, getString(R.string.image_upload_failed), Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }
    }

    private static class BottomNavigationViewHelper {

        @SuppressLint("RestrictedApi")
        static void removeShiftMode(BottomNavigationView view) {
            BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);

            if (menuView != null) {

                try {
                    Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");

                    shiftingMode.setAccessible(true);
                    shiftingMode.setBoolean(menuView, false);
                    shiftingMode.setAccessible(false);

                    for (int i = 0; i < menuView.getChildCount(); i++) {
                        BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);

                        item.setShiftingMode(false);

                        item.setSelected(item.getItemData().isChecked());
                    }

                    menuView.updateMenuView();

                } catch (NoSuchFieldException e) {
                    // e.printStackTrace();

                } catch (IllegalAccessException e) {
                    // e.printStackTrace();
                }
            }
        }
    }

}
