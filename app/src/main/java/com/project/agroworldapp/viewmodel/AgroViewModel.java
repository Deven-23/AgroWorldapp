package com.project.agroworldapp.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.project.agroworldapp.articles.model.CropsResponse;
import com.project.agroworldapp.articles.model.DiseasesResponse;
import com.project.agroworldapp.articles.model.FlowersResponse;
import com.project.agroworldapp.articles.model.FruitsResponse;
import com.project.agroworldapp.articles.model.HowToExpandResponse;
import com.project.agroworldapp.articles.model.InsectControlResponse;
import com.project.agroworldapp.payment.model.PaymentModel;
import com.project.agroworldapp.repository.AgroWorldRepository;
import com.project.agroworldapp.shopping.model.ProductModel;
import com.project.agroworldapp.transport.model.VehicleModel;
import com.project.agroworldapp.utils.Resource;
import com.project.agroworldapp.weather.model.weather_data.WeatherResponse;
import com.project.agroworldapp.weather.model.weatherlist.WeatherDatesResponse;

import java.util.List;

public class AgroViewModel extends ViewModel {
    private AgroWorldRepository repository;

    public void init(Context context) {
        repository = new AgroWorldRepository(context);
    }

    public LiveData<Resource<WeatherResponse>> performWeatherRequest(double latitude, double longitude, String apiKey) {
        return repository.performWeatherRequest(latitude, longitude, apiKey);
    }

    public LiveData<Resource<WeatherDatesResponse>> performWeatherForecastRequest(double latitude, double longitude, String apiKey) {
        return repository.performWeatherForecastRequest(latitude, longitude, apiKey);
    }

    public LiveData<Resource<List<DiseasesResponse>>> getDiseasesResponseLivedata() {
        return repository.getDiseasesResponse();
    }

    public LiveData<Resource<List<InsectControlResponse>>> getInsectAndControlLivedata() {
        return repository.getInsectAndControlResponse();
    }

    public LiveData<Resource<List<FruitsResponse>>> getFruitsResponseLivedata() {
        return repository.getFruitsResponse();
    }

    public LiveData<Resource<List<HowToExpandResponse>>> getHowToExpandResponseLivedata() {
        return repository.getHowToExpandResponse();
    }

    public LiveData<Resource<List<CropsResponse>>> getCropsResponseLivedata() {
        return repository.getCropsResponse();
    }

    public LiveData<Resource<List<FlowersResponse>>> getFlowersResponseLivedata() {
        return repository.getFlowersResponse();
    }

    public LiveData<Resource<List<ProductModel>>> getProductModelLivedata() {
        return repository.getProductListFromFirebase();
    }

    public LiveData<Resource<List<ProductModel>>> getLocalizedProductDataList() {
        return repository.getLocalizedProductDataList();
    }

    public LiveData<Resource<List<VehicleModel>>> getVehicleModelLivedata() {
        return repository.getVehicleListFromFirebase();
    }

    public LiveData<Resource<List<PaymentModel>>> getTransactionList(String email) {
        return repository.getTransactionList(email);
    }

    public LiveData<Resource<String>> removeProductFromFirebase(String title) {
        return repository.removeProductFromFirebase(title);
    }

    public LiveData<Resource<String>> removeLocalizedProduct(String title) {
        return repository.removeLocalizedProduct(title);
    }

    public LiveData<Resource<String>> performVehicleRemovalAction(String vehicleModel) {
        return repository.performProductRemovalAction(vehicleModel);
    }

    public void uploadTransaction(PaymentModel paymentModel, String email) {
        repository.uploadTransactionDetail(paymentModel, email);
    }

    public void deleteCartData(String email) {
        repository.deleteCartData(email);
    }

    public void clearAllHistory(String email) {
        repository.removeAllTransactionHistory(email);
    }

    public LiveData<String> checkLoadingStatus() {
        return repository.getRequestErrorLivedata();
    }

}