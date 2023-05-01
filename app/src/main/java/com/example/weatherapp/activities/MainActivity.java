package com.example.weatherapp.activities;

import static com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.weatherapp.R;
import com.example.weatherapp.databinding.ActivityLoadingBinding;
import com.example.weatherapp.databinding.ActivityMainBinding;
import com.example.weatherapp.network.Network;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    String currentLocation;

    final public String TAG = "DEBUGGING";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();

        currentLocation = intent.getStringExtra("LATITUDE") +
                intent.getStringExtra("LONGITUDE");
        Log.v(TAG, currentLocation);

        final OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .url(Network.openWeatherAPI + "current.json?key=" + Network.openWeatherAPIKey
                        + "&aqi=no&q=" + currentLocation)
                .build();

        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()){
                        return null;
                    }
                    return response.body().string();
                } catch (Exception e){
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String s){
                super.onPostExecute(s);
                if(s != null){
                    JSONObject jsonResponse = null;
                    try {
                        jsonResponse = new JSONObject(s);
                        JSONObject locationObject = jsonResponse.getJSONObject("location");
                        binding.locationText2.setText(locationObject.getString("name"));
                    } catch (JSONException e){
                        throw new RuntimeException(e);
                    }
                }
            }
        };

        asyncTask.execute();
    }
}