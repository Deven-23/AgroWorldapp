package com.project.agroworld.transport.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DatabaseReference;
import com.project.agroworld.R;
import com.project.agroworld.databinding.ActivityTransportDataBinding;
import com.project.agroworld.transport.adapter.VehicleAdapter;
import com.project.agroworld.transport.listener.TransportAdminListener;
import com.project.agroworld.transport.model.VehicleModel;
import com.project.agroworld.utils.Constants;
import com.project.agroworld.utils.Permissions;
import com.project.agroworld.viewmodel.AgroViewModel;

import java.util.ArrayList;
import java.util.List;

public class TransportDataActivity extends AppCompatActivity implements TransportAdminListener {
    private final ArrayList<VehicleModel> vehicleItemList = new ArrayList<>();
    private ActivityTransportDataBinding binding;
    private DatabaseReference databaseReference;
    private VehicleAdapter vehicleAdapter;
    private AgroViewModel agroViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_transport_data);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        agroViewModel = ViewModelProviders.of(this).get(AgroViewModel.class);
        agroViewModel.init(this);
        if (Permissions.checkConnection(this)) {
            getVehicleListFromFirebase();
        }
        binding.ivSearch.setOnClickListener(v -> {
            binding.tvUsername.setVisibility(View.GONE);
            binding.ivSearch.setVisibility(View.GONE);
            binding.searchBar.setVisibility(View.VISIBLE);
        });

        binding.searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                binding.searchBar.setVisibility(View.GONE);
                binding.tvUsername.setVisibility(View.VISIBLE);
                binding.ivSearch.setVisibility(View.VISIBLE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchProduct(newText);

                return false;
            }
        });
    }

    private void getVehicleListFromFirebase() {
        binding.shimmer.startShimmer();
        agroViewModel.getVehicleModelLivedata().observe(this, vehicleModelResource -> {
            switch (vehicleModelResource.status) {
                case ERROR:
                    binding.shimmer.stopShimmer();
                    binding.shimmer.setVisibility(View.GONE);
                    binding.recyclerViewVehicle.setVisibility(View.GONE);
                    binding.tvNoDataFoundErr.setVisibility(View.VISIBLE);
                    binding.tvNoDataFoundErr.setText(vehicleModelResource.message);
                    break;
                case LOADING:
                    binding.shimmer.startShimmer();
                    break;
                case SUCCESS:
                    if (vehicleModelResource.data != null) {
                        updateUI(vehicleModelResource.data);
                    } else {
                        binding.shimmer.stopShimmer();
                        binding.shimmer.setVisibility(View.GONE);
                        binding.tvNoDataFoundErr.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        });
    }

    private void updateUI(List<VehicleModel> vehicleModelList) {
        if (vehicleModelList.isEmpty()) {
            binding.shimmer.stopShimmer();
            binding.shimmer.setVisibility(View.GONE);
            binding.tvNoDataFoundErr.setVisibility(View.VISIBLE);
            binding.tvNoDataFoundErr.setText(getText(R.string.no_data_found));
            binding.recyclerViewVehicle.setVisibility(View.GONE);
        } else {
            vehicleItemList.clear();
            vehicleItemList.addAll(vehicleModelList);
            binding.shimmer.stopShimmer();
            binding.shimmer.setVisibility(View.GONE);
            binding.tvNoDataFoundErr.setVisibility(View.GONE);
            binding.recyclerViewVehicle.setVisibility(View.VISIBLE);
            setRecyclerView();
        }
    }

    private void setRecyclerView() {
        vehicleAdapter = new VehicleAdapter(this, vehicleItemList, this, 0);
        binding.recyclerViewVehicle.setAdapter(vehicleAdapter);
        binding.recyclerViewVehicle.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewVehicle.setHasFixedSize(true);
    }


    private void searchProduct(String query) {
        ArrayList<VehicleModel> searchProductList = new ArrayList<VehicleModel>();
        for (int i = 0; i < vehicleItemList.size(); i++) {
            if (vehicleItemList.get(i).getModel().toLowerCase().contains(query.toLowerCase())) {
                searchProductList.add(vehicleItemList.get(i));
            }
        }
        if (vehicleItemList.isEmpty()) {
            Constants.showToast(this, "No product found");
        } else {
            vehicleAdapter.searchInVehicleList(searchProductList);
        }
    }

    @Override
    public void performCallAction(VehicleModel vehicleModel) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + vehicleModel.getContact()));
        startActivity(intent);
    }

    @Override
    public void performEditAction(VehicleModel vehicleModel) {
        Intent intent = new Intent(this, TransportActivity.class);
        intent.putExtra("vehicleModel", vehicleModel);
        intent.putExtra("isActionWithData", true);
        startActivity(intent);
    }

    @Override
    public void performDeleteAction(VehicleModel vehicleModel) {
        agroViewModel.performVehicleRemovalAction(vehicleModel.getModel()).observe(this, stringResource -> {
            switch (stringResource.status) {
                case ERROR:
                    Constants.showToast(this, stringResource.data);
                    break;
                case LOADING:
                    break;
                case SUCCESS:
                    Constants.showToast(this, stringResource.data);
                    if (vehicleItemList.isEmpty()) {
                        binding.recyclerViewVehicle.setVisibility(View.GONE);
                        binding.tvNoDataFoundErr.setVisibility(View.VISIBLE);
                    }
                    vehicleAdapter.notifyDataSetChanged();
                    break;
            }
        });
    }
}