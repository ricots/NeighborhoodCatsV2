package com.roberterrera.neighborhoodcats.models;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Rob on 5/4/16.
 */
public interface PetfinderService {
    @GET("shelter.find")
    Call<List<Shelter>> listShelters(
            @Query("format") String format,
            @Query("location") String zipcode,
            @Query("key") String key);
}
