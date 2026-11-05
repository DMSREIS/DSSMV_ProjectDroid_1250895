package com.example.nutriscan.helper;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nutriscan.R;
import com.example.nutriscan.model.Achievement;

import java.util.List;

public class AchievementPopup {

    private static final long DURATION_MS = 3000;
    private static final String TAG = "achievement_popup";

    public static void show(Activity activity, List<Achievement> unlocked) {
        if (unlocked == null || unlocked.isEmpty()
                || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        ViewGroup content = activity.findViewById(android.R.id.content);
        View old = content.findViewWithTag(TAG);
        if (old != null) {
            content.removeView(old);
        }

        View card = LayoutInflater.from(activity)
                .inflate(R.layout.view_achievement_popup, content, false);
        card.setTag(TAG);

        TextView emoji = card.findViewById(R.id.popupEmoji);
        TextView title = card.findViewById(R.id.popupTitle);
        TextView name = card.findViewById(R.id.popupName);
        TextView desc = card.findViewById(R.id.popupDesc);

        if (unlocked.size() == 1) {
            Achievement a = unlocked.get(0);
            emoji.setText(a.getEmoji());
            title.setText("Conquista desbloqueada!");
            name.setText(a.getTitle());
            desc.setText(a.getDescription());
        } else {
            StringBuilder names = new StringBuilder();
            for (int i = 0; i < unlocked.size(); i++) {
                if (i > 0) {
                    names.append(", ");
                }
                names.append(unlocked.get(i).getTitle());
            }
            emoji.setText("🏆");
            title.setText(unlocked.size() + " novas conquistas!");
            name.setText(names.toString());
            desc.setText("Vê todas no teu perfil");
        }

        // Fica abaixo da barra de estado do telemóvel
        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(content);
        int statusBar = insets == null
                ? 0 : insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
        float density = activity.getResources().getDisplayMetrics().density;
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) card.getLayoutParams();
        lp.topMargin = statusBar + (int) (12 * density);
        card.setLayoutParams(lp);

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable dismiss = () -> fadeOut(content, card);

        // Com o dedo no cartão o tempo pára; ao largar, recomeça
        card.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    handler.removeCallbacks(dismiss);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    handler.postDelayed(dismiss, DURATION_MS);
                    break;
                default:
                    break;
            }
            return true;
        });

        // X: fecha logo
        card.findViewById(R.id.popupClose).setOnClickListener(v -> {
            handler.removeCallbacks(dismiss);
            fadeOut(content, card);
        });

        card.setAlpha(0f);
        content.addView(card);
        card.animate().alpha(1f).setDuration(250).start();
        handler.postDelayed(dismiss, DURATION_MS);
    }

    private static void fadeOut(ViewGroup content, View card) {
        card.animate().alpha(0f).setDuration(300)
                .withEndAction(() -> content.removeView(card)).start();
    }
}