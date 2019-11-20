package com.muvasia.android_places_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.android.gms.location.places.*;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Button btnFetchAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialization();


        btnFetchAddress.setOnClickListener((View view) -> launchAddressPicker());
    }

    private void launchAddressPicker() {
        Intent intent = new Intent(this, AddressPickerActivity.class);
        startActivity(intent);

    }

    private void initialization() {
        btnFetchAddress = findViewById(R.id.btnFetchAddress);
    }
}
