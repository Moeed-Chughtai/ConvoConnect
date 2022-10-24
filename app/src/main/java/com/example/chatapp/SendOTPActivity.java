package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class SendOTPActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_otp);

        final EditText phoneNumberInput = findViewById(R.id.numberInput);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        Button buttonGetOTP = findViewById(R.id.getOtpButton);

        // TODO: Add more country codes to drop-down menu
        // Creating drop-down menu
        Spinner country_codes = findViewById(R.id.country_codes);
        // Renders each item from array onto the screen
        // CreateFromResource(within which activity, name of stringArray, layout type)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.country_codes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        country_codes.setAdapter(adapter);

        // View is a rectangular box that responds to user inputs, eg. button, editText
        buttonGetOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Display message if field is empty
                if(phoneNumberInput.getText().toString().trim().isEmpty()) {
                    Toast.makeText(SendOTPActivity.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                buttonGetOTP.setVisibility(View.INVISIBLE);

                // Authenticate phone number through Firebase
                PhoneAuthOptions options =
                        PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                                // TODO: Repurpose for different country codes
                                .setPhoneNumber("+44" + phoneNumberInput.getText().toString())
                                .setTimeout(60L, TimeUnit.SECONDS)
                                .setActivity(SendOTPActivity.this)
                                .setCallbacks(
                                        new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                            @Override
                                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                                progressBar.setVisibility(View.GONE);
                                                buttonGetOTP.setVisibility(View.VISIBLE);
                                            }

                                            // Displays error message if verification fails
                                            @Override
                                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                                progressBar.setVisibility(View.GONE);
                                                buttonGetOTP.setVisibility(View.VISIBLE);
                                                Toast.makeText(SendOTPActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }

                                            // Intent is an object used to request action from another activity
                                            @Override
                                            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                                progressBar.setVisibility(View.GONE);
                                                buttonGetOTP.setVisibility(View.VISIBLE);
                                                // Intent.putExtra sends information to the verifyOTPActivity
                                                Intent intent = new Intent(getApplicationContext(), VerifyOTPActivity.class);
                                                intent.putExtra("phoneNumber", phoneNumberInput.getText().toString());
                                                intent.putExtra("verificationId", verificationId);
                                                startActivity(intent);
                                            }
                                        }
                                )
                                .build();
                PhoneAuthProvider.verifyPhoneNumber(options);
            }
        });
    }
}
