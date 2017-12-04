package com.nedeos.ride.details;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.nedeos.ride.R;
import com.nedeos.ride.RideApplication;
import com.nedeos.ride.messages.GlideApp;
import com.nedeos.ride.users.User;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DetailFragment extends Fragment {

    private User user;

    private ImageView icon;

    private DetailImageView image_1;
    private DetailImageView image_2;
    private DetailImageView image_3;

    private SharedPreferences preferences;

    public DetailFragment() {
        // Required empty public constructor
    }

    public static DetailFragment newInstance(User user) {
        DetailFragment fragment = new DetailFragment();

        Bundle args = new Bundle();

        args.putParcelable(RideApplication.DETAILFRAGMENT_DETAIL_USER, user);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            user = getArguments().getParcelable(RideApplication.DETAILFRAGMENT_DETAIL_USER);
        }

        if (savedInstanceState != null) {
            user = savedInstanceState.getParcelable(RideApplication.DETAILFRAGMENT_DETAIL_USER);
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (outState != null) {
            outState.putParcelable(RideApplication.DETAILFRAGMENT_DETAIL_USER, user);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        icon = view.findViewById(R.id.detail_icon);

        if (icon != null) {
            RequestOptions requestOptions = new RequestOptions()
                    .dontAnimate()
                    .centerCrop();

            GlideApp.with(this).load(user.getIcon())
                    .apply(requestOptions)
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(5)))
                    .into(icon);

            icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (user != null) {
                        addToFavorite(user);
                    }
                }
            });

            icon.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    if (user != null) {
                        addToBlocked(user);
                    }

                    return true;
                }
            });
        }

        image_1 = view.findViewById(R.id.detail_image_1);

        if (image_1 != null) {

            if ((user != null) && (user.getImage_1() != null) && (user.getImage_1().length() > 0)) {
                RequestOptions requestOptions = new RequestOptions()
                        .override(112, 112)
                        .dontAnimate()
                        .centerCrop();

                GlideApp.with(this).load(user.getImage_1())
                        .apply(requestOptions)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(5)))
                        .into(image_1);

            } else {
                image_1.setVisibility(View.GONE);
            }
        }

        image_2 = view.findViewById(R.id.detail_image_2);

        if (image_2 != null) {

            if ((user != null) && (user.getImage_2() != null) && (user.getImage_2().length() > 0)) {
                RequestOptions requestOptions = new RequestOptions()
                        .override(112, 112)
                        .dontAnimate()
                        .centerCrop();

                GlideApp.with(this).load(user.getImage_2())
                        .apply(requestOptions)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(5)))
                        .into(image_2);

            } else {
                image_2.setVisibility(View.GONE);
            }
        }

        image_3 = view.findViewById(R.id.detail_image_3);

        if (image_3 != null) {

            if ((user != null) && (user.getImage_3() != null) && (user.getImage_3().length() > 0)) {
                RequestOptions requestOptions = new RequestOptions()
                        .override(112, 112)
                        .dontAnimate()
                        .centerCrop();

                GlideApp.with(this).load(user.getImage_3())
                        .apply(requestOptions)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(5)))
                        .into(image_3);

            } else {
                image_3.setVisibility(View.GONE);
            }
        }

        return view;
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
    public void onResume() {
        super.onResume();

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(R.string.user_details);
        }

        AppBarLayout appBarLayout = getActivity().findViewById(R.id.appbar);

        if (appBarLayout != null) {
            appBarLayout.setExpanded(true, true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void addToFavorite(final User user) {

        try {
            final JSONObject jsonObject = new JSONObject();

            jsonObject.put(RideApplication.ARG_IDENTIFY, preferences.getString(RideApplication.ARG_IDENTIFY, ""));
            jsonObject.put(RideApplication.ARG_PASSWORD, preferences.getString(RideApplication.ARG_PASSWORD, ""));

            jsonObject.put(RideApplication.ARG_USID, user.getUsid());

            final OkHttpClient client = RideApplication.getOkHttpClient();

            if (client != null) {
                RequestBody body = RequestBody.create(RideApplication.JSON, jsonObject.toString());

                Request request = new Request.Builder()
                        .url("https://ride.barubox.com/favorUser")
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {

                    @Override
                    public void onFailure(Call call, IOException e) {
                        onPressed(1);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        if (response.code() == 200) {
                            onPressed(0);

                        } else if (response.code() == 202) {
                            onPressed(5);

                        } else if (response.code() == 204) {
                            onPressed(6);

                        } else {
                            onPressed(1);
                        }

                        response.body().close();
                    }
                });
            }

        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    private void addToBlocked(final User user) {

        try {
            final JSONObject jsonObject = new JSONObject();

            jsonObject.put(RideApplication.ARG_IDENTIFY, preferences.getString(RideApplication.ARG_IDENTIFY, ""));
            jsonObject.put(RideApplication.ARG_PASSWORD, preferences.getString(RideApplication.ARG_PASSWORD, ""));

            jsonObject.put(RideApplication.ARG_USID, user.getUsid());

            final OkHttpClient client = RideApplication.getOkHttpClient();

            if (client != null) {
                RequestBody body = RequestBody.create(RideApplication.JSON, jsonObject.toString());

                Request request = new Request.Builder()
                        .url("https://ride.barubox.com/blockUser")
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {

                    @Override
                    public void onFailure(Call call, IOException e) {
                        onPressed(3);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        if (response.isSuccessful()) {
                            onPressed(2);

                        } else {
                            onPressed(3);
                        }

                        response.body().close();
                    }
                });
            }

        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    private void onPressed(final int detailStatus) {

        Handler handler = new Handler(Looper.getMainLooper());

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewGroup container = (CoordinatorLayout) getActivity().getWindow().getDecorView().findViewById(R.id.ride_coordinatorlayout);

                if (container != null) {
                    String sResult = "";

                    if (detailStatus == 0) {
                        sResult = getString(R.string.added_to_favorites);

                    } else if (detailStatus == 1) {
                        sResult = getString(R.string.failed_to_add_to_favorites);

                    } else if (detailStatus == 2) {
                        sResult = getString(R.string.user_blocked);

                    } else if (detailStatus == 3) {
                        sResult = getString(R.string.failed_to_block_user);

                    } else if (detailStatus == 5) {
                        sResult = getString(R.string.already_saved);

                    } else if (detailStatus == 6) {
                        sResult = getString(R.string.user_is_blocked);

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

}
