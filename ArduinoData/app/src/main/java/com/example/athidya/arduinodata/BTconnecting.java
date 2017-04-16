package com.example.athidya.arduinodata;

import android.bluetooth.BluetoothServerSocket;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;



public class BTconnecting extends AppCompatActivity {
    Button btnDis;
    TextView textView0;
    TextView textView1;
    TextView textView2;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_connection);
        textView0 = (TextView)findViewById(R.id.textView0);
        textView1 = (TextView)findViewById(R.id.textView1);
        textView2 = (TextView)findViewById(R.id.textView2);
        //receive the address of the bluetooth device
        Intent newint = getIntent();
        address = newint.getStringExtra(BluetoothPairing.EXTRA_ADDRESS);

        new ConnectBT().execute();
        //call the widgtes
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

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(BTconnecting.this, "Connecting...", "Please wait");  //show a progress dialog
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
            readData();

        }
        private void readData() {
            textView0.setText("Gas Level: " + gas());
            textView1.setText("Temperature: " + temp());
            textView2.setText("Sound (Decibels): "+sound());
        }
        private String temp() {
            String temp = "t";

            try {
                outStream.write(temp.getBytes());
                temp = String.valueOf(inStream.read());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return temp;
        }
        private String gas(){
           String gas = "g";
            try {
                outStream.write(gas.getBytes());
                gas = String.valueOf(inStream.read());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return gas;
        }
        private String sound(){
            String dec = "o";
            try{
                outStream.write(dec.getBytes());
                dec = String.valueOf(inStream.read());
            }catch(IOException e) {
                e.printStackTrace();
            }
            return dec;
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
}
