package com.example.nutriscan;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nutriscan.model.CurrentMeal;
import com.example.nutriscan.model.Food;
import com.example.nutriscan.model.MealItem;
import com.example.nutriscan.service.FoodService;

import java.util.Locale;

public class QuantityActivity extends AppCompatActivity {

    public static final String EXTRA_FOOD = "food";

    private Food food;
    private EditText etGrams;
    private TextView tvTotals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quantity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.quantity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        food = IntentCompat.getSerializableExtra(getIntent(), EXTRA_FOOD, Food.class);
        if (food == null) {
            finish();
            return;
        }

        TextView tvFood = findViewById(R.id.tvFood);
        etGrams = findViewById(R.id.etGrams);
        tvTotals = findViewById(R.id.tvTotals);
        Button btnConfirm = findViewById(R.id.btnConfirm);

        tvFood.setText(food.getName()
                + (food.getBrand() == null || food.getBrand().isEmpty() ? "" : " (" + food.getBrand() + ")")
                + "\n\nPor 100 g:"
                + "\nCalorias: " + fmt(food.getKcal()) + " kcal"
                + "\nProteínas: " + fmt(food.getProteins()) + " g"
                + "\nHidratos: " + fmt(food.getCarbohydrates()) + " g"
                + "\nGorduras: " + fmt(food.getFat()) + " g");

        etGrams.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                updateTotals();
            }
        });

        btnConfirm.setOnClickListener(v -> {
            Double grams = getGrams();
            if (grams == null || grams <= 0) {
                etGrams.setError("Indica a quantidade em gramas");
                return;
            }

            FoodService.saveFood(food);
            CurrentMeal.add(new MealItem(food, grams));

            // Volta ao ecrã da refeição (fecha o scan/manual/quantidade que estavam por cima)
            Intent intent = new Intent(this, AddMealActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void updateTotals() {
        Double grams = getGrams();
        if (grams == null) {
            tvTotals.setText("");
            return;
        }
        tvTotals.setText("Total para " + fmt(grams) + " g:"
                + "\nCalorias: " + calc(food.getKcal(), grams) + " kcal"
                + "\nProteínas: " + calc(food.getProteins(), grams) + " g"
                + "\nHidratos: " + calc(food.getCarbohydrates(), grams) + " g"
                + "\nGorduras: " + calc(food.getFat(), grams) + " g");
    }

    private Double getGrams() {
        String s = etGrams.getText().toString().trim().replace(',', '.');
        if (s.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // valor por 100 g -> valor para a quantidade indicada
    private String calc(Double per100g, double grams) {
        return per100g == null ? "-" : fmt(per100g * grams / 100.0);
    }

    private String fmt(Double value) {
        return value == null ? "-" : String.format(Locale.getDefault(), "%.1f", value);
    }
}