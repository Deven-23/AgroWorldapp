package com.project.agroworld.fragments;

import static com.project.agroworld.utils.Constants.setAppLocale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.project.agroworld.BuildConfig;
import com.project.agroworld.R;
import com.project.agroworld.databinding.FragmentProfileBinding;
import com.project.agroworld.db.FarmerModel;
import com.project.agroworld.db.PreferenceHelper;
import com.project.agroworld.taskmanager.activity.AddTaskActivity;
import com.project.agroworld.taskmanager.adapter.FarmerAdapter;
import com.project.agroworld.taskmanager.listener.OnItemClickListener;
import com.project.agroworld.taskmanager.receiver.EventReceiver;
import com.project.agroworld.taskmanager.viewmodel.FarmerViewModel;
import com.project.agroworld.utils.Constants;

import java.util.List;


public class ProfileFragment extends Fragment implements OnItemClickListener {
    private final static int REQUEST_CODE = 6124;
    PreferenceHelper preferenceHelper;
    FarmerViewModel viewModel;
    FarmerAdapter farmerAdapter;
    FirebaseAuth auth;
    FirebaseUser user;
    private FragmentProfileBinding dataBinding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false);
        return dataBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        preferenceHelper = PreferenceHelper.getInstance(getContext());
        viewModel = ViewModelProviders.of(requireActivity()).get(FarmerViewModel.class);
        updateUI(user);
        setUpRecyclerView();
        viewModel.getRoutineList().observe(getViewLifecycleOwner(), farmerModels -> updateTaskUI(farmerModels));

        dataBinding.ivLanguage.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), dataBinding.ivMenuOption);
            popupMenu.getMenuInflater().inflate(R.menu.home_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                // Toast message on menu item clicked
                switch (menuItem.getItemId()) {
                    case R.id.menu_english_lng:
                        setAppLocale(getContext(), "en");
                        preferenceHelper.saveData(Constants.ENGLISH_KEY, true);
                        preferenceHelper.saveData(Constants.HINDI_KEY, false);
                        Constants.showToast(getContext(), "Language updated to english");
                        return true;
                    case R.id.menu_hindi_lng:
                        setAppLocale(getContext(), "hi");
                        preferenceHelper.saveData(Constants.ENGLISH_KEY, false);
                        preferenceHelper.saveData(Constants.HINDI_KEY, true);
                        Constants.showToast(getContext(), "Language updated to hindi");
                        return true;
                }
                return true;
            });
            // Showing the popup menu
            popupMenu.show();
        });

        dataBinding.ivMenuOption.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), dataBinding.ivMenuOption);
            popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                // Toast message on menu item clicked
                switch (menuItem.getItemId()) {
                    case R.id.mnAboutUs:
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.ABOUT_US_PAGE));
                        startActivity(intent);
                        return true;
                    case R.id.mnContactUs:
                        Intent intent1 = new Intent(Intent.ACTION_DIAL);
                        intent1.setData(Uri.parse("tel:" + "+918591347448"));
                        startActivity(intent1);
                        return true;
                    case R.id.mnAppVersion:
                        Constants.showToast(getContext(), BuildConfig.VERSION_NAME + "-" + BuildConfig.VERSION_CODE);
                        return true;
                }
                return true;
            });
            // Showing the popup menu
            popupMenu.show();
        });

        dataBinding.addAlarmFab.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddTaskActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
        });

        dataBinding.ivLogout.setOnClickListener(v -> {
            Constants.logoutAlertMessage(getActivity(), auth);

        });
    }

    private void updateTaskUI(List<FarmerModel> farmerModels) {
        if (farmerModels.isEmpty()) {
            dataBinding.userProfilePostsRecycler.setVisibility(View.GONE);
            dataBinding.tvProfileNoDataFound.setVisibility(View.VISIBLE);
        } else {
            dataBinding.userProfilePostsRecycler.setVisibility(View.VISIBLE);
            dataBinding.tvProfileNoDataFound.setVisibility(View.GONE);
            farmerAdapter.submitList(farmerModels);
        }
    }

    private void setUpRecyclerView() {
        farmerAdapter = new FarmerAdapter(requireContext(), this);
        dataBinding.userProfilePostsRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        dataBinding.userProfilePostsRecycler.setAdapter(farmerAdapter);
    }

    private void updateUI(FirebaseUser user) {
        dataBinding.uploadProgressBarProfile.setVisibility(View.GONE);
        if (user != null) {
            Constants.bindImage(dataBinding.userImageUserFrag, String.valueOf(user.getPhotoUrl()), dataBinding.userImageUserFrag);
            dataBinding.tvProfileUserName.setText(user.getDisplayName());
            dataBinding.tvProfileUserEmail.setText(user.getEmail());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        viewModel.getRoutineList().observe(getViewLifecycleOwner(), farmerModels -> updateTaskUI(farmerModels));
    }

    @Override
    public void markTaskCompleted(FarmerModel model) {
        deactivateAlarm(model.getId());
        viewModel.delete(model);
        Constants.showToast(requireContext(), getString(R.string.task_completed));
    }

    @Override
    public void onDeleteClick(FarmerModel model) {
        deactivateAlarm(model.getId());
        viewModel.delete(model);
        Constants.showToast(requireContext(), getString(R.string.task_deleted));
    }

    public void deactivateAlarm(int id) {
        AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), EventReceiver.class);
        intent.setAction("com.project.agroworld");
        intent.putExtra("maxIDCount", id);
        intent.putExtra("task", "ignore");
        intent.putExtra("desc", "ignore");
        intent.putExtra("time", "ignore");
        intent.putExtra("date", "ignore");
        intent.putExtra("setNotify", "SetNotificationNot");
        PendingIntent pi = PendingIntent.getBroadcast(requireActivity(), id, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        assert am != null;
        am.cancel(pi);
        printLog("Alarm deactivated for " + id);
    }

    private void printLog(String message) {
        Log.d("ProfileFragment", message);
    }
}