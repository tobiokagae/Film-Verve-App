package com.example.filmverveapp.response;

import com.example.filmverveapp.model.Movie;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MovieResponse {

    @SerializedName("results")
    private List<Movie> movieList;


    @SerializedName("total_pages")
    private int totalPages;

    public List<Movie> getMovieList() {
        return movieList;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
