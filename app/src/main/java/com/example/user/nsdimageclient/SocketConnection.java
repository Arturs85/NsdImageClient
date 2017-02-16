package com.example.user.nsdimageclient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by user on 2017.01.07..
 * <p>
 * Created by user on 2017.01.02..
 */

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

public class SocketConnection {

    private Handler mUpdateHandler;

    public SckClient mSckClient;

    private static final String TAG = "SckConnection";

    private Socket mSocket;
    private int mPort = -1;

    public SocketConnection(Handler handler) {
        mUpdateHandler = handler;

    }

    public void tearDown() {

        if (mSckClient != null)
            mSckClient.tearDown();
    }

    public void connectToServer(InetAddress address, int port) {
        mSckClient = new SckClient(address, port);
        Log.i(TAG, "Sck client created: " + address);
        updateMessages(TAG + "Sck client created: " + address, 0);
    }

    public Bitmap getBitmap() {
        Bitmap bitmap = null;
        if (mSckClient != null)
            bitmap = mSckClient.bitmap;
        return bitmap;
    }

    public void addMessage(int msg) {
        if (mSckClient != null) {
            mSckClient.addMessageToQueue(msg);

        }
    }

    public int getLocalPort() {
        return mPort;
    }

    public void setLocalPort(int port) {
        mPort = port;
    }


    public synchronized void updateMessages(String msg, int lenght) {
        Log.e(TAG, "Updating message: ");


        Bundle messageBundle = new Bundle();
        // messageBundle.putByteArray("msg", msg);
        messageBundle.putString("msg", msg);
        messageBundle.putInt(null, lenght);
        Message message = new Message();
        message.setData(messageBundle);
        mUpdateHandler.sendMessage(message);

    }

    private synchronized void setSocket(Socket socket) {
        Log.d(TAG, "setSocket being called.");
        if (socket == null) {
            Log.d(TAG, "Setting a null socket.");
        }
        if (mSocket != null) {
            if (mSocket.isConnected()) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    // TODO(alexlucas): Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        mSocket = socket;
    }

    private Socket getSocket() {
        return mSocket;
    }


    public class SckClient {

        private InetAddress mAddress;
        private int PORT;
        private Bitmap bitmap;

        private final String CLIENT_TAG = "SckClient";

        public Thread mSendThread;
        private Thread mRecThread;
private  BlockingQueue<Integer> mmMessageQueue;
        private int QUEUE_CAPACITY = 10;
        public SckClient(InetAddress address, int port) {

            Log.d(CLIENT_TAG, "Creating sckClient");
            this.mAddress = address;
            this.PORT = port;
            mmMessageQueue = new ArrayBlockingQueue<Integer>(QUEUE_CAPACITY);

            mSendThread = new SendingThread();
            mSendThread.start();

        }

        public class SendingThread extends Thread {
            DataOutputStream dao;
            //public BlockingQueue<Integer> mMessageQueue;


            public SendingThread() {
               // mMessageQueue = new ArrayBlockingQueue<Integer>(QUEUE_CAPACITY);

            }

           // public void addToQueue(int msg) {
          //      mMessageQueue.add(msg);
            //}

            @Override
            public void run() {
                try {
                    if (getSocket() == null) {
                        setSocket(new Socket(mAddress, PORT));
                        Log.d(CLIENT_TAG, "Client-side socket initialized.");

                    } else {
                        Log.d(CLIENT_TAG, "Socket already initialized. skipping!");
                    }

                    mRecThread = new Thread(new ReceivingThread());
                    mRecThread.start();

                } catch (UnknownHostException e) {
                    Log.d(CLIENT_TAG, "Initializing socket failed, UHE", e);
                } catch (IOException e) {
                    Log.d(CLIENT_TAG, "Initializing socket failed, IOE.", e);
                }

                while (true) {
                    try {
                        int msg = mmMessageQueue.take();
                        if (dao == null) {
                            dao = getOutputStream();
                        }
                        dao.writeInt(msg);
                        dao.writeInt(msg);
                        // sendMessage(msg);
                    } catch (InterruptedException ie) {
                        Log.d(CLIENT_TAG, "Message sending loop interrupted, exiting");
                    } catch (IOException ie) {
                        Log.d(CLIENT_TAG, "io exception");
                    }
                }
            }
        }

        class ReceivingThread implements Runnable {
            Bitmap recievedBitmap;

            @Override
            public void run() {

                DataInputStream input;
                try {
                    input = (new DataInputStream(mSocket.getInputStream()));
                    while (!Thread.currentThread().isInterrupted()) {
                        int lenght = input.readInt();
                        if (lenght > 0) {
                            byte[] messageImage = new byte[lenght];
                            input.readFully(messageImage);
                            Bitmap recievedBitmap = BitmapFactory.decodeByteArray(messageImage, 0, messageImage.length);
                            //Bitmap recievedBitmap = BitmapFactory.decodeStream(input);
                            //input.reset();
                            if (recievedBitmap != null) {
                                Log.d(CLIENT_TAG, "Read from the stream: ");
                                bitmap = recievedBitmap;
//recievedBitmap.recycle();
                                updateMessages("kadrs", lenght);
                            } else {
                                Log.d(CLIENT_TAG, "The nulls! The nulls!");
                                // break;
                            }
                        }

                    }
                    input.close();

                } catch (IOException e) {
                    Log.e(CLIENT_TAG, "Server loop error: ", e);
                }
            }
        }

        public void tearDown() {
            try {
                getSocket().close();
                mRecThread.interrupt();
            } catch (NullPointerException e) {
                Log.e(CLIENT_TAG, "Nav socket, ko aizvērt.");

            } catch (IOException ioe) {
                Log.e(CLIENT_TAG, "Error when closing server socket.");
            }
        }

        public synchronized Bitmap getBitmap() {

            return bitmap;
        }

        public void sendMessage(int msg) {
            try {
                Socket socket = getSocket();
                if (socket == null) {
                    Log.d(CLIENT_TAG, "Socket is null, wtf?");
                } else if (socket.getOutputStream() == null) {
                    Log.d(CLIENT_TAG, "Socket output stream is null, wtf?");
                }

                PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(getSocket().getOutputStream())), true);
                out.println(msg);
                out.flush();
                //updateMessages(msg, true);
            } catch (UnknownHostException e) {
                Log.d(CLIENT_TAG, "Unknown Host", e);
            } catch (IOException e) {
                Log.d(CLIENT_TAG, "I/O Exception", e);
            } catch (Exception e) {
                Log.d(CLIENT_TAG, "Error3", e);
            }
            Log.d(CLIENT_TAG, "Client sent message: ");
        }

        public synchronized void addMessageToQueue(int msg) {
           // mSendThreadaddToQueue(msg);
            //mMessageQueue;
        mmMessageQueue.add(msg);
        }

        public synchronized DataOutputStream getOutputStream() {
            DataOutputStream dao = null;
            if (mSocket != null) {
                try {
                    dao = (new DataOutputStream(getSocket().getOutputStream()));

                } catch (UnknownHostException e) {
                    Log.d(TAG, "Unknown Host", e);
                } catch (IOException e) {
                    Log.d(TAG, "I/O Exception", e);
                } catch (Exception e) {
                    Log.d(TAG, "Error3", e);
                }
                Log.d(TAG, "Returning Stream: ");


            } else
                Log.d(TAG, "mSocket= null ");

            return dao;
        }
    }
}

