package com.example.nutriscan;

import com.google.gson.annotations.SerializedName;

public class ProductResponse {
    public int status;
    public Product product;

    public static class Product {
        @SerializedName("product_name")
        public String productName;
        public String brands;
        public Nutriments nutriments;
    }

    public static class Nutriments {
        @SerializedName("energy-kcal_100g")
        public Double kcal;
        @SerializedName("proteins_100g")
        public Double proteins;
        @SerializedName("carbohydrates_100g")
        public Double carbohydrates;
        @SerializedName("fat_100g")
        public Double fat;
    }
}