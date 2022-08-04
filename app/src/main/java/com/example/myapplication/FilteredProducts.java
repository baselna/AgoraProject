package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

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

public class FilteredProducts extends AppCompatActivity {
    private String url="http://10.0.2.2:3000";//****Put your  URL here******
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    ArrayList<minimal_product> productsList;
    String user_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtered_products);
        Bundle bundle = getIntent().getExtras();
        user_email = bundle.getString("email");
        JSONObject products = null;
        try {
            products = new JSONObject(getIntent().getStringExtra("products"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        productsList= new ArrayList<>();
        JSONArray arr = null;
        try {
            arr = (JSONArray) products.get("products");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < arr.length(); i++) {
            minimal_product tmp = null;
            try {
                JSONObject cur = (JSONObject) arr.get(i);
                tmp = new minimal_product(cur.getString("name"),
                        cur.getString("city"), cur.getInt("rating"),
                        cur.getInt("ID"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            productsList.add(tmp);
        }
        ListView mListView = (ListView) findViewById(R.id.listView);
        ProductsListAdapter adapter = new ProductsListAdapter(this, R.layout.adapter_view_layout, productsList);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
                JSONObject obj = new JSONObject();
                try {
                    obj.put("email", user_email );
                    obj.put("product_id", adapter.getItem(position).getId());

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                inc_views_request("/inc_num_of_views", obj.toString());
                navigateToProductActivity(adapter.getItem(position).getId());

            }
        });

    }

    public void returnToHomepagebuttonHandler(View view) {
        finish();
        Intent intent = new Intent(FilteredProducts.this,HomePage.class);
        intent.putExtra("email", user_email);
        startActivity(intent);
    }

    void navigateToProductActivity(int product_id){
        finish();
        Intent intent = new Intent(FilteredProducts.this,Product.class);
        intent.putExtra("email", user_email);
        intent.putExtra("product_id", product_id);
        startActivity(intent);
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
}