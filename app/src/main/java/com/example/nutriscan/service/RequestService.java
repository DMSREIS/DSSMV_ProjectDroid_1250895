package com.example.nutriscan.service;

import com.example.nutriscan.dto.Mapper;
import com.example.nutriscan.dto.ProductDTO;
import com.example.nutriscan.helper.Utils;
import com.example.nutriscan.json.JsonHandler;
import com.example.nutriscan.model.Food;
import com.example.nutriscan.network.HttpOperation;

public class RequestService {

    public static Food getProduct(String barcode) {
        Food food = null;
        try {
            String url = Utils.URL_PRODUCT_PREFIX + barcode + Utils.URL_PRODUCT_SUFFIX;
            String jsonString = HttpOperation.get(url);
            ProductDTO dto = JsonHandler.deSerializeJson2ProductDTO(jsonString);
            food = Mapper.productDTO2food(dto);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return food;
    }
}