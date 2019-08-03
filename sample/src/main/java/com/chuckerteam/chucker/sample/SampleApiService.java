/*
 * Copyright (C) 2017 Jeff Gilfelt.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chuckerteam.chucker.sample;

import io.github.wax911.library.annotation.GraphQuery;
import io.github.wax911.library.model.request.QueryContainerBuilder;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

class SampleApiService {

    static HttpbinApi getInstance(OkHttpClient client) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://httpbin.org")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        return retrofit.create(HttpbinApi.class);
    }

    static GraphQLSWApi getGraphQLInstance(OkHttpClient client) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://graphql.org")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        return retrofit.create(GraphQLSWApi.class);
    }

    static class Data {
        final String thing;

        Data(String thing) {
            this.thing = thing;
        }
    }

    interface HttpbinApi {
        @GET("/get")
        Call<Void> get();

        @POST("/post")
        Call<Void> post(@Body Data body);

        @PATCH("/patch")
        Call<Void> patch(@Body Data body);

        @PUT("/put")
        Call<Void> put(@Body Data body);

        @DELETE("/delete")
        Call<Void> delete();

        @GET("/status/{code}")
        Call<Void> status(@Path("code") int code);

        @GET("/stream/{lines}")
        Call<Void> stream(@Path("lines") int lines);

        @GET("/stream-bytes/{bytes}")
        Call<Void> streamBytes(@Path("bytes") int bytes);

        @GET("/delay/{seconds}")
        Call<Void> delay(@Path("seconds") int seconds);

        @GET("/bearer")
        Call<Void> bearer(@Header("Authorization") String token);

        @GET("/redirect-to")
        Call<Void> redirectTo(@Query("url") String url);

        @GET("/redirect/{times}")
        Call<Void> redirect(@Path("times") int times);

        @GET("/relative-redirect/{times}")
        Call<Void> redirectRelative(@Path("times") int times);

        @GET("/absolute-redirect/{times}")
        Call<Void> redirectAbsolute(@Path("times") int times);

        @GET("/image")
        Call<Void> image(@Header("Accept") String accept);

        @GET("/gzip")
        Call<Void> gzip();

        @GET("/xml")
        Call<Void> xml();

        @GET("/encoding/utf8")
        Call<Void> utf8();

        @GET("/deflate")
        Call<Void> deflate();

        @GET("/cookies/set")
        Call<Void> cookieSet(@Query("k1") String value);

        @GET("/basic-auth/{user}/{passwd}")
        Call<Void> basicAuth(@Path("user") String user, @Path("passwd") String passwd);

        @GET("/drip")
        Call<Void> drip(@Query("numbytes") int bytes, @Query("duration") int seconds, @Query("delay") int delay, @Query("code") int code);

        @GET("/deny")
        Call<Void> deny();

        @GET("/cache")
        Call<Void> cache(@Header("If-Modified-Since") String ifModifiedSince);

        @GET("/cache/{seconds}")
        Call<Void> cache(@Path("seconds") int seconds);
    }

    interface GraphQLSWApi {
        @POST("/swapi-graphql")
        @GraphQuery("AllFilms")
        @Headers("Content-Type: application/json")
        Call<Void> getAllFilms(@Body QueryContainerBuilder request);

        @POST("/swapi-graphql")
        @GraphQuery("AllVehicles")
        @Headers("Content-Type: application/json")
        Call<Void> getAllVehicles(@Body QueryContainerBuilder request);

        @POST("/swapi-graphql")
        @GraphQuery("AllPeople")
        @Headers("Content-Type: application/json")
        Call<Void> getAllPeople(@Body QueryContainerBuilder request);

        @POST("/swapi-graphql")
        @GraphQuery("AllPlanets")
        @Headers("Content-Type: application/json")
        Call<Void> getAllPlanets(@Body QueryContainerBuilder request);

        @POST("/swapi-graphql")
        @GraphQuery("AllSpacies")
        @Headers("Content-Type: application/json")
        Call<Void> getAllSpacies(@Body QueryContainerBuilder request);

        @POST("/swapi-graphql")
        @GraphQuery("AllStarships")
        @Headers("Content-Type: application/json")
        Call<Void> getAllStarships(@Body QueryContainerBuilder request);
    }
}