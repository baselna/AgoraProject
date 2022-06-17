package com.example.myapplication;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;

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


public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private EditText textField_message;
    private Button button_send_post;
    private Button button_send_get;
    private TextView textView_response;
    private String url="http://10.100.102.195:3000";//****Put your  URL here******
    private String POST="POST";
    private String GET="GET";
    SignInButton signInButton;
    private GoogleApiClient googleApiClient;
    TextView textView;
    private static final int RC_SIGN_IN = 1;
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static MainActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance= this;
        setContentView(R.layout.activity_main);

        textField_message=findViewById(R.id.txtField_message);
        button_send_post=findViewById(R.id.button_send_post);
        button_send_get=findViewById(R.id.button_send_get);
        textView_response=findViewById(R.id.textView_response);

        /*making a post request.*/
        button_send_post.setOnClickListener(view -> {

            //get the test in the text field.In this example you should type your name here
            String text=textField_message.getText().toString();
            if(text.isEmpty()){
                textField_message.setError("This cannot be empty for post request");
            }else {
                /*if name text is not empty,then call the function to make the post request*/
                sendRequest(POST, "getname", "name", text);
            }
            //super.onCreate(savedInstanceState);
           // setContentView(R.layout.activity_main);
        });

        /*making the get request*/
        button_send_get.setOnClickListener(view -> {
            /*in ourr server.py file we implemented a get method  named "get_fact()".
            We specified its URL invocation as '/getfact' there.
            Here we pass it to the sendRequest() function*/
            sendRequest(GET,"",null,null);
        });

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
    private void gotoProfile(){
        Intent intent=new Intent(MainActivity.this,ProfileActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    void sendRequest(String type,String method,String paramname,String param){

        /* if url is of our get request, it should not have parameters according to our implementation.
         * But our post request should have 'name' parameter. */
        String fullURL=url+"/"+method+(param==null?"":"/"+param);
        Request request;

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS).build();

        /* If it is a post request, then we have to pass the parameters inside the request body*/
        if(type.equals(POST)){
            RequestBody formBody = new FormBody.Builder()
                    .add(paramname, param)
                    .build();

            request=new Request.Builder()
                    .url(fullURL)
                    .post(formBody)
                    .build();
        }else{
            /*If it's our get request, it doen't require parameters, hence just sending with the url*/
            request = new Request.Builder()
                    .url(fullURL)
                    .build();
        }
        /* this is how the callback get handled */
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                // Read data on the worker thread
                final String responseData = response.body().string();

                // Run view-related code back on the main thread.
                // Here we display the response message in our text view
                MainActivity.this.runOnUiThread(() -> textView_response.setText(responseData));
            }
        });
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
                //System.out.print(response.headers());
                // Run view-related code back on the main thread.
                // Here we display the response message in our text view
                MainActivity.this.runOnUiThread(() -> textView_response.setText(responseData));
            }
        });
    }

    public static MainActivity getInstance(){
        return instance;
    }
}