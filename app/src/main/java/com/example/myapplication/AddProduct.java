package com.example.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddProduct extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    EditText nameEditText;
    EditText descriptionEditText;
    int chosen_rating, chosen_category;
    String chosen_city;
    String donor_email;


    ArrayList<String> array_cities;
    Spinner city_spin;
    String[] rating = { "very poor", "poor", "fair", "good", "very good"};
    Spinner rating_spin;
    String[] category = { "appliances", "clothing", "furniture", "plants and animals", "various"};
    String[] new_cities;
    Spinner category_spin;
    private String url="http://10.0.2.2:3000";//****Put your  URL here******

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        nameEditText = (EditText) findViewById(R.id.names);
        descriptionEditText = (EditText) findViewById(R.id.description);

        array_cities = new ArrayList<String>();

        try {
            while(array_cities.isEmpty()) {
                get_all_cities();
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new_cities = array_cities.toArray(new String[0]);
        //setContentView(R.layout.activity_add_product);setContentView(R.layout.activity_add_product);

        city_spin = (Spinner) findViewById(R.id.spinner);
        city_spin.setOnItemSelectedListener(this);
        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter city_adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,new_cities);
        city_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        city_spin.setAdapter(city_adapter);

        rating_spin = (Spinner) findViewById(R.id.spinner2);
        rating_spin.setOnItemSelectedListener(this);
        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter rating_adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,rating);
        city_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        rating_spin.setAdapter(rating_adapter);

        category_spin = (Spinner) findViewById(R.id.category_spinner);
        category_spin.setOnItemSelectedListener(this);
        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter category_adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,category);
        category_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        category_spin.setAdapter(category_adapter);

    }


    private void get_all_cities(){
        String fullURL=url+"/"+"get_all_cities";
        Request request;

        OkHttpClient client = new OkHttpClient();

        request = new Request.Builder()
                .url(fullURL)
                .build();
        /* this is how the callback get handled */
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                List<String> arrayList = new ArrayList<>();
                // Read data on the worker thread
                final String responseData = response.body().string();
                JSONObject Jobject = null;
                try {
                    Jobject = new JSONObject(responseData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    JSONArray arr =(JSONArray) Jobject.get("cities");
                    ArrayList<String> array2 = new ArrayList<String>();
                    for (int i = 0; i < arr.length(); i++) {
                        array_cities.add(arr.get(i).toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    //Performing action onItemSelected and onNothing selected
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        if(arg0.getId() == R.id.spinner2) {
            //Toast.makeText(getApplicationContext(), rating[position], Toast.LENGTH_LONG).show();
            chosen_rating = position + 1;
        }
        else if(arg0.getId() == R.id.category_spinner) {
            //Toast.makeText(getApplicationContext(), category[position], Toast.LENGTH_LONG).show();
            chosen_category = position;
        }
        else{
            //Toast.makeText(getApplicationContext(), new_cities[position], Toast.LENGTH_LONG).show();
            chosen_city = new_cities[position];
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        if(arg0.getId() == R.id.spinner2) {
            //Toast.makeText(getApplicationContext(), rating[position], Toast.LENGTH_LONG).show();
            chosen_rating = 1;
        }
        else if(arg0.getId() == R.id.category_spinner) {
            //Toast.makeText(getApplicationContext(), category[position], Toast.LENGTH_LONG).show();
            chosen_category = 0;
        }
        else{
            //Toast.makeText(getApplicationContext(), new_cities[position], Toast.LENGTH_LONG).show();
            chosen_city = new_cities[0];
        }
    }

    void navigateToAddImgActivity(String user_email, int product_id){
        finish();
        Intent intent = new Intent(AddProduct.this,AddImageActivity.class);
        intent.putExtra("email",user_email);
        intent.putExtra("product_id",product_id);
        startActivity(intent);
    }

    void SendRequest(String url, String json) {
        String new_url = this.url + url;
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(new_url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                //System.out.print(response.body().contentLength());

                final String responseData = response.body().string();

                JSONObject Jobject = null;
                try {
                    Jobject = new JSONObject(responseData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    int product_id = Jobject.getInt("ID");
                    navigateToAddImgActivity(donor_email, product_id);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void submitbuttonHandler(View view) {
        //Decide what happens when the user clicks the submit button
        String name = nameEditText.getText().toString();
        String desc = descriptionEditText.getText().toString();

        Bundle bundle = getIntent().getExtras();
        String donor_phone = bundle.getString("phone");
        donor_email = bundle.getString("email");

        JSONObject obj = new JSONObject();
        try {
            obj.put("name", name);
            obj.put("category", chosen_category);
            obj.put("description", desc);
            obj.put("rating", chosen_rating);
            obj.put("city", chosen_city);
            obj.put("phone", donor_phone);

            String wrap = "/add_product";
            SendRequest(wrap,obj.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}