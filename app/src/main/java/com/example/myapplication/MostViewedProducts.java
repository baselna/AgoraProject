package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

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

public class MostViewedProducts extends AppCompatActivity {

    public static ArrayList<minimal_product> topProducts;
    public static ArrayList<minimal_product> final_topProducts;
    private String user_email;
    private String url ="http://10.0.2.2:3000";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");



    void get_top_products_request(){
        String fullURL=url+"/most_viewed_products";
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
                    JSONArray arr =(JSONArray) Jobject.get("most_viewed");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject cur = (JSONObject) arr.get(i);
                        minimal_product tmp = new minimal_product(cur.getString("name"),
                                cur.getString("city"), cur.getInt("rating"),
                                cur.getInt("ID"));
                        topProducts.add(tmp);
                    }
                    int y =0;
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
        Intent intent = new Intent(MostViewedProducts.this, Product.class);
        intent.putExtra("email", user_email);
        intent.putExtra("product_id", product_id);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_most_viewed_products);

        topProducts= new ArrayList<>();
        Bundle bundle = getIntent().getExtras();
        user_email = bundle.getString("email");

        ListView mListView = (ListView) findViewById(R.id.listView);

        try {
            while (topProducts.size() == 0) {
                get_top_products_request();
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final_topProducts = new ArrayList<minimal_product>(topProducts);
        ProductsListAdapter adapter = new ProductsListAdapter(this, R.layout.adapter_view_layout, final_topProducts);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(getApplicationContext(),"I will buy tesla "+ adapter.getItem(position).getId()
//                        ,Toast.LENGTH_LONG).show();
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
        Intent intent = new Intent(MostViewedProducts.this,HomePage.class);
        intent.putExtra("email", user_email);
        startActivity(intent);
    }


}