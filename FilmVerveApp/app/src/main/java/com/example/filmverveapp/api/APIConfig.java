package com.example.filmverveapp.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIConfig {

    private static String BASE_URL = "https://api.themoviedb.org/3/";
    private static String key = "456d859f86b1222d4eaa386b66f700ba";

    public static APIService getAPIService(){
        Retrofit retrofit = new Retrofit
                .Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory
                        .create())
                .build();
        return retrofit.create(APIService.class);
    }

    public static String getKey(){ return key; }
}

