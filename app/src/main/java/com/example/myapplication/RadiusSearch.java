package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.util.Log;


public class RadiusSearch extends Activity implements LocationListener {

    protected LocationManager locationManager;
    protected Context context;


    private String url ="http://10.0.2.2:3000";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    String donor_email;
    int radius;
    static public ArrayList<minimal_product> productsList;
    static public ArrayList<minimal_product> final_productList;
    Location loc;
    int empty_list = -1;


    void get_products_request(String json) {
        String new_url = this.url + "/get_products_in_search_radius";
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
                    empty_list = Jobject.getInt("empty");

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radius_search);
        Bundle bundle = getIntent().getExtras();
        radius = bundle.getInt("radius");
        donor_email = bundle.getString("email");
        productsList= new ArrayList<>();



        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            final int INITIAL_REQUEST=1337;
            final String[] INITIAL_PERMS={
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_CONTACTS
            };
            ActivityCompat.requestPermissions(this,INITIAL_PERMS, INITIAL_REQUEST);

        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


        ListView mListView = (ListView) findViewById(R.id.listView);

        JSONObject obj = new JSONObject();
        try {
            obj.put("radius", radius );
            obj.put("lat", loc.getLatitude());
            obj.put("lng", loc.getLongitude());

        } catch (JSONException e) {
            e.printStackTrace();
        }


//        get_products_request(obj.toString());
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        try {
            while(productsList.isEmpty() && empty_list == -1) {
                get_products_request(obj.toString());
                Thread.sleep(4500);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(empty_list == 0) {
            final_productList = new ArrayList<minimal_product>(productsList);
            ProductsListAdapter adapter = new ProductsListAdapter(this, R.layout.adapter_view_layout, final_productList);
            mListView.setAdapter(adapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    JSONObject obj = new JSONObject();
                    int prod_id = adapter.getItem(position).getId();
                    try {
                        obj.put("email", donor_email);
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

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
    }


    void navigateToProductActivity(int product_id){
        finish();
        Intent intent = new Intent(RadiusSearch.this,Product.class);
        intent.putExtra("email", donor_email);
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

    public void returnToHomepagebuttonHandler(View view) {
        finish();
        Intent intent = new Intent(RadiusSearch.this,HomePage.class);
        intent.putExtra("email", donor_email);
        startActivity(intent);
    }
}