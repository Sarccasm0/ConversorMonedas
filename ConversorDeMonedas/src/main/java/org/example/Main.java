package org.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Main {


    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("FIXE_API_KEY");
    private static final String BASE_URL = dotenv.get("FIXE_API_BASE_URL");


    public static double getExchangeRate(String fromCurrency, String toCurrency) {
        try {
            // Construct the URL
            String urlString = BASE_URL + "latest?access_key=" + API_KEY;
            URL url = new URL(urlString);

            // Open connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            // Check response code
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("HTTP Error: " + conn.getResponseCode());
            }

            // Read the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parse JSON response
            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();

            // Check for success
            if (!jsonResponse.get("success").getAsBoolean()) {
                System.out.println("Error: " + jsonResponse.get("error").getAsJsonObject().get("info").getAsString());
                return -1;
            }

            // Get exchange rates
            JsonObject rates = jsonResponse.getAsJsonObject("rates");

            // Validate currencies
            if (!rates.has(fromCurrency) || !rates.has(toCurrency)) {
                System.out.println("Error: One or both currencies not found in exchange rates.");
                return -1;
            }

            // Calculate exchange rate
            double fromRate = rates.get(fromCurrency).getAsDouble();
            double toRate = rates.get(toCurrency).getAsDouble();
            return toRate / fromRate;

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return -1;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("--Currency Converter--");

        // Get user input
        System.out.print("Enter the currency you want to convert from (e.g., USD): ");
        String fromCurrency = scanner.nextLine().toUpperCase();

        System.out.print("Enter the currency you want to convert to (e.g., EUR): ");
        String toCurrency = scanner.nextLine().toUpperCase();

        System.out.print("Enter the amount to convert: ");
        double amount = scanner.nextDouble();

        // Get exchange rate and convert
        double rate = getExchangeRate(fromCurrency, toCurrency);

        if (rate != -1) {
            double convertedAmount = amount * rate;
            System.out.printf("%.2f %s = %.2f %s%n", amount, fromCurrency, convertedAmount, toCurrency);
        } else {
            System.out.println("Failed to fetch the exchange rate.");
        }



        scanner.close();
    }
}
