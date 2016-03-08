package com.example.xyzreader.remote;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by ibalashov on 3/7/2016.
 */
public interface RSSService {
    @GET("u/231329/xyzreader_data/data.json")
    Call<List<RSSNewsItem>> getRSS();
}
