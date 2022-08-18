package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class Registration extends AppCompatActivity {

    EditText nameEditText;
    EditText phoneEditText;
    CheckBox conditionsCheckBox;
    //check current state of the check box

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        nameEditText = (EditText) findViewById(R.id.names);
        phoneEditText = (EditText) findViewById(R.id.Phone);
        conditionsCheckBox = (CheckBox) findViewById(R.id.conditions);
    }
    public void submitbuttonHandler(View view) {
        //Decide what happens when the user clicks the submit button
        String fullName = nameEditText.getText().toString();
        String phoneNum = phoneEditText.getText().toString();
        Boolean checkBoxState = conditionsCheckBox.isChecked();

        Bundle bundle = getIntent().getExtras();
        String email = bundle.getString("email");

        if(fullName.equals("") || phoneNum.equals("")){
            Toast.makeText(this, "Please fill all the details!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!checkBoxState){
            Toast.makeText(getApplicationContext(),"terms not approved",Toast.LENGTH_LONG).show();
        }
        else{
            JSONObject obj = new JSONObject();
            try {
                obj.put("email", email);
                obj.put("phone", phoneNum);
                obj.put("name", fullName);
                String wrap = "/signup";
                MainActivity.getInstance().SendRequest2(wrap,obj.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }

}