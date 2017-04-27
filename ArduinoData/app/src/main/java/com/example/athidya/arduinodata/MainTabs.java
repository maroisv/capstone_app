package com.example.athidya.arduinodata;

import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import android.bluetooth.BluetoothServerSocket;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.Timer;
import java.util.TimerTask;

public class MainTabs extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private SectionsPageAdapter mSectionsPageAdapter;

    private ViewPager mViewPager;

    BluetoothAdapter myBluetooth;
    BluetoothSocket btSocket;
    BluetoothDevice mydevice;
    private boolean isBtConnected = false;
    //MAC address of bluetooth module received in create
    private String address = "";
    private ProgressDialog progress;
    //SSP UUID for android devices
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    OutputStream outStream;
    InputStream inStream;
    ConnectBT connectionThread;
    Button btnDis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tabs);
        Log.d(TAG, "onCreate: Starting.");

        mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        //receive the address of the bluetooth device
        Intent newint = getIntent();
        address = newint.getStringExtra(BluetoothPairing.EXTRA_ADDRESS);

        new ConnectBT().execute();
        //call the widgets
        btnDis = (Button)findViewById(R.id.button1);
        btnDis.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect(); //close connection
            }
        });
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(MainTabs.this, "Connecting...", "Please wait");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice remoteDevice = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = remoteDevice.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                    outStream=btSocket.getOutputStream();
                    inStream=btSocket.getInputStream();
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected");
                isBtConnected = true;


            }
            progress.dismiss();


        }

    }
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    private void Disconnect() {
        if (btSocket != null) //If the btSocket is busy
        {
            try {
                btSocket.close(); //close connection
            } catch (IOException e) {
                msg("Error");
            }
        }
        finish(); //return to the first layout
    }

    public String temp() {
        return getStringFromBluetooth("t");
    }

    public String gas() {
        return getStringFromBluetooth("g");
    }

    public String sound(){
        return getStringFromBluetooth("o");
    }

    public String getStringFromBluetooth(String cmd) {
        int num = 0;
        String content = "";
        String temp = "";

        try {
            outStream.write(cmd.getBytes());

            temp = String.valueOf(inStream.read()-48);
            while (!temp.equals("-35")) {
                if (temp.equals("-4")) {
                    content = content + ",";
                } else {
                    content = content + temp;
                }
                temp = String.valueOf(inStream.read()-48);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return content;
    }

    public String[] getCoord() {
        String[] coords = new String[6];

        String content = getStringFromBluetooth("m");
        int index = 0;
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) == ',') {
                index++;
            } else {
                coords[index] = coords[index] + content.charAt(i);
            }
        }
        return coords;
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new SensorFragment(), "Sensors Monitor");
        adapter.addFragment(new MapFragment(), "Environment Map");
        viewPager.setAdapter(adapter);
    }


}