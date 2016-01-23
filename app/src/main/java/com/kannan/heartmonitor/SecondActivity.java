package com.kannan.heartmonitor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Random;

public class SecondActivity extends AppCompatActivity {

    private static final Random RANDOM = new Random();
    private LineGraphSeries<DataPoint> series;
    private int lastX = 0;
    public boolean running=true;
    public int y=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        // we get graph view instance
        GraphView graph = (GraphView) findViewById(R.id.graph);
        // data
        series = new LineGraphSeries<DataPoint>();
        graph.addSeries(series);
        // customize a little bit viewport
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(100);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setMaxX(30);
        viewport.setScrollable(true);
        Button run = (Button) findViewById(R.id.button);
        Button stop = (Button) findViewById(R.id.button2);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.patientid, R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //run
        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                running=true;
            }
        });

        //stop
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                running=false;
            }
        });
    }

        @Override
        public void onResume () {

            super.onResume();
           new Thread(new Runnable() { //thread for live graph

                @Override
                public void run() {
                    // thread loop
                    for (int i = 0; ; i++) {
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    int a[]={5,5,5,4,7,5,6,4,3,5,10,2,5,5,6,4,5,5,5,5};
                                    addEntry(a[y]*10);//adding datapoints
                                    y++;
                                    if(y==19){
                                        y=0;
                                    }


                                }
                            });

                        // sleep to slow down the add of entries
                        try {
                            Thread.sleep(400);
                        } catch (InterruptedException e) {
                            // manage error ...
                        }

                    }
                }
            }).start();
        }

    // add random data to graph
    private void addEntry(int y) {
        // here, we choose to display max 10 points on the viewport and we scroll to end

        if (running==true) {

                //series.appendData(new DataPoint(lastX++, RANDOM.nextDouble() * 10d), true, 10);
                //series.appendData(new DataPoint(lastX++, RANDOM.nextDouble() * 100d), true, 30);
                series.appendData(new DataPoint(lastX++,y ), true,30);

        }


    }

}