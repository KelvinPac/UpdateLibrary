package com.homeautogroup.mylittlelibrary;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class WebsiteService extends IntentService {


    private static final String TAG =  "WebsiteService";
    //private static final String URL = "http://androidtutorialpoint.com/lucky_number.php";
    private static final String URL = "http://192.168.137.1/app_login/lucky_number.php";
    private static final int POLL_INTERVAL = 60 * 1000; // 1 minutes

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */

    public WebsiteService(String name) {
        super(name);
    }

    public WebsiteService(){
        super(TAG);
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        if (!isNetworkAvailableAndConnected()) {
            return;
        }

        String cancel_req_tag = "login";
        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response);
                try {
                    //JSONArray jsonArray = new JSONArray(response);
                    JSONObject jObj = new JSONObject(response);
                   // JSONObject obj = jsonArray.getJSONObject(0);
                    //final int luckyNumber = obj.getInt("lucky_number");
                    final int luckyNumber =jObj.getInt("lucky_number");
                    // Creates an explicit intent for an Activity in your app
                    Intent resultIntent = new Intent(WebsiteService.this, ResultActivity.class);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(WebsiteService.this);
                    stackBuilder.addParentStack(ResultActivity.class);
                    stackBuilder.addNextIntent(resultIntent);

                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

                    /*Notification notification = new NotificationCompat.Builder(WebsiteService.this)
                                .setTicker("Lucky Number")
                                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                                .setContentTitle("Lucky Number: " + luckyNumber)
                                .setAutoCancel(true)
                                .build();
                    NotificationManagerCompat notificationManager =
                                NotificationManagerCompat.from(WebsiteService.this);
                    notificationManager.notify(0, notification);*/

                    NotificationCompat.Builder mBuilder =
                            (NotificationCompat.Builder) new NotificationCompat.Builder(WebsiteService.this)
                                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                                    .setContentTitle("Lucky Number: " + luckyNumber)
                                    .setAutoCancel(true);

                    Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.sales);
                    NotificationCompat.BigPictureStyle s = new NotificationCompat.BigPictureStyle().bigPicture(largeIcon);
                    s.setSummaryText("Summary text appears on expanding the notification");
                    mBuilder.setStyle(s);

                    mBuilder.setContentIntent(resultPendingIntent);
                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(1, mBuilder.build());


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        // Adding request to request queue
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(strReq,cancel_req_tag);

        Log.i(TAG, "Received an intent: " + intent);
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable &&
                cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;

    }

    public static Intent newIntent(Context context) {
        return new Intent(context, WebsiteService.class);
    }

    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = WebsiteService.newIntent(context);
        /*
        * To refresh the lucky number every minute, we need something to send the intent to android
        * intent service on our behalf at an interval of 1 minute. This is done by AlarmManager*/
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,  SystemClock.elapsedRealtime(), POLL_INTERVAL, pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }
}
