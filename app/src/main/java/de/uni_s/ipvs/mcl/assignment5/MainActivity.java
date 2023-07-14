package de.uni_s.ipvs.mcl.assignment5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.prominence.openweathermap.api.OpenWeatherMapClient;
import com.github.prominence.openweathermap.api.enums.Language;
import com.github.prominence.openweathermap.api.enums.UnitSystem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";
    private final OpenWeatherMapClient openWeatherClient = new OpenWeatherMapClient(
            "a23ec089a5b1cfc2ce6ccfd9524c7448");
    private DatabaseReference selectedReference;
    private TextView temperatureTextView;
    private String selectedCity;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // FOR GENERAL TESTING USE devReference, FOR FINAL TESTS USE mainReference
        // selectedReference = devReference;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Log.i(TAG, "current time: " + System.currentTimeMillis());

        // FirebaseApp.initializeApp(this);

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId("de.uni_s.ipvs.mcl.assignment5")
                .setApiKey("AIzaSyBnbpsN3vfoo5xES2y4iTuKVz0btRoPUBU")
                .setDatabaseUrl("https://assignment5-b5c92.firebaseio.com")
                .build();

        FirebaseApp.initializeApp(getApplicationContext(), options, "My application");

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference node = databaseRef.child("teams").child("15");
        Button button_update = findViewById(R.id.button1);
        Button button_update_2 = findViewById(R.id.button2);
        DatabaseReference locationNodeRef = databaseRef.child("location");

        button_update.setOnClickListener(l -> {
            Log.i(TAG, "Change value to 1");
            node.setValue(1);
        });
        button_update_2.setOnClickListener(l -> {
            Log.i(TAG, "Change value to 2");
            node.setValue(2);
        });

        node.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Handle the retrieved data here
                Log.i(TAG, "Detected data is changed to: " + dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors
            }
        });

        Spinner citiesSpinner = findViewById(R.id.citiesSpinner);
        temperatureTextView = findViewById(R.id.temperatureTv);
        final String[] spinnerItems = {"Stuttgart", "Paris", "Berlin"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, spinnerItems);
        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this, R.array.cities,
                android.R.layout.simple_spinner_item);
        citiesSpinner.setAdapter(arrayAdapter);


        // arrayAdapter.add("Waiting for data ...");


        locationNodeRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.i("firebase", "Error getting data", task.getException());
                }
                else {
                    Log.i(TAG, "Fetching child of location in get...");
                    Log.i("firebase", String.valueOf(task.getResult().getValue()));


                }
            }
        });



        DatabaseReference parisRef = locationNodeRef.child("Paris");
        parisRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                temperatureTextView.setText(snapshot.getValue().toString());
                Log.i(TAG, "Paris: " + snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference stuttRef = locationNodeRef.child("Stuttgart");
        stuttRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                temperatureTextView.setText(snapshot.getValue().toString());
                Log.i(TAG, "Stuttgart: " + snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference berlinRef = locationNodeRef.child("Berlin");
        berlinRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                temperatureTextView.setText(snapshot.getValue().toString());
                Log.i(TAG, "Berlin: " + snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // this func is nec
        locationNodeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // arrayAdapter.clear();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    // Process each child node
                    if(!childSnapshot.hasChildren()){
                        Log.i(TAG,"Location node has no child!");
                    }
                    else{
                        Log.i(TAG, "Fetching child of location...");
                    }

                    String childKey = childSnapshot.getKey();
                    // arrayAdapter.add(childKey);
                    Object childValue = childSnapshot.getValue();
                    Log.i(TAG,"Get updated location: " + childKey);
                    // Do something with the child key and value
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        citiesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, spinnerItems[position] + " is selected.");
                selectedCity = spinnerItems[position];

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        EditText editText = findViewById(R.id.editText);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.i(TAG, "User input: " + v.getText());
                    handled = true;
                    writeTemperatureToDatabase(selectedCity, v.getText().toString());

                }
                return handled;
            }
        });

        /*

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

         */
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
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("location").child(city);
        String formattedCurrentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, Object> value = new HashMap<>();
        value.put(String.valueOf(System.currentTimeMillis()), temperature);
        reference.child(formattedCurrentDate).push().setValue(value);
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