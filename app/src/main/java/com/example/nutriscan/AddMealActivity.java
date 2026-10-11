package com.example.nutriscan;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.content.Intent;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
public class AddMealActivity extends AppCompatActivity {

    private TextView tvResult;
    private OpenFoodFactsApi api;

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

        Button btnScan = findViewById(R.id.btnScan);
        Button btnManual = findViewById(R.id.btnManual);
        Button btnSaved = findViewById(R.id.btnSaved);
        tvResult = findViewById(R.id.tvResult);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://world.openfoodfacts.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(OpenFoodFactsApi.class);

        btnScan.setOnClickListener(v ->
                scanLauncher.launch(new Intent(this, ScanActivity.class)));
        btnManual.setOnClickListener(v ->
                Toast.makeText(this, "Manual: em breve", Toast.LENGTH_SHORT).show());
        btnSaved.setOnClickListener(v ->
                Toast.makeText(this, "Guardado: em breve", Toast.LENGTH_SHORT).show());
    }

    private void fetchProduct(String barcode) {
        tvResult.setText("A procurar...");

        api.getProduct(barcode, "product_name,brands,nutriments")
                .enqueue(new Callback<ProductResponse>() {
                    @Override
                    public void onResponse(Call<ProductResponse> call,
                                           Response<ProductResponse> response) {
                        ProductResponse body = response.body();
                        if (!response.isSuccessful() || body == null
                                || body.status != 1 || body.product == null) {
                            tvResult.setText("Produto não encontrado.");
                            return;
                        }

                        ProductResponse.Product p = body.product;
                        String text = "Nome: " + p.productName
                                + "\nMarca: " + p.brands;
                        if (p.nutriments != null) {
                            ProductResponse.Nutriments n = p.nutriments;
                            text += "\n\nPor 100 g:"
                                    + "\nCalorias: " + fmt(n.kcal) + " kcal"
                                    + "\nProteínas: " + fmt(n.proteins) + " g"
                                    + "\nHidratos: " + fmt(n.carbohydrates) + " g"
                                    + "\nGorduras: " + fmt(n.fat) + " g";
                        }
                        tvResult.setText(text);
                    }

                    @Override
                    public void onFailure(Call<ProductResponse> call, Throwable t) {
                        tvResult.setText("Erro de ligação: " + t.getMessage());
                    }
                });
    }

    private String fmt(Double value) {
        return value == null ? "-" : String.valueOf(value);
    }
}