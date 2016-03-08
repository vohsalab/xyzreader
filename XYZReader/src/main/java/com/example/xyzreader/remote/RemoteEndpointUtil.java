package com.example.xyzreader.remote;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RemoteEndpointUtil {
    private static final String TAG = "RemoteEndpointUtil";
    private static final String BASE_URL = "https://dl.dropboxusercontent.com";

    private RemoteEndpointUtil() {
    }

    public static BlockingQueue<List<RSSNewsItem>> fetchItems() {
        final BlockingQueue<List<RSSNewsItem>> blockingQueue = new ArrayBlockingQueue<List<RSSNewsItem>>(1);
        Retrofit client = getRetrofitClient();
        RSSService service  = client.create(RSSService.class);
        Call<List<RSSNewsItem>> call = service.getRSS();
        call.enqueue(new Callback<List<RSSNewsItem>>() {
            @Override
            public void onResponse(Call<List<RSSNewsItem>> call, retrofit2.Response<List<RSSNewsItem>> response) {
                List<RSSNewsItem> list = response.body();
                blockingQueue.add(list);
            }

            @Override
            public void onFailure(Call<List<RSSNewsItem>> call, Throwable t) {

            }
        });
        return blockingQueue;



    }

    @NonNull
    private static Retrofit getRetrofitClient() {
        OkHttpClient okClient = new OkHttpClient
                .Builder().addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response response = chain.proceed(chain.request());
                return response;
            }
        }).build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
