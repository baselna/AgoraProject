package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

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

public class HomePage extends AppCompatActivity {
    private String url ="http://10.0.2.2:3000";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private String user_phone;
    private String user_email;
    static public ArrayList<minimal_product> productsList;
    static public ArrayList<minimal_product> final_productList;
    GoogleSignInClient gsc;
    GoogleSignInOptions gso;


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


    void get_all_products_request(){
        String fullURL=url+"/"+"get_all_products";
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
                    JSONArray arr =(JSONArray) Jobject.get("products");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject cur = (JSONObject) arr.get(i);
                        minimal_product tmp = new minimal_product(cur.getString("name"),
                                cur.getString("city"), cur.getInt("rating"),
                                cur.getInt("ID"));
                        productsList.add(tmp);
                    }
                    int y =0;
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    void navigateToProductActivity(int product_id){
        finish();
        Intent intent = new Intent(HomePage.this,Product.class);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this,gso);
        Bundle bundle = getIntent().getExtras();
        productsList= new ArrayList<>();
        String user_email = bundle.getString("email");
        JSONObject obj = new JSONObject();
        this.user_email = user_email;
        try {
            obj.put("email", user_email);
            String wrap = "/get_user_by_email";
            get_user_info_request(wrap,obj.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // creating the listview
        ListView mListView = (ListView) findViewById(R.id.listView);

        try {
            while(productsList.isEmpty()) {
                get_all_products_request();
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



//        minimal_product p1 = new minimal_product("x","haifa",1,1);
//        minimal_product p2 = new minimal_product("y","haifa",1,2);
//        minimal_product p3 = new minimal_product("z","haifa",1,3);
//        productsList.add(p1);
//
//        productsList.add(p2);
//        productsList.add(p3);

        final_productList = new ArrayList<minimal_product>(productsList);
        ProductsListAdapter adapter = new ProductsListAdapter(this, R.layout.adapter_view_layout, final_productList);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                JSONObject obj = new JSONObject();
                int prod_id = adapter.getItem(position).getId();
                try {
                    obj.put("email", user_email );
                    obj.put("product_id", adapter.getItem(position).getId());

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                inc_views_request("/inc_num_of_views", obj.toString());
                navigateToProductActivity(prod_id);

            }
        });

    }

    public void addprudoctbuttonHandler(View view) {
        finish();
        Bundle bundle = getIntent().getExtras();
        String user_email = bundle.getString("email");
        Intent intent = new Intent(HomePage.this,AddProduct.class);
        intent.putExtra("phone", user_phone);
        intent.putExtra("email", user_email);
        startActivity(intent);
    }

    public void mostviewedbuttonHandler(View view) {
        finish();
        Intent intent = new Intent(HomePage.this,MostViewedProducts.class);
        intent.putExtra("email", user_email);
        startActivity(intent);
    }


    public void filterbuttonHandler(View view) {
        finish();
        Intent intent = new Intent(HomePage.this,Filter.class);
        intent.putExtra("email", user_email);
        startActivity(intent);
    }

    public void handleLogout(View view) {
        gsc.signOut().addOnCompleteListener(this, task -> {
            finish();
            startActivity(new Intent(HomePage.this, MainActivity.class));
        });
    }

}