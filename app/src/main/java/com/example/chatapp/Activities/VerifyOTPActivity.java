package com.example.chatapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.databinding.ActivityVerifyOtpBinding;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerifyOTPActivity extends AppCompatActivity {

    ActivityVerifyOtpBinding binding;

    // Sensitive data so variables are private
    private EditText inputCode1, inputCode2, inputCode3, inputCode4, inputCode5, inputCode6;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        verificationId = getIntent().getStringExtra("verificationId");
        inputCode1 = binding.inputCode1;
        inputCode2 = binding.inputCode2;
        inputCode3 = binding.inputCode3;
        inputCode4 = binding.inputCode4;
        inputCode5 = binding.inputCode5;
        inputCode6 = binding.inputCode6;

        binding.phoneText.setText(String.format(getIntent().getStringExtra("selectedCode")
                + "-%s", getIntent().getStringExtra("phoneNumber")));

        setupOTPInputs();

        binding.verifyButton.setOnClickListener(v -> {
            // If any text fields are not entered correctly, a message is displayed
            if (inputCode1.getText().toString().trim().isEmpty()
                    || inputCode2.getText().toString().trim().isEmpty()
                    || inputCode3.getText().toString().trim().isEmpty()
                    || inputCode4.getText().toString().trim().isEmpty()
                    || inputCode5.getText().toString().trim().isEmpty()
                    || inputCode6.getText().toString().trim().isEmpty()) {
                Toast.makeText(VerifyOTPActivity.this, "Please enter valid code", Toast.LENGTH_SHORT).show();
                return;
            }

            // String containing 6 digit code
            String code =
                    inputCode1.getText().toString() +
                            inputCode2.getText().toString() +
                            inputCode3.getText().toString() +
                            inputCode4.getText().toString() +
                            inputCode5.getText().toString() +
                            inputCode6.getText().toString();

            if (verificationId != null) {
                // Replace button with progress bar
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.verifyButton.setVisibility(View.INVISIBLE);
                // Firebase verifies code entered by user
                PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(verificationId, code);
                // Once verification is complete
                FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
                        .addOnCompleteListener(task -> {
                            // If code is correct and authentication is successful, it redirects to MainActivity
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(getApplicationContext(), SetupProfileActivity.class);
                                // Clears any existing tasks so the user cannot go back to this activity
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            // If code is incorrect, displays error message
                            } else {
                                Toast.makeText(VerifyOTPActivity.this, "Incorrect Verification Code", Toast.LENGTH_SHORT).show();
                                binding.progressBar.setVisibility(View.INVISIBLE);
                                binding.verifyButton.setVisibility(View.VISIBLE);
                            }
                        });
            }
        });

        binding.resendOTP.setOnClickListener(v -> {
            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                            .setPhoneNumber(getIntent().getStringExtra("selectedCode") + getIntent().getStringExtra("phoneNumber"))
                            .setTimeout(30L, TimeUnit.SECONDS)
                            .setActivity(VerifyOTPActivity.this)
                            .setCallbacks(
                                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                        @Override
                                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                        }

                                        @Override
                                        public void onVerificationFailed(@NonNull FirebaseException e) {
                                            // Firebase error message displayed as popup
                                            Toast.makeText(VerifyOTPActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onCodeSent(@NonNull String newVerificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                            // The new code is the newVerificationId
                                            verificationId = newVerificationId;
                                            // Confirmation popup message displayed
                                            Toast.makeText(VerifyOTPActivity.this, "OTP Sent", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            )
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        });
    }

    private void setupOTPInputs() {
        // This code will repeat 6 times for every input box
        inputCode1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            // Only onTextChanged is required in this TextWatcher to observe the changes
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Automatically lead to next input box after a number is entered into current box
                if (!s.toString().trim().isEmpty()) {
                    inputCode2.requestFocus();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        inputCode2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputCode3.requestFocus();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        inputCode3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputCode4.requestFocus();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }

        });
        inputCode4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputCode5.requestFocus();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }

        });
        inputCode5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputCode6.requestFocus();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}
