package com.example.athidya.arduinodata;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.androidplot.util.PixelUtils;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.*;
import com.jjoe64.graphview.series.DataPoint;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.athidya.arduinodata.R.id.plot;

/**
 * Created by User on 2/28/2017.
 */

public class MapFragment extends Fragment {
    private static final String TAG = "Tab2Fragment";
    TextView textView0;
    private XYPlot map;
    TimerTask mTimerTask;
    final Handler handler = new Handler();
    Timer t = new Timer();
    MainTabs main;
    String[] coordsArr;
    Button btnStart;

/* mapping information given in coordinate (x,y)
*  orientation float
*  distance sensors left, middle, right
*  accessed by case m*/

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map,container,false);
        super.onCreate(savedInstanceState);

        btnStart = (Button) view.findViewById(R.id.button1);
        btnStart.setOnClickListener(mButtonStartListener);

        Integer[] xcoords = new Integer[1000];
        Integer[] ycoords = new Integer[1000];


        // initialize our XYPlot reference:
        map = (XYPlot) view.findViewById(R.id.plot);
        map.setDomainBoundaries(0, 10, BoundaryMode.FIXED);
        map.setRangeBoundaries(0, 10, BoundaryMode.FIXED);

        // create a couple arrays of y-values to plot:
        final Number[] domainLabels = {1, 2, 3, 6, 7, 8, 9, 10, 13, 14, 1, 2, 3, 6, 7, 8, 9, 10, 13, 14};
        Number[] series1Numbers = {1, 4, 2, 8, 4, 16, 8, 32, 16, 64, 1, 2, 3, 6, 7, 8, 9, 10, 13, 14};

        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        XYSeries series1 = new SimpleXYSeries(
                Arrays.asList(series1Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");

        LineAndPointFormatter series1Format = new LineAndPointFormatter(Color.RED, Color.GREEN, null, null);

        //smoothing if necessary
        /*series1Format.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));*/

        // add a new series' to the xyplot:
        map.addSeries(series1, series1Format);

        map.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                int i = Math.round(((Number) obj).floatValue());
                return toAppendTo.append(domainLabels[i]);
            }
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        });

      //  readData();
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
                        readData();
                        //plot coordsArr[0] for x and coordsArr[1] for y
                    }
                });
            }};

        // public void schedule (TimerTask task, long delay, long period)
        t.schedule(mTimerTask, 1000, 3000);  //

    }

    private String[] readData() {
        main = (MainTabs) getActivity();
        coordsArr = main.sendCoords();
        String x = coordsArr[0];
        String y = coordsArr[1];
        String orient = coordsArr[2];
        String ob1 = coordsArr[3];
        String ob2 = coordsArr[4];
        String ob3 = coordsArr[5];
        System.out.println("Coords: " + x + ',' + y + "," + orient + "," + ob1 + ',' + ob2 + ',' + ob3 );
        //textView0.setText("Coords: " + x + ',' + y + "," + orient + "," + ob1 + ',' + ob2 + ',' + ob3 );
        return coordsArr;
    }

}