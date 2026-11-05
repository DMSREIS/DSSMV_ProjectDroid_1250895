package com.example.nutriscan.service;

import android.content.Context;
import android.net.Uri;

import com.example.nutriscan.helper.ImageHelper;
import com.example.nutriscan.helper.MainThread;
import com.example.nutriscan.network.FirebaseRest;
import com.example.nutriscan.network.HttpResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ProfileService {

    // GET users/{uid}/profile/photo  (devolve o texto Base64, ou null se não houver foto)
    public static void loadPhoto(FoodService.Callback<String> callback) {
        new Thread(() -> {
            try {
                HttpResponse response = FirebaseRest.get("profile/photo");
                String photo = parsePhoto(response.getBody());
                MainThread.post(() -> callback.onSuccess(photo));
            } catch (Exception e) {
                MainThread.post(() -> callback.onError(e));
            }
        }).start();
    }

    // PUT users/{uid}/profile/photo  (substitui a foto anterior)
    public static void savePhoto(Context context, Uri uri, FoodService.Callback<String> callback) {
        final Context appContext = context.getApplicationContext();
        new Thread(() -> {
            try {
                String base64 = ImageHelper.uriToBase64(appContext, uri);
                // O corpo de um PUT com texto tem de ser um JSON válido: o texto entre aspas
                FirebaseRest.put("profile/photo", JSONObject.quote(base64));
                MainThread.post(() -> callback.onSuccess(base64));
            } catch (Exception e) {
                MainThread.post(() -> callback.onError(e));
            }
        }).start();
    }

    private static String parsePhoto(String body) {
        if (body == null || body.trim().isEmpty() || body.trim().equals("null")) {
            return null;
        }
        try {
            Object value = new JSONTokener(body).nextValue();
            return value instanceof String ? (String) value : null;
        } catch (JSONException e) {
            return null;
        }
    }
}