package com.everis.ciabapp.viewmodel;

public class APIUtils {

    public static API getAPIService() {

        return RetrofitClient.getClient(API.BASE_URL).create(API.class);
    }
}
