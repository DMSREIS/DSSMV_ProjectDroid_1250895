package com.example.nutriscan.service;

import com.example.nutriscan.model.Food;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FoodService {

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    // users/{uid}/foods
    private static CollectionReference foodsCollection() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return null;
        }
        return FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid()).collection("foods");
    }

    public static void saveFood(Food food) {
        CollectionReference col = foodsCollection();
        if (col == null) {
            return;
        }
        String id = food.getId();
        if (id == null || id.isEmpty()) {
            String barcode = food.getBarcode();
            id = (barcode != null && !barcode.isEmpty()) ? barcode : col.document().getId();
            food.setId(id);
        }
        col.document(id).set(food);
    }

    public static void loadFoods(Callback<List<Food>> callback) {
        CollectionReference col = foodsCollection();
        if (col == null) {
            callback.onError(new IllegalStateException("Sem sessão iniciada"));
            return;
        }
        col.orderBy("name").get()
                .addOnSuccessListener(snapshot -> {
                    List<Food> foods = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Food food = doc.toObject(Food.class);
                        if (food != null) {
                            food.setId(doc.getId());
                            foods.add(food);
                        }
                    }
                    callback.onSuccess(foods);
                })
                .addOnFailureListener(callback::onError);
    }

    public static void deleteFood(String id, Callback<Void> callback) {
        CollectionReference col = foodsCollection();
        if (col == null || id == null) {
            callback.onError(new IllegalStateException("Alimento inválido"));
            return;
        }
        col.document(id).delete()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }
}