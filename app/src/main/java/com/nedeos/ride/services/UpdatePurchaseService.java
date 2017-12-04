package com.nedeos.ride.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.nedeos.ride.RideApplication;
import com.nedeos.ride.messages.RideDBHelper;
import com.nedeos.ride.messages.Subscription;
import com.nedeos.ride.purchases.Purchase;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UpdatePurchaseService extends IntentService {

    public UpdatePurchaseService() {
        super("UpdatePurchaseService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (preferences != null) {
            final Bundle extras = intent.getExtras();

            if (extras != null) {
                Purchase purchase = extras.getParcelable(RideApplication.ARG_PURCHASE);

                if (purchase != null) {

                    try {
                        final JSONObject jsonObject = new JSONObject();

                        jsonObject.put(RideApplication.ARG_IDENTIFY, preferences.getString(RideApplication.ARG_IDENTIFY, ""));
                        jsonObject.put(RideApplication.ARG_PASSWORD, preferences.getString(RideApplication.ARG_PASSWORD, ""));

                        jsonObject.put(RideApplication.ARG_TOKEN, purchase.getToken());
                        jsonObject.put(RideApplication.ARG_PRODUCT_ID, purchase.getSku());
                        jsonObject.put(RideApplication.ARG_ORDER_ID, purchase.getOrderId());
                        jsonObject.put(RideApplication.ARG_PURCHASE_TIME, purchase.getPurchaseTime());
                        jsonObject.put(RideApplication.ARG_PURCHASE_STATE, purchase.getPurchaseState());
                        jsonObject.put(RideApplication.ARG_DEVELOPER_PAYLOAD, purchase.getDeveloperPayload());

                        final OkHttpClient client = RideApplication.getOkHttpClient();

                        if (client != null) {
                            RequestBody body = RequestBody.create(RideApplication.JSON, jsonObject.toString());

                            Request request = new Request.Builder()
                                    .url("https://ride.barubox.com/productsShop")
                                    .post(body)
                                    .build();

                            client.newCall(request).enqueue(new Callback() {

                                @Override
                                public void onFailure(Call call, IOException e) {
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {

                                    if (response.isSuccessful()) {
                                        Subscription subscription = extras.getParcelable(RideApplication.ARG_SUBSCRIPTION);

                                        if (subscription != null) {
                                            RideDBHelper.setUsedInSubDB(RideApplication.DB_FOUNDSUBS_TABLE, subscription, "1");

                                            RideDBHelper.setUsedInSubDB(RideApplication.DB_SUBSCRIPTIONS_TABLE, subscription, "1");
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
    }

}
