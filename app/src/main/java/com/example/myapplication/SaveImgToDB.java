package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class SaveImgToDB extends AppCompatActivity {

    private String url ="http://132.69.208.167:3000";
    static String email;
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public void navigateToHomepage(){
        finish();
        Intent intent = new Intent(SaveImgToDB.this,HomePage.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }

    void save_img_to_db_request(String url, String json) {
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
                    if(Jobject.length() == 0){
//                        Toast.makeText(getApplicationContext(), "Failed to upload photo, please try again later.", Toast.LENGTH_LONG).show();
                    }
                    else{
//                        Toast.makeText(getApplicationContext(), "Photo uploaded successfully", Toast.LENGTH_LONG).show();

                    }
                    navigateToHomepage();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_img_to_db);
        Bundle bundle = getIntent().getExtras();
        String img_url = bundle.getString("img_url");
        int product_id = bundle.getInt("product_id");
        email = bundle.getString("email");
        JSONObject obj = new JSONObject();
        try {
            obj.put("img_url", img_url);
            obj.put("product_id", product_id);
            String wrap = "/add_photo";
            save_img_to_db_request(wrap,obj.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}