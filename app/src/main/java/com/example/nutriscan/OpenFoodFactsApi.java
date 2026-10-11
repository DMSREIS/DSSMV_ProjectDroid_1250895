package com.example.nutriscan;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OpenFoodFactsApi {

    @Headers("User-Agent: NutriScan/1.0 (github.com/DMSREIS)")
    @GET("api/v2/product/{barcode}.json")
    Call<ProductResponse> getProduct(
            @Path("barcode") String barcode,
            @Query("fields") String fields);
}