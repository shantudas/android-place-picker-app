package com.muvasia.android_places_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.places.*;

import static com.muvasia.android_places_demo.Constants.REQUEST_CODE_LOCATION;
import static com.muvasia.android_places_demo.Constants.RESULT_LOCATION_KEY;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Button btnFetchAddress;
    private TextView tvAddress;
    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialization();


        btnFetchAddress.setOnClickListener((View view) -> launchAddressPicker());
    }

    private void launchAddressPicker() {
        Intent intent = new Intent(this, AddressPickerActivity.class);
        //startActivity(intent);
        startActivityForResult(intent, REQUEST_CODE_LOCATION);

    }

    private void initialization() {
        btnFetchAddress = findViewById(R.id.btnFetchAddress);
        tvAddress = findViewById(R.id.tvAddress);

        address = "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE_LOCATION) {
            if (resultCode == RESULT_OK) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    address = extras.getString(RESULT_LOCATION_KEY);
                    tvAddress.setText(address);
                }
            }
        }
    }
}
