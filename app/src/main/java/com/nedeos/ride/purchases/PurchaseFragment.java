package com.nedeos.ride.purchases;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.nedeos.ride.R;
import com.nedeos.ride.RideApplication;
import com.nedeos.ride.messages.GlideApp;
import com.nedeos.ride.messages.RideDBHelper;
import com.nedeos.ride.messages.RideLinearLayoutManager;
import com.nedeos.ride.messages.Subscription;
import com.nedeos.ride.services.UpdatePurchaseService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PurchaseFragment extends Fragment {

    private static int SUCCESSFULLY_SUBSCRIBED = 0;
    private static int FAILED_TO_SUBSCRIBE = 1;

    private static int SUCCESSFULLY_UNSUBSCRIBED = 2;
    private static int FAILED_TO_UNSUBSCRIBE = 3;

    private static int SUCCESSFULLY_UPDATED = 4;
    private static int NOT_SUBSCRIBED = 5;

    private ArrayList<Subscription> subscriptionsList;

    private PurchaseAdapter purchaseAdapter;

    private RecyclerView recyclerView;

    private OnPurchaseListener mPurchaseListener;

    private IabHelper mHelper;

    private List<String> additionalSkus;

    SharedPreferences preferences;

    private ImageView emptyImage;

    private String query;

    public PurchaseFragment() {
        // Required empty public constructor
    }

    public static PurchaseFragment newInstance(int layoutPosition, String query) {
        PurchaseFragment fragment = new PurchaseFragment();

        Bundle args = new Bundle();

        args.putInt(RideApplication.ARG_LAYOUT_POSITION, layoutPosition);

        args.putString(RideApplication.ARG_QUERY, query);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (subscriptionsList == null) {
            subscriptionsList = new ArrayList<>();

        } else {
            subscriptionsList.clear();
        }

        if (additionalSkus == null) {
            additionalSkus = new ArrayList<>();

        } else {
            additionalSkus.clear();
        }

        purchaseAdapter = new PurchaseAdapter(this, subscriptionsList);

        query = getArguments().getString(RideApplication.ARG_QUERY);

        // Get all pay subscriptions loaded from local DB

        if ((query != null) && (query.length() > 0)) {
            getAllSubsFromDB(RideApplication.DB_FOUNDSUBS_TABLE);

        } else {
            getAllSubsFromDB(RideApplication.DB_SUBSCRIPTIONS_TABLE);
        }

        mHelper = new IabHelper(getActivity(), RideApplication.base64EncodedPublicKey);

        mHelper.enableDebugLogging(false);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {

                if ((mHelper == null) || result.isFailure()) {
                    return;
                }

                mHelper.queryInventoryAsync(true, additionalSkus, new IabHelper.QueryInventoryFinishedListener() {
                    @Override
                    public void onQueryInventoryFinished(final IabResult result, Inventory inv) {

                        if ((mHelper == null) || result.isFailure()) {
                            return;
                        }

                        if ((subscriptionsList != null) && (subscriptionsList.size() > 0)) {
                            subscriptionsList.clear();
                        }

                        // Add al pay subscriptions from Google list

                        for (String skuId : additionalSkus) {
                            final SkuDetails skuDetails = inv.getSkuDetails(skuId);

                            // if ((skuDetails != null) && (inv.getPurchase(skuId) == null)) {
                            if (skuDetails != null) {
                                Subscription subscription = new Subscription();

                                subscription.setCost(skuDetails.getPrice());
                                subscription.setProd(skuDetails.getSku());
                                subscription.setHead(skuDetails.getTitle());
                                subscription.setText(skuDetails.getDescription());

                                if ((query != null) && (query.length() > 0)) {
                                    getOneSubsFromDB(subscription, RideApplication.DB_FOUNDSUBS_TABLE, skuDetails.getSku());

                                } else {
                                    getOneSubsFromDB(subscription, RideApplication.DB_SUBSCRIPTIONS_TABLE, skuDetails.getSku());
                                }

                                if (subscriptionsList != null) {
                                    subscriptionsList.add(subscription);
                                }
                            }
                        }

                        if (subscriptionsList != null) {

                            if ((query != null) && (query.length() > 0)) {
                                getAllFreeSubsFromDB(RideApplication.DB_FOUNDSUBS_TABLE);

                            } else {
                                getAllFreeSubsFromDB(RideApplication.DB_SUBSCRIPTIONS_TABLE);
                            }

                            Collections.shuffle(subscriptionsList);
                        }

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if ((purchaseAdapter != null) && (subscriptionsList != null)) {

                                        if (recyclerView != null) {
                                            recyclerView.getRecycledViewPool().clear();

                                            purchaseAdapter.notifyDataSetChanged();
                                        }

                                        for (int i = 0; i < subscriptionsList.size(); i++) {
                                            Subscription subscription = subscriptionsList.get(i);

                                            purchaseAdapter.addItem(subscription);
                                        }
                                    }

                                    if ((subscriptionsList != null) && (subscriptionsList.size() == 0) && (emptyImage != null)) {
                                        AlphaAnimation animation = new AlphaAnimation(.0f, 1.f);

                                        animation.setDuration(1000);
                                        animation.setStartOffset(0);

                                        emptyImage.setVisibility(View.VISIBLE);
                                        emptyImage.startAnimation(animation);
                                    }
                                }
                            });

                        } else {
                            Handler handler = new Handler(Looper.getMainLooper());

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if ((purchaseAdapter != null) && (subscriptionsList != null)) {

                                        if (recyclerView != null) {
                                            recyclerView.getRecycledViewPool().clear();

                                            purchaseAdapter.notifyDataSetChanged();
                                        }

                                        for (int i = 0; i < subscriptionsList.size(); i++) {
                                            Subscription subscription = subscriptionsList.get(i);

                                            purchaseAdapter.addItem(subscription);
                                        }
                                    }

                                    if ((subscriptionsList != null) && (subscriptionsList.size() == 0) && (emptyImage != null)) {
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
                });
            }
        });

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_purchase, container, false);

        recyclerView = view.findViewById(R.id.purchase_recyclerview);

        recyclerView.setClickable(true);

        recyclerView.isFocusable();

        recyclerView.setFocusableInTouchMode(true);

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(purchaseAdapter);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();

        display.getSize(size);

        RideLinearLayoutManager layoutManager = new RideLinearLayoutManager(getActivity());

        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        layoutManager.setExtraLayoutSpace(size.y);
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

                if ((subscriptionsList != null) && (viewHolder != null)) {
                    final int position = viewHolder.getAdapterPosition();

                    final Subscription subscription = subscriptionsList.get(position);

                    if ((purchaseAdapter != null) && (subscription != null)) {

                        if (direction == ItemTouchHelper.LEFT) {

                            if ((subscription.getCost() == null) || (subscription.getCost().length() == 0) || (subscription.getCost().equals("0.00"))) {
                                freeUnSubscription(subscription, position);

                            } else {
                                purchaseAdapter.notifyItemChanged(position);
                            }

                        } else {

                            if ((subscription.getCost() == null) || (subscription.getCost().length() == 0) || (subscription.getCost().equals("0.00"))) {
                                freeSubscription(subscription, position);

                            } else {
                                purchaseSubscription(subscription);

                                Handler handler = new Handler();

                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        purchaseAdapter.notifyItemChanged(position);
                                    }
                                }, 200);
                            }
                        }
                    }
                }
            }
        });

        swipeToDismissHelper.attachToRecyclerView(recyclerView);

        emptyImage = view.findViewById(R.id.purchase_emptyImage);

        if (emptyImage != null) {
            RequestOptions requestOptions = new RequestOptions()
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .fallback(R.drawable.icon)
                    .error(R.drawable.icon);

            if ((preferences != null) && preferences.getString(RideApplication.ARG_ICON, "").length() > 0) {
                GlideApp.with(this).load(preferences.getString(RideApplication.ARG_ICON, "https://load.barubox.com/images/noimage.png"))
                        .apply(requestOptions)
                        .into(emptyImage);

                if (preferences.getString(RideApplication.ARG_LINK, "").length() > 0) {

                    emptyImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(preferences.getString(RideApplication.ARG_LINK, "")));

                            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {

                                try {
                                    startActivity(intent);

                                } catch (Exception e) {
                                    // e.printStackTrace();
                                }
                            }
                        }
                    });
                }

            } else {
                int random = new Random().nextInt(10000);

                if ((random % 2) == 0) {
                    GlideApp.with(this).load(R.drawable.image_1)
                            .apply(requestOptions)
                            .into(emptyImage);

                } else if ((random % 3) == 0) {
                    GlideApp.with(this).load(R.drawable.image_2)
                            .apply(requestOptions)
                            .into(emptyImage);

                } else {
                    GlideApp.with(this).load(R.drawable.icon)
                            .apply(requestOptions)
                            .into(emptyImage);
                }

                emptyImage.setOnClickListener(null);
            }
        }

        return view;
    }

    public void onPressed(int subscriptionStatus) {

        if (mPurchaseListener != null) {
            mPurchaseListener.onPurchaseFragment(subscriptionStatus);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(R.string.subscriptions);
        }

        AppBarLayout appBarLayout = getActivity().findViewById(R.id.appbar);

        if (appBarLayout != null) {
            appBarLayout.setExpanded(true, true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (emptyImage != null) {
            emptyImage.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnPurchaseListener) {
            mPurchaseListener = (OnPurchaseListener) context;

        } else {
            throw new RuntimeException(context.toString() + " must implement OnPurchaseListener");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mPurchaseListener = null;
    }

    public interface OnPurchaseListener {
        void onPurchaseFragment(int subscriptionStatus);
    }

    public boolean checkIabHelperHandleActivityResult(int requestCode, int resultCode, Intent data) {
        return (mHelper != null) && mHelper.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (!checkIabHelperHandleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private boolean verifyPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        if (payload.equals(RideApplication.ARG_DEVELOPER_PAYLOAD)) {
            Log.d("Ver", "OK");
        }

        return true;
    }

    public void purchaseSubscription(final Subscription subscription) {

        if ((getActivity() != null) && (subscription != null) && (subscription.getProd() != null) && (mHelper != null) && !mHelper.mAsyncInProgress) {

            mHelper.launchPurchaseFlow(getActivity(), subscription.getProd(), IabHelper.ITEM_TYPE_SUBS, RideApplication.RIDE_PRO_RETURN_CODE, new IabHelper.OnIabPurchaseFinishedListener() {
                @Override
                public void onIabPurchaseFinished(IabResult result, Purchase info) {

                    if ((mHelper == null) || (result == null) || result.isFailure()) {
                        return;
                    }

                    /*
                    if (!verifyPayload(info)) {
                        return;
                    }
                    */

                    if ((info != null) && (info.getSku() != null) && info.getSku().equals(subscription.getProd())) {

                        Intent purchaseService = new Intent(getActivity(), UpdatePurchaseService.class);
                        purchaseService.putExtra(RideApplication.ARG_SUBSCRIPTION, subscription);
                        purchaseService.putExtra(RideApplication.ARG_PURCHASE, info);
                        getActivity().startService(purchaseService);

                        onPressed(SUCCESSFULLY_SUBSCRIBED);

                    } else {
                        onPressed(FAILED_TO_SUBSCRIBE);
                    }

                }

            }, RideApplication.ARG_DEVELOPER_PAYLOAD);
        }
    }

    public void freeSubscription(final Subscription subscription, final int position) {

        if ((subscription != null) && (subscription.getProd() != null)) {

            try {
                final JSONObject jsonObject = new JSONObject();

                jsonObject.put(RideApplication.ARG_IDENTIFY, preferences.getString(RideApplication.ARG_IDENTIFY, ""));
                jsonObject.put(RideApplication.ARG_PASSWORD, preferences.getString(RideApplication.ARG_PASSWORD, ""));

                jsonObject.put(RideApplication.ARG_PRODUCT_ID, subscription.getProd());

                final OkHttpClient client = RideApplication.getOkHttpClient();

                if (client != null) {
                    RequestBody body = RequestBody.create(RideApplication.JSON, jsonObject.toString());

                    Request request = new Request.Builder()
                            .url("https://ride.barubox.com/productsFree")
                            .post(body)
                            .build();

                    client.newCall(request).enqueue(new Callback() {

                        @Override
                        public void onFailure(Call call, IOException e) {
                            onPressed(FAILED_TO_SUBSCRIBE);

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if ((purchaseAdapter != null) && (position > -1)) {
                                        purchaseAdapter.notifyItemChanged(position);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                            if (response.isSuccessful()) {

                                try {
                                    JSONObject object = new JSONObject(response.body().string());

                                    if (preferences != null) {
                                        SharedPreferences.Editor editor = preferences.edit();
                                        editor.putString(RideApplication.ARG_SUBSCRIPTION_HASH, preferences.getString(RideApplication.ARG_IDENTIFY, "")).apply();
                                    }

                                    if (object.optString("status", "").equals("added")) {
                                        onPressed(SUCCESSFULLY_SUBSCRIBED);

                                    } else if (object.optString("status", "").equals("updated")) {
                                        onPressed(SUCCESSFULLY_UPDATED);
                                    }

                                    RideDBHelper.setUsedInSubDB(RideApplication.DB_FOUNDSUBS_TABLE, subscription, "1");

                                    RideDBHelper.setUsedInSubDB(RideApplication.DB_SUBSCRIPTIONS_TABLE, subscription, "1");

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            response.body().close();

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if ((purchaseAdapter != null) && (position > -1)) {
                                        purchaseAdapter.notifyItemChanged(position);
                                    }
                                }
                            });
                        }
                    });
                }

            } catch (Exception e) {
                // e.printStackTrace();
            }
        }
    }

    public void freeUnSubscription(final Subscription subscription, final int position) {

        if ((subscription != null) && (subscription.getProd() != null)) {

            try {
                final JSONObject jsonObject = new JSONObject();

                jsonObject.put(RideApplication.ARG_IDENTIFY, preferences.getString(RideApplication.ARG_IDENTIFY, ""));
                jsonObject.put(RideApplication.ARG_PASSWORD, preferences.getString(RideApplication.ARG_PASSWORD, ""));

                jsonObject.put(RideApplication.ARG_PRODUCT_ID, subscription.getProd());

                final OkHttpClient client = RideApplication.getOkHttpClient();

                if (client != null) {
                    RequestBody body = RequestBody.create(RideApplication.JSON, jsonObject.toString());

                    Request request = new Request.Builder()
                            .url("https://ride.barubox.com/productsDown")
                            .post(body)
                            .build();

                    client.newCall(request).enqueue(new Callback() {

                        @Override
                        public void onFailure(Call call, IOException e) {
                            onPressed(FAILED_TO_UNSUBSCRIBE);

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if ((purchaseAdapter != null) && (position > -1)) {
                                        purchaseAdapter.notifyItemChanged(position);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                            if (response.isSuccessful()) {

                                try {
                                    JSONObject object = new JSONObject(response.body().string());

                                    if (preferences != null) {
                                        SharedPreferences.Editor editor = preferences.edit();
                                        editor.putString(RideApplication.ARG_SUBSCRIPTION_HASH, preferences.getString(RideApplication.ARG_IDENTIFY, "")).apply();
                                    }

                                    if (object.optString("status", "").equals("removed")) {
                                        onPressed(SUCCESSFULLY_UNSUBSCRIBED);

                                    } else if (object.optString("status", "").equals("not_subscribed")) {
                                        onPressed(NOT_SUBSCRIBED);
                                    }

                                    RideDBHelper.setUsedInSubDB(RideApplication.DB_FOUNDSUBS_TABLE, subscription, "0");

                                    RideDBHelper.setUsedInSubDB(RideApplication.DB_SUBSCRIPTIONS_TABLE, subscription, "0");

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            response.body().close();

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if ((purchaseAdapter != null) && (position > -1)) {
                                        purchaseAdapter.notifyItemChanged(position);
                                    }
                                }
                            });
                        }
                    });
                }

            } catch (Exception e) {
                // e.printStackTrace();
            }
        }
    }

    private void getAllSubsFromDB(final String table) {
        final RideDBHelper rideDBHelper = RideApplication.getRideDBHelper();

        if (rideDBHelper != null) {
            final SQLiteDatabase db = rideDBHelper.getReadableDatabase();

            if (db != null) {
                int size = additionalSkus.size();

                if (size > 0) {
                    additionalSkus.clear();

                    purchaseAdapter.notifyItemRangeRemoved(0, size);
                }

                Cursor cursor = db.rawQuery("SELECT prod FROM " + table + " WHERE stat > 1 ORDER BY _id DESC LIMIT ?", new String[]{Integer.toString(RideApplication.DB_MAX_BACKLOG_ENTRIES)});

                while (cursor.moveToNext()) {
                    String product = cursor.getString(0);

                    additionalSkus.add(product);
                }

                cursor.close();
            }
        }
    }

    private void getAllFreeSubsFromDB(final String table) {
        final RideDBHelper rideDBHelper = RideApplication.getRideDBHelper();

        if (rideDBHelper != null) {
            final SQLiteDatabase db = rideDBHelper.getReadableDatabase();

            if (db != null) {
                Cursor cursor = db.rawQuery("SELECT prod, head, text, icon, shot, cost, used, stat, time, usid FROM " + table + " WHERE stat = 1 ORDER BY _id DESC LIMIT ?", new String[]{Integer.toString(RideApplication.DB_MAX_BACKLOG_ENTRIES)});

                while (cursor.moveToNext()) {
                    Subscription subscription = new Subscription();

                    subscription.setProd(cursor.getString(0));
                    subscription.setHead(cursor.getString(1));
                    subscription.setText(cursor.getString(2));
                    subscription.setIcon(cursor.getString(3));
                    subscription.setShot(cursor.getString(4));
                    subscription.setCost(cursor.getString(5));
                    subscription.setUsed(cursor.getString(6));
                    subscription.setStat(cursor.getInt(7));
                    subscription.setTime(cursor.getInt(8));
                    subscription.setUsid(cursor.getInt(9));

                    subscriptionsList.add(subscription);
                }

                cursor.close();
            }
        }
    }

    private void getOneSubsFromDB(Subscription subscription, String table, String product) {
        final RideDBHelper rideDBHelper = RideApplication.getRideDBHelper();

        if (rideDBHelper != null) {
            final SQLiteDatabase db = rideDBHelper.getReadableDatabase();

            if (db != null) {
                Cursor cursor = db.rawQuery("SELECT icon, shot, used, usid FROM " + table + " WHERE prod = ?", new String[]{product});

                while (cursor.moveToNext()) {
                    subscription.setIcon(cursor.getString(0));
                    subscription.setShot(cursor.getString(1));
                    subscription.setUsed(cursor.getString(2));
                    subscription.setUsid(cursor.getInt(3));
                }

                cursor.close();
            }
        }
    }

}
