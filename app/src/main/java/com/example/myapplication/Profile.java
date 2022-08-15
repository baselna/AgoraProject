package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Profile extends AppCompatActivity {

    private String user_email;
    static public ArrayList<minimal_product> productsList;
    static public ArrayList<minimal_product> final_productList;
    int empty_list = -1;
    private String url ="http://10.0.2.2:3000";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Bundle bundle = getIntent().getExtras();
        user_email = bundle.getString("email");
        productsList= new ArrayList<>();
        ListView mListView = (ListView) findViewById(R.id.listView);

        JSONObject obj = new JSONObject();
        try {
            obj.put("email", user_email);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            while(productsList.isEmpty() && empty_list == -1) {
                get_my_products_request(obj.toString());
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(empty_list == 1) {
            final_productList = new ArrayList<minimal_product>(productsList);
            ProductsListAdapter adapter = new ProductsListAdapter(this, R.layout.adapter_view_layout, final_productList);
            mListView.setAdapter(adapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    JSONObject obj = new JSONObject();
                    int prod_id = adapter.getItem(position).getId();
                    try {
                        obj.put("email", user_email);
                        obj.put("product_id", adapter.getItem(position).getId());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    inc_views_request("/inc_num_of_views", obj.toString());
                    navigateToProductActivity(prod_id);

                }
            });
        }
        else{
            TextView msg = findViewById(R.id.empty_list);
            msg.setVisibility(View.VISIBLE);
        }

    }

    void get_my_products_request(String json) {
        String new_url = this.url + "/get_my_products";
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
                    JSONArray arr =(JSONArray) Jobject.get("products");
                    empty_list = Jobject.getInt("flag");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject cur = (JSONObject) arr.get(i);
                        minimal_product tmp = new minimal_product(cur.getString("name"),
                                cur.getString("city"), cur.getInt("rating"),
                                cur.getInt("ID"));
                        productsList.add(tmp);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    void inc_views_request(String url, String json) {
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
            }
        });
    }

    void navigateToProductActivity(int product_id){
        finish();
        Intent intent = new Intent(Profile.this,Product.class);
        intent.putExtra("email", user_email);
        intent.putExtra("product_id", product_id);
        startActivity(intent);
    }

    public void returnToHomepagebuttonHandler(View view) {
        finish();
        Intent intent = new Intent(Profile.this,HomePage.class);
        intent.putExtra("email", user_email);
        startActivity(intent);
    }
}