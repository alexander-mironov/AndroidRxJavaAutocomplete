package ru.eightbps.rxjavaautocomplete.data;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

public enum RestClient {
    INSTANCE;

    private final String BASE_URL = "https://maps.googleapis.com/maps/api/place/";
    private final String API_KEY = "AIzaSyD5-tQ5alcqco18LzeLd2pb5ov15Gb4Ghs"; // feel free to use this key. It just map's key that should be private
    private final GooglePlacesClient googlePlacesClient;

    RestClient() {

        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request request = chain.request();
                HttpUrl url = request.url().newBuilder().addQueryParameter("key", API_KEY).build();
                request = request.newBuilder().url(url).build();
                return chain.proceed(request);
            }
        }).build();

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient)
                .baseUrl(BASE_URL)
                .build();

        googlePlacesClient = retrofit.create(GooglePlacesClient.class);
    }

    public GooglePlacesClient getGooglePlacesClient() {
        return googlePlacesClient;
    }
}
