package com.roberterrera.neighborhoodcats.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.roberterrera.neighborhoodcats.models.petfinderclasses.Shelter;

import java.io.StringReader;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Rob on 5/4/16.
 */
public interface PetfinderAPI {
    @GET("/shelter.find")
    Call<Shelter> loadShelters(
            @Query("format") String format,
            @Query("location") String location,
            @Query("key") String key);

    class Factory{
        // If the service hasn't started, create it. Otherwise, call on the existing service.
        private static PetfinderAPI service;
        public static PetfinderAPI getInstance(){
            if (service == null){

                Gson gson = new GsonBuilder()
                        .setLenient()
                        .create();

                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://api.petfinder.com")
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();

//                JsonReader reader = new JsonReader(new StringReader(String.valueOf(retrofit)));
//                reader.setLenient(true);

                service = retrofit.create(PetfinderAPI.class);
                return service;
            } else {
                return service;
            }
        }
    }
}
