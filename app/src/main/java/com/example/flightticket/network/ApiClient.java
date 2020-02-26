package com.example.flightticket.network;

import com.example.flightticket.app.Const;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static int REQUEST_TIMEOUT = 60;
    private static Retrofit retrofit = null;
    private static OkHttpClient okHttpClient = null;

    public static Retrofit getClient() {
        if (null == okHttpClient) {
            initOkHttpClient();
        }

        if (null == retrofit) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Const.BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();

        }
        return retrofit;
    }

    static void initOkHttpClient() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS);

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(httpLoggingInterceptor);

        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder()
                        .addHeader("Accept", "applicatio/json")
                        .addHeader("Request-Type", "Android")
                        .addHeader("Content-Type", "application/json");
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });
        okHttpClient = httpClient.build();

    }


    public static void resetApiClient() {
        retrofit = null;
        okHttpClient = null;
    }
}
