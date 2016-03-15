package com.kannan.heartmonitor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.kannan.Bean.AccelEntryBean;
import com.kannan.Bean.PatientBean;
import com.kannan.database.sqlite.PatientDBHelper;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SecondActivity extends AppCompatActivity implements SensorEventListener{

    private final String PATIENT_BUNDLE_INFO = "PATIENT_INFO";

    private static final Random RANDOM = new Random();
    private LineGraphSeries<DataPoint> seriesX;
    private LineGraphSeries<DataPoint> seriesY;
    private LineGraphSeries<DataPoint> seriesZ;
    private int lastX = 0;
    public boolean running=true;

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
        seriesX = new LineGraphSeries<DataPoint>();
        seriesY = new LineGraphSeries<DataPoint>();
        seriesZ = new LineGraphSeries<DataPoint>();

        seriesX.setColor(Color.RED);
        seriesY.setColor(Color.GREEN);
        seriesZ.setColor(Color.BLUE);

        graph.addSeries(seriesX);
        graph.addSeries(seriesY);
        graph.addSeries(seriesZ);

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
        Button uploadButton = (Button) findViewById(R.id.button3);

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
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    UploadtoServer up=new UploadtoServer();
                    File database = getApplicationContext().getDatabasePath("patient.db");
                    up.execute(database);

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
                int nX = (int) ((entry.getX() * 10.0) + 50); // scale the x-value so it's more visible on graph
                int nY = (int) ((entry.getY() * 10.0) + 50); // scale the y-value so it's more visible on graph
                int nZ = (int) ((entry.getZ() * 10.0) - 50); // scale the z-value so it's more visible on graph
                addEntry(nX, nY, nZ);
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
    private void addEntry(int x, int y, int z) {
        // here, we choose to display max 10 points on the viewport and we scroll to end

        if (running==true) {

                //series.appendData(new DataPoint(lastX++, RANDOM.nextDouble() * 10d), true, 10);
                //series.appendData(new DataPoint(lastX++, RANDOM.nextDouble() * 100d), true, 30);
                seriesX.appendData(new DataPoint(lastX++,x ), true,30);
                seriesY.appendData(new DataPoint(lastX++,y ), true,30);
                seriesZ.appendData(new DataPoint(lastX++,z ), true,30);
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
            int nX = (int) ((x * 10.0) + 50); // scale the y-value so it's more visible on graph
            int nY = (int) ((y * 10.0) + 50); // scale the y-value so it's more visible on graph
            int nZ = (int) ((z * 10.0) - 50); // scale the y-value so it's more visible on graph
            addEntry(nX, nY, nZ); // add to graph
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

class UploadtoServer extends AsyncTask<File,Void,Void>
{
        TextView messageText;
        Button uploadButton;
        int serverResponseCode = 0;
        ProgressDialog dialog = null;
        String upLoadServerUri = null;

        /**********  File Path *************/

        public int uploadFile(File sourceFileUri)
        {
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    // Not implemented
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    // Not implemented
                }
            } };

            try {
                SSLContext sc = SSLContext.getInstance("TLS");

                sc.init(null, trustAllCerts, new java.security.SecureRandom());

                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            upLoadServerUri = "https://impact.asu.edu/Appenstance/UploadToServerGPS.php";
            File fileName = sourceFileUri;
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File sourceFile = new File(String.valueOf(sourceFileUri));
            if (!sourceFile.isFile())
            {
                dialog.dismiss();
                Log.e("uploadFile", "Source File not exist :");
//                runOnUiThread(new Runnable()
//                {
//                    public void run()
//                    {
//                        messageText.setText("Source File not exist :");
//                    }
//                });
//            return 0;
            }
            else
            {
            try
            {
                // open a URL connection to the Servlet

                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);
                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
               // conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                //conn.setRequestProperty("uploaded_file", "group22.db");


                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\"; filename=\"group22.db\"" + lineEnd);
                dos.writeBytes(lineEnd);
                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

//                if(serverResponseCode == 200){
//
//                            String msg = "File Upload Completed.\n";
//
//                            messageText.setText(msg);
//                            Toast.makeText(UploadtoServer.this, msg, Toast.LENGTH_SHORT).show();
//                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex)
            {

               // dialog.dismiss();
                ex.printStackTrace();

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                //dialog.dismiss();
                e.printStackTrace();
                Log.e("Upload file to server Exception", "Exception : " + e.getMessage(), e);
            }
           // dialog.dismiss();


        } // End else block
        return serverResponseCode;
    }

    @Override
    protected Void doInBackground(File... DB)
    {
        uploadFile(DB[0]);
        return null;
    }
}