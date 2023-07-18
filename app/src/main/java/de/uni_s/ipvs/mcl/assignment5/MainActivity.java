package de.uni_s.ipvs.mcl.assignment5;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.prominence.openweathermap.api.OpenWeatherMapClient;
import com.github.prominence.openweathermap.api.enums.Language;
import com.github.prominence.openweathermap.api.enums.UnitSystem;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    private final OpenWeatherMapClient openWeatherClient = new OpenWeatherMapClient(
            "a23ec089a5b1cfc2ce6ccfd9524c7448");
    private DatabaseReference devReference;
    private DatabaseReference mainReference;
    private DatabaseReference selectedReference;
    private TextView temperatureTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId("de.uni_s.ipvs.mcl.assignment5")
                .setApiKey("AIzaSyBnbpsN3vfoo5xES2y4iTuKVz0btRoPUBU")
                .setDatabaseUrl("https://assignment5-b5c92.firebaseio.com")
                .build();

        FirebaseApp.initializeApp(getApplicationContext(), options, "My application");

        devReference = FirebaseDatabase.getInstance().getReference().child("teams").child("15");
        mainReference = FirebaseDatabase.getInstance().getReference().child("location");
        selectedReference = mainReference; // Choose the appropriate reference here

        Spinner citiesSpinner = findViewById(R.id.citiesSpinner);
        temperatureTextView = findViewById(R.id.temperatureTv);
        Button fetchTemperatureButton = findViewById(R.id.getDataButton);
        Button setTemperatureButton = findViewById(R.id.setTemperatureButton);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.cities,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citiesSpinner.setAdapter(adapter);

        citiesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                temperatureTextView.setText("Temperature: no temperature fetched");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Handle the case when nothing is selected
            }
        });

        fetchTemperatureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = citiesSpinner.getSelectedItem().toString();
                readTemperatureFromDatabase(selectedReference, city);
            }
        });

        setTemperatureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = citiesSpinner.getSelectedItem().toString();
                // this must be done in a separate thread because it connects to the network
                new Thread(() -> {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);

                    String temperature = readTemperatureFromAPI(city);

                    temperatureTextView.setText("Temperature: " + temperature + " °C");
                    writeTemperatureToDatabase(city, temperature);
                }).run();
            }
        });
    }

    private String readTemperatureFromAPI(String city) {
        String[] tempData = openWeatherClient.currentWeather()
                .single()
                .byCityName(city)
                .language(Language.GERMAN)
                .unitSystem(UnitSystem.METRIC)
                .retrieve()
                .asJava()
                .toString().split(", ");

        return tempData[tempData.length-3].split("°C")[0].trim();
    }

    private void writeTemperatureToDatabase(String city, String temperature) {
        if (temperature != null) {
            String formattedCurrentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Map<String, Object> value = new HashMap<>();
            value.put(String.valueOf(System.currentTimeMillis()), temperature);
            selectedReference.child(city).child(formattedCurrentDate).setValue(value);
        } else {
            Log.d(TAG, "Temperature is null");
        }
    }

    private void readTemperatureFromDatabase(DatabaseReference reference, String city) {
        reference.child(city).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                temperatureTextView.setText("Temperature: " + getTemperatureFromMap(
                        (Map<String, Object>) task.getResult().getValue()) + " °C");
            } else {
                Log.d(TAG, "onComplete: " + task.getException());
            }
        });
    }

    private String getTemperatureFromMap(Map<String, Object> map) {
        Log.w(TAG, map.toString());

        Map<String, Object> temperatureMap = (Map<String, Object>) map.values().toArray()[0];
        Log.i("read value", String.valueOf(temperatureMap));

        return String.valueOf(temperatureMap.values().toArray()[0]);
    }
}
