package com.example.filmverveapp.model;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Movie {

    private String title;
    @SerializedName("poster_path")
    private String poster;
    @SerializedName("backdrop_path")
    private String backdrop;
    private String release_date;
    @SerializedName("overview")
    private String sinopsis;
    private int id;
    @SerializedName("vote_average")
    private double rating;
    @SerializedName("genre_ids")
    private List<Integer> genreIds;
    private List<Genre> genres;
    private String original_language;
    @SerializedName("adult")
    private boolean age;

    private static final String BASE_IMAGE_URL = "https://image.tmdb.org/t/p/w500";

    public Movie(String title, String poster, String backdrop, String release_date, String sinopsis, int id, double rating, List<Integer> genreIds, List<Genre> genres, String original_language, boolean age) {
        this.title = title;
        this.poster = poster;
        this.backdrop = backdrop;
        this.release_date = release_date;
        this.sinopsis = sinopsis;
        this.id = id;
        this.rating = rating;
        this.genreIds = genreIds;
        this.genres = genres;
        this.original_language = original_language;
        this.age = age;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPoster() {
        return BASE_IMAGE_URL + poster;
    }

    public String getBackdrop() {
        return BASE_IMAGE_URL + backdrop;
    }

    public String getRelease_date() {
        return release_date;
    }

    public String getSinopsis() {
        return sinopsis;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getRating() {
        return rating;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public String getOriginal_language() {
        return original_language;
    }

    public boolean getAge() {
        return age;
    }

}
