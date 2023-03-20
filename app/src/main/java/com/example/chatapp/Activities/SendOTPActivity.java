package com.example.chatapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.R;
import com.example.chatapp.databinding.ActivitySendOtpBinding;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class SendOTPActivity extends AppCompatActivity {

    FirebaseAuth auth;
    ActivitySendOtpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySendOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // If user has already logged in, skip verification
        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null) {
            Intent intent = new Intent(SendOTPActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Creating drop-down menu
        Spinner countryCode = binding.countryCode;
        // Returns a view for each object in the array
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.country_codes, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        countryCode.setAdapter(adapter);

        binding.getOtpButton.setOnClickListener(v -> {
            if(binding.numberInput.getText().toString().trim().isEmpty()) {
                Toast.makeText(SendOTPActivity.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
                return;
            }
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.getOtpButton.setVisibility(View.INVISIBLE);

            String selectedCode = countryCode.getSelectedItem().toString();
            // Authenticate phone number through Firebase
            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                            .setPhoneNumber(selectedCode + binding.numberInput.getText().toString())
                            .setTimeout(30L, TimeUnit.SECONDS)
                            .setActivity(SendOTPActivity.this)
                            .setCallbacks(
                                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                        @Override
                                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                            binding.progressBar.setVisibility(View.GONE);
                                            binding.getOtpButton.setVisibility(View.VISIBLE);
                                        }

                                        // Displays error message if verification fails
                                        @Override
                                        public void onVerificationFailed(@NonNull FirebaseException e) {
                                            binding.progressBar.setVisibility(View.GONE);
                                            binding.getOtpButton.setVisibility(View.VISIBLE);
                                            Toast.makeText(SendOTPActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }

                                        // Code sent to user successfully
                                        @Override
                                        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                            binding.progressBar.setVisibility(View.GONE);
                                            binding.getOtpButton.setVisibility(View.VISIBLE);
                                            // Intent object used to request to launch next activity
                                            Intent intent = new Intent(getApplicationContext(), VerifyOTPActivity.class);
                                            // Sends information to next activity
                                            intent.putExtra("phoneNumber", binding.numberInput.getText().toString());
                                            intent.putExtra("verificationId", verificationId);
                                            intent.putExtra("selectedCode", selectedCode);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                            )
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        });
    }
}
