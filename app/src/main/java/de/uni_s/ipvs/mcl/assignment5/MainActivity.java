package de.uni_s.ipvs.mcl.assignment5;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.prominence.openweathermap.api.OpenWeatherMapClient;
import com.github.prominence.openweathermap.api.enums.Language;
import com.github.prominence.openweathermap.api.enums.UnitSystem;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";
    private final OpenWeatherMapClient openWeatherClient = new OpenWeatherMapClient(
            "a23ec089a5b1cfc2ce6ccfd9524c7448");
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference myRef = database.getReference();
    private final DatabaseReference devReference = myRef.child("teams").child("15");
    private final DatabaseReference mainReference = myRef.child("location");
    private DatabaseReference selectedReference;
    private TextView temperatureTextView;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // FOR GENERAL TESTING USE devReference, FOR FINAL TESTS USE mainReference
        selectedReference = devReference;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner citiesSpinner = findViewById(R.id.citiesSpinner);
        temperatureTextView = findViewById(R.id.temperatureTv);
        Button fetchTemperatureButton = findViewById(R.id.getDataButton);
        Button setTemperatureButton = findViewById(R.id.setTemperatureButton);

        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this, R.array.cities,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        citiesSpinner.setAdapter(adapter);

        citiesSpinner.setOnItemClickListener((adapterView, view, i, l) -> {
            temperatureTextView.setText("Temperature: no temperature fetched");
        });

        fetchTemperatureButton.setOnClickListener(l -> {
            readTemperatureFromDatabase(selectedReference, citiesSpinner.getSelectedItem().toString());
        });

        setTemperatureButton.setOnClickListener(l -> {
            String temperature = getTemperatureFromAPI(citiesSpinner.getSelectedItem().toString());
            temperatureTextView.setText("Temperature: " + temperature);
            writeTemperatureToDatabase(selectedReference, citiesSpinner.getSelectedItem().toString(),
                    temperature);
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

    private void writeTemperatureToDatabase(DatabaseReference reference, String city, String temperature) {
        String formattedCurrentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, Object> value = new HashMap<>();
        value.put(String.valueOf(System.currentTimeMillis()), temperature);
        reference.child(city).child(formattedCurrentDate).setValue(value);
    }

    private void readTemperatureFromDatabase(DatabaseReference reference, String city) {
        reference.child(city).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                temperatureTextView.setText("Temperature: " + getTemperatureFromMap(city,
                        (Map<String, Object>) task.getResult().getValue()));
            } else {
                Log.d(TAG, "onComplete: " + task.getException());
            }
        });
    }

    private String getTemperatureFromMap(String city, Map<String, Object> map) {
        Map<String, Object> cityMap = (Map<String, Object>) map.get(city);
        Map<String, Object> obj = (Map<String, Object>) cityMap.get(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        Log.i("read value", String.valueOf(obj));

        long max_time = 0;
        float temperature = 0;
        for (Map.Entry<String,Object> entry : obj.entrySet()){
            Map<String, Object> val = (Map<String, Object>) entry.getValue();
            float currentTemperature = Float.parseFloat(val.get("temp").toString());
            long time = Long.valueOf(val.get("time").toString());
            if (time > max_time){
                max_time = time;
                temperature = currentTemperature;
            }
        }

        return String.valueOf(temperature);
    }
}