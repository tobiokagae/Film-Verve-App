package com.example.filmverveapp.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filmverveapp.R;
import com.example.filmverveapp.adapter.MovieAdapter;
import com.example.filmverveapp.model.Movie;
import com.example.filmverveapp.api.APIService;
import com.example.filmverveapp.api.APIConfig;
import com.example.filmverveapp.response.MovieResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private RecyclerView rvSearch;
    private MovieAdapter movieAdapter;
    private Boolean isLoading = false;
    private Boolean isLastPage = false;
    private List<Movie> movieList = new ArrayList<>();
    private List<Movie> filteredMovies = new ArrayList<>();
    private ProgressBar progressBar;
    private FrameLayout layoutNoData;
    private ExecutorService executorService;
    private SearchView searchView;
    private Handler handler = new Handler();
    private Runnable runnable;
    private static final String key = APIConfig.getKey();
    private APIService apiService;
    private int currentPage = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvSearch = view.findViewById(R.id.rv_search);
        progressBar = view.findViewById(R.id.progress_bar);
        layoutNoData = view.findViewById(R.id.layout_no_data);
        searchView = view.findViewById(R.id.search_view);
        apiService = APIConfig.getAPIService();
        executorService = Executors.newFixedThreadPool(5);

        movieAdapter = new MovieAdapter(getContext(), movieList);
        rvSearch.setAdapter(movieAdapter);
        rvSearch.setLayoutManager(new GridLayoutManager(getContext(), 3));

        if (!isNetworkAvailable()) {
            showNetworkAlertDialog();
            layoutNoData.setVisibility(View.VISIBLE);
            return;
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                handler.removeCallbacks(runnable);

                if (TextUtils.isEmpty(newText)) {
                    movieList.clear();
                    movieAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    rvSearch.setVisibility(View.GONE);
                    return true;
                }

                handler.postDelayed(runnable = () -> {
                    progressBar.setVisibility(View.VISIBLE);
                    layoutNoData.setVisibility(View.GONE);

                    movieList.clear();
                    currentPage = 1;
                    isLastPage = false;

                    fetchMoviesFromAPI(newText, currentPage);
                }, 1000);

                return true;
            }
        });

        rvSearch.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == movieList.size() - 1) {
                    if (!isLoading && !isLastPage) {
                        currentPage++;
                        fetchMoviesFromAPI(searchView.getQuery().toString(), currentPage);
                    }
                }
            }
        });
    }

    private void fetchMoviesFromAPI(final String query, final int page) {
        isLoading = true;
        apiService.searchMovies(key, query, page).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieResponse movieResponse = response.body();
                    List<Movie> movies = movieResponse.getMovieList();
                    if (movies != null) {
                        movieList.addAll(movies);
                        filterAndDisplayMovies(query);
                    }
                    isLoading = false;
                    if (page >= movieResponse.getTotalPages()) {
                        isLastPage = true;
                    }
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void filterAndDisplayMovies(String query) {
        filteredMovies.clear();
        if (TextUtils.isEmpty(query)) {
            filteredMovies.addAll(movieList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(lowerCaseQuery), Pattern.CASE_INSENSITIVE);
            for (Movie movie : movieList) {
                if (pattern.matcher(movie.getTitle().toLowerCase()).find()) {
                    filteredMovies.add(movie);
                }
            }
        }
        movieAdapter.notifyDataSetChanged();

        if (filteredMovies.isEmpty()) {
            layoutNoData.setVisibility(View.VISIBLE);
        } else {
            layoutNoData.setVisibility(View.GONE);
            rvSearch.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showNetworkAlertDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("No Network Connection")
                .setMessage("You need an active network connection to view the app. Please check your network settings.")
                .setPositiveButton("Settings", (dialog, which) -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
