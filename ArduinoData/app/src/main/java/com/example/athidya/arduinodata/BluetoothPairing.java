package com.example.athidya.arduinodata;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import java.util.Set;
import java.util.ArrayList;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;


public class BluetoothPairing extends AppCompatActivity {
    Button btnPaired;
    ListView devicelist;

    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> BluetoothPairing;
    public static String EXTRA_ADDRESS = "device_address";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_pairing);

        btnPaired = (Button) findViewById(R.id.button);
        devicelist = (ListView) findViewById(R.id.listView);
        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if (myBluetooth == null) {
            //Show a msg that the device has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            //finish apk
           // finish();
        } else {
            if (myBluetooth.isEnabled()) {
            } else {
                //Ask to the user turn the bluetooth on
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon, 1);
            }
        }
        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothPairingList(); //method that will be called
            }
        });
    }
    //get list of paired devices
    private void BluetoothPairingList() {
        Set<BluetoothDevice> BluetoothPairing = myBluetooth.getBondedDevices();
        ArrayList pairedlist = new ArrayList();
        ArrayList locallist = new ArrayList();

        if (BluetoothPairing.size() > 0) {
            for (BluetoothDevice bt : BluetoothPairing) {
                pairedlist.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }
        // finding unpaired devices TODO
        Intent discoverableIntent = new Intent(myBluetooth.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(myBluetooth.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, pairedlist);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked




    }
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            msg(address);
            // Make an intent to start next activity.
            Intent i = new Intent(BluetoothPairing.this, BTconnecting.class);
            i.putExtra(EXTRA_ADDRESS, address);
            startActivity(i);
            msg("Trying to Connect");
        }
    };
}