package com.nedeos.ride.users;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.nedeos.ride.R;
import com.nedeos.ride.RideApplication;
import com.nedeos.ride.messages.GlideApp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserFragment extends Fragment {

    private ArrayList<User> userList;

    private UserAdapter userAdapter;

    private RecyclerView recyclerView;

    private RecyclerView.SmoothScroller smoothScroller;

    SharedPreferences preferences;

    ImageView emptyImage;

    private OnUserListener mUserListener;

    public UserFragment() {
        // Required empty public constructor
    }

    public static UserFragment newInstance() {
        UserFragment fragment = new UserFragment();

        Bundle args = new Bundle();

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (userList == null) {
            userList = new ArrayList<>();

        } else {
            userList.clear();
        }

        userAdapter = new UserAdapter(this, userList);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        recyclerView = view.findViewById(R.id.user_recyclerview);

        recyclerView.setClickable(true);

        recyclerView.isFocusable();

        recyclerView.setHasFixedSize(true);

        recyclerView.setFocusableInTouchMode(true);

        recyclerView.setAdapter(userAdapter);

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        if (getActivity() != null) {
            smoothScroller = new LinearSmoothScroller(getActivity()) {
                @Override
                protected int getVerticalSnapPreference() {
                    return LinearSmoothScroller.SNAP_TO_START;
                }

            /*
            @Override
            protected void updateActionForInterimTarget(Action action) {
                action.jumpTo(position);
            }
            */
            };
        }

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);

        layoutManager.setOrientation(GridLayoutManager.VERTICAL);

        layoutManager.setAutoMeasureEnabled(true);

        recyclerView.setLayoutManager(layoutManager);

        final ItemTouchHelper swipeToDismissHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    final float alpha = RideApplication.ALPHA_FULL - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
                    viewHolder.itemView.setAlpha(alpha);
                    viewHolder.itemView.setTranslationX(dX);

                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                if ((userList != null) && (viewHolder != null)) {
                    final int position = viewHolder.getAdapterPosition();

                    final User user = userList.get(position);

                    if ((userAdapter != null) && (user != null)) {

                        if (direction == ItemTouchHelper.RIGHT) {
                            addToFavorite(user, position);

                        } else {
                            addToBlocked(user, position);

                            if (userList.size() == 0) {

                                if (emptyImage != null) {
                                    AlphaAnimation animation = new AlphaAnimation(.0f, 1.f);

                                    animation.setDuration(1000);
                                    animation.setStartOffset(0);

                                    emptyImage.setVisibility(View.VISIBLE);
                                    emptyImage.startAnimation(animation);
                                }
                            }
                        }
                    }
                }
            }
        });

        swipeToDismissHelper.attachToRecyclerView(recyclerView);

        emptyImage = view.findViewById(R.id.user_emptyImage);

        if (emptyImage != null) {
            GlideApp.get(getActivity()).clearMemory();

            RequestOptions requestOptions = new RequestOptions()
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .fallback(R.drawable.icon)
                    .error(R.drawable.icon);

            GlideApp.with(this).load(R.drawable.icon)
                    .apply(requestOptions)
                    .into(emptyImage);
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (userList == null) {
            userList = new ArrayList<>();

        } else {
            userList.clear();
        }

        if ((recyclerView != null) && (userAdapter != null)) {
            recyclerView.getRecycledViewPool().clear();

            userAdapter.notifyDataSetChanged();
        }

        if ((preferences != null) && (userList != null) && (userList.size() == 0)) {

            try {
                final double lat = preferences.getFloat(RideApplication.ARG_LATITUDE, 0.0f);
                final double lng = preferences.getFloat(RideApplication.ARG_LONGITUDE, 0.0f);

                JSONObject jsonObject = new JSONObject();

                jsonObject.put("identify", preferences.getString(RideApplication.ARG_IDENTIFY, ""));
                jsonObject.put("password", preferences.getString(RideApplication.ARG_PASSWORD, ""));
                jsonObject.put("latitude", lat);
                jsonObject.put("longitude", lng);

                if ((lat != 0.0f) && (lng != 0.0f)) {
                    OkHttpClient client = RideApplication.getOkHttpClient();

                    if (client != null) {
                        RequestBody body = RequestBody.create(RideApplication.JSON, jsonObject.toString());

                        Request request = new Request.Builder()
                                .url("https://ride.barubox.com/listallUsers")
                                .post(body)
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {

                                if ((userList == null) || userList.size() == 0) {

                                    if (emptyImage != null) {
                                        AlphaAnimation animation = new AlphaAnimation(.0f, 1.f);

                                        animation.setDuration(1000);
                                        animation.setStartOffset(0);

                                        emptyImage.setVisibility(View.VISIBLE);
                                        emptyImage.startAnimation(animation);
                                    }
                                }
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {

                                if (response.isSuccessful()) {

                                    try {
                                        JSONArray users = new JSONArray(response.body().string());

                                        if (users.length() > 0) {

                                            for (int i = 0; i < users.length(); i++) {
                                                JSONObject user = users.optJSONObject(i);

                                                final User item = new User();

                                                item.setUsid(user.optInt(RideApplication.ARG_USID));

                                                item.setIcon(user.optString(RideApplication.ARG_ICON));

                                                item.setImage_1(user.optString(RideApplication.ARG_IMAGE_1));
                                                item.setImage_2(user.optString(RideApplication.ARG_IMAGE_2));
                                                item.setImage_3(user.optString(RideApplication.ARG_IMAGE_3));

                                                Handler handler = new Handler(Looper.getMainLooper());

                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        userAdapter.addItem(item);
                                                    }
                                                });
                                            }

                                        } else {

                                            if ((userList == null) || userList.size() == 0) {
                                                Handler handler = new Handler(Looper.getMainLooper());

                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (emptyImage != null) {
                                                            AlphaAnimation animation = new AlphaAnimation(.0f, 1.f);

                                                            animation.setDuration(1000);
                                                            animation.setStartOffset(0);

                                                            emptyImage.setVisibility(View.VISIBLE);
                                                            emptyImage.startAnimation(animation);
                                                        }
                                                    }
                                                });
                                            }
                                        }

                                    } catch (JSONException e) {
                                        //e.printStackTrace();
                                    }

                                } else {

                                    if ((userList == null) || userList.size() == 0) {
                                        Handler handler = new Handler(Looper.getMainLooper());

                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (emptyImage != null) {
                                                    AlphaAnimation animation = new AlphaAnimation(.0f, 1.f);

                                                    animation.setDuration(1000);
                                                    animation.setStartOffset(0);

                                                    emptyImage.setVisibility(View.VISIBLE);
                                                    emptyImage.startAnimation(animation);
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    }
                }

            } catch (JSONException e) {
                // e.printStackTrace();
            }
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
    public void onResume() {
        super.onResume();

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(R.string.peep_around);
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

    public void onPressed(int userStatus) {

        if (mUserListener != null) {
            mUserListener.onUserFragment(userStatus);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnUserListener) {
            mUserListener = (OnUserListener) context;

        } else {
            throw new RuntimeException(context.toString() + " must implement OnUserListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mUserListener = null;
    }

    public interface OnUserListener {
        void onUserFragment(int userStatus);
    }

    private void addToFavorite(User user, final int position) {

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

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (userAdapter != null) {
                                    userAdapter.notifyItemChanged(position);
                                }
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        if (response.code() == 200) {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (userAdapter != null) {
                                        userAdapter.notifyItemChanged(position);
                                    }
                                }
                            });

                            onPressed(0);

                        } else if (response.code() == 202) {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (userAdapter != null) {
                                        userAdapter.notifyItemChanged(position);
                                    }
                                }
                            });

                            onPressed(5);

                        } else {
                            onPressed(1);

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (userAdapter != null) {
                                        userAdapter.notifyItemChanged(position);
                                    }
                                }
                            });
                        }

                        response.body().close();
                    }
                });
            }

        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    private void addToBlocked(final User user, final int position) {

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

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (userAdapter != null) {
                                    userAdapter.notifyItemChanged(position);
                                }
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        if (response.isSuccessful()) {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (userAdapter != null) {
                                        userAdapter.removeItem(user);
                                    }

                                    if ((userList == null) || (userList.size() == 0)) {

                                        if (emptyImage != null) {
                                            AlphaAnimation animation = new AlphaAnimation(.0f, 1.f);

                                            animation.setDuration(1000);
                                            animation.setStartOffset(0);

                                            emptyImage.setVisibility(View.VISIBLE);
                                            emptyImage.startAnimation(animation);
                                        }
                                    }
                                }
                            });

                            onPressed(2);

                        } else {
                            onPressed(3);

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (userAdapter != null) {
                                        userAdapter.notifyItemChanged(position);
                                    }
                                }
                            });
                        }

                        response.body().close();
                    }
                });
            }

        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

}
