package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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

public class HomePage extends AppCompatActivity {
    private String url ="http://192.168.14.22:3000";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private String user_phone;
    private Button add_product_button;

    void get_user_info_request(String url, String json) {
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
                final String responseData = response.body().string();
                JSONObject Jobject = null;
                try {
                    Jobject = new JSONObject(responseData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    user_phone = Jobject.get("phone").toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        Bundle bundle = getIntent().getExtras();
        String user_email = bundle.getString("email");
        JSONObject obj = new JSONObject();
        try {
            obj.put("email", user_email);
            String wrap = "/get_user_by_email";
            get_user_info_request(wrap,obj.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        add_product_button = (Button) findViewById(R.id.add_product);

    }

    public void addprudoctbuttonHandler(View view) {
        finish();
        Bundle bundle = getIntent().getExtras();
        String user_email = bundle.getString("email");
        Intent intent = new Intent(HomePage.this,AddImageActivity.class);
        intent.putExtra("phone", user_phone);
        intent.putExtra("email", user_email);
        startActivity(intent);
    }

//    public void mostviewedbuttonHandler(View view) {
//        //Decide what happens when the user clicks the submit button
//        JSONObject obj = new JSONObject();
//        try {
//            obj.put("email", email);
//            obj.put("phone", phoneNum);
//            obj.put("name", fullName);
//            String wrap = "/signup";
//            MainActivity.getInstance().SendRequest2(wrap, obj.toString());
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

}