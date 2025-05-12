package com.example.todolist.network;

import java.util.List;

// Modèle pour la réponse JSON de l'API météo (OpenWeatherMap)
public class WeatherResponse {
    public Main main;                  // Température et données principales
    public List<Weather> weather;     // Liste des descriptions météo

    // Données de température (ex: temp en °C)
    public class Main {
        public double temp;
    }

    // Description de la météo (ex: clear sky, rain...)
    public class Weather {
        public String description;
    }
}
