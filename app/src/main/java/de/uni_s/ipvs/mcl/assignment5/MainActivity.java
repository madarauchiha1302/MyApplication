package de.uni_s.ipvs.mcl.assignment5;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
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
    private final DatabaseReference devReference = FirebaseDatabase.getInstance().getReference().child("teams").child("15");
    private final DatabaseReference mainReference = FirebaseDatabase.getInstance().getReference().child("location");
    private DatabaseReference selectedReference;
    private TextView temperatureTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        selectedReference = devReference; // Choose the appropriate reference here

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
                readTemperatureFromDatabase(city);
            }
        });

        setTemperatureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = citiesSpinner.getSelectedItem().toString();
                String temperature = getTemperatureFromAPI(city);
                temperatureTextView.setText("Temperature: " + temperature);
                writeTemperatureToDatabase(city, temperature);
            }
        });
    }

    private String getTemperatureFromAPI(String city) {
        return openWeatherClient
                .currentWeather()
                .single()
                .byCityName(city)
                .language(Language.GERMAN)
                .unitSystem(UnitSystem.METRIC)
                .retrieve()
                .asJava()
                .toString();
    }

    private void writeTemperatureToDatabase(String city, String temperature) {
        if (temperature != null) {
            String formattedCurrentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Map<String, Object> value = new HashMap<>();
            value.put(String.valueOf(System.currentTimeMillis()), temperature);
            selectedReference.child(city).child(formattedCurrentDate).setValue(value);
        } else {
            Log.d(TAG, "Temperature is null");
            // Handle the case when the temperature is null
        }
    }

    private void readTemperatureFromDatabase(String city) {
        selectedReference.child(city).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    temperatureTextView.setText("Temperature: " + getTemperatureFromMap(city, dataSnapshot.getValue(Map.class)));
                } else {
                    temperatureTextView.setText("Temperature: Data not found");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });
    }

    private String getTemperatureFromMap(String city, Map<String, Object> map) {
        Map<String, Object> cityMap = (Map<String, Object>) map.get(city);
        Map<String, Object> obj = (Map<String, Object>) cityMap.get(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        Log.i("read value", String.valueOf(obj));

        long max_time = 0;
        float temperature = 0;
        for (Map.Entry<String, Object> entry : obj.entrySet()) {
            Map<String, Object> val = (Map<String, Object>) entry.getValue();
            float currentTemperature = Float.parseFloat(val.get("temp").toString());
            long time = Long.valueOf(val.get("time").toString());
            if (time > max_time) {
                max_time = time;
                temperature = currentTemperature;
            }
        }

        return String.valueOf(temperature);
    }
}
