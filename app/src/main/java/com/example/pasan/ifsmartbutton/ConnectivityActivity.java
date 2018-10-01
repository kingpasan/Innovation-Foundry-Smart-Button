package com.example.pasan.ifsmartbutton;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Xml;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ConnectivityActivity extends AppCompatActivity {

    Spinner txtSSID;
    EditText txtPass;
    Button btnConfigDevice, btnExit;

    WifiManager wifiManager;
    WifiScanReceiever receiever;
    List<String> ssidList;
    ArrayAdapter<String> adapter;

    String wifiID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connectivity);

        init();

        Intent previuosIntent = getIntent();
        Bundle data = previuosIntent.getExtras();
        wifiID = data.get("wifiid").toString();



        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        ssidList = new ArrayList<String>();
        receiever = new WifiScanReceiever();

        registerReceiver(receiever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();


        btnConfigDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    Toast.makeText(ConnectivityActivity.this, "Information Sending to Device..",Toast.LENGTH_LONG).show();

                    String ssid = txtSSID.getSelectedItem().toString();
                    String pass = txtPass.getText().toString();

                    txtSSID.setEnabled(false);
                    txtPass.setEnabled(false);
                    btnConfigDevice.setEnabled(false);

                    ssid = ssid.replace(' ', '+');
                    pass = URLEncoder.encode(pass, "UTF-8");

                    RequestQueue queue = Volley.newRequestQueue(ConnectivityActivity.this);

                    String url = "http://192.168.4.1/?SSID=" + ssid + "&Password=" + pass + "&END=Submit";


                    StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            if (error instanceof NoConnectionError) {
                                Toast.makeText(ConnectivityActivity.this, "Device gone offline!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    queue.add(request);


                    //wifiManager.removeNetwork(netID);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                Toast.makeText(ConnectivityActivity.this, "Because of device firmware problem app cannot detect device configuration success or not. If device make 3 Beeps then Device is configured successfully!", Toast.LENGTH_SHORT).show();


            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiManager.removeNetwork(Integer.parseInt(wifiID));
                unregisterReceiver(receiever);

                System.exit(0);
            }
        });


    }


    @Override
    protected void onStop() {
        super.onStop();
        wifiManager.removeNetwork(Integer.parseInt(wifiID));
        unregisterReceiver(receiever);
    }

    private void init() {
        txtSSID = (Spinner) findViewById(R.id.ssidSpinner);
        txtPass = (EditText) findViewById(R.id.txtPassword);
        btnConfigDevice = (Button) findViewById(R.id.btnConfig);
        btnExit = (Button)findViewById(R.id.btnExit);
    }


    private class WifiScanReceiever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            List<ScanResult> results = wifiManager.getScanResults();

            for (int a = 0; a < results.size(); a++) {
                ssidList.add(results.get(a).SSID);
            }

            adapter = new ArrayAdapter<>(ConnectivityActivity.this, R.layout.spinner_item, ssidList);
            txtSSID.setAdapter(adapter);

        }
    }
}
