package com.nedeos.ride.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.nedeos.ride.R;
import com.nedeos.ride.RideActivity;
import com.nedeos.ride.RideApplication;
import com.nedeos.ride.messages.Message;
import com.nedeos.ride.messages.RideDBHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Karsten on 28.09.2016.
 */

public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if ((preferences != null) && preferences.getBoolean("notifications_new_message", true)) {
            final int category = preferences.getInt(RideApplication.ARG_CATEGORY, 255);

            if (remoteMessage.getData().size() > 0) {
                Map data = remoteMessage.getData();

                if (data.containsKey("post")) {

                    try {
                        JSONObject jsonObject = new JSONObject(data.get("post").toString());

                        if ((category & jsonObject.optInt(RideApplication.ARG_CATEGORY, 255)) != 0) {
                            final int resourceId = getResourceId(category & jsonObject.optInt(RideApplication.ARG_CATEGORY, 255));

                            if (jsonObject.optString(RideApplication.ARG_ICON, "").equals("null") || (jsonObject.optString(RideApplication.ARG_ICON, "").length() == 0)) {
                                jsonObject.put("icon", "https://load.barubox.com/images/ride.png");
                            }

                            final Message message = new Message();

                            message.setHead(jsonObject.optString(RideApplication.ARG_HEAD, ""));
                            message.setText(jsonObject.optString(RideApplication.ARG_TEXT, ""));
                            message.setLink(jsonObject.optString(RideApplication.ARG_LINK, ""));
                            message.setIcon(jsonObject.optString(RideApplication.ARG_ICON, ""));
                            message.setShot(jsonObject.optString(RideApplication.ARG_SHOT, ""));
                            message.setProd(jsonObject.optString(RideApplication.ARG_PROD, ""));

                            message.setTime(System.currentTimeMillis());

                            message.setNoid(new Random().nextInt(100000) + 1);

                            notifyApplication(message, false, resourceId);
                        }

                    } catch (JSONException e) {
                        // e.printStackTrace();
                    }

                } else if (data.containsKey("push") && preferences.getBoolean("receive_all_locals", true)) {

                    try {
                        JSONObject jsonObject = new JSONObject(data.get("push").toString());

                        if ((category & jsonObject.optInt(RideApplication.ARG_CATEGORY, 255)) != 0) {
                            final int resourceId = getResourceId(category & jsonObject.optInt(RideApplication.ARG_CATEGORY, 255));

                            if (jsonObject.optString(RideApplication.ARG_ICON, "").equals("null") || (jsonObject.optString(RideApplication.ARG_ICON, "").length() == 0)) {
                                jsonObject.put("icon", "https://load.barubox.com/images/ride.png");
                            }

                            final Message message = new Message();

                            message.setHead(jsonObject.optString(RideApplication.ARG_HEAD, ""));
                            message.setText(jsonObject.optString(RideApplication.ARG_TEXT, ""));
                            message.setLink(jsonObject.optString(RideApplication.ARG_LINK, ""));
                            message.setIcon(jsonObject.optString(RideApplication.ARG_ICON, ""));
                            message.setShot(jsonObject.optString(RideApplication.ARG_SHOT, ""));
                            message.setProd(jsonObject.optString(RideApplication.ARG_PROD, ""));

                            message.setTime(System.currentTimeMillis());

                            message.setNoid(new Random().nextInt(100000) + 1);

                            notifyApplication(message, false, resourceId);
                        }

                    } catch (JSONException e) {
                        // e.printStackTrace();
                    }

                } else if (data.containsKey("ride")) {

                    try {
                        JSONObject jsonObject = new JSONObject(data.get("ride").toString());

                        if (jsonObject.optString(RideApplication.ARG_ICON, "").equals("null") || (jsonObject.optString(RideApplication.ARG_ICON, "").length() == 0)) {
                            jsonObject.put("icon", "https://load.barubox.com/images/ride.png");
                        }

                        final Message message = new Message();

                        message.setHead(jsonObject.optString(RideApplication.ARG_HEAD, ""));
                        message.setText(jsonObject.optString(RideApplication.ARG_TEXT, ""));
                        message.setLink(jsonObject.optString(RideApplication.ARG_LINK, ""));
                        message.setIcon(jsonObject.optString(RideApplication.ARG_ICON, ""));
                        message.setShot(jsonObject.optString(RideApplication.ARG_SHOT, ""));
                        message.setProd(jsonObject.optString(RideApplication.ARG_PROD, ""));

                        message.setTime(System.currentTimeMillis());

                        message.setNoid(999999);

                        notifyApplication(message, true, R.mipmap.ic_alert_white);

                    } catch (JSONException e) {
                        // e.printStackTrace();
                    }

                } else if (data.containsKey("subs")) {

                    try {
                        JSONObject jsonObject = new JSONObject(data.get("subs").toString());

                        if (jsonObject.optString(RideApplication.ARG_ICON, "").equals("null") || (jsonObject.optString(RideApplication.ARG_ICON, "").length() == 0)) {
                            jsonObject.put("icon", "https://load.barubox.com/images/ride.png");
                        }

                        final Message message = new Message();

                        message.setHead(jsonObject.optString(RideApplication.ARG_HEAD, ""));
                        message.setText(jsonObject.optString(RideApplication.ARG_TEXT, ""));
                        message.setLink(jsonObject.optString(RideApplication.ARG_LINK, ""));
                        message.setIcon(jsonObject.optString(RideApplication.ARG_ICON, ""));
                        message.setShot(jsonObject.optString(RideApplication.ARG_SHOT, ""));
                        message.setProd(jsonObject.optString(RideApplication.ARG_PROD, ""));

                        message.setTime(System.currentTimeMillis());

                        message.setNoid(new Random().nextInt(100000) + 1);

                        notifyApplication(message, false, R.mipmap.ic_car_white);

                    } catch (JSONException e) {
                        // e.printStackTrace();
                    }

                }
            }

            if (remoteMessage.getNotification() != null) {
                //
            }
        }
    }

    private void notifyApplication(final Message message, final boolean emergeny, final int resourceId) {
        final OkHttpClient client = RideApplication.getOkHttpClient();

        if (client != null) {
            final Request request = new Request.Builder()
                    .url(message.getIcon())
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Bitmap minicon = null;

                    try {
                        minicon = getCircleBitmap(BitmapFactory.decodeStream(getApplicationContext().getAssets().open("launcher.png")));

                    } catch (IOException k) {
                        // k.printStackTrace();
                    }

                    if (minicon != null) {
                        addMessageToRideDB(message);

                        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        if (notificationManager != null) {
                            final String channelID = "rideChannel";

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                NotificationChannel notificationChannel = new NotificationChannel(channelID, "Ride", NotificationManager.IMPORTANCE_DEFAULT);

                                notificationChannel.setDescription("Ride Channel");
                                notificationChannel.setLightColor(Color.CYAN);
                                notificationChannel.canShowBadge();
                                notificationChannel.setShowBadge(true);

                                notificationManager.createNotificationChannel(notificationChannel);
                            }

                            Intent intent = new Intent(getApplicationContext(), RideActivity.class);
                            intent.setAction(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_LAUNCHER);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), message.getNoid(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

                            final int counter = RideDBHelper.getCountFromDB(RideApplication.DB_MESSAGES_TABLE);

                            if (counter > 1) {
                                Notification notification = new NotificationCompat.Builder(MessagingService.this, channelID)
                                        .setContentTitle(message.getHead())
                                        .setSmallIcon(resourceId)
                                        .setLargeIcon(minicon)
                                        .setBadgeIconType(R.mipmap.ic_launcher)
                                        .setSound(Uri.parse(getSound(emergeny)))
                                        .setDefaults(getVibrate(emergeny))
                                        .setContentText(message.getText())
                                        .setContentIntent(pendingIntent)
                                        .setAutoCancel(true)
                                        .setStyle(new NotificationCompat.InboxStyle()
                                                .setBigContentTitle(message.getHead())
                                                .addLine(message.getText())
                                                .setSummaryText("+" + String.valueOf(counter - 1) + " " + getString(R.string.more)))
                                        .build();

                                notificationManager.notify(0, notification);

                            } else if (counter == 1) {
                                Notification notification = new NotificationCompat.Builder(MessagingService.this, channelID)
                                        .setContentTitle(message.getHead())
                                        .setSmallIcon(resourceId)
                                        .setLargeIcon(minicon)
                                        .setBadgeIconType(R.mipmap.ic_launcher)
                                        .setSound(Uri.parse(getSound(emergeny)))
                                        .setDefaults(getVibrate(emergeny))
                                        .setContentText(message.getText())
                                        .setContentIntent(pendingIntent)
                                        .setAutoCancel(true)
                                        .build();

                                notificationManager.notify(0, notification);
                            }
                        }
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Bitmap minicon = null;

                    if (response.isSuccessful()) {
                        minicon = getCircleBitmap(BitmapFactory.decodeStream(response.body().byteStream()));

                        if (minicon == null) {

                            try {
                                minicon = getCircleBitmap(BitmapFactory.decodeStream(getApplicationContext().getAssets().open("launcher.png")));

                            } catch (IOException k) {
                                // k.printStackTrace();
                            }
                        }

                    } else {

                        try {
                            minicon = getCircleBitmap(BitmapFactory.decodeStream(getApplicationContext().getAssets().open("launcher.png")));

                        } catch (IOException k) {
                            // k.printStackTrace();
                        }
                    }

                    if (minicon != null) {
                        addMessageToRideDB(message);

                        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        if (notificationManager != null) {
                            final String channelID = "rideChannel";

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                NotificationChannel notificationChannel = new NotificationChannel(channelID, "Ride", NotificationManager.IMPORTANCE_DEFAULT);

                                notificationChannel.setDescription("Ride Channel");
                                notificationChannel.setLightColor(Color.CYAN);
                                notificationChannel.canShowBadge();
                                notificationChannel.setShowBadge(true);

                                notificationManager.createNotificationChannel(notificationChannel);
                            }

                            Intent intent = new Intent(getApplicationContext(), RideActivity.class);
                            intent.setAction(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_LAUNCHER);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), message.getNoid(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

                            final int counter = RideDBHelper.getCountFromDB(RideApplication.DB_MESSAGES_TABLE);

                            if (counter > 1) {
                                Notification notification = new NotificationCompat.Builder(MessagingService.this, channelID)
                                        .setContentTitle(message.getHead())
                                        .setSmallIcon(resourceId)
                                        .setLargeIcon(minicon)
                                        .setBadgeIconType(R.mipmap.ic_launcher)
                                        .setSound(Uri.parse(getSound(emergeny)))
                                        .setDefaults(getVibrate(emergeny))
                                        .setContentText(message.getText())
                                        .setContentIntent(pendingIntent)
                                        .setAutoCancel(true)
                                        .setStyle(new NotificationCompat.InboxStyle()
                                                .setBigContentTitle(message.getHead())
                                                .addLine(message.getText())
                                                .setSummaryText("+" + String.valueOf(counter - 1) + " " + getString(R.string.more)))
                                        .build();

                                notificationManager.notify(0, notification);

                            } else if (counter == 1) {
                                Notification notification = new NotificationCompat.Builder(MessagingService.this, channelID)
                                        .setContentTitle(message.getHead())
                                        .setSmallIcon(resourceId)
                                        .setLargeIcon(minicon)
                                        .setBadgeIconType(R.mipmap.ic_launcher)
                                        .setSound(Uri.parse(getSound(emergeny)))
                                        .setDefaults(getVibrate(emergeny))
                                        .setContentText(message.getText())
                                        .setContentIntent(pendingIntent)
                                        .setAutoCancel(true)
                                        .build();

                                notificationManager.notify(0, notification);
                            }
                        }
                    }

                    response.body().close();
                }
            });
        }
    }

    private int getVibrate(boolean emergeny) {

        if (!emergeny) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

            if (preferences.getBoolean("notifications_new_message_vibrate", false)) {
                return Notification.DEFAULT_VIBRATE;
            }

            return 0;
        }

        return Notification.DEFAULT_VIBRATE;
    }

    private String getSound(boolean emergeny) {

        if (!emergeny) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            return preferences.getString("notifications_new_message_ringtone", "DEFAULT_SOUND");
        }

        return "content://settings/system/notification_sound";
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap output = null;

        if (bitmap != null) {

            if (bitmap.getWidth() > bitmap.getHeight()) {
                output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

            } else {
                output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
            }

            final Canvas canvas = new Canvas(output);

            final int color = Color.RED;

            final Paint paint = new Paint();

            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

            paint.setAntiAlias(true);

            canvas.drawARGB(0, 0, 0, 0);

            paint.setColor(color);

            float r;

            if (bitmap.getWidth() > bitmap.getHeight()) {
                r = bitmap.getHeight() / 2;

            } else {
                r = bitmap.getWidth() / 2;
            }

            canvas.drawCircle(r, r, r, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

            canvas.drawBitmap(bitmap, rect, rect, paint);

            bitmap.recycle();
        }

        return output;
    }

    private void addMessageToRideDB(Message message) {
        final RideDBHelper rideDBHelper = RideApplication.getRideDBHelper();

        if (rideDBHelper != null) {
            final SQLiteDatabase db = rideDBHelper.getWritableDatabase();

            if (db != null) {
                ContentValues values = new ContentValues();

                values.put(RideApplication.ARG_HEAD, message.getHead());
                values.put(RideApplication.ARG_TEXT, message.getText());
                values.put(RideApplication.ARG_LINK, message.getLink());
                values.put(RideApplication.ARG_ICON, message.getIcon());
                values.put(RideApplication.ARG_SHOT, message.getShot());
                values.put(RideApplication.ARG_PROD, message.getProd());
                values.put(RideApplication.ARG_TIME, message.getTime());
                values.put(RideApplication.ARG_NOID, message.getNoid());

                db.insert(RideApplication.DB_MESSAGES_TABLE, null, values);

                Intent intent = new Intent(RideApplication.ARG_MESSAGE_NOTIFICATION);
                intent.putExtra(RideApplication.ARG_MESSAGE, message);

                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }
        }
    }

    private int getResourceId(int category) {

        if (category == 1) {
            return R.mipmap.ic_rss_feed_white;

        } else if (category == 2) {
            return R.mipmap.ic_car_white;

        } else if (category == 4) {
            return R.mipmap.ic_event_white;

        } else if (category == 8) {
            return R.mipmap.ic_shopping_cart_white;
        }

        return R.mipmap.ic_car_white;
    }

}
