package com.example.user.nsdimageclient;

import android.graphics.Color;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;

public class ConnectionViewActivity extends AppCompatActivity {
    TextView statusText;
    TextView baituSkaits;
    SocketConnection socketConnection;
    String TAG = "ConnViewActivity";
    FrameLayout frameLayoutPicture;
    AttelaSkats skats;
    double incomingByteCount = 0;
int instrukcija=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initialize();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        socketConnection.tearDown();
        Log.d(TAG, "onDestroy");

        super.onDestroy();


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        skats.velamaisAugstums = ((FrameLayout) findViewById(R.id.framePicture)).getHeight();
        super.onWindowFocusChanged(hasFocus);
    }

    void initialize() {
        Bundle bundle = this.getIntent().getExtras();
        NsdServiceInfo serviceInfo = (NsdServiceInfo) bundle.getParcelable("param1");
        statusText = (TextView) findViewById(R.id.textViewStatus);
        statusText.setText(serviceInfo.toString());
        baituSkaits = (TextView) findViewById(R.id.textViewBaituSkaititajs);

        FrameLayout frameLayoutPicture = (FrameLayout) findViewById(R.id.framePicture);
GridLayout poguGrid = (GridLayout)findViewById(R.id.poguGrid);

        skats = new AttelaSkats(this, frameLayoutPicture.getHeight());
        frameLayoutPicture.addView(skats);
        socketConnection = new SocketConnection(mUpdateHandler);
        connectToServer(serviceInfo);
        //skats.bitmap = socketConnection.getBitmap();
        statusText.bringToFront();
        baituSkaits.bringToFront();
        poguGrid.bringToFront();
        statusText.setBackgroundColor(Color.TRANSPARENT);
    }

    private Handler mUpdateHandler = new Handler() {//a ui tredaa
        @Override
        public void handleMessage(Message msg) {
            if ((msg.getData().getString("msg")).compareTo("kadrs") != 0) {
                statusText.append(msg.getData().getString("msg"));
            } else
//
            {

                skats.mSetBitmap(socketConnection.getBitmap());
                incomingByteCount += msg.getData().getInt(null);
                baituSkaits.setText(incomingByteCount / 1000000 + " Mb");
            }
//skats.invalidate();
        }
    };

    void connectToServer(NsdServiceInfo serviceInfo) {
        if (serviceInfo != null) {
            Log.d(TAG, "Connecting.");
            socketConnection.connectToServer(serviceInfo.getHost(),
                    serviceInfo.getPort());
        } else {
            Log.d(TAG, "No service to connect to!");
        }
    }
  public   void forwardClick(View view){
        instrukcija++;
        socketConnection.addMessage(instrukcija);

    }
   public void revrseClick(View view){
        instrukcija--;
        socketConnection.addMessage(instrukcija);


    }
}
