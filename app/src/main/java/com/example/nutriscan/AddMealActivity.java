package com.example.nutriscan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nutriscan.adapter.MealItemAdapter;
import com.example.nutriscan.model.CurrentMeal;
import com.example.nutriscan.model.Food;
import com.example.nutriscan.service.RequestService;

import java.util.Locale;

public class AddMealActivity extends AppCompatActivity {

    private TextView tvResult, tvTotal;
    private MealItemAdapter adapter;
    private String notFoundBarcode = null;

    private final ActivityResultLauncher<Intent> scanLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String barcode = result.getData().getStringExtra(ScanActivity.EXTRA_BARCODE);
                    if (barcode != null) {
                        fetchProduct(barcode);
                    }
                }
            });

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

        tvResult = findViewById(R.id.tvResult);
        tvTotal = findViewById(R.id.tvTotal);
        ListView listItems = findViewById(R.id.listItems);
        Button btnFinish = findViewById(R.id.btnFinish);
        Button btnScan = findViewById(R.id.btnScan);
        Button btnManual = findViewById(R.id.btnManual);
        Button btnSaved = findViewById(R.id.btnSaved);

        adapter = new MealItemAdapter(this, R.layout.list_item_meal, CurrentMeal.getItems());
        listItems.setAdapter(adapter);
        listItems.setEmptyView(findViewById(R.id.tvEmpty));
        listItems.setOnItemLongClickListener((parent, view, position, id) -> {
            CurrentMeal.remove(position);
            refresh();
            return true;
        });

        btnFinish.setOnClickListener(v -> finishMeal());

        btnScan.setOnClickListener(v ->
                scanLauncher.launch(new Intent(this, ScanActivity.class)));

        btnManual.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManualFoodActivity.class);
            intent.putExtra(ManualFoodActivity.EXTRA_BARCODE, notFoundBarcode);
            startActivity(intent);
            notFoundBarcode = null;
            tvResult.setText("");
        });

        btnSaved.setOnClickListener(v ->
                startActivity(new Intent(this, SavedFoodsActivity.class)));

        // Voltar atrás com alimentos na refeição: pede confirmação
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (CurrentMeal.getItems().isEmpty()) {
                    finish();
                    return;
                }
                new AlertDialog.Builder(AddMealActivity.this)
                        .setTitle("Descartar refeição?")
                        .setMessage("Os alimentos adicionados serão perdidos.")
                        .setPositiveButton("Descartar", (dialog, which) -> {
                            CurrentMeal.clear();
                            finish();
                        })
                        .setNegativeButton("Continuar", null)
                        .show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh(); // atualiza a lista quando voltas do ecrã de quantidade
    }

    private void refresh() {
        adapter.notifyDataSetChanged();
        tvTotal.setText(String.format(Locale.getDefault(),
                "Total: %.0f kcal", CurrentMeal.totalKcal()));
    }

    private void finishMeal() {
        if (CurrentMeal.getItems().isEmpty()) {
            Toast.makeText(this, "Adiciona pelo menos um alimento", Toast.LENGTH_SHORT).show();
            return;
        }
        // Por agora só mostramos o resumo. Guardar online vem no passo seguinte.
        Toast.makeText(this, String.format(Locale.getDefault(),
                "Refeição concluída: %.0f kcal", CurrentMeal.totalKcal()), Toast.LENGTH_LONG).show();
        CurrentMeal.clear();
        finish();
    }

    private void fetchProduct(String barcode) {
        tvResult.setText("A procurar...");
        new Thread() {
            public void run() {
                final Food result = RequestService.getProduct(barcode);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result == null) {
                            notFoundBarcode = barcode;
                            tvResult.setText("Produto não encontrado (ou sem ligação à internet).\n"
                                    + "Podes inseri-lo manualmente.");
                        } else {
                            notFoundBarcode = null;
                            tvResult.setText("");
                            Intent intent = new Intent(AddMealActivity.this, QuantityActivity.class);
                            intent.putExtra(QuantityActivity.EXTRA_FOOD, result);
                            startActivity(intent);
                        }
                    }
                });
            }
        }.start();
    }
}