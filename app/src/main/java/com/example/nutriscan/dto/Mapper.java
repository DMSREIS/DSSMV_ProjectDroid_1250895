package com.example.nutriscan.dto;

import com.example.nutriscan.model.Food;

public class Mapper {

    public static Food productDTO2food(ProductDTO dto) {
        if (dto == null) {
            return null;
        }
        return new Food(dto.getBarcode(), dto.getName(), dto.getBrand(),
                dto.getKcal(), dto.getProteins(), dto.getCarbohydrates(), dto.getFat());
    }
}