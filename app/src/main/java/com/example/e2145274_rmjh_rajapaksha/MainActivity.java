package com.example.e2145274_rmjh_rajapaksha;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    TextView textLatitude, textLongitude, textAddress, textTime, textTemperature, textHumidity, textWeatherDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textLatitude = findViewById(R.id.text_latitude);
        textLongitude = findViewById(R.id.text_longitude);
        textAddress = findViewById(R.id.text_address);
        textTime = findViewById(R.id.text_time);
        textTemperature = findViewById(R.id.text_temperature);
        textHumidity = findViewById(R.id.text_humidity);
        textWeatherDescription = findViewById(R.id.text_weather_description);

        double lat = 6.9897916;
        double lng = 81.0688096;
        String openWeatherMapsAPIKeY = "fc633195a750adbf3c5ec93fee8f1838";
        textLatitude.setText("Latitude: " + lat);
        textLongitude.setText("Longitude: " + lng);

        String urlForWeatherAPI = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lng + "&appid=" + openWeatherMapsAPIKeY;
        new ReadJSONFeedTask().execute(urlForWeatherAPI);

        // Current time
        updateCurrentTime();
        startCounter();
    }

    private void updateCurrentTime() {
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateTime = dateFormat.format(currentDate);
        textTime.setText("Time: " + currentDateTime);
    }

    public void startCounter() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateCurrentTime();
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public String readJSONFeed(String address) {
        StringBuilder stringBuilder = new StringBuilder();
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(address);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream content = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(content));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return stringBuilder.toString();
    }

    private class ReadJSONFeedTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {
            return readJSONFeed(urls[0]);
        }

        protected void onPostExecute(String result) {
            try {
                JSONObject jObj = new JSONObject(result);
                JSONObject mainObj = jObj.getJSONObject("main");
                Weather weather = new Weather();

                weather.setHumidity(mainObj.getInt("humidity"));
                weather.setTemp((float) mainObj.getDouble("temp"));

                textTemperature.setText("Temperature: " + weather.getTemp() + "°K");
                textHumidity.setText("Humidity: " + weather.getHumidity() + "%");

                JSONObject weatherObj = jObj.getJSONArray("weather").getJSONObject(0);
                String description = weatherObj.getString("description");
                textWeatherDescription.setText("Weather: " + description);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
