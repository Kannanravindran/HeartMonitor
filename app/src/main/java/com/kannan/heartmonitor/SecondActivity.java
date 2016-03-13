package com.kannan.heartmonitor;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.kannan.Bean.AccelEntryBean;
import com.kannan.Bean.PatientBean;
import com.kannan.database.sqlite.PatientDBHelper;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
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

    //Database Helper
    PatientDBHelper patientDB;
    public void uploaddb() throws Exception{

        String url = "https://impact.asu.edu/Appenstance/UploadToServerGPS.php";
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/Users/vimalKhanna/Desktop/patient.db");
        try {
            HttpClient httpclient = HttpClientBuilder.create().build();

            HttpPost httppost = new HttpPost(url);

            InputStreamEntity reqEntity = new InputStreamEntity(new FileInputStream(file), -1);
            reqEntity.setContentType("binary/octet-stream");
            reqEntity.setChunked(true); // Send in multiple parts if needed
            httppost.setEntity(reqEntity);
            ResponseHandler<String> responseHandler=new BasicResponseHandler();
            String responseBody = httpclient.execute(httppost, responseHandler);
//            JSONObject response=new JSONObject(responseBody);
//            HttpResponse response = httpclient.execute(httppost);
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context,responseBody, Toast.LENGTH_LONG);
            //Do something with response...

            } catch (Exception e) {
            Log.e("Error caught", null);


            }
//        try {
//            //                URL url = new URL("https://impact.asu.edu/Appenstance/UploadToServerGPS.php");
//            String url = "";
//            HttpGet request = new HttpGet(url);
//            ResponseHandler<String> responseHandler = new BasicResponseHandler();
//            HttpClient httpClient = HttpClientBuilder.create().build();
//            ///Users/kannanravindran/Desktop
//        }
//        catch (Exception e){
//            Log.e("Error caught", null);
//        }
        }
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
        Button uploaddb = (Button) findViewById(R.id.button3);

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

        /* SETUP SENSORS*/
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensorAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);


        /* CREATE BUTTON LISTENERS*/
        //upload
        uploaddb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    uploaddb();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

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
                isReadyForAnotherRead = false;
            }
        });


        /* GET DB INFORMATION FROM PATIENT RECORDS */
        patientDB = new PatientDBHelper(this,
                patientInfo.getName(),
                patientInfo.getId(),
                patientInfo.getAge(),
                patientInfo.getSex());

        List<AccelEntryBean> history = null;
        try {
            history = patientDB.getAllEntries();

        } catch (SQLiteException ex) { // history for patient not available
            ex.printStackTrace();

            // history isn't available, try creating a table?
            try {
                patientDB.createTableForPatient();
            } catch (Exception e) {
                e.printStackTrace();

                // no history, but table is there or can't create table... very bad if we get here
                Context context = getApplicationContext();
                CharSequence text = "There was a problem loading patient records";
                int duration = Toast.LENGTH_SHORT;
            }

        }

        // add all the entries for previous records
        if(history != null) {
            for(AccelEntryBean entry : history) {
                int nY = (int) ((entry.getY() * 10.0) + 50); // scale the y-value so it's more visible on graph
                addEntry(nY);
            }
        }
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
                                if(running) {
                                    isReadyForAnotherRead = true; // flag for the sensorChanged to record new value
                                }
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
        float x = 0;
        float y = 0;
        float z = 0;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            x = sensorEvent.values[0];
            y = sensorEvent.values[1];
            z = sensorEvent.values[2];
        }

        // a second has happsed, so read & record accelerometer data
        if(isReadyForAnotherRead) {
            int nY = (int) ((y * 10.0) + 50); // scale the y-value so it's more visible on graph
            addEntry(nY); // add to graph
            isReadyForAnotherRead = false; // wait for timer to set back to true so that we read in one second
            AccelEntryBean newAccelRead = AccelEntryBean.getNewAccelEntry(x, y, z); // add new reading to DB
            patientDB.addEntry(newAccelRead);
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