package com.example.mosqueaishatv;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

public class LocationSetting extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_setting);
    }


    public void setLocation(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences("SharedPreferences", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        switch (view.getId()) {
            case R.id.NiagaraButton:
                myEdit.putString("location", "niagara");
                myEdit.commit();
                break;

            case R.id.ThoroldButton:
                myEdit.putString("location", "thorold");
                myEdit.commit();
                break;
        }
        startActivity(new Intent(this, MainActivity.class));
    }
}