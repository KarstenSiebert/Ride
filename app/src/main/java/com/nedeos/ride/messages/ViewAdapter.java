package com.nedeos.ride.messages;

import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.nedeos.ride.purchases.PurchaseFragment;
import com.nedeos.ride.users.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Karsten on 02.04.2017.
 */

public class ViewAdapter extends RecyclerView.Adapter<ViewAdapter.ViewRowHolder> {

    private ArrayList<Message> messageList;

    private ViewFragment viewFragment;

    public static class ViewRowHolder extends RecyclerView.ViewHolder {

        ViewAdapter adapter;

        CardView card;

        ImageView icon;
        ImageView shot;

        TextView head;
        TextView text;
        TextView time;

        public ViewRowHolder(View itemView, final ViewAdapter adapter) {
            super(itemView);

            this.adapter = adapter;

            card = itemView.findViewById(R.id.contact_relative);

            if (card != null) {

                card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION) {
                            final Message message = adapter.getMessageList().get(position);

                            if ((message != null) && (message.getProd() != null) && (message.getProd().length() > 0)) {

                                if (message.getProd().startsWith("user.")) {
                                    adapter.getSingleUser(message.getProd().substring(5));

                                } else {
                                    adapter.getSingleProduct(message.getProd());
                                }
                            }
                        }
                    }
                });
            }

            icon = itemView.findViewById(R.id.message_icon);

            shot = itemView.findViewById(R.id.message_shot);

            head = itemView.findViewById(R.id.message_head);

            text = itemView.findViewById(R.id.message_text);

            time = itemView.findViewById(R.id.message_time);

            if ((time != null) && (adapter != null)) {
                time.setTextColor(ContextCompat.getColor(adapter.getViewFragment().getActivity(), R.color.colorPrimaryLight));
            }
        }
    }

    public ViewAdapter(ViewFragment viewFragment, ArrayList<Message> messageList) {
        this.viewFragment = viewFragment;

        this.messageList = messageList;
    }

    @Override
    public ViewRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_items, parent, false);

        return new ViewRowHolder(v, this);
    }

    @Override
    public void onBindViewHolder(ViewRowHolder holder, int position) {
        int safePosition = holder.getAdapterPosition();

        if ((safePosition != RecyclerView.NO_POSITION) && (messageList != null) && (messageList.size() > safePosition)) {
            final Message message = messageList.get(safePosition);

            holder.icon.setVisibility(View.VISIBLE);

            if (message != null) {

                if ((message.getIcon() != null) && (message.getIcon().length() > 0)) {

                    if (message.getNoid() != 0) {
                        holder.time.setTextColor(ContextCompat.getColor(getViewFragment().getActivity(), R.color.colorPrimaryDark));

                    } else {
                        holder.time.setTextColor(ContextCompat.getColor(getViewFragment().getActivity(), R.color.colorPrimaryLight));
                    }

                    RequestOptions requestOptions = new RequestOptions()
                            .override(48, 48)
                            .dontAnimate()
                            .circleCrop();

                    GlideApp.with(viewFragment).load(message.getIcon())
                            .apply(requestOptions)
                            .into(holder.icon);
                }

                if ((message.getHead() != null) && (message.getHead().length() > 0)) {

                    if ((message.getLink() != null) && (message.getLink().length() > 0)) {
                        holder.head.setTextColor(Color.BLUE);

                    } else {
                        holder.head.setTextColor(ContextCompat.getColor(viewFragment.getActivity(), R.color.colorSecondaryText));
                    }

                    if (message.getNoid() == 999999) {
                        holder.head.setTextColor(ContextCompat.getColor(viewFragment.getActivity(), R.color.colorPrimaryDark));
                    }

                    holder.head.setText(message.getHead());
                }

                if ((message.getText() != null) && (message.getText().length() > 0)) {

                    if ((message.getProd() != null) && message.getProd().startsWith("user.")) {
                        holder.text.setText(R.string.we_have_chosen_each_other);

                    } else {
                        holder.text.setText(message.getText());
                    }
                }

                if ((message.getTime() != 0)) {
                    SimpleDateFormat format = new SimpleDateFormat("dd MMM, HH:mm:ss");
                    Date newDate = new Date(message.getTime());
                    holder.time.setText(format.format(newDate));
                }

                if ((message.getShot() != null) && (message.getShot().length() > 0)) {

                    RequestOptions requestOptions = new RequestOptions()
                            .dontAnimate()
                            .centerCrop();

                    GlideApp.with(viewFragment).load(message.getShot())
                            .apply(requestOptions)
                            .apply(RequestOptions.bitmapTransform(new RoundedCorners(5)))
                            .into(holder.shot);

                    holder.shot.setVisibility(View.VISIBLE);

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
        return (null != messageList ? messageList.size() : 0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItem(final Message item) {

        if ((messageList != null) && (item != null)) {
            notifyItemInserted(0);
        }
    }

    public void removeItem(final Message item) {

        if ((messageList != null) && (item != null)) {
            notifyItemRemoved(messageList.indexOf(item));
        }
    }

    public ArrayList<Message> getMessageList() {
        return messageList;
    }

    public ViewFragment getViewFragment() {
        return viewFragment;
    }

    private void getSingleProduct(final String query) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getViewFragment().getActivity());

        if (preferences != null) {
            RideDBHelper.delAllFromDB(RideApplication.DB_FOUNDSUBS_TABLE);

            try {
                final JSONObject jsonObject = new JSONObject();

                jsonObject.put(RideApplication.ARG_IDENTIFY, preferences.getString(RideApplication.ARG_IDENTIFY, ""));
                jsonObject.put(RideApplication.ARG_PASSWORD, preferences.getString(RideApplication.ARG_PASSWORD, ""));

                jsonObject.put(RideApplication.ARG_SINGLE, "yes");

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

                                                FragmentManager fragmentManager = getViewFragment().getFragmentManager();

                                                if (fragmentManager != null) {

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
        }
    }

    private void getSingleUser(final String query) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getViewFragment().getActivity());

        if (preferences != null) {
            RideDBHelper.delAllFromDB(RideApplication.DB_FOUNDSUBS_TABLE);

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
                                                            FragmentManager fragmentManager = getViewFragment().getFragmentManager();

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
