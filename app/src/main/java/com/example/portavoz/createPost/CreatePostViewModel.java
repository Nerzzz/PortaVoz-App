package com.example.portavoz.createPost;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class CreatePostViewModel extends ViewModel {

    // ContentFragment
    private final MutableLiveData<String> title = new MutableLiveData<>("");
    private final MutableLiveData<String> description = new MutableLiveData<>("");

    // TagFragment
    private final MutableLiveData<List<String>> hashtags = new MutableLiveData<>(new ArrayList<>());

    // mapFragment
    private final MutableLiveData<Double> latitude = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> longitude = new MutableLiveData<>(0.0);

    // address
    private final MutableLiveData<String> address = new MutableLiveData<>("");

    // location (JSON)
    private final MutableLiveData<String> locationJson = new MutableLiveData<>("");

    // image
    private final MutableLiveData<List<String>> imagePaths = new MutableLiveData<>(new ArrayList<>());


    // getters
    public LiveData<String> getTitle() { return title; }

    public LiveData<String> getDescription() { return description; }

    public LiveData<List<String>> getHashtags() { return hashtags; }

    public LiveData<Double> getLatitude() { return latitude; }

    public LiveData<Double> getLongitude() { return longitude; }

    public LiveData<String> getAddress() { return address; }

    public LiveData<String> getLocationJson() { return locationJson; }

    public LiveData<List<String>> getImagePaths() { return imagePaths; }


    // setters
    public void setTitle(String newTitle) { title.setValue(newTitle); }

    public void setDescription(String newDescription) { description.setValue(newDescription); }

    public void setHashtags(List<String> newHashtags) { hashtags.setValue(newHashtags); }

    public void setLatitude(double newLatitude) { latitude.setValue(newLatitude); }

    public void setLongitude(double newLongitude) { longitude.setValue(newLongitude); }

    public void setAddress(String newAddress) { address.setValue(newAddress); }

    public void setLocationJson(String json) { locationJson.setValue(json); }

    public void setImagePaths(List<String> newImagePaths) { imagePaths.setValue(newImagePaths); }
}
