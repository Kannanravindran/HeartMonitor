package com.kannan.heartmonitor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.kannan.Bean.PatientBean;

/**
 * Created by mldof on 3/4/2016.
 */
public class PatientInfoActivity extends AppCompatActivity  {

    private final String PATIENT_BUNDLE_INFO = "PATIENT_INFO";

    private EditText edtTxtName;
    private EditText edtTxtId;
    private EditText edtTxtAge;
    private Button btnSubmit;

    private String radioGroupSex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info);
        Button btnSubmit = (Button) findViewById(R.id.btn_submit);
        radioGroupSex = "M";

        edtTxtName = (EditText) findViewById(R.id.edtTxt_Name);
        edtTxtId = (EditText) findViewById(R.id.edtTxtId);
        edtTxtAge = (EditText) findViewById(R.id.edtTxtAge);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* VALIDATE USER INPUT BEFORE GOING TO NEXT ACTIVITY */
                // validate name
                if(edtTxtName.getText().length() < 1) {
                    Context context = getApplicationContext();
                    CharSequence text = "Must insert a name";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    return;
                }

                // validate id
                if(edtTxtId.getText().length() < 1) {
                    Context context = getApplicationContext();
                    CharSequence text = "Must insert a numeric Id";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    return;
                }

                // validate age
                if(edtTxtAge.getText().length() < 1) {
                    Context context = getApplicationContext();
                    CharSequence text = "Must enter an age";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    return;
                }

                int age = 0;
                int id = 0;

                try {
                    age = Integer.parseInt(String.valueOf(edtTxtAge.getText()));
                    id = Integer.parseInt(String.valueOf(edtTxtId.getText()));
                } catch (Exception ex) {
                    Context context = getApplicationContext();
                    CharSequence text = "Failed to parse age or id (must be numbers)";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    return;
                }

                // package patient info and pass to next activity
                String patientName = String.valueOf(edtTxtName.getText());
                Intent i = new Intent(PatientInfoActivity.this, SecondActivity.class);
                i.putExtra("PATIENT_INFO_NAME", patientName);
                i.putExtra("PATIENT_INFO_ID", id);
                i.putExtra("PATIENT_INFO_AGE", age);
                i.putExtra("PATIENT_INFO_SEX", radioGroupSex);
                startActivity(i);
            }
        });
    }

    public void onSexRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radioBtn_Male:
                if (checked)
                    radioGroupSex = "M";
                    break;
            case R.id.radioBtn_Female:
                if (checked)
                    radioGroupSex = "F";
                    break;
        }
    }
}
