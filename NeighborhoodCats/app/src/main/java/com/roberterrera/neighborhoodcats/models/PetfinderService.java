package com.roberterrera.neighborhoodcats.models;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Rob on 5/4/16.
 */
public interface PetfinderService {
    @GET("shelter.find?key=e8736f4c0a4c61832d001b9d357055f4")
    Call<List<Shelter>> listShelters(
            @Query("location") int zipcode);
}
