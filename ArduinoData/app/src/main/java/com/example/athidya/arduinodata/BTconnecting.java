package com.example.athidya.arduinodata;

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

public class BTconnecting extends AppCompatActivity {
    GraphView graphGas;
    GraphView graphTemp;
    GraphView graphSound;

    LineGraphSeries<DataPoint> gasSeries;
    LineGraphSeries<DataPoint> tempSeries;
    LineGraphSeries<DataPoint> soundSeries;

    Button btnDis;
    Button btnStart;

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

    TimerTask mTimerTask;
    final Handler handler = new Handler();
    Timer t = new Timer();
    private int nCounter = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_connection);
        textView0 = (TextView)findViewById(R.id.textView0);
        textView1 = (TextView)findViewById(R.id.textView1);
        textView2 = (TextView)findViewById(R.id.textView2);

        graphGas = (GraphView) findViewById(R.id.graph1);
        gasSeries = new LineGraphSeries<>();
        graphGas.addSeries(gasSeries);
        graphTemp = (GraphView) findViewById(R.id.graph2);
        tempSeries = new LineGraphSeries<>();
        graphTemp.addSeries(tempSeries);
        graphSound = (GraphView) findViewById(R.id.graph3);
        soundSeries = new LineGraphSeries<>();
        graphSound.addSeries(soundSeries);

        //receive the address of the bluetooth device
        Intent newint = getIntent();
        address = newint.getStringExtra(BluetoothPairing.EXTRA_ADDRESS);

        new ConnectBT().execute();
        //call the widgtes
        btnStart = (Button)findViewById(R.id.button2);
        btnStart.setOnClickListener(mButtonStartListener);
        btnDis = (Button)findViewById(R.id.button1);
        btnDis.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect(); //close connection
            }
        });

        Viewport gasViewport = graphGas.getViewport();
        Viewport tempViewport = graphTemp.getViewport();
        Viewport soundViewport = graphSound.getViewport();

        gasViewport.setYAxisBoundsManual(true);
        tempViewport.setYAxisBoundsManual(true);
        soundViewport.setYAxisBoundsManual(true);

        gasViewport.setMinY(0);
        tempViewport.setMinY(0);
        soundViewport.setMinY(0);

        gasViewport.setMaxY(80);
        tempViewport.setMaxY(80);
        soundViewport.setMaxY(80);

        graphGas.getViewport().setXAxisBoundsManual(true);
        graphTemp.getViewport().setXAxisBoundsManual(true);
        graphSound.getViewport().setXAxisBoundsManual(true);

        gasViewport.setMinX(0);
        tempViewport.setMinX(0);
        soundViewport.setMinX(0);

        gasViewport.setMaxX(20);
        tempViewport.setMaxX(20);
        soundViewport.setMaxX(20);

        gasViewport.setScrollable(true);
        tempViewport.setScrollable(true);
        soundViewport.setScrollable(true);

    }

    View.OnClickListener mButtonStartListener = new View.OnClickListener() {
        public void onClick(View v) {
            doTimerTask();
        }
    };


    public void doTimerTask(){

        mTimerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        DataPoint[] currData = readData();
                        gasSeries.appendData(currData[0], true, 20);
                        tempSeries.appendData(currData[1], true, 20);
                        soundSeries.appendData(currData[2], true, 20);
                        Log.d("TIMER", "TimerTask run");
                        nCounter++;
                    }
                });
            }};

        // public void schedule (TimerTask task, long delay, long period)
        t.schedule(mTimerTask, 2000, 1000);  //

    }
    private DataPoint[] readData() {
        String gasVal = gas();
        String tempVal = temp();
        String soundVal = sound();

        textView0.setText("Gas Level: " + gasVal);
        textView1.setText("Temperature: " + tempVal);
        textView2.setText("Sound (Decibels): "+soundVal);

        DataPoint gasPoint = new DataPoint(nCounter, Integer.parseInt(gasVal));
        DataPoint tempPoint = new DataPoint(nCounter, Integer.parseInt(tempVal));
        DataPoint soundPoint = new DataPoint(nCounter, Integer.parseInt(soundVal));

        DataPoint[] currData = new DataPoint[]{gasPoint, tempPoint, soundPoint};
        return currData;
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
