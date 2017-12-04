package com.nedeos.ride.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.google.firebase.iid.FirebaseInstanceId;
import com.nedeos.ride.RideApplication;

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

public class UpdateLocationService extends IntentService {

    public UpdateLocationService() {
        super("UpdateLocationService");
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

                            if (response.isSuccessful()) {
                                SharedPreferences.Editor editor = preferences.edit();

                                editor.putString(RideApplication.ARG_ICON, "").apply();
                                editor.putString(RideApplication.ARG_LINK, "").apply();

                                try {
                                    final JSONArray jsonArray = new JSONArray(response.body().string());

                                    if (jsonArray.length() > 0) {
                                        final JSONObject object = jsonArray.getJSONObject(0);

                                        editor.putString(RideApplication.ARG_ICON, object.optString(RideApplication.ARG_ICON, "")).apply();
                                        editor.putString(RideApplication.ARG_LINK, object.optString(RideApplication.ARG_LINK, "")).apply();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                editor.putString(RideApplication.ARG_SUBSCRIPTION_HASH, preferences.getString(RideApplication.ARG_IDENTIFY, "")).apply();
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
