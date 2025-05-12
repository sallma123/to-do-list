package com.example.todolist.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//Singleton fournissant une instance Retrofit configurée pour l'API météo.

public class WeatherClient {
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private static Retrofit retrofit = null;

    // Retourne une instance du service API Retrofit
    public static WeatherApiService getService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()) // Conversion JSON ↔ objets Java
                    .build();
        }
        return retrofit.create(WeatherApiService.class);
    }
}
