package com.project.agroworld.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.project.agroworld.R;
import com.project.agroworld.databinding.ActivityManufactureBinding;
import com.project.agroworld.manufacture.activity.ManufactureActivity;
import com.project.agroworld.manufacture.activity.ManufactureDataActivity;
import com.project.agroworld.transport.activity.TransportActivity;
import com.project.agroworld.transport.activity.TransportDataActivity;
import com.project.agroworld.utils.Constants;

import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {

    private ActivityManufactureBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String userType;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manufacture);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        Intent intent = getIntent();

        if (intent != null) {
            userType = intent.getStringExtra("manufacturerUser");
            if (Objects.equals(userType, "manufacturer")) {
                binding.tvUserType.setText(getString(R.string.show_product));
                binding.tvUserType.setText(getString(R.string.manufacture_panel));
            } else {
                binding.tvUserType.setText(getString(R.string.show_vehicles));
                binding.tvUserType.setText(getString(R.string.transport_panel));
            }
        }

        if (user != null) {
            Constants.bindImage(binding.ivMfrProfile, String.valueOf(user.getPhotoUrl()), binding.ivMfrProfile);
            binding.tvMfrName.setText(user.getDisplayName());
            binding.tvMfrEmail.setText(user.getEmail());
            binding.tvMfrWelcomeMsg.setText("Welcome '" + user.getDisplayName() + "' to the AgroWorld");
        }

        binding.btnMfrProceed.setOnClickListener(v -> {
            Intent intent1;
            if (Objects.equals(userType, "manufacturer")) {
                intent1 = new Intent(UserProfileActivity.this, ManufactureActivity.class);
            } else {
                intent1 = new Intent(UserProfileActivity.this, TransportActivity.class);
            }
            intent1.putExtra("isActionWithData", false);
            startActivity(intent1);
        });

        binding.btnShowHistory.setOnClickListener(v -> {
            Intent intent1;
            if (Objects.equals(userType, "manufacturer")) {
                intent1 = new Intent(UserProfileActivity.this, ManufactureDataActivity.class);
            } else {
                intent1 = new Intent(UserProfileActivity.this, TransportDataActivity.class);
            }
            startActivity(intent1);
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_logout:
                logoutUser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logoutUser() {
        Constants.logoutAlertMessage(UserProfileActivity.this, auth);
    }
}