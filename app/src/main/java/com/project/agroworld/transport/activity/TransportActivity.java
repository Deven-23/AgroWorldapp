package com.project.agroworld.transport.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.agroworld.R;
import com.project.agroworld.databinding.ActivityTransportBinding;
import com.project.agroworld.transport.model.VehicleModel;
import com.project.agroworld.utils.Constants;
import com.project.agroworld.utils.CustomMultiColorProgressBar;
import com.project.agroworld.utils.Permissions;
import com.project.agroworld.viewmodel.AgroViewModel;

public class TransportActivity extends AppCompatActivity {

    private ActivityTransportBinding binding;
    private final int REQUEST_CODE = 99;
    private Uri imageUri;
    private DatabaseReference firebaseStorage;
    private StorageReference storage;
    private CustomMultiColorProgressBar progressBar;
    private AgroViewModel agroViewModel;
    private boolean isImageSelected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_transport);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Transport Panel");
        actionBar.setDisplayHomeAsUpEnabled(true);
        progressBar = new CustomMultiColorProgressBar(this, getString(R.string.loader_message));
        agroViewModel = ViewModelProviders.of(this).get(AgroViewModel.class);
        agroViewModel.init();
        binding.crdUploadImageVehicle.setOnClickListener(v -> {
            isImageSelected = true;
            selectImage();
        });

        binding.btnUpdateDataVehicle.setOnClickListener(v -> {
            String model = binding.etVehicleModel.getText().toString();
            String rate = binding.etVehicleRate.getText().toString();
            String address = binding.etVehicleAddress.getText().toString();
            String contact = binding.etVehicleContact.getText().toString();

            if (Permissions.checkConnection(this)
                    && !model.isEmpty()
                    && !rate.isEmpty()
                    && !address.isEmpty()
                    && Constants.contactValidation(contact)
                    && isImageSelected) {
                uploadImageToFirebase(model, rate, address, contact);
            } else {
                Constants.showToast(this, getString(R.string.requiredDataChecks));
            }
        });
    }

    private void uploadImageToFirebase(String model, String rates, String address, String contact) {
        progressBar.showProgressBar();
        storage = FirebaseStorage.getInstance().getReference("vehicle");
        storage.child(model).putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            Constants.showToast(TransportActivity.this, getString(R.string.image_uploaded));
            taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(task -> {
                String imageUrl = task.getResult().toString();
                Log.d("fileLink", imageUrl);
                uploadDataToFirebase(model, rates, address, contact, imageUrl);
            }).addOnFailureListener(e -> Constants.showToast(TransportActivity.this, getString(R.string.failed_to_generate_url)));

        }).addOnFailureListener(e -> {
            progressBar.hideProgressBar();
            Log.d("onFailureImageUpload", e.getLocalizedMessage());
            Constants.showToast(TransportActivity.this, "Failed to upload image");
        });
    }

    private void uploadDataToFirebase(String model, String rates, String address, String contact, String imageUrl) {
        firebaseStorage = FirebaseDatabase.getInstance().getReference("vehicle");
        VehicleModel vehicleModel = new VehicleModel(model, address, rates, contact, imageUrl);
        firebaseStorage.child(model).setValue(vehicleModel).addOnSuccessListener(unused -> {

            binding.ivVehicleSelected.setImageResource(R.color.colorPrimary);
            binding.ivVehicleUploadIcon.setVisibility(View.VISIBLE);
            binding.etVehicleModel.setText(null);
            binding.etVehicleAddress.setText(null);
            binding.etVehicleRate.setText(null);
            binding.etVehicleContact.setText(null);
            progressBar.hideProgressBar();
            Constants.showToast(TransportActivity.this, getString(R.string.vehicle_updated));
            startActivity(new Intent(TransportActivity.this, TransportDataActivity.class));

        }).addOnFailureListener(e -> {
            progressBar.hideProgressBar();
            Constants.showToast(TransportActivity.this, getString(R.string.failed_to_update_vehicle));
        });
    }


    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.transport_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.transport_list:
                moveToTransportDataActivity();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void moveToTransportDataActivity() {
        Intent intent = new Intent(TransportActivity.this, TransportDataActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && data != null && data.getData() != null) {
            imageUri = data.getData();
            Log.d("imageURi", imageUri.toString());
            binding.ivVehicleUploadIcon.setVisibility(View.GONE);
            binding.ivVehicleSelected.setImageURI(imageUri);
        }
    }
}
