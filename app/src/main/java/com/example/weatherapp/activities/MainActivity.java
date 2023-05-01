package com.example.weatherapp.activities;

import static com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.weatherapp.R;
import com.example.weatherapp.databinding.ActivityLoadingBinding;
import com.example.weatherapp.databinding.ActivityMainBinding;
import com.example.weatherapp.network.Network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

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

        currentLocation = intent.getStringExtra("LATITUDE") + "," +
                intent.getStringExtra("LONGITUDE");
        Log.v(TAG, currentLocation);

        final OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .url(Network.openWeatherAPI + "forecast.json?key=" + Network.openWeatherAPIKey
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
                        JSONObject currentObject = jsonResponse.getJSONObject("current");
                        JSONObject forecastObject = jsonResponse.getJSONObject("forecast");
                        JSONObject dayObject = forecastObject.getJSONArray(
                                "forecastday").getJSONObject(0).getJSONObject(
                                        "day");
                        Log.v(TAG, dayObject.toString());
                        binding.locationText.setText(locationObject.getString("region"));
                        binding.minimunText.setText("min: "+dayObject.getString("mintemp_c")+"°C");
                        binding.currentText.setText(currentObject.getString("temp_c")+"°C");
                        binding.maximumText.setText("max: "+dayObject.getString("maxtemp_c")+"°C");

                        JSONObject conditionObject = currentObject.getJSONObject("condition");
                        new DownloadImageFromInternet((ImageView) findViewById(R.id.conditionImage)).execute("https:"+conditionObject.getString("icon"));
                    } catch (JSONException e){
                        throw new RuntimeException(e);
                    }
                }
            }

            class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
                ImageView imageView;
                public DownloadImageFromInternet(ImageView imageView) {
                    this.imageView=imageView;
                    Toast.makeText(getApplicationContext(), "Please wait, it may take a few minutes...",Toast.LENGTH_SHORT).show();
                }
                protected Bitmap doInBackground(String... urls) {
                    String imageURL=urls[0];
                    Bitmap bimage=null;
                    try {
                        InputStream in=new java.net.URL(imageURL).openStream();
                        bimage= BitmapFactory.decodeStream(in);
                    } catch (Exception e) {
                        Log.e("Error Message", e.getMessage());
                        e.printStackTrace();
                    }
                    return bimage;
                }
                protected void onPostExecute(Bitmap result) {
                    imageView.setImageBitmap(result);
                }
            }
        };

        asyncTask.execute();
    }
}