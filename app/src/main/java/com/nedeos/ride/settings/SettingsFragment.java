package com.nedeos.ride.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ListView;

import com.nedeos.ride.R;
import com.nedeos.ride.RideApplication;
import com.nedeos.ride.services.UpdateLocationService;

/**
 * Created by Karsten on 28.09.2016.
 */

public class SettingsFragment extends PreferenceFragment {

    private SharedPreferences preferences;

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();

        Bundle args = new Bundle();

        fragment.setArguments(args);

        return fragment;
    }

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        RideSwitchPreference newsAndAlerts = (RideSwitchPreference) getPreferenceScreen().findPreference("news_alerts");

        if (newsAndAlerts != null) {

            newsAndAlerts.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    RideSwitchPreference switchPreference = (RideSwitchPreference) preference;

                    if (switchPreference != null) {
                        boolean isChecked = (boolean) newValue;

                        if (switchPreference.isChecked() != isChecked) {
                            SharedPreferences.Editor editor = preferences.edit();

                            int category = preferences.getInt(RideApplication.ARG_CATEGORY, 255);

                            if (isChecked) {
                                category |= 1;
                                editor.putInt(RideApplication.ARG_CATEGORY, category).apply();

                            } else {
                                category &= 254;
                                editor.putInt(RideApplication.ARG_CATEGORY, category).apply();
                            }

                            Intent updateService = new Intent(getActivity(), UpdateLocationService.class);
                            getActivity().startService(updateService);
                        }
                    }

                    return true;
                }
            });
        }

        RideSwitchPreference travelParking = (RideSwitchPreference) getPreferenceScreen().findPreference("travel_parking");

        if (travelParking != null) {

            travelParking.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    RideSwitchPreference switchPreference = (RideSwitchPreference) preference;

                    if (switchPreference != null) {
                        boolean isChecked = (boolean) newValue;

                        if (switchPreference.isChecked() != isChecked) {
                            SharedPreferences.Editor editor = preferences.edit();

                            int category = preferences.getInt(RideApplication.ARG_CATEGORY, 255);

                            if (isChecked) {
                                category |= 2;
                                editor.putInt(RideApplication.ARG_CATEGORY, category).apply();

                            } else {
                                category &= 253;
                                editor.putInt(RideApplication.ARG_CATEGORY, category).apply();
                            }

                            Intent updateService = new Intent(getActivity(), UpdateLocationService.class);
                            getActivity().startService(updateService);
                        }
                    }

                    return true;
                }
            });
        }

        RideSwitchPreference eventsShows = (RideSwitchPreference) getPreferenceScreen().findPreference("events_shows");

        if (eventsShows != null) {

            eventsShows.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    RideSwitchPreference switchPreference = (RideSwitchPreference) preference;

                    if (switchPreference != null) {
                        boolean isChecked = (boolean) newValue;

                        if (switchPreference.isChecked() != isChecked) {
                            SharedPreferences.Editor editor = preferences.edit();

                            int category = preferences.getInt(RideApplication.ARG_CATEGORY, 255);

                            if (isChecked) {
                                category |= 4;
                                editor.putInt(RideApplication.ARG_CATEGORY, category).apply();

                            } else {
                                category &= 251;
                                editor.putInt(RideApplication.ARG_CATEGORY, category).apply();
                            }

                            Intent updateService = new Intent(getActivity(), UpdateLocationService.class);
                            getActivity().startService(updateService);
                        }
                    }

                    return true;
                }
            });
        }

        RideSwitchPreference shoppingSales = (RideSwitchPreference) getPreferenceScreen().findPreference("shopping_sales");

        if (shoppingSales != null) {

            shoppingSales.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    RideSwitchPreference switchPreference = (RideSwitchPreference) preference;

                    if (switchPreference != null) {
                        boolean isChecked = (boolean) newValue;

                        if (switchPreference.isChecked() != isChecked) {
                            SharedPreferences.Editor editor = preferences.edit();

                            int category = preferences.getInt(RideApplication.ARG_CATEGORY, 255);

                            if (isChecked) {
                                category |= 8;
                                editor.putInt(RideApplication.ARG_CATEGORY, category).apply();

                            } else {
                                category &= 247;
                                editor.putInt(RideApplication.ARG_CATEGORY, category).apply();
                            }

                            Intent updateService = new Intent(getActivity(), UpdateLocationService.class);
                            getActivity().startService(updateService);
                        }
                    }

                    return true;
                }
            });
        }

        RideSwitchPreference homeMessages = (RideSwitchPreference) getPreferenceScreen().findPreference("home_messages");

        if (homeMessages != null) {

            homeMessages.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    RideSwitchPreference switchPreference = (RideSwitchPreference) preference;

                    if (preference != null) {
                        boolean isChecked = (boolean) newValue;

                        if (switchPreference.isChecked() != isChecked) {
                            SharedPreferences.Editor editor = preferences.edit();

                            int category = preferences.getInt(RideApplication.ARG_CATEGORY, 255);

                            if (isChecked) {
                                category |= 16;
                                editor.putInt(RideApplication.ARG_CATEGORY, category).apply();

                            } else {
                                category &= 239;
                                editor.putInt(RideApplication.ARG_CATEGORY, category).apply();
                            }

                            Intent updateService = new Intent(getActivity(), UpdateLocationService.class);
                            getActivity().startService(updateService);
                        }
                    }

                    return true;
                }
            });
        }

        Preference pushKey = getPreferenceScreen().findPreference("app_push_key");

        if (pushKey != null) {
            final String appKey = preferences.getString(RideApplication.ARG_IDENTIFY, "");

            if (appKey.length() > 0) {
                pushKey.setTitle(appKey.substring(3));
            }
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        final MenuItem homeItem = menu.findItem(R.id.action_settings);

        if (homeItem != null) {
            final Configuration configuration = getResources().getConfiguration();

            if (configuration.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                homeItem.setIcon(R.mipmap.ic_arrow_right);

            } else {
                homeItem.setIcon(R.mipmap.ic_arrow_left);
            }
        }

        final MenuItem searchItem = menu.findItem(R.id.action_search);

        if (searchItem != null) {
            searchItem.setVisible(false);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView listView = view.findViewById(android.R.id.list);

        if (listView != null) {
            listView.setVerticalScrollBarEnabled(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(R.string.adjustments);
        }

        BottomNavigationView navigation = getActivity().findViewById(R.id.navigation);

        if (navigation != null) {
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) navigation.getLayoutParams();

            if (layoutParams != null) {
                navigation.animate().translationY(navigation.getHeight() + layoutParams.bottomMargin).setInterpolator(new LinearInterpolator()).start();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        BottomNavigationView navigation = getActivity().findViewById(R.id.navigation);

        if (navigation != null) {
            navigation.setVisibility(View.VISIBLE);

            navigation.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
        }
    }

}
