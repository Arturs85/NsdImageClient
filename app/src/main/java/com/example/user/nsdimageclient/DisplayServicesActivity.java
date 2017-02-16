package com.example.user.nsdimageclient;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class DisplayServicesActivity extends AppCompatActivity {
    String TAG = "NsdImageClient";
    public static final String SERVICE_TYPE = "_http._tcp.";
    private Handler mLogUpdateHandler;
    ArrayList<NsdServiceInfo> atrastieServisi;
    TextView textView;
    Button buttonStopDiscovery;
    Context context;
    NsdHelper nsdHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_services);
        initialize();
        nsdHelper.initializeNsd();
        nsdHelper.discoverServices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, " onDestroy");
        if (nsdHelper.discoveryActive)
            nsdHelper.stopDiscovery();
//nsdHelper.tearDown();
        if (nsdHelper.mRegistrationListener != null) {
            nsdHelper.mNsdManager.unregisterService(nsdHelper.mRegistrationListener);
        }
        nsdHelper = null;

    }

    void initialize() {
        textView = (TextView) findViewById(R.id.textFieldServices);
        buttonStopDiscovery = (Button) findViewById(R.id.buttonStopDiscovery);
        buttonStopDiscovery.setOnClickListener(buttonStopDiscListener);
        atrastieServisi = new ArrayList<NsdServiceInfo>();
        final ArrayAdapter adapter = new ArrayAdapter<NsdServiceInfo>(this, R.layout.list_item, atrastieServisi);
        final ListView listViewServices = (ListView) findViewById(R.id.listViewServices);
        listViewServices.setAdapter(adapter);

        mLogUpdateHandler = new Handler() {//atjaunina pieejamo servisu sarakstu ui tredaa
            @Override
            public void handleMessage(Message msg) {
                ArrayList<NsdServiceInfo> serviceInfo = msg.getData().getParcelableArrayList("serviceInfo");//.getString("msg");
                atrastieServisi.clear();
                atrastieServisi.addAll(serviceInfo);//add(logText);

                if (!serviceInfo.isEmpty()) {
                    textView.append(serviceInfo.get(0).getServiceName());
                } else adapter.clear();
            }
        };
        context = this;
        nsdHelper = new NsdHelper(context, mLogUpdateHandler);
        listViewServices.setOnItemClickListener(servicesListListener);
    }


    AdapterView.OnItemClickListener servicesListListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        nsdHelper.startResolve(atrastieServisi.get(position));
        }
    };

    View.OnClickListener buttonStopDiscListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(nsdHelper.discoveryActive)
            nsdHelper.stopDiscovery();

        }
    };
}

