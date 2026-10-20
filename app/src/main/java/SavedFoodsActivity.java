package com.example.nutriscan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nutriscan.adapter.FoodAdapter;
import com.example.nutriscan.model.Food;
import com.example.nutriscan.service.FoodService;

import java.util.ArrayList;
import java.util.List;

public class SavedFoodsActivity extends AppCompatActivity {

    private final List<Food> foods = new ArrayList<>();
    private FoodAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_saved_foods);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.savedFoods), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ListView listFoods = findViewById(R.id.listFoods);
        tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new FoodAdapter(this, R.layout.list_item_meal, foods);
        listFoods.setAdapter(adapter);
        listFoods.setEmptyView(tvEmpty);

        // Toque: escolher o alimento e indicar a quantidade
        listFoods.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, QuantityActivity.class);
            intent.putExtra(QuantityActivity.EXTRA_FOOD, foods.get(position));
            startActivity(intent);
        });

        // Toque longo: apagar
        listFoods.setOnItemLongClickListener((parent, view, position, id) -> {
            Food food = foods.get(position);
            new AlertDialog.Builder(this)
                    .setTitle("Apagar alimento?")
                    .setMessage(food.getName())
                    .setPositiveButton("Apagar", (dialog, which) -> deleteFood(food))
                    .setNegativeButton("Cancelar", null)
                    .show();
            return true;
        });

        loadFoods();
    }

    private void loadFoods() {
        FoodService.loadFoods(new FoodService.Callback<List<Food>>() {
            @Override
            public void onSuccess(List<Food> result) {
                foods.clear();
                foods.addAll(result);
                adapter.notifyDataSetChanged();
                tvEmpty.setText("Ainda não tens alimentos guardados.\n"
                        + "Adiciona um por scan ou manualmente e ele fica aqui.");
            }

            @Override
            public void onError(Exception e) {
                tvEmpty.setText("Erro ao carregar os alimentos.");
            }
        });
    }

    private void deleteFood(Food food) {
        FoodService.deleteFood(food.getId(), new FoodService.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                foods.remove(food);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(SavedFoodsActivity.this,
                        "Não foi possível apagar", Toast.LENGTH_SHORT).show();
            }
        });
    }
}