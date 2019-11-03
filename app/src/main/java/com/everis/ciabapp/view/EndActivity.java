package com.everis.ciabapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.everis.ciabapp.R;

public class EndActivity extends AppCompatActivity {

    Button btVoltarInicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);
        initVars();
    }

    void initVars(){
        btVoltarInicio = findViewById(R.id.bt_end_voltar);
        btVoltarInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });
    }

    void goBack() {
        Intent intent = new Intent(this, OnboardActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() { }
}
