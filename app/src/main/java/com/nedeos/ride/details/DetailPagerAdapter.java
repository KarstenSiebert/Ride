package com.nedeos.ride.details;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by Karsten on 17.11.2017.
 */
public class DetailPagerAdapter extends FragmentStatePagerAdapter {

    final private ArrayList<Fragment> fragments;

    public DetailPagerAdapter(FragmentManager fragmentManager, ArrayList<Fragment> fragments) {
        super(fragmentManager);

        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {

        if (getCount() > position) {
            return fragments.get(position);

        } else {
            return null;
        }
    }

    @Override
    public int getCount() {
        return (null != fragments ? fragments.size() : 0);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }

}
