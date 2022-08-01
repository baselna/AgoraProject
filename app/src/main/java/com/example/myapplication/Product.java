package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Product extends AppCompatActivity {
    private String user_email;
    private int product_id;
    private String user_phone;
//    product details:
    private String name;
    private int category;
    private int rating;
    private String city;
    private String region;
    private String phone_num;
    private String description;
    // TODO: maybe need to change this to bytearray
    private String image;

    private String url ="http://192.168.1.172:3000";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");


    void get_product_request(String json) {
        String new_url = this.url + "/get_product_by_id";
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
                try {
                    Jobject = new JSONObject(responseData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    name = Jobject.getString("name");
                    category = Jobject.getInt("category");
                    rating = Jobject.getInt("rating");
                    city = Jobject.getString("city");
                    region = Jobject.getString("region");
                    phone_num = Jobject.getString("phone");
                    description = Jobject.getString("description");
                    image = Jobject.getString("image_url");


                    if(image.equals("null")){
                        ImageButton image_btn = (ImageButton) findViewById(R.id.imageButton);
                        image_btn.setVisibility(View.INVISIBLE);
                    }
//                    else{
//                        // handle image
//                    }
                    String str_category = "", str_rating = "";
                    switch(category){
                        case 0:
                            str_category = "appliances";
                            break;
                        case 1:
                            str_category = "clothing";
                            break;
                        case 2:
                            str_category = "furniture";
                            break;
                        case 3:
                            str_category = "plants and animals";
                            break;
                        case 4:
                            str_category = "various";
                            break;
                    }

                    switch (rating){
                        case 1:
                            str_rating = "very poor";
                            break;
                        case 2:
                            str_rating = "poor";
                            break;
                        case 3:
                            str_rating = "fair";
                            break;
                        case 4:
                            str_rating = "good";
                            break;
                        case 5:
                            str_rating = "very good";
                            break;
                    }


                    TextView name_tv = (TextView) findViewById(R.id.name);
                    name_tv.setText(name);
                    TextView category_tv = (TextView) findViewById(R.id.category);
                    category_tv.setText(str_category);
                    TextView rating_tv = (TextView) findViewById(R.id.rating);
                    rating_tv.setText(str_rating);
                    TextView city_tv = (TextView) findViewById(R.id.city);
                    city_tv.setText(city);
                    TextView region_tv = (TextView) findViewById(R.id.region);
                    region_tv.setText(region);
                    TextView phone_tv = (TextView) findViewById(R.id.Phone_num);
                    phone_tv.setText(phone_num);
                    TextView desc_tv = (TextView) findViewById(R.id.description);
                    desc_tv.setText(description);



                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    void get_user_info_request(String json) {
        String new_url = this.url + "/get_user_by_email";
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
                try {
                    Jobject = new JSONObject(responseData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    user_phone = Jobject.getString("phone");
                    Button delete_btn = (Button) findViewById(R.id.delete_button);
                    if(user_phone.equals(phone_num)){
                        delete_btn.setVisibility(View.INVISIBLE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    void delete_product_request(String json) {
        String new_url = this.url + "/delete_product";
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
                try {
                    Jobject = new JSONObject(responseData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    String error = Jobject.getString("error_message");
                    if(error.length() > 0){
                        Toast.makeText(getApplicationContext(), error,Toast.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void deleteProductButtonHandler(View view) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("product_id", product_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        delete_product_request(obj.toString());
        returnToHomepagebuttonHandler(view);
    }

    public void returnToHomepagebuttonHandler(View view) {
        finish();
        Intent intent = new Intent(Product.this,HomePage.class);
        intent.putExtra("email", user_email);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        Bundle bundle = getIntent().getExtras();
        user_email = bundle.getString("email");
        product_id = bundle.getInt("product_id");

        JSONObject obj = new JSONObject();
        try {
            obj.put("product_id", product_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        get_product_request(obj.toString());


        JSONObject obj2 = new JSONObject();
        try {
            obj2.put("email", user_email);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        get_user_info_request(obj2.toString());
    }
}