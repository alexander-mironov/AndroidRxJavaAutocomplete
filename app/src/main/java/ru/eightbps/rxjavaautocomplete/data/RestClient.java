package ru.eightbps.rxjavaautocomplete.data;

import android.support.annotation.NonNull;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

public enum RestClient {
    INSTANCE;

    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/place/";
    private static final String API_KEY = "AIzaSyD5-tQ5alcqco18LzeLd2pb5ov15Gb4Ghs"; // feel free to use this key. It just map's key that should be private
    private final GooglePlacesClient googlePlacesClient;

    RestClient() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new GoogleApiInterceptor())
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .baseUrl(BASE_URL)
                .build();
        googlePlacesClient = retrofit.create(GooglePlacesClient.class);
    }

    public GooglePlacesClient getGooglePlacesClient() {
        return googlePlacesClient;
    }

    private static class GoogleApiInterceptor implements Interceptor {
        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();
            HttpUrl url = request.url().newBuilder().addQueryParameter("key", API_KEY).build();
            request = request.newBuilder().url(url).build();
            return chain.proceed(request);
        }
    }
}
