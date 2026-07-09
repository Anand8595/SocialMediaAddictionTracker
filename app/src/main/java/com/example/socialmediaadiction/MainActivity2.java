//RegisterActivity
package com.example.socialmediaadiction;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.socialmediaadiction.Api.ApiService;
import com.example.socialmediaadiction.Api.RetrofitClient;
import com.example.socialmediaadiction.Model.RegisterResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity2 extends AppCompatActivity {

    TextInputEditText name, email, phone, address, city, dob, password;
    AutoCompleteTextView gender;
    MaterialButton registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);
        address = findViewById(R.id.address);
        city = findViewById(R.id.city);
        dob = findViewById(R.id.dob);
        password = findViewById(R.id.password);
        gender = findViewById(R.id.gender);
        registerBtn = findViewById(R.id.register_btn);

        // Gender dropdown
        String[] items = {"Male", "Female", "Other"};
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        gender.setAdapter(adapter);

        registerBtn.setOnClickListener(v -> registerUser());
        dob.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {

                        String date = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                        dob.setText(date);

                    }, year, month, day);

            datePicker.show();
        });
    }

    private void registerUser() {

        String sName = name.getText().toString().trim();
        String sEmail = email.getText().toString().trim();
        String sPhone = phone.getText().toString().trim();
        String sAddress = address.getText().toString().trim();
        String sCity = city.getText().toString().trim();
        String sDob = dob.getText().toString().trim();
        String sGender = gender.getText().toString().trim();
        String sPassword = password.getText().toString().trim();

        if (TextUtils.isEmpty(sName) ||
                TextUtils.isEmpty(sEmail) ||
                TextUtils.isEmpty(sPhone) ||
                TextUtils.isEmpty(sPassword)) {

            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        sendDataToApi(sName, sEmail, sPhone, sAddress, sCity, sDob, sGender, sPassword);
    }

    private void sendDataToApi(String sName, String sEmail, String sPhone,
                               String sAddress, String sCity,
                               String sDob, String sGender, String sPassword) {

        ApiService api = RetrofitClient.getInstance().getApi();

        Call<RegisterResponse> call = api.useradd(
                sName,
                sEmail,
                sPassword,
                sPhone,
                sAddress,
                sCity,
                sDob,
                sGender
        );

        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    RegisterResponse res = response.body();

                    Toast.makeText(MainActivity2.this,
                            res.getMessage(),
                            Toast.LENGTH_LONG).show();

                    if ("success".equalsIgnoreCase(res.getStatus())) {

                        Toast.makeText(MainActivity2.this,
                                "Registration Successful",
                                Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(MainActivity2.this, MainActivity.class));
                        finish();
                    }

                } else {
                    Toast.makeText(MainActivity2.this,
                            "Server Error: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Toast.makeText(MainActivity2.this,
                        "Network Error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                Log.e("API_ERROR", t.getMessage());
            }
        });
    }
}
