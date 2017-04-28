package com.example.athidya.arduinodata;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.INotificationSideChannel;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    Button btnStart;

    SampleDynamicSeries series;
    SampleDynamicSeries seriesob;

/* mapping information given in coordinate (x,y)
*  orientation float
*  distance sensors left, middle, right
*  accessed by case m*/

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map,container,false);
        super.onCreate(savedInstanceState);

        List<Integer> xVals = ((MainTabs) getActivity()).getxVals();
        List<Integer> yVals = ((MainTabs) getActivity()).getyVals();
        List<Integer> xObVals = ((MainTabs) getActivity()).getxobVals();
        List<Integer> yObVals = ((MainTabs) getActivity()).getyobVals();

        xVals.add(0);
        yVals.add(0);

        btnStart = (Button) view.findViewById(R.id.button1);
        btnStart.setOnClickListener(mButtonStartListener);

        // initialize our XYPlot reference:
        map = (XYPlot) view.findViewById(R.id.plot);
        map.setDomainBoundaries(-200, 200, BoundaryMode.FIXED);
        map.setRangeBoundaries(-200, 200, BoundaryMode.FIXED);

        // create a couple arrays of y-values to plot:

        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        series = new SampleDynamicSeries(xVals, yVals, "Series1");
        seriesob = new SampleDynamicSeries(xVals, yVals, "Series Object");

        LineAndPointFormatter series1Format = new LineAndPointFormatter(Color.RED, Color.GREEN, null, null);
        LineAndPointFormatter seriesObjFormat = new LineAndPointFormatter(null, Color.BLUE, null, null);
        //smoothing if necessary
        /*series1Format.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));*/

        // add a new series' to the xyplot:
        map.addSeries(series, series1Format);
        map.addSeries(seriesob, seriesObjFormat);

        map.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                int i = Math.round(((Number) obj).floatValue());
                return toAppendTo.append(((MainTabs) getActivity()).getxVals().get(i));
                //toAppendTo.append(((MainTabs) getActivity()).getxobVals().get(i));
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

            series.setVals(((MainTabs) getActivity()).getxVals(),
                            ((MainTabs) getActivity()).getyVals());
            seriesob.setVals(((MainTabs) getActivity()).getxobVals(), ((MainTabs) getActivity()).getyobVals());
            map.redraw();
            //doTimerTask();
        }
    };


    public void doTimerTask(){


        mTimerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        map.redraw();
                        Log.d("Map", "Redrew map");
                        //readData();
                        //plot coordsArr[0] for x and coordsArr[1] for y
                    }
                });
            }};

        // public void schedule (TimerTask task, long delay, long period)
        t.schedule(mTimerTask, 3000, 1000);  //

    }

//    private String[] readData() {
//        main = (MainTabs) getActivity();
//        coordsArr = main.sendCoords();
//        String x = coordsArr[0];
//        String y = coordsArr[1];
//        String orient = coordsArr[2];
//        String ob1 = coordsArr[3];
//        String ob2 = coordsArr[4];
//        String ob3 = coordsArr[5];
//        //System.out.println("Coords: " + x + ',' + y + "," + orient + "," + ob1 + ',' + ob2 + ',' + ob3 );
//        //textView0.setText("Coords: " + x + ',' + y + "," + orient + "," + ob1 + ',' + ob2 + ',' + ob3 );
//
//        xVals.add(Integer.parseInt(coordsArr[0]));
//        yVals.add(Integer.parseInt(coordsArr[1]));
//
//
//        /*
//        if (counter > 5) {
//            map.redraw();
//            counter = 0;
//        }
//        counter++;
//
//
//        // Log.d("Map", "x:" + x + " y:" + y);
//        return coordsArr;
//    }

    class SampleDynamicSeries implements XYSeries {
        private String title;

        private List<Integer> xVals;
        private List<Integer> yVals;

        public SampleDynamicSeries(List<Integer> xVals, List<Integer> yVals, String title) {
            this.xVals = xVals;
            this.yVals = yVals;
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public int size() {
            return xVals.size();
        }

        @Override
        public Number getX(int index) {
            return xVals.get(index);
        }

        @Override
        public Number getY(int index) {
            return yVals.get(index);
        }

        public void addPoint(int x, int y) {
            xVals.add(x);
            yVals.add(y);
        }

        public void setVals(List<Integer> xVals, List<Integer> yVals) {
            this.xVals = xVals;
            this.yVals = yVals;
        }
    }

}