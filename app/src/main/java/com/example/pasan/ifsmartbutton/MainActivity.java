package com.example.pasan.ifsmartbutton;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    Button btnConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ConnectWifiTask().execute();
            }
        });

    }

    private void init() {
        btnConnect = (Button) findViewById(R.id.btnConnect);

    }


    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET,
                        Manifest.permission.CHANGE_WIFI_STATE,
                        Manifest.permission.ACCESS_WIFI_STATE

                }, 87);
            }
        }

        statusCheck();
    }


    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private class ConnectWifiTask extends AsyncTask<Void, Void, Void> {


        WifiManager wifiManager;
        WifiConfiguration conf;

        ProgressDialog pDialog;


        @Override
        protected void onPreExecute() {

            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.setMessage("Searching Device..");
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... voids) {

            wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            conf = new WifiConfiguration();

            String networkSSID = "\"" + "Smart Button" + "\"";

            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }

            wifiManager.disconnect();

            conf.SSID = networkSSID;
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

            int netID = wifiManager.addNetwork(conf);


            if (wifiManager.enableNetwork(netID, true)) {

                long start = System.currentTimeMillis();
                long end = start + 30 * 1000;

                while (System.currentTimeMillis() < end) {

                    if (wifiManager.getConnectionInfo().getSupplicantState() == SupplicantState.COMPLETED) {

                        String connectedWIFI = wifiManager.getConnectionInfo().getSSID().toString();

                        if (connectedWIFI.equals(networkSSID)) {

                            if (pDialog != null) {
                                pDialog.dismiss();
                                pDialog = null;
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Device Connected Successfully!", Toast.LENGTH_SHORT).show();
                                }
                            });

                            Intent intent = new Intent(MainActivity.this, ConnectivityActivity.class);
                            intent.putExtra("wifiid", netID);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            finish();
                            break;

                        } else {

                            if (pDialog != null) {
                                pDialog.dismiss();
                                pDialog = null;
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Device not Found, Please retry!", Toast.LENGTH_SHORT).show();
                                }
                            });

                            wifiManager.removeNetwork(netID);
                            break;
                        }
                    }
                }


                if(System.currentTimeMillis() > end){

                    if (pDialog != null) {
                        pDialog.dismiss();
                        pDialog = null;
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Device not Found, Please retry!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    wifiManager.removeNetwork(netID);
                }
            }

            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (pDialog != null) {
                pDialog.dismiss();
                pDialog = null;
            }

        }
    }


}
