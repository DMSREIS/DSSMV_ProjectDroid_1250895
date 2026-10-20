package com.example.nutriscan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nutriscan.model.Food;

public class ManualFoodActivity extends AppCompatActivity {

    public static final String EXTRA_BARCODE = "barcode";

    private EditText etName, etKcal, etProteins, etCarbs, etFat;
    private String barcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manual_food);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.manualFood), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        barcode = getIntent().getStringExtra(EXTRA_BARCODE);

        etName = findViewById(R.id.etName);
        etKcal = findViewById(R.id.etKcal);
        etProteins = findViewById(R.id.etProteins);
        etCarbs = findViewById(R.id.etCarbs);
        etFat = findViewById(R.id.etFat);

        findViewById(R.id.btnContinue).setOnClickListener(v -> continueToQuantity());
    }

    private void continueToQuantity() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Indica o nome");
            return;
        }
        Double kcal = parse(etKcal);
        if (kcal == null) {
            etKcal.setError("Indica as calorias");
            return;
        }

        Food food = new Food(barcode, name, "", kcal,
                orZero(parse(etProteins)), orZero(parse(etCarbs)), orZero(parse(etFat)));

        Intent intent = new Intent(this, QuantityActivity.class);
        intent.putExtra(QuantityActivity.EXTRA_FOOD, food);
        startActivity(intent);
    }

    private Double parse(EditText editText) {
        String s = editText.getText().toString().trim().replace(',', '.');
        if (s.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double orZero(Double value) {
        return value == null ? 0.0 : value;
    }
}