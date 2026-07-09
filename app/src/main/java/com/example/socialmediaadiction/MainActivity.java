//Login activity
package com.example.socialmediaadiction;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialmediaadiction.Api.ApiService;
import com.example.socialmediaadiction.Api.RetrofitClient;
import com.example.socialmediaadiction.Fragment.Constants;
import com.example.socialmediaadiction.Fragment.SessionManager;
import com.example.socialmediaadiction.Model.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private TextView email, pass, idregister;
    private Button btnlogin;
    SessionManager sessionManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // ✅ CHECK SESSION FIRST (AUTO LOGIN)
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        }

        setContentView(R.layout.activity_main);

        email = findViewById(R.id.email);
        pass = findViewById(R.id.pass);
        idregister = findViewById(R.id.idregister);
        btnlogin = findViewById(R.id.btnlogin);

        idregister.setOnClickListener(view -> {
            Intent i = new Intent(MainActivity.this, MainActivity2.class);
            startActivity(i);
        });

        btnlogin.setOnClickListener(v -> signIn());
    }

    private void signIn() {

        String email1 = email.getText().toString().trim();
        String password = pass.getText().toString().trim();

        if (TextUtils.isEmpty(email1)) {
            email.setError("Email is Required");
            email.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            pass.setError("Password is Required");
            pass.requestFocus();
            return;
        }

        login(email1, password);
    }

    private void login(String email1, String password) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RetrofitClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService api = retrofit.create(ApiService.class);
        Call<LoginResponse> call = api.userlogin(email1, password);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse resp = response.body();

                    if ("success".equals(resp.getStatus()) && resp.getUser() != null) {

                        // ✅ SAVE USER DATA
                        sessionManager.putStringData(Constants.KEY_ID, resp.getUser().getId());
                        sessionManager.putStringData(Constants.NAME, resp.getUser().getName());
                        sessionManager.putStringData(Constants.EMAIL, resp.getUser().getEmail());

                        // ✅ VERY IMPORTANT (LOGIN STATE)
                        sessionManager.setLoggedIn(true);

                        Log.d("SESSION", "Login Success → Saved");

                        goToHome(resp.getUser().getId());

                    } else {
                        Toast.makeText(MainActivity.this, "Invalid Details", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(MainActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Failed: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToHome(String id) {
        Intent i = new Intent(MainActivity.this, HomeActivity.class);
        i.putExtra("contact_no", id);
        startActivity(i);
        finish();
    }
}

////LoginActivity
//package com.example.socialmediaadiction;
//
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.annotation.SuppressLint;
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.bumptech.glide.Glide;
//import com.example.socialmediaadiction.Api.ApiService;
//import com.example.socialmediaadiction.Api.RetrofitClient;
//import com.example.socialmediaadiction.Fragment.Constants;
//import com.example.socialmediaadiction.Fragment.SessionManager;
//import com.example.socialmediaadiction.Model.LoginResponse;
//import com.google.android.gms.auth.api.signin.*;
//import com.google.android.gms.common.api.ApiException;
//import com.google.android.gms.tasks.Task;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;
//
//public class MainActivity extends AppCompatActivity {
//
//    private static final int RC_SIGN_IN = 100;
//    private GoogleSignInClient mGoogleSignInClient;
//    private TextView email,pass,idregister;
//
//    SessionManager sessionManager;
//    private Button btnlogin;
//
//    @SuppressLint("MissingInflatedId")
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        email = findViewById(R.id.email);
//        pass = findViewById(R.id.pass);
//        idregister = findViewById(R.id.idregister);
//        btnlogin = findViewById(R.id.btnlogin);
//
//        sessionManager = new SessionManager(this);
//        idregister.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent(MainActivity.this,MainActivity2.class);
//                startActivity(i);
//
//            }
//        });
//        findViewById(R.id.btnlogin).setOnClickListener(v -> signIn());
//        //signOutBtn.setOnClickListener(v -> signOut());
//    }
//
//    private void signIn() {
//
//        String email1= email.getText().toString();
//        String password = pass.getText().toString();
//
//        if(TextUtils.isEmpty(email1)){
//            email.setError("Email is Required");
//            email.requestFocus();
//            return;
//        }
//        else if(TextUtils.isEmpty(password)){
//            pass.setError("Password is Required");
//            pass.requestFocus();
//            return;
//        }
//        else {
//            login(email1,password);
//
//        }
//
//
//
//    }
//
//    private void login(String email1, String password) {
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(RetrofitClient.BASE_URL)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//        ApiService api = retrofit.create(ApiService.class);
//        Call<LoginResponse> call = api.userlogin(email1, password);
//
//        call.enqueue(new Callback<LoginResponse>() {
//            @Override
//            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    LoginResponse resp = response.body();
//
//                    if ("success".equals(resp.getStatus()) && resp.getUser() != null) {
//                        // Save session
//                        sessionManager.putStringData(Constants.KEY_ID, resp.getUser().getId());
//                        sessionManager.putStringData(Constants.NAME, resp.getUser().getName());
//                        sessionManager.putStringData(Constants.EMAIL, resp.getUser().getEmail());
//
//                        // Log
//                        Log.d("LoginInfo", "User ID: " + resp.getUser().getId() +
//                                ", Name: " + resp.getUser().getName());
//
//                        goToHome(resp.getUser().getId());
//                    } else {
//                        Toast.makeText(MainActivity.this, "Invalid Details", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Toast.makeText(MainActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<LoginResponse> call, Throwable t) {
//                Toast.makeText(MainActivity.this, "Failed: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void goToHome(String no) {
//        Intent i = new Intent(MainActivity.this, HomeActivity.class);
//        i.putExtra("contact_no", no);
//        startActivity(i);
//        finish();
//    }
//}
