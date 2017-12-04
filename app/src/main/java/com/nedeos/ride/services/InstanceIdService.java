package com.nedeos.ride.services;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.nedeos.ride.RideApplication;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Karsten on 28.09.2016.
 */

public class InstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (preferences != null) {
            final String identify = preferences.getString(RideApplication.ARG_IDENTIFY, "");
            final String password = preferences.getString(RideApplication.ARG_PASSWORD, "");

            if ((identify.length() > 0) && (password.length() > 0)) {

                try {
                    JSONObject jsonObject = new JSONObject();

                    jsonObject.put(RideApplication.ARG_IDENTIFY, identify);
                    jsonObject.put(RideApplication.ARG_PASSWORD, password);

                    jsonObject.put(RideApplication.ARG_TOKENUPD, FirebaseInstanceId.getInstance().getToken());

                    jsonObject.put(RideApplication.ARG_CATEGORY, preferences.getInt(RideApplication.ARG_CATEGORY, 255));

                    jsonObject.put(RideApplication.ARG_LATITUDE, preferences.getFloat(RideApplication.ARG_LATITUDE, 0.f));
                    jsonObject.put(RideApplication.ARG_LONGITUDE, preferences.getFloat(RideApplication.ARG_LONGITUDE, 0.f));

                    final OkHttpClient client = RideApplication.getOkHttpClient();

                    if (client != null) {
                        RequestBody body = RequestBody.create(RideApplication.JSON, jsonObject.toString());

                        Request request = new Request.Builder()
                                .url("https://ride.barubox.com/registerUser")
                                .post(body)
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
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
