package com.example.nutriscan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.nutriscan.helper.DateHelper;
import com.example.nutriscan.model.CurrentMeal;
import com.example.nutriscan.model.Food;
import com.example.nutriscan.model.Meal;
import com.example.nutriscan.service.FoodService;
import com.example.nutriscan.service.MealService;
import com.example.nutriscan.service.RequestService;
import com.example.nutriscan.service.SavedMealService;

import java.util.Locale;

public class AddMealActivity extends AppCompatActivity {

    private TextView tvResult, tvTotal;
    private MealItemAdapter adapter;
    private String notFoundBarcode = null;
    private AlertDialog saveDialog;

    private final ActivityResultLauncher<Intent> scanLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String barcode = result.getData().getStringExtra(ScanActivity.EXTRA_BARCODE);
                    if (barcode != null) {
                        fetchProduct(barcode);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> savedMealLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                // Se foi registada uma refeição guardada e não há nada em curso,
                // o utilizador só queria isso: fechamos este ecrã.
                if (result.getResultCode() == RESULT_OK && CurrentMeal.getItems().isEmpty()) {
                    CurrentMeal.clear();
                    finish();
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
        Button btnSavedMeal = findViewById(R.id.btnSavedMeal);

        // Se a refeição é de outro dia (vindo do calendário), o título diz qual
        String dateKey = CurrentMeal.getDateKey();
        if (dateKey != null && !dateKey.equals(DateHelper.todayKey())) {
            String[] p = dateKey.split("-");
            ((TextView) findViewById(R.id.tvMealTitle)).setText("Refeição · "
                    + DateHelper.dayLabel(Integer.parseInt(p[2]), Integer.parseInt(p[1]) - 1));
        }

        adapter = new MealItemAdapter(this, R.layout.list_item_meal, CurrentMeal.getItems());
        listItems.setAdapter(adapter);
        listItems.setEmptyView(findViewById(R.id.tvEmpty));
        listItems.setOnItemLongClickListener((parent, view, position, id) -> {
            CurrentMeal.remove(position); // toque longo: tira o alimento da refeição
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

        btnSavedMeal.setOnClickListener(v ->
                savedMealLauncher.launch(new Intent(this, SavedMealsActivity.class)));

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
        refresh();          // atualiza a lista quando voltas do ecrã de quantidade
        showSaveDialog();   // volta a mostrar o pop-up se ficou por responder (ex.: ao rodar o ecrã)
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (saveDialog != null) {
            saveDialog.dismiss();
        }
    }

    private void refresh() {
        adapter.notifyDataSetChanged();
        tvTotal.setText(String.format(Locale.getDefault(),
                "Total: %.0f kcal", CurrentMeal.totalKcal()));
    }

    // A refeição fica registada no dia logo ao concluir; depois pergunta se a guarda como modelo
    private void finishMeal() {
        if (CurrentMeal.getItems().isEmpty()) {
            Toast.makeText(this, "Adicione pelo menos um alimento", Toast.LENGTH_SHORT).show();
            return;
        }

        Button btnFinish = findViewById(R.id.btnFinish);
        btnFinish.setEnabled(false);

        final Meal meal = MealService.buildFromCurrent();
        MealService.saveMeal(meal, new FoodService.Callback<String>() {
            @Override
            public void onSuccess(String id) {
                meal.setId(id); // a chave serve para dar nome a esta refeição, se for guardada
                // Esvazia já a lista (evita registar a mesma refeição duas vezes)
                CurrentMeal.clear();
                CurrentMeal.setPendingTemplate(meal);
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                Toast.makeText(AddMealActivity.this, String.format(Locale.getDefault(),
                        "Refeição registada: %.0f kcal", meal.getKcal()), Toast.LENGTH_SHORT).show();
                refresh();
                showSaveDialog();
            }

            @Override
            public void onError(Exception e) {
                btnFinish.setEnabled(true);
                Toast.makeText(AddMealActivity.this,
                        "Não foi possível guardar a refeição", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Pop-up "guardar esta refeição?". Não fecha ao tocar fora nem com o botão Voltar.
    private void showSaveDialog() {
        final Meal pending = CurrentMeal.getPendingTemplate();
        if (pending == null || isFinishing() || isDestroyed()) {
            return;
        }
        if (saveDialog != null && saveDialog.isShowing()) {
            return;
        }

        View view = getLayoutInflater().inflate(R.layout.dialog_save_meal, null);
        EditText etName = view.findViewById(R.id.etMealName);
        TextView tvError = view.findViewById(R.id.tvMealNameError);

        saveDialog = new AlertDialog.Builder(this)
                .setTitle("Guardar refeição?")
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("Guardar", null) // o clique é definido abaixo, para não fechar com erros
                .setNegativeButton("Cancelar", (dialog, which) -> closeAfterMeal())
                .create();
        saveDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        saveDialog.show();

        Button btnSave = saveDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                tvError.setText("A refeição tem de ter nome.");
                tvError.setVisibility(View.VISIBLE);
                return;
            }
            tvError.setVisibility(View.GONE);
            btnSave.setEnabled(false);

            SavedMealService.saveTemplate(pending, name, new FoodService.Callback<String>() {
                @Override
                public void onSuccess(String savedName) {
                    // Dá o mesmo nome à refeição que acabou de ser registada no dia
                    MealService.renameMeal(pending.getDateKey(), pending.getId(), savedName,
                            new FoodService.Callback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    finishSaving("Guardada como \u00ab" + savedName + "\u00bb");
                                }

                                @Override
                                public void onError(Exception e) {
                                    finishSaving("Guardada como \u00ab" + savedName
                                            + "\u00bb, mas a refeição do dia ficou sem nome");
                                }
                            });
                }

                @Override
                public void onError(Exception e) {
                    btnSave.setEnabled(true);
                    if (e instanceof SavedMealService.DuplicateNameException) {
                        tvError.setText("Já existe uma refeição guardada com este nome.");
                    } else {
                        tvError.setText("Não foi possível guardar. Tenta outra vez.");
                    }
                    tvError.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    // Fecha o pop-up e o ecrã depois de guardar
    private void finishSaving(String message) {
        CurrentMeal.clear();
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        if (isFinishing() || isDestroyed()) {
            return;
        }
        if (saveDialog != null) {
            saveDialog.dismiss();
        }
        finish();
    }

    private void closeAfterMeal() {
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
                                    + "Caso este erro persista, pode inserir o alimento manualmente.");
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