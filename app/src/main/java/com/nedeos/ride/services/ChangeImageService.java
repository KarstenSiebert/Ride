package com.nedeos.ride.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.nedeos.ride.RideApplication;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChangeImageService extends IntentService {

    public ChangeImageService() {
        super("ChangeImageService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {
            Bundle extras = intent.getExtras();

            if (extras != null) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

                if (preferences != null) {
                    final int number = extras.getInt(RideApplication.ARG_IMAGE_NUMBER, 0);

                    try {
                        JSONObject jsonObject = new JSONObject();

                        jsonObject.put(RideApplication.ARG_IDENTIFY, preferences.getString(RideApplication.ARG_IDENTIFY, ""));
                        jsonObject.put(RideApplication.ARG_PASSWORD, preferences.getString(RideApplication.ARG_PASSWORD, ""));
                        jsonObject.put(RideApplication.ARG_IMAGE_NUMBER, number);

                        String filename = "avatar.jpg";

                        switch (number % 4) {
                            case 1:
                                filename = "image1.jpg";
                                break;

                            case 2:
                                filename = "image2.jpg";
                                break;

                            case 3:
                                filename = "image3.jpg";
                                break;
                        }

                        uploadFile("https://ride.barubox.com/uploadImages", jsonObject.toString(), filename);

                    } catch (Exception e) {
                        // e.printStackTrace();
                    }

                } else {
                    Intent lIntent = new Intent(RideApplication.ARG_IMAGE_UPLOAD_FAILED);
                    LocalBroadcastManager.getInstance(ChangeImageService.this).sendBroadcast(lIntent);
                }
            }
        }
    }

    private void uploadFile(final String upLoadServerUri, final String command, final String filename) {

        OkHttpClient client = RideApplication.getOkHttpClient();

        if (client != null) {

            File sourceFile = new File(getFilesDir(), filename);

            if (sourceFile.exists() && (sourceFile.length() > 0)) {

                RequestBody body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("user", command)
                        .addFormDataPart("uploaded_file", filename, RequestBody.create(RideApplication.MEDIA_TYPE_JPG, sourceFile))
                        /*
                        .addFormDataPart("uploaded_file", "avatar.jpg", new CounterRequestBody("image/jpg", sourceFile, new CounterRequestBody.ProgressListener() {
                            @Override
                            public void transferred(long num) {
                                Log.d("Length", String.valueOf(num));
                            }
                        }))
                        */
                        .build();

                Request request = new Request.Builder()
                        .url(upLoadServerUri)
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Intent intent = new Intent(RideApplication.ARG_IMAGE_UPLOAD_FAILED);
                        LocalBroadcastManager.getInstance(ChangeImageService.this).sendBroadcast(intent);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        if (!response.isSuccessful()) {
                            Intent intent = new Intent(RideApplication.ARG_IMAGE_UPLOAD_FAILED);
                            LocalBroadcastManager.getInstance(ChangeImageService.this).sendBroadcast(intent);
                        }

                        response.body().close();
                    }
                });
            }
        }
    }

}
