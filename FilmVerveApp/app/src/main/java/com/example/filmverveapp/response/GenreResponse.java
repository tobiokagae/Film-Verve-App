package com.example.filmverveapp.response;

import com.example.filmverveapp.model.Genre;

import java.util.List;

public class GenreResponse {

    private List<Genre> genres;

    public GenreResponse(List<Genre> genres) {
        this.genres = genres;
    }

    public List<Genre> getGenres() {
        return genres;
    }

}
