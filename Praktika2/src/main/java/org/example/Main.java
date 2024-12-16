package org.example;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Main {
    private static final String API_KEY = "c8d4d79bc902460f0a0df1afcecf636e";
    private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/weather";

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        String city;
        boolean continueProgram = true;

        while (continueProgram) {
            System.out.print("Введите город (или \"Выход\" для завершения): ");
            city = scanner.nextLine();

            if (city.equalsIgnoreCase("Выход")) {
                continueProgram = false;
                continue;
            }

            String finalCity = city;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    String urlString = BASE_URL + "?q=" + finalCity + "&appid=" + API_KEY + "&units=metric&lang=ru";
                    URL url = new URL(urlString);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 404) {
                        System.out.println("Город не найден. Пожалуйста, попробуйте еще раз.");
                        return;
                    }

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    conn.disconnect();

                    JSONObject jsonResponse = new JSONObject(response.toString());

                    if (jsonResponse.has("main")) {
                        JSONObject main = jsonResponse.getJSONObject("main");
                        double temperature = main.getDouble("temp");
                        int humidity = main.getInt("humidity");
                        System.out.println("Погода в " + finalCity + ":");
                        System.out.println("Температура: " + temperature + "°C");
                        System.out.println("Влажность: " + humidity + "%");
                    } else {
                        System.out.println("Ошибка: Отсутствует блок 'main' в JSON ответе.");
                        return;
                    }

                    if (jsonResponse.has("wind")) {
                        JSONObject wind = jsonResponse.getJSONObject("wind");
                        double windSpeed = wind.getDouble("speed");
                        System.out.println("Скорость ветра: " + windSpeed + " м/с");
                    } else {
                        System.out.println("Ошибка: Отсутствует блок 'wind' в JSON ответе.");
                    }

                    if (jsonResponse.has("weather")) {
                        JSONObject weather = jsonResponse.getJSONArray("weather").getJSONObject(0);
                        String description = weather.getString("description");
                        System.out.println("Описание: " + description);
                    } else {
                        System.out.println("Ошибка: Отсутствует блок 'weather' в JSON ответе.");
                    }

                } catch (Exception e) {
                    System.out.println("Ошибка: " + e.getMessage());
                }
            });
            future.get();
        }
        scanner.close();
        System.out.println("Программа завершена.");
    }
}