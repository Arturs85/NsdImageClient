package com.example.user.nsdimageclient;

/**
 * Created by user on 2017.01.06..
 */

import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by user on 2017.01.02..
 */
/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class NsdHelper {

    Context mContext;
    private Handler mUpdateHandler;
    ArrayList<NsdServiceInfo> atrastieServisi;
    NsdManager mNsdManager;
    NsdManager.ResolveListener mResolveListener;
    NsdManager.DiscoveryListener mDiscoveryListener;
    NsdManager.RegistrationListener mRegistrationListener;

    public static final String SERVICE_TYPE = "_http._tcp.";

    public static final String TAG = "NsdHelper";
    public String mServiceName = "NsdChat2";

    NsdServiceInfo mService;
boolean discoveryActive = false;
    public NsdHelper(Context context, Handler handler) {
        atrastieServisi = new ArrayList<NsdServiceInfo>();
        mUpdateHandler = handler;
        mContext = context;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public void initializeNsd() {
        initializeResolveListener();
        initializeDiscoveryListener();
        // initializeRegistrationListener();

        //mNsdManager.init(mContext.getMainLooper(), this);

    }

    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            discoveryActive =true;
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(TAG, "Service discovery success" + service);
                mAdd(atrastieServisi,service);

                updateLogMessages("Atrasts serviss " + service);

                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same machine: " + mServiceName);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost" + service);
               mRemove(atrastieServisi, service);// izdzēš no saraksta ja nosaukumi utt ir vienaadi

                updateLogMessages("Pazudis serviss " + service);

                if (mService == service) {
                    mService = null;
                }
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                discoveryActive = false;
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
                discoveryActive = false;

            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                discoveryActive = false;

                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }
                mService = serviceInfo;
                Toast.makeText(mContext, "Resolved: "+serviceInfo.getServiceName(),
                        Toast.LENGTH_SHORT).show();
            startConnectionActivity(serviceInfo);
            }
        };
    }

    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                mServiceName = NsdServiceInfo.getServiceName();
                Log.d(TAG, "Service registered");
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
                Log.d(TAG, "Service reg filed");
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                Log.d(TAG, "Service unregistred");
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.d(TAG, "Service unreg filed");
            }

        };
    }

    public void registerService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(mServiceName);
        serviceInfo.setServiceType(SERVICE_TYPE);

        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);

    }

    public void discoverServices() {
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public void stopDiscovery() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }

    public NsdServiceInfo getChosenServiceInfo() {
        return mService;
    }

    public void tearDown() {
        mNsdManager.unregisterService(mRegistrationListener);
    }

    public synchronized void updateLogMessages(String msg) {
        Log.d(TAG, "Updating message: " + msg + " ArryList garums: " + atrastieServisi.size());

        Bundle messageBundle = new Bundle();
        messageBundle.putParcelableArrayList("serviceInfo", atrastieServisi);//.putString("msg", msg);

        Message message = new Message();
        message.setData(messageBundle);
        mUpdateHandler.sendMessage(message);

    }

    void mAdd(ArrayList<NsdServiceInfo> list, NsdServiceInfo info) {
        boolean rez = false;
        for (int i = 0; i < list.size(); i++) {
            if ((list.get(i).toString()).equals(info.toString())) {
                rez = true;
                break;
            } else
                rez = false;
        }
        if (!rez){
            list.add(info);
            Log.d(TAG, " ArrayListam pievienots serviss " + info);
    }
    }

    void mRemove(ArrayList<NsdServiceInfo> list, NsdServiceInfo info) {
        for (int i = 0; i < list.size(); i++) {
            if ((list.get(i).toString()).equals(info.toString())) {
                list.remove(i);
                Log.d(TAG, " No ArrayLista izņemts serviss " + info);
                break;
            }
        }
    }

    public void startResolve(NsdServiceInfo info){
        mNsdManager.resolveService(info, mResolveListener);

    }
    private void startConnectionActivity(NsdServiceInfo info){
        Intent i = new Intent(mContext,ConnectionViewActivity.class); //(TweetTestActivity.this, MapsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("param1", info);
        i.putExtras(bundle);
        mContext.startActivity(i);

    }
}
