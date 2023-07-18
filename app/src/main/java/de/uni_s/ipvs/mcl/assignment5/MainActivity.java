package de.uni_s.ipvs.mcl.assignment5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.widget.Toast;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";
    private final OpenWeatherMapClient openWeatherClient = new OpenWeatherMapClient(
            "a23ec089a5b1cfc2ce6ccfd9524c7448");
    private DatabaseReference selectedReference;
    private TextView temperatureTextView;
    private TextView latestTempTextView;
    private TextView avgTempTextView;
    private String selectedCity;
    private Context mContext;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        latestTempTextView = findViewById(R.id.textView_latestTemp);
        avgTempTextView = findViewById(R.id.textView_avgTemp);
        mContext = this;

        // Log.i(TAG, "current time: " + System.currentTimeMillis());

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId("de.uni_s.ipvs.mcl.assignment5")
                .setApiKey("AIzaSyBnbpsN3vfoo5xES2y4iTuKVz0btRoPUBU")
                .setDatabaseUrl("https://assignment5-b5c92.firebaseio.com")
                .build();

        FirebaseApp.initializeApp(getApplicationContext(), options, "My application");

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference locationNodeRef = databaseRef.child("location");


        Spinner citiesSpinner = findViewById(R.id.citiesSpinner);
        temperatureTextView = findViewById(R.id.temperatureTv);
        final String[] spinnerItems = {"Stuttgart", "Paris", "Berlin"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, spinnerItems);
        citiesSpinner.setAdapter(arrayAdapter);


        DatabaseReference parisRef = locationNodeRef.child("Paris");

        // register the event listener to update display on current selected city
        parisRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Log.i(TAG, "Paris: " + snapshot.getValue().toString());

                // only if the selected city matched, update the display
                // this avoids display update in the func overwriting current display
                // when start the first time
                if(selectedCity.equals("Paris")){
                    readLatestTemperature(snapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference stuttRef = locationNodeRef.child("Stuttgart");
        stuttRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i(TAG, "Stuttgart: " + snapshot.getValue().toString());
                if(selectedCity.equals("Stuttgart")){
                    readLatestTemperature(snapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference berlinRef = locationNodeRef.child("Berlin");
        berlinRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i(TAG, "Berlin: " + snapshot.getValue().toString());
                if(selectedCity.equals("Berlin")){
                    readLatestTemperature(snapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //  when user select a city, fetch data and update display
        citiesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, spinnerItems[position] + " is selected.");
                selectedCity = spinnerItems[position];

                if(selectedCity.equals("Stuttgart")){
                    stuttRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            readLatestTemperature(snapshot);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
                else if(selectedCity.equals("Berlin")){
                    berlinRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            readLatestTemperature(snapshot);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
                else if(selectedCity.equals("Paris")){
                    parisRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            readLatestTemperature(snapshot);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }


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
                    String inputTemp = v.getText().toString();

                    if(inputTemp.matches("\\d+(\\.\\d+)?")){
                        writeTemperatureToDatabase(selectedCity, v.getText().toString());
                    }
                    else{
                        Toast.makeText(mContext, "Please input a valid temperature! Hint: two number digits.", Toast.LENGTH_SHORT).show();
                    }


                }
                return handled;
            }
        });
    }

    private void writeTemperatureToDatabase(String city, String temperature) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("location").child(city);
        String formattedCurrentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, Object> value = new HashMap<>();
        value.put(String.valueOf(System.currentTimeMillis()), temperature);
        reference.child(formattedCurrentDate).push().setValue(value);
    }

    private void readLatestTemperature(DataSnapshot dataSnapshot) {
        if(!dataSnapshot.exists()) return;
        Log.i(TAG, "Reading temp under " + dataSnapshot.getKey() + "...");

        String formattedCurrentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        float latestTemp = Float.NEGATIVE_INFINITY;
        String latestTime = "0";
        for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
            // Process each child node

            // list to store all valid temperature value within one day
            List<Float> list = new LinkedList<>();
            String dateKey = dateSnapshot.getKey();
            if (dateKey != null ){
                if(dateKey.equals(formattedCurrentDate)) {
                    Log.i(TAG, "date: " + dateKey);

                    for (DataSnapshot timeSnapshot : dateSnapshot.getChildren()) {
                        if(!timeSnapshot.exists()) return;
                        // for timeSnapshot has {id=temperature} as child
                        if (timeSnapshot.hasChildren()) {

                            for (DataSnapshot idSnapshot : timeSnapshot.getChildren()) {
                                Log.i(TAG, "id: " + idSnapshot.getKey());
                                String temp = idSnapshot.getValue().toString();
                                if(!temp.matches("\\d+(\\.\\d+)?")) {
                                    continue;
                                }
                                float temperature = Float.parseFloat(idSnapshot.getValue().toString());
                                String time = String.valueOf(idSnapshot.getKey());
                                Log.i(TAG, "time: " + timeSnapshot.getKey());
                                if(time.compareTo(latestTime) > 0){
                                    latestTime = time;
                                    latestTemp= temperature;
                                }
                                list.add(temperature);
                                Log.i(TAG, "temperature: " + idSnapshot.getValue());
                            }
                        }
                        // for timeSnapshot has {time=temperature} structure
                        else{
                            String temp = timeSnapshot.getValue().toString();
                            if(!temp.matches("\\d+(\\.\\d+)?")) {
                                continue;
                            }
                            float temperature = Float.parseFloat(timeSnapshot.getValue().toString());
                            list.add(temperature);
                            String time = String.valueOf(timeSnapshot.getKey());
                            Log.i(TAG, "time: " + timeSnapshot.getKey());
                            if(time.compareTo(latestTime) > 0){
                                latestTime = time;
                                latestTemp= temperature;
                            }
                        }



                    }

                    Log.i(TAG, "Latest temp under " + dataSnapshot.getKey() + ": " + latestTemp);
                    latestTempTextView.setText(String.valueOf(latestTemp));
                    float avgTemp = calculateAvg(list);
                    Log.i(TAG, "Average temp under " + dataSnapshot.getKey() + ": " + avgTemp);
                    avgTempTextView.setText(String.valueOf(avgTemp));

                }


            }

        }
    }


    private float calculateAvg(List<Float> l){
        float sum = 0;
        for(float num: l){
            sum += num;
        }
        return sum / l.size();
    }
}