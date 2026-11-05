package com.example.nutriscan.json;

import com.example.nutriscan.dto.ProductDTO;

import org.json.JSONObject;

public class JsonHandler {

    public static ProductDTO deSerializeJson2ProductDTO(String resp) {
        try {
            JSONObject root = new JSONObject(resp);
            if (root.optInt("status", 0) != 1) {
                return null; // product not found
            }
            JSONObject product = root.getJSONObject("product");

            ProductDTO dto = new ProductDTO();
            dto.setBarcode(root.optString("code", ""));
            dto.setName(product.optString("product_name", ""));
            dto.setBrand(product.optString("brands", ""));

            JSONObject nutriments = product.optJSONObject("nutriments");
            if (nutriments != null) {
                dto.setKcal(getDouble(nutriments, "energy-kcal_100g"));
                dto.setProteins(getDouble(nutriments, "proteins_100g"));
                dto.setCarbohydrates(getDouble(nutriments, "carbohydrates_100g"));
                dto.setFat(getDouble(nutriments, "fat_100g"));
            }
            return dto;
        } catch (Exception e) {
            // if problem, return null
            return null;
        }
    }

    private static Double getDouble(JSONObject obj, String key) {
        double value = obj.optDouble(key, Double.NaN);
        return Double.isNaN(value) ? null : value;
    }
}