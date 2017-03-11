/**
 * Created by Kelvin-M on 2/13/2017. at 21:26
 * for homeautogroup.co.ke (Flyboypac@gmail.com)
 * +254705419309
 * PROJECT [AppUpdater]
 */
package com.homeautogroup.mylittlelibrary;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.DownloadManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class Update extends AppCompatActivity {
    private final Context _context;

    //url for json with app data
    private String JSON_URL;

    //json Response from server will be saved in this string
    private String myJSONString;

    //to be able to differentiate our downloads from others
    private long App_DownloadId;

    private PrefManager prefManager;
    // we use double instead of int as app version is typically 1.2, 1.5.
    private double currentVersionCode;

    //data from myJSONString is extracted to the individual strings to be later displayed to the user
    private String whatsNew, newAppUrl, createdAt, newVersion, newSize;
    private boolean updateCheck;
    private String TAG = Update.class.getSimpleName();

    public Update(Context context, String serverUrl) {
        this._context = context;
        this.JSON_URL = serverUrl;
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        _context.registerReceiver(downloadReceiver, filter);
        prefManager = new PrefManager(context);


    }


    public void checkVersionCode() {
        try {
            currentVersionCode = _context.getPackageManager().getPackageInfo(_context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    public String getJSON_URL() {
        return JSON_URL;
    }

    //Method to check updates in the server
    /*
    * We first check if network connection is available
    * We use a background task as the update check may take long.
    * Using the Async task the app performance wont be affected as the check is not in the main thread
    *
    * */
    public void checkUpdate(String url) {
        if (isNetworkAvailable()) {
            class GetDataJson extends AsyncTask<String, String, String> {


                @Override
                protected String doInBackground(String... params) {
                    String uri = params[0];
                    BufferedReader bufferedReader = null;

                    try {
                        URL url = new URL(uri);
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        StringBuilder sb = new StringBuilder();
                        bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        String json;
                        while ((json = bufferedReader.readLine()) != null) {
                            sb.append(json + "\n");
                            /*
                            * If an update was checked successfully and appended to the sting.
                            * Set the boolean updateCheck to true. This will then be used by the onPostExecute Method
                            * */
                            updateCheck = true;
                            Log.e(TAG, "update check is okay");
                        }

                        //send results to onPostExecute
                        return sb.toString().trim();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }


                }

                /*
                *
                * invoked after background computation in doInBackground method completes processing
                * results of the doInBackground is passed to this method*/
                @Override
                protected void onPostExecute(String s) {
                    //Check if updateCheck is true. By default its false.
                    //This prevents app from crashing if the background task was not successful maybe because of internet connection
                    if (updateCheck) {
                        super.onPostExecute(s);
                        try {
                            myJSONString = s;
                        } catch (Exception ignored) {

                        }

                        //Try and extract the data.
                        try {
                            JSONObject jsonObject = new JSONObject(myJSONString);
                            boolean error = jsonObject.getBoolean("error");
                            if (!error) {
                                //uid contains new app version
                                double uid = jsonObject.getDouble("uid");
                                JSONObject user = jsonObject.getJSONObject("user");
                                newVersion = user.getString("version");
                                newAppUrl = user.getString("url");
                                createdAt = user.getString("created_at");
                                whatsNew = user.getString("new");
                                newSize = user.getString("size");
                                Log.e(TAG, newAppUrl);

                                //Compare the app version with server version
                                //if server version is greater . start updateAvailable method

                                if (uid > currentVersionCode) {
                                    //Call this method which will download the app


                                    //PrefManager prefManager = new PrefManager(_context);
                                    prefManager.setUpdateInfo(whatsNew, newSize, newAppUrl, true, newVersion);

                                    updateAvailable();

                                   /* Uri app_url = Uri.parse(newAppUrl);
                                    App_DownloadId = DownloadData(app_url);
                                    Log.e(TAG, "starting download");
*/
                                    Log.e(TAG, "update check is okay " + uid);
                                } else {
                                    /*
                                    * if no update you can use a string to notify users
                                    * sample responses
                                    * {"error":true,"error_msg":"Sorry No Update for know"}
                                    * If NO RESPONSE FROM SERVER IF THERE IS NO UPDATE.. COMMENT THE BELOW
                                    * LINES. App may crash if the response is not found
                                    * */
                                    String errorMsg = jsonObject.getString("error_msg");
                                    /*Toast.makeText(getApplicationContext(),
                                            errorMsg, Toast.LENGTH_LONG).show();*/
                                    Toast.makeText(_context, errorMsg, Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(_context, "UPDATE JSON ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                }
            }

            //create class object
            GetDataJson g = new GetDataJson();
            g.execute(url);

        }
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void updateAvailable() {

        boolean test = false;

        AlertDialog.Builder builder = new AlertDialog.Builder(_context);
        builder.setTitle("Update Available");
        builder.setPositiveButton("Download", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Uri app_url = Uri.parse(newAppUrl);
                App_DownloadId= DownloadData(app_url);
                Log.e(TAG, "starting download");


            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(_context, "Please Consider Updating later\n" + "new Size is " + newSize, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //TODO - implement Later
            }
        });
        builder.setMessage("Stay updated to enjoy latest features \n" + whatsNew);
        AlertDialog update = builder.create();
        update.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        update.show();



    }

    private long DownloadData(Uri uri) {

        long downloadReference;
        DownloadManager downloadManager = (DownloadManager) _context.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        //Setting title of request
        request.setTitle("KYU App Updating");
        //Setting description of request
        request.setDescription("Downloading. powered by HomeAuto Group");
        //TODO: First I wanted to store my update .apk file on internal storage for my app but apparently android does not allow you to open and install
        //TODO: application with existing package from there. alternative solution is Download directory in external storage.
        // If there is better way please give me the feedback

        /*Using your apps private storage also increases your apps size drastically
        * which may lead to user uninstalling your app*/
        String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
        //choose a unique name to avoid conflicts for the update to be successful
        String fileName = "kyuUpdate2017.apk";


        //add the filename to destination
        destination += fileName;
        final Uri uridest = Uri.parse("file://" + destination);
        //Delete update file if exists
        File file = new File(destination);
        if (file.exists()) {
            //TODO: test this because it fails sometimes
            file.delete();
        }
        request.setDestinationUri(uridest);
        Log.e(TAG, "saved in" + uridest);
        //Enqueue download and save the referenceId
        downloadReference = downloadManager.enqueue(request);
        return downloadReference;
    }


    //Broadcast receiver. called when download is finished
    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //check if the broadcast message is for our Enqueued download
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (referenceId == App_DownloadId) {
                //Download is know complete. call this method to handle the update
                updateApp();
            }
        }
    };

    private void updateApp() {
        Toast.makeText(_context, "Updating App", Toast.LENGTH_SHORT).show();
        Intent update = new Intent(Intent.ACTION_VIEW);
        update.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/download/" + "kyuUpdate2017.apk")), "application/vnd.android.package-archive");
        update.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(update);
    }
}
