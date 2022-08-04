package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Filter extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener{

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    String donor_email;
    int chosen_rating, chosen_category;
    String chosen_city;
    String chosen_region;

    ArrayList<String> array_cities;
    Spinner city_spin;
    ArrayList<String> array_regions;
    Spinner region_spin;
    String[] rating = { "very poor", "poor", "fair", "good", "very good", "all conditions"};
    Spinner rating_spin;
    String[] category = { "appliances", "clothing", "furniture", "plants and animals", "various", "all categories"};
    String[] new_cities;
    String[] new_regions;
    Spinner category_spin;
    private String url="http://10.0.2.2:3000";//****Put your  URL here******


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
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
        city_spin.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) this);
        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter city_adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,new_cities);
        city_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        city_spin.setAdapter(city_adapter);

        array_regions = new ArrayList<String>();
        try {
            while(array_regions.isEmpty()) {
                get_all_regions();
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new_regions = array_regions.toArray(new String[0]);
        //setContentView(R.layout.activity_add_product);setContentView(R.layout.activity_add_product);

        region_spin = (Spinner) findViewById(R.id.spinner4);
        region_spin.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) this);
        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter region_adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,new_regions);
        region_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        region_spin.setAdapter(region_adapter);

        rating_spin = (Spinner) findViewById(R.id.spinner2);
        rating_spin.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) this);
        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter rating_adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,rating);
        city_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        rating_spin.setAdapter(rating_adapter);

        category_spin = (Spinner) findViewById(R.id.category_spinner);
        category_spin.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) this);
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
                    array_cities.add("all cities");
                    for (int i = 0; i < arr.length(); i++) {
                        array_cities.add(arr.get(i).toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void get_all_regions(){
        String fullURL=url+"/"+"get_all_regions";
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
                    JSONArray arr =(JSONArray) Jobject.get("regions");
                    ArrayList<String> array2 = new ArrayList<String>();
                    array_regions.add("all regions");
                    for (int i = 0; i < arr.length(); i++) {
                        array_regions.add(arr.get(i).toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

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
        else if(arg0.getId() == R.id.spinner4){
            chosen_region = new_regions[position];
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
            chosen_rating = 6;
        }
        else if(arg0.getId() == R.id.category_spinner) {
            //Toast.makeText(getApplicationContext(), category[position], Toast.LENGTH_LONG).show();
            chosen_category = 5;
        }
        else if (arg0.getId() == R.id.spinner4){
            chosen_region = "all regions";
        }
        else{
            //Toast.makeText(getApplicationContext(), new_cities[position], Toast.LENGTH_LONG).show();
            chosen_city = "all cities";
        }
    }

    void navigateToFilteredProductsActivity(JSONObject obj){
        finish();
        Intent intent = new Intent(Filter.this,FilteredProducts.class);
        intent.putExtra("email",donor_email);
        intent.putExtra("products",  obj.toString());
        startActivity(intent);
    }

    void SendFilterRequest(String json) {
        String new_url = this.url + "/filter_products";
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

                final String responseData = response.body().string();

                JSONObject Jobject = null;
                JSONArray arr = null;
                try {
                    Jobject = new JSONObject(responseData);
                   // arr =(JSONArray) Jobject.get("products");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                navigateToFilteredProductsActivity(Jobject);

            }
        });
    }


    public void submitbuttonHandler(View view) {

        Bundle bundle = getIntent().getExtras();
        donor_email = bundle.getString("email");

        Switch img_switch = findViewById(R.id.switch1);

        JSONObject obj = new JSONObject();
        try {
            if (chosen_category != 5){
                obj.put("categories", chosen_category);
            }
            if (chosen_rating != 6) {
                obj.put("rating", chosen_rating);
            }
            if(!chosen_city.equals("all cities")) {
                obj.put("cities", chosen_city);
            }
            if(!chosen_region.equals("all regions")) {
                obj.put("regions", chosen_region);
            }
            if (img_switch.isChecked()){
                obj.put("photo","yes");
            }
            SendFilterRequest(obj.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}