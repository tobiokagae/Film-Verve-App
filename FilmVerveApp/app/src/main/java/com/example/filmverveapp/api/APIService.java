package com.example.filmverveapp.api;

import com.example.filmverveapp.model.Movie;
import com.example.filmverveapp.response.GenreResponse;
import com.example.filmverveapp.response.MovieResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIService {

    @GET("movie/popular")
    Call<MovieResponse> getPopular (
            @Query("api_key") String key
    );

    @GET("movie/{movie_id}")
    Call<Movie> getMovieDetails(
            @Path("movie_id") int movieId,
            @Query("api_key") String key
    );


    @GET("movie/now_playing")
    Call<MovieResponse> getNowPlaying (
            @Query("api_key") String key
    );

    @GET("movie/upcoming")
    Call<MovieResponse> getUpcoming (
            @Query("api_key") String key
    );

    @GET("discover/movie")
    Call<MovieResponse> getAllMovie (
            @Query("api_key") String key,
            @Query("page") int page
    );

    @GET("trending/movie/day")
    Call<MovieResponse> getTrendingMoviesPerDay(
            @Query("api_key") String apiKey
    );

    @GET("genre/movie/list")
    Call<GenreResponse> getGenres(
            @Query("api_key") String apiKey
    );

    @GET("search/movie")
    Call<MovieResponse> searchMovies (
            @Query("api_key") String key,
            @Query("query") String query,
            @Query("page") int page
    );
}


