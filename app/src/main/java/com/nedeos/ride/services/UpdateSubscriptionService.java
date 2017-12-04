package com.nedeos.ride.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.nedeos.ride.RideApplication;
import com.nedeos.ride.messages.RideDBHelper;
import com.nedeos.ride.messages.Subscription;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UpdateSubscriptionService extends IntentService {

    public UpdateSubscriptionService() {
        super("UpdateSubscriptionService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (preferences != null) {

            try {
                final JSONObject jsonObject = new JSONObject();

                jsonObject.put(RideApplication.ARG_IDENTIFY, preferences.getString(RideApplication.ARG_IDENTIFY, ""));
                jsonObject.put(RideApplication.ARG_PASSWORD, preferences.getString(RideApplication.ARG_PASSWORD, ""));

                jsonObject.put(RideApplication.ARG_SUBSCRIPTION_HASH, preferences.getString(RideApplication.ARG_SUBSCRIPTION_HASH, ""));

                final OkHttpClient client = RideApplication.getOkHttpClient();

                if (client != null) {
                    RequestBody body = RequestBody.create(RideApplication.JSON, jsonObject.toString());

                    Request request = new Request.Builder()
                            .url("https://ride.barubox.com/loadhashSubs")
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
                                        JSONObject jsonHash = jsonObject.getJSONObject("hash");

                                        if ((jsonHash != null) && (jsonHash.length() > 0)) {
                                            final String hash = jsonHash.optString("subscriptions_hash");

                                            if ((hash != null) && (hash.length() > 0)) {
                                                SharedPreferences.Editor editor = preferences.edit();
                                                editor.putString(RideApplication.ARG_SUBSCRIPTION_HASH, hash).apply();
                                            }

                                            if ((hash != null) && hash.equals("empty_database")) {
                                                RideDBHelper.delAllFromDB(RideApplication.DB_SUBSCRIPTIONS_TABLE);
                                            }
                                        }

                                        JSONArray jsonSubs = jsonObject.getJSONArray("subs");

                                        if ((jsonSubs != null) && (jsonSubs.length() > 0)) {
                                            RideDBHelper.delAllFromDB(RideApplication.DB_SUBSCRIPTIONS_TABLE);

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

                                                    RideDBHelper.addSubToDB(subscription, RideApplication.DB_SUBSCRIPTIONS_TABLE);
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
