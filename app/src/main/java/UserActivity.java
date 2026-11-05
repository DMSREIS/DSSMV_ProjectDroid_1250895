package com.example.nutriscan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nutriscan.helper.AchievementPopup;
import com.example.nutriscan.helper.ImageHelper;
import com.example.nutriscan.model.Achievement;
import com.example.nutriscan.service.AchievementService;
import com.example.nutriscan.service.FoodService;
import com.example.nutriscan.service.ProfileService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserActivity extends AppCompatActivity {

    private ImageView ivAvatar;
    private ProgressBar progress;
    private Button btnAchievements;

    // Seletor de fotos do Android: não precisa de permissões
    private final ActivityResultLauncher<PickVisualMediaRequest> pickPhoto =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    savePhoto(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            goToLogin();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.user), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ivAvatar = findViewById(R.id.ivAvatar);
        progress = findViewById(R.id.progress);
        btnAchievements = findViewById(R.id.btnAchievements);
        ((TextView) findViewById(R.id.tvEmail)).setText(user.getEmail());

        // Tocar na foto abre o seletor
        ivAvatar.setOnClickListener(v -> pickPhoto.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        btnAchievements.setOnClickListener(v ->
                startActivity(new Intent(UserActivity.this, AchievementsActivity.class)));

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            goToLogin();
        });

        loadPhoto();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ivAvatar != null) {
            checkAchievements();
        }
    }

    // Atualiza o contador do botão e mostra o pop-up se houver conquistas novas
    private void checkAchievements() {
        AchievementService.refresh(new FoodService.Callback<AchievementService.Result>() {
            @Override
            public void onSuccess(AchievementService.Result result) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                btnAchievements.setText("🏆 Conquistas · " + result.getUnlockedCount()
                        + "/" + Achievement.values().length);
                AchievementPopup.show(UserActivity.this, result.newlyUnlocked);
            }

            @Override
            public void onError(Exception e) {
                // sem ligação: o botão fica como está
            }
        });
    }

    private void loadPhoto() {
        ProfileService.loadPhoto(new FoodService.Callback<String>() {
            @Override
            public void onSuccess(String base64) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                Bitmap bitmap = base64 == null ? null : ImageHelper.base64ToBitmap(base64);
                ivAvatar.setImageBitmap(bitmap); // sem foto: fica o círculo preto
            }

            @Override
            public void onError(Exception e) {
                // sem foto ou sem ligação: fica o círculo preto
            }
        });
    }

    private void savePhoto(Uri uri) {
        progress.setVisibility(View.VISIBLE);
        ivAvatar.setEnabled(false);
        ProfileService.savePhoto(this, uri, new FoodService.Callback<String>() {
            @Override
            public void onSuccess(String base64) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                progress.setVisibility(View.GONE);
                ivAvatar.setEnabled(true);
                ivAvatar.setImageBitmap(ImageHelper.base64ToBitmap(base64));
                checkAchievements(); // pode desbloquear "Com cara"
            }

            @Override
            public void onError(Exception e) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                progress.setVisibility(View.GONE);
                ivAvatar.setEnabled(true);
                Toast.makeText(UserActivity.this,
                        "Não foi possível guardar a foto", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}