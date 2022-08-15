package com.example.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity<Public> extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private String url="http://10.0.2.2:3000";//****Put your  URL here******
    private String POST="POST";
    private String GET="GET";
    SignInButton signInButton;
    private GoogleApiClient googleApiClient;

    private static final int RC_SIGN_IN = 1;
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static MainActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance= this;
        setContentView(R.layout.activity_main);


        GoogleSignInOptions gso =  new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleApiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if(acct!=null){
            navigateToSecondActivity();
            //navigateToHomeActivity();
        }
        ActivityResultLauncher<Intent> activityResultLaunch = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        try {
                            Intent data1 = result.getData();
                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                            task.getResult(ApiException.class);
                            GoogleSignInResult result2 = Auth.GoogleSignInApi.getSignInResultFromIntent(data1);
                            handleSignInResult(result2);
                        }
                        catch (ApiException e){
                            Toast.makeText(getApplicationContext(),"something wrong",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                activityResultLaunch.launch(intent);
            }
        });
    }

    void navigateToSecondActivity(){
        finish();
        Intent intent = new Intent(MainActivity.this,SecondActivity.class);
        startActivity(intent);
    }

    void navigateToHomeActivity(String email){
        finish();
        Intent intent = new Intent(MainActivity.this,HomePage.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }

    void navigateToRegistrationActivity(String email){
        finish();
        Intent intent = new Intent(MainActivity.this,Registration.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }
    private void handleSignInResult(GoogleSignInResult result){
        if(result.isSuccess()){
            JSONObject obj = new JSONObject();
            //Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result);
            try {
                obj.put("email", result.getSignInAccount().getEmail());
                String wrap = "/login";
                SendRequest2(wrap,obj.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(getApplicationContext(),"Sign in cancel",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    void SendRequest2(String url, String json) {
        url = this.url + url;
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

//        try (Response response = client.newCall(request).execute()) {
//            return response.body().string();
//        }
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                // Read data on the worker thread
                final String responseData = response.body().string();
                JSONObject Jobject = null;
                try {
                    Jobject = new JSONObject(responseData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(Jobject.has("phone")){
                    try {
                        navigateToHomeActivity(Jobject.get("email").toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //navigateToSecondActivity();
                }
                else{
                    try {
                        navigateToRegistrationActivity(Jobject.get("email").toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    public static MainActivity getInstance(){
        return instance;
    }
}