package com.kannan.heartmonitor;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.kannan.Bean.PatientBean;

import java.util.Random;

public class SecondActivity extends AppCompatActivity implements SensorEventListener{

    private final String PATIENT_BUNDLE_INFO = "PATIENT_INFO";

    private static final Random RANDOM = new Random();
    private LineGraphSeries<DataPoint> series;
    private int lastX = 0;
    public boolean running=true;
    public int y=0;

    private PatientBean patientInfo;

    // UI elements
    private TextView txtVw_Name;
    private TextView txtVw_Id;
    private TextView txtVw_Age;
    private TextView txtVw_Sex;

    // Accelerometer members
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private boolean isReadyForAnotherRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        // we get graph view instance
        GraphView graph = (GraphView) findViewById(R.id.graph);
        isReadyForAnotherRead = false;

        // get reference to UI elements
        txtVw_Name = (TextView) findViewById(R.id.txtVw_PatientName);
        txtVw_Id = (TextView) findViewById(R.id.txtVw_PatientId);
        txtVw_Age = (TextView) findViewById(R.id.txtVw_PatientAge);
        txtVw_Sex = (TextView) findViewById(R.id.txtVw_PatientSex);

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

        //Get patient information from previous activity
        try {
            String patientName = "";
            Intent intent = getIntent();//.getExtras();
            if (intent != null) {
                patientName = intent.getStringExtra("PATIENT_INFO_NAME");
                int patientId = intent.getIntExtra("PATIENT_INFO_ID", 0);
                int patientAge = intent.getIntExtra("PATIENT_INFO_AGE", 0);
                String patientSex = intent.getStringExtra("PATIENT_INFO_SEX");

                patientInfo = new PatientBean(patientId, patientName, patientAge, patientSex);
            }
            // display patient information
            if (patientInfo != null) {
                txtVw_Name.setText(patientInfo.getName());
                txtVw_Id.setText(String.valueOf(patientInfo.getId()));
                txtVw_Age.setText(String.valueOf(patientInfo.getAge()));
                txtVw_Sex.setText(patientInfo.getSex());
            } else {
                txtVw_Name.setText("Patient not found");
            }
        } catch(Exception ex) {
            txtVw_Name.setText("Patient not found");
            ex.printStackTrace();
        }


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensorAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);


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
            sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

            new Thread(new Runnable() { //thread for live graph

                @Override
                public void run() {
                    while(true) {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                isReadyForAnotherRead = true;
                            }
                        });

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
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

    /* IMPLEMENT SENSOR EVENT LISTENER  */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // if sensor is the accelerometer:
        Sensor mySensor = sensorEvent.sensor;
        float y = 0;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
        }
        if(isReadyForAnotherRead) {
            int nY = (int) ((y * 10.0) + 50);
            addEntry(nY);
            isReadyForAnotherRead = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}