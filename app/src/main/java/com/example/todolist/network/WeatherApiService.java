package com.example.todolist.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

// Interface Retrofit pour accéder à l'API météo OpenWeatherMap
public interface WeatherApiService {

    // Appel GET vers /weather avec paramètres de ville, unités et clé API
    @GET("weather")
    Call<WeatherResponse> getWeather(
            @Query("q") String city,        // Nom de la ville (ex: Rabat,MA)
            @Query("units") String units,   // Unités (metric pour °C)
            @Query("appid") String apiKey   // Clé API OpenWeatherMap
    );
}
