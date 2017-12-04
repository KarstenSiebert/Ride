package com.nedeos.ride.purchases;

import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.nedeos.ride.R;
import com.nedeos.ride.RideApplication;
import com.nedeos.ride.details.DetailFragment;
import com.nedeos.ride.messages.GlideApp;
import com.nedeos.ride.messages.Subscription;
import com.nedeos.ride.users.User;

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

/**
 * Created by Karsten on 02.04.2017.
 */

public class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.ViewRowHolder> {

    private ArrayList<Subscription> subscriptionsList;

    private PurchaseFragment purchaseFragment;

    public static class ViewRowHolder extends RecyclerView.ViewHolder {

        PurchaseAdapter adapter;

        CardView card;

        ImageView icon;
        ImageView shot;

        TextView head;
        TextView text;
        TextView cost;

        public ViewRowHolder(View itemView, final PurchaseAdapter adapter) {
            super(itemView);

            this.adapter = adapter;

            card = itemView.findViewById(R.id.purchase_relative);

            if (card != null) {

                card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION) {
                            final Subscription subscription = adapter.getSubscriptionsList().get(position);

                            if ((subscription != null) && (subscription.getUsid() != 0)) {
                                adapter.getSingleUser(Integer.toString(subscription.getUsid()));
                            }
                        }
                    }
                });
            }

            icon = itemView.findViewById(R.id.purchase_icon);

            head = itemView.findViewById(R.id.purchase_head);

            text = itemView.findViewById(R.id.purchase_text);

            cost = itemView.findViewById(R.id.purchase_cost);

            if ((cost != null) && (adapter != null)) {
                cost.setTextColor(ContextCompat.getColor(adapter.getPurchaseFragment().getActivity(), R.color.colorPrimaryLight));
            }

            shot = itemView.findViewById(R.id.purchase_shot);
        }
    }

    public PurchaseAdapter(PurchaseFragment purchaseFragment, ArrayList<Subscription> subscriptionsList) {
        this.purchaseFragment = purchaseFragment;

        this.subscriptionsList = subscriptionsList;
    }

    @Override
    public ViewRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.purchase_items, parent, false);

        return new ViewRowHolder(v, this);
    }

    @Override
    public void onBindViewHolder(final ViewRowHolder holder, int position) {
        int safePosition = holder.getAdapterPosition();

        if ((safePosition != RecyclerView.NO_POSITION) && (subscriptionsList != null) && (subscriptionsList.size() > safePosition)) {
            final Subscription subscription = subscriptionsList.get(safePosition);

            holder.icon.setVisibility(View.VISIBLE);
            holder.shot.setVisibility(View.VISIBLE);

            if (subscription != null) {

                if ((subscription.getIcon() != null) && (subscription.getIcon().length() > 0)) {

                    if ((subscription.getUsed() != null) && subscription.getUsed().equals("1")) {
                        holder.cost.setTextColor(ContextCompat.getColor(getPurchaseFragment().getActivity(), R.color.colorPrimaryLight));
                    } else {
                        holder.cost.setTextColor(ContextCompat.getColor(getPurchaseFragment().getActivity(), R.color.colorPrimaryDark));
                    }

                    RequestOptions requestOptions = new RequestOptions()
                            .override(48, 48)
                            .fallback(R.drawable.icon)
                            .error(R.drawable.icon)
                            .circleCrop();

                    GlideApp.with(purchaseFragment).load(subscription.getIcon())
                            .apply(requestOptions)
                            .into(holder.icon);
                }

                if ((subscription.getHead() != null) && (subscription.getHead().length() > 0)) {
                    holder.head.setText(subscription.getHead());
                }

                if ((subscription.getText() != null) && (subscription.getText().length() > 0)) {
                    holder.text.setText(subscription.getText());
                }

                if ((subscription.getCost() != null) && (subscription.getCost().length() > 0) && !subscription.getCost().equals("0.00")) {
                    holder.cost.setText(subscription.getCost());

                } else {
                    holder.cost.setText(R.string.free);
                }

                if ((subscription.getShot() != null) && (subscription.getShot().length() > 0)) {

                    RequestOptions requestOptions = new RequestOptions()
                            .dontAnimate()
                            .centerCrop();

                    GlideApp.with(purchaseFragment).load(subscription.getShot())
                            .apply(requestOptions)
                            .apply(RequestOptions.bitmapTransform(new RoundedCorners(5)))
                            .into(holder.shot);

                } else {
                    holder.shot.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return (null != subscriptionsList ? subscriptionsList.size() : 0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItem(final Subscription item) {

        if ((subscriptionsList != null) && (item != null) && (subscriptionsList.indexOf(item) < getItemCount())) {
            notifyItemInserted(subscriptionsList.indexOf(item));
        }
    }

    public ArrayList<Subscription> getSubscriptionsList() {
        return subscriptionsList;
    }

    public PurchaseFragment getPurchaseFragment() {
        return purchaseFragment;
    }

    private void getSingleUser(final String query) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getPurchaseFragment().getActivity());

        if (preferences != null) {

            try {
                final JSONObject jsonObject = new JSONObject();

                jsonObject.put(RideApplication.ARG_IDENTIFY, preferences.getString(RideApplication.ARG_IDENTIFY, ""));
                jsonObject.put(RideApplication.ARG_PASSWORD, preferences.getString(RideApplication.ARG_PASSWORD, ""));

                jsonObject.put(RideApplication.ARG_QUERY, query);

                final OkHttpClient client = RideApplication.getOkHttpClient();

                if (client != null) {
                    RequestBody body = RequestBody.create(RideApplication.JSON, jsonObject.toString());

                    Request request = new Request.Builder()
                            .url("https://ride.barubox.com/getSingle")
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
                                        JSONArray jsonSubs = jsonObject.getJSONArray("user");

                                        if ((jsonSubs != null) && (jsonSubs.length() > 0)) {

                                            for (int i = 0; i < jsonSubs.length(); i++) {
                                                JSONObject object = jsonSubs.getJSONObject(i);

                                                if (object != null) {
                                                    final User user = new User();

                                                    user.setUsid(object.optInt(RideApplication.ARG_USID));

                                                    user.setIcon(object.optString(RideApplication.ARG_ICON));

                                                    user.setImage_1(object.optString(RideApplication.ARG_IMAGE_1));
                                                    user.setImage_2(object.optString(RideApplication.ARG_IMAGE_2));
                                                    user.setImage_3(object.optString(RideApplication.ARG_IMAGE_3));

                                                    Handler handler = new Handler(Looper.getMainLooper());

                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            FragmentManager fragmentManager = getPurchaseFragment().getFragmentManager();

                                                            if (fragmentManager != null) {

                                                                fragmentManager.beginTransaction()
                                                                        .replace(R.id.content_ride, DetailFragment.newInstance(user), "DetailFragment")
                                                                        .addToBackStack("DetailFragment")
                                                                        .commit();
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        }
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
        }
    }

}
