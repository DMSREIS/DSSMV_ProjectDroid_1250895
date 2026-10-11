package com.example.nutriscan;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddMealActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_meal);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addMeal), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnScan = findViewById(R.id.btnScan);
        Button btnManual = findViewById(R.id.btnManual);
        Button btnSaved = findViewById(R.id.btnSaved);

        btnScan.setOnClickListener(v ->
                Toast.makeText(this, "Scan: em breve", Toast.LENGTH_SHORT).show());
        btnManual.setOnClickListener(v ->
                Toast.makeText(this, "Manual: em breve", Toast.LENGTH_SHORT).show());
        btnSaved.setOnClickListener(v ->
                Toast.makeText(this, "Guardado: em breve", Toast.LENGTH_SHORT).show());
    }
}