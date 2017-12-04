package com.nedeos.ride.details;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.nedeos.ride.R;
import com.nedeos.ride.RideApplication;
import com.nedeos.ride.users.User;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class DetailPagerFragment extends Fragment {

    private ArrayList<User> userlist;

    SharedPreferences preferences;

    private int localPosition = 0;

    private DetailPagerAdapter pageAdapter;

    private ArrayList<Fragment> fragments;

    private ViewPager viewPager;

    private ViewPager.OnPageChangeListener onPageChangeListener;

    public DetailPagerFragment() {
        // Required empty public constructor
    }

    public static DetailPagerFragment newInstance(ArrayList<User> userlist, int localPosition) {
        DetailPagerFragment fragment = new DetailPagerFragment();

        Bundle args = new Bundle();

        args.putParcelableArrayList(RideApplication.USERFRAGMENT_USER_LIST, userlist);
        args.putInt(RideApplication.ARG_LAYOUT_POSITION, localPosition);

        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        userlist = getArguments().getParcelableArrayList(RideApplication.USERFRAGMENT_USER_LIST);

        if (userlist == null) {
            userlist = new ArrayList<>();
        }

        localPosition = getArguments().getInt(RideApplication.ARG_LAYOUT_POSITION);

        if (savedInstanceState != null) {
            localPosition = savedInstanceState.getInt(RideApplication.ARG_LAYOUT_POSITION);

            userlist = savedInstanceState.getParcelableArrayList(RideApplication.USERFRAGMENT_USER_LIST);
        }

        setHasOptionsMenu(true);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (viewPager != null) {
            localPosition = viewPager.getCurrentItem();
        }

        if (outState != null) {
            outState.putInt(RideApplication.ARG_LAYOUT_POSITION, localPosition);

            outState.putParcelableArrayList(RideApplication.USERFRAGMENT_USER_LIST, userlist);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        if (savedInstanceState != null) {
            localPosition = savedInstanceState.getInt(RideApplication.ARG_LAYOUT_POSITION);
        }

        if (onPageChangeListener != null) {
            onPageChangeListener.onPageSelected(localPosition);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);

        if (searchItem != null) {
            searchItem.setVisible(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_pager, container, false);

        fragments = buildFragments();

        viewPager = view.findViewById(R.id.detail_viewpager);

        if (viewPager != null) {

            FragmentManager fragmentManager = getChildFragmentManager();

            if (fragmentManager != null) {
                pageAdapter = new DetailPagerAdapter(fragmentManager, fragments);

                viewPager.setAdapter(pageAdapter);
            }

            onPageChangeListener = new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

                            if (actionBar != null) {
                                actionBar.setTitle(R.string.user_details);
                            }
                        }
                    });
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            };

            viewPager.addOnPageChangeListener(onPageChangeListener);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (viewPager != null) {

            viewPager.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_BACK)) {

                        if (viewPager.getCurrentItem() == 0) {
                            return true;

                        } else {
                            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                            return true;
                        }
                    }

                    return false;
                }
            });

            viewPager.setCurrentItem(localPosition);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        localPosition = viewPager.getCurrentItem();

        if (onPageChangeListener != null) {
            viewPager.removeOnPageChangeListener(onPageChangeListener);
            onPageChangeListener = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (onPageChangeListener != null) {
            viewPager.removeOnPageChangeListener(onPageChangeListener);
            onPageChangeListener = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (fragments.size() > 0) {
            fragments.clear();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");

            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private ArrayList<Fragment> buildFragments() {

        ArrayList<Fragment> fragments = new ArrayList<Fragment>();

        for (int i = 0; i < userlist.size(); i++) {

            if (i < RideApplication.DB_MAX_BACKLOG_ENTRIES) {
                DetailFragment detailFragment = DetailFragment.newInstance(userlist.get(i));

                if (detailFragment != null) {
                    fragments.add(detailFragment);
                }

            } else {
                return fragments;
            }
        }

        return fragments;
    }

}
