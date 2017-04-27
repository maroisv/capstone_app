package com.example.athidya.arduinodata;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import android.os.Handler;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by User on 2/28/2017.
 */

public class SensorFragment extends Fragment {
    private static final String TAG = "Tab1Fragment";
    GraphView graphGas;
    GraphView graphTemp;
    GraphView graphSound;

    LineGraphSeries<DataPoint> gasSeries;
    LineGraphSeries<DataPoint> tempSeries;
    LineGraphSeries<DataPoint> soundSeries;

    LineGraphSeries<DataPoint> gasNormSeries;
    LineGraphSeries<DataPoint> tempNormSeries;
    LineGraphSeries<DataPoint> soundNormSeries;

    Button btnStart;

    TextView textView0;
    TextView textView1;
    TextView textView2;
    TextView textView3;

    TimerTask mTimerTask;
    final Handler handler = new Handler();
    Timer t = new Timer();
    private int nCounter = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor,container,false);
        btnStart = (Button) view.findViewById(R.id.button2);
        btnStart.setOnClickListener(mButtonStartListener);

        super.onCreate(savedInstanceState);
        textView0 = (TextView) view.findViewById(R.id.textView0);
        textView1 = (TextView) view.findViewById(R.id.textView1);
        textView2 = (TextView) view.findViewById(R.id.textView2);

        graphGas = (GraphView) view.findViewById(R.id.graph1);
        gasSeries = new LineGraphSeries<>();
        graphGas.addSeries(gasSeries);
        gasNormSeries = new LineGraphSeries<>();
        graphGas.addSeries(gasNormSeries);
        gasNormSeries.setColor(Color.RED);



        graphTemp = (GraphView) view.findViewById(R.id.graph2);
        tempNormSeries = new LineGraphSeries<>();
        graphTemp.addSeries(tempNormSeries);
        tempNormSeries.setColor(Color.RED);
        tempSeries = new LineGraphSeries<>();
        graphTemp.addSeries(tempSeries);

        graphSound = (GraphView) view.findViewById(R.id.graph3);
        soundSeries = new LineGraphSeries<>();
        graphSound.addSeries(soundSeries);
        soundNormSeries = new LineGraphSeries<>();
        graphSound.addSeries(soundNormSeries);
        soundNormSeries.setColor(Color.RED);

        Viewport gasViewport = graphGas.getViewport();
        Viewport tempViewport = graphTemp.getViewport();
        Viewport soundViewport = graphSound.getViewport();

        gasViewport.setYAxisBoundsManual(true);
        tempViewport.setYAxisBoundsManual(true);
        soundViewport.setYAxisBoundsManual(true);

        gasViewport.setMinY(0);
        tempViewport.setMinY(0);
        soundViewport.setMinY(0);

        gasViewport.setMaxY(100);
        tempViewport.setMaxY(100);
        soundViewport.setMaxY(100);

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

        return view;
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
                        gasNormSeries.appendData(new DataPoint(nCounter,50), true, 25);

                        tempNormSeries.appendData(new DataPoint(nCounter,22), true, 25);
                        tempSeries.appendData(currData[1], true, 20);

                        soundSeries.appendData(currData[2], true, 20);
                        soundNormSeries.appendData(new DataPoint(nCounter,50), true, 25);

                        Log.d("TIMER", "TimerTask run");
                        nCounter++;
                    }
                });
            }};

        // public void schedule (TimerTask task, long delay, long period)
        t.schedule(mTimerTask, 2000, 2000);  //

    }
    private DataPoint[] readData() {
        MainTabs main = (MainTabs) getActivity();
        String gasVal = main.gas();
        String tempVal = main.temp();
        String soundVal = main.sound();

        textView0.setText("Gas Level: " + gasVal);
        textView1.setText("Temperature: " + tempVal);
        textView2.setText("Sound (Decibels): "+soundVal);
        //main.getCoord();
        /*
        if (nCounter == 0){
            String[] mapCoords = main.getCoord();
            String x = mapCoords[0];
            String y = mapCoords[1];
            String orient = mapCoords[2];
            String ob1 = mapCoords[3];
            String ob2 = mapCoords[4];
            String ob3 = mapCoords[5];

        //    System.out.println(mapCoords[0] + "," + mapCoords[1] +  "," + mapCoords[2] + "," + mapCoords[3] + "," + mapCoords[4] + "," + mapCoords[5]);
        //    textView3.setText(mapCoords[0] + "," + mapCoords[1] +  "," + mapCoords[2] + "," + mapCoords[3] + "," + mapCoords[4] + "," + mapCoords[5]);
        }
        */

        DataPoint gasPoint = new DataPoint(nCounter, Integer.parseInt(gasVal));
        DataPoint tempPoint = new DataPoint(nCounter, Integer.parseInt(tempVal));
        DataPoint soundPoint = new DataPoint(nCounter, Integer.parseInt(soundVal));

        DataPoint[] currData = new DataPoint[]{gasPoint,tempPoint,soundPoint};
        return currData;
    }

}