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
        String temp = "t";
        String tempten = "";
        String tempone = "";
        String tempdis = "";

        try {
            outStream.write(temp.getBytes());

            tempten = String.valueOf(inStream.read()-48);
            tempone = String.valueOf(inStream.read()-48);
            tempdis = String.valueOf(inStream.read()-48);

            if (tempdis.equals("-35")){
                temp = tempten+tempone;
                inStream.skip(1);
            }
            else{
                temp = tempten+tempone+tempdis;
                inStream.skip(2);
            }
            Log.d("debug", temp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temp;
    }

    public String gas(){
        String gas = "g";
        String gasten = "";
        String gasone = "";
        String gasdis = "";

        try {
            outStream.write(gas.getBytes());

            gasten = String.valueOf(inStream.read()-48);
            gasone = String.valueOf(inStream.read()-48);
            gasdis = String.valueOf(inStream.read()-48);
            if (gasdis.equals("-35")){
                gas = gasten+gasone;
                inStream.skip(1);
            }
            else{
                gas = gasten+gasone+gasdis;
                inStream.skip(2);
            }
            Log.d("debug", gas);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gas;
    }

    public String sound(){
        String dec = "o";

        String decten = "";
        String decone = "";
        String decdis = "";

        try {
            outStream.write(dec.getBytes());

            decten = String.valueOf(inStream.read()-48);
            decone = String.valueOf(inStream.read()-48);
            decdis = String.valueOf(inStream.read()-48);
            if (decdis.equals("-35")){
                dec = decten+decone;
                inStream.skip(1);
            }
            else{
                dec = decten+decone+decdis;
                inStream.skip(2);
            }
            Log.d("debug", dec);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dec;
    }

    public String[] getCoord() {
        String coord = "m";
        String[] coords = new String[6];

        String coordtenx = "";
        String coordonex = "";
        String coorddis1 = "";
        String coordteny = "";
        String coordoney = "";
        String coorddis2 = "";
        String coordtenorien = "";
        String coordoneorien = "";
        String coorddis3 = "";
        String coordteno1 = "";
        String coordoneo1 = "";
        String coorddis4;
        String coordteno2 = "";
        String coordoneo2 = "";
        String coorddis5;
        String coordteno3 = "";
        String coordoneo3 = "";
        String coorddis6;

        try {
            outStream.write(coord.getBytes());
            coordtenx = String.valueOf(inStream.read()-48);
            coordonex = String.valueOf(inStream.read()-48);
            if (coordonex.equals("-4")){
                coords[0] = coordtenx;
            }
            else{
                coorddis1 = String.valueOf(inStream.read()-48);
                System.out.println(coorddis1);

                if (coorddis1.equals("-4")){
                    coords[0] = coordtenx+coordonex;
                }
                else{
                    coords[0] = coordtenx+coordonex+coorddis1;
                    inStream.skip(1);
                }
            }


            coordteny = String.valueOf(inStream.read()-48);
            coordoney = String.valueOf(inStream.read()-48);
            if (coordoney.equals("-4")){
                coords[1] = coordteny;
            }
            else{
                coorddis2 = String.valueOf(inStream.read()-48);
                System.out.println(coorddis2);
                if (coorddis2.equals("-4")){
                    coords[1] = coordteny+coordoney;
                }
                else{
                    coords[1] = coordteny+coordoney+coorddis2;
                    inStream.skip(1);
                }
            }

            coordtenorien = String.valueOf(inStream.read()-48);
            coordoneorien = String.valueOf(inStream.read()-48);
            if (coordoneorien.equals("-4")){
                coords[2] = coordtenorien;
            }
            else{
                coorddis3 = String.valueOf(inStream.read()-48);
                if (coorddis3.equals("-4")){
                    coords[2] = coordtenorien+coordoneorien;
                }
                else{
                    coords[2] = coordtenorien+coordoneorien+coorddis3;
                    inStream.skip(1);
                }
            }


            coordteno1 = String.valueOf(inStream.read()-48);
            coordoneo1 = String.valueOf(inStream.read()-48);
            if (coordoneo1.equals("-4")){
                coords[3] = coordteno1;
            }
            else{
                coorddis4 = String.valueOf(inStream.read()-48);
                if (coorddis4.equals("-4")){
                    coords[3] = coordteno1+coordoneo1;
                }
                else{
                    coords[3] = coordteno1+coordoneo1+coorddis4;
                    inStream.skip(1);
                }
            }


            coordteno2 = String.valueOf(inStream.read()-48);
            coordoneo2 = String.valueOf(inStream.read()-48);
            if (coordoneo2.equals("-4")){
                coords[4] = coordteno2;
            }
            else{
                coorddis5 = String.valueOf(inStream.read()-48);
                if (coorddis5.equals("-4")){
                    coords[4] = coordteno2+coordoneo2;
                }
                else{
                    coords[4] = coordteno2+coordoneo2+coorddis5;
                    inStream.skip(1);
                }
            }


            coordteno3 = String.valueOf(inStream.read()-48);
            coordoneo3 = String.valueOf(inStream.read()-48);
            if (coordoneo3.equals("-35")){
                coords[5] = coordteno3;
            }
            else{
                coorddis6 = String.valueOf(inStream.read()-48);
                if (coorddis6.equals("-35")){
                    coords[5] = coordteno3+coordoneo3;
                    inStream.skip(1);
                }
                else{
                    coords[5] = coordteno3+coordoneo3+coorddis6;
                    inStream.skip(2);
                }
            }

        }catch(IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < coords.length; i++){
            System.out.println(coords[i]);
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