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
import com.example.nutriscan.view.SliderInput;

public class ManualFoodActivity extends AppCompatActivity {

    public static final String EXTRA_BARCODE = "barcode";

    private EditText etName;
    private SliderInput sliderKcal, sliderProteins, sliderCarbs, sliderFat;
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
        sliderKcal = findViewById(R.id.sliderKcal);
        sliderProteins = findViewById(R.id.sliderProteins);
        sliderCarbs = findViewById(R.id.sliderCarbs);
        sliderFat = findViewById(R.id.sliderFat);

        sliderKcal.setup(900, 1);
        sliderProteins.setup(100, 0.1);
        sliderCarbs.setup(100, 0.1);
        sliderFat.setup(100, 0.1);

        sliderKcal.setLabel("Calorias por 100 gramas");
        sliderProteins.setLabel("Proteínas por 100 gramas");
        sliderCarbs.setLabel("Hidratos de carbono por 100 gramas");
        sliderFat.setLabel("Gorduras por 100 gramas");

        findViewById(R.id.btnContinue).setOnClickListener(v -> continueToQuantity());
    }

    private void continueToQuantity() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Indica o nome");
            return;
        }

        Food food = new Food(barcode, name, "",
                sliderKcal.getValue(), sliderProteins.getValue(),
                sliderCarbs.getValue(), sliderFat.getValue());

        Intent intent = new Intent(this, QuantityActivity.class);
        intent.putExtra(QuantityActivity.EXTRA_FOOD, food);
        startActivity(intent);
    }
}