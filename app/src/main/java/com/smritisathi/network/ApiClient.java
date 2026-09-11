package com.smritisathi.network;

import com.smritisathi.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton network client managing Retrofit configuration and API service instances.
 * Initialized with BuildConfig.BACKEND_BASE_URL with support for dynamic URL override.
 */
public class ApiClient {

    private static ApiClient instance;
    private Retrofit retrofit;
    private ScoringApiService scoringApiService;
    private String currentBaseUrl;

    private ApiClient() {
        this.currentBaseUrl = BuildConfig.BACKEND_BASE_URL;
        buildRetrofitClient(currentBaseUrl);
    }

    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    private void buildRetrofitClient(String baseUrl) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build();

        // Ensure baseUrl ends with trailing slash for Retrofit
        String formattedUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";

        this.retrofit = new Retrofit.Builder()
                .baseUrl(formattedUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.scoringApiService = retrofit.create(ScoringApiService.class);
    }

    public ScoringApiService getScoringApiService() {
        return scoringApiService;
    }

    /**
     * Allows dynamic switching between local emulator (http://10.0.2.2:8080/)
     * and live cloud deployment (e.g. Render https://your-app.onrender.com/).
     */
    public synchronized void setBaseUrl(String newBaseUrl) {
        if (newBaseUrl != null && !newBaseUrl.trim().isEmpty() && !newBaseUrl.equals(currentBaseUrl)) {
            this.currentBaseUrl = newBaseUrl;
            buildRetrofitClient(newBaseUrl);
        }
    }

    public String getCurrentBaseUrl() {
        return currentBaseUrl;
    }
}
