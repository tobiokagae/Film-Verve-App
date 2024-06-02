package com.example.filmverveapp.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import com.example.filmverveapp.R;
import com.example.filmverveapp.adapter.MovieAdapter;
import com.example.filmverveapp.adapter.TrendingAdapter;
import com.example.filmverveapp.database.DBHelper;
import com.example.filmverveapp.model.Movie;
import com.example.filmverveapp.api.APIConfig;
import com.example.filmverveapp.api.APIService;
import com.example.filmverveapp.model.User;
import com.example.filmverveapp.response.MovieResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView rvTrending, rvPopular, rvNowPlaying, rvUpcoming;
    private ProgressBar progressBar;
    private static final String key = "456d859f86b1222d4eaa386b66f700ba";
    private List<Movie> trendingMovieList = new ArrayList<>();
    private List<Movie> popularMovieList = new ArrayList<>();
    private List<Movie> nowPlayingMovieList = new ArrayList<>();
    private List<Movie> upcomingMovieList = new ArrayList<>();
    private TrendingAdapter trendingAdapter;
    private MovieAdapter popularAdapter, nowPlayingAdapter, upcomingAdapter;
    private TextView tvTrending, tvPopular, tvNowPlaying, tvUpcoming, tvName, tvHi;
    private String username;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireActivity().getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT, () -> requireActivity().finishAndRemoveTask());
        } else {
            requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    requireActivity().finishAndRemoveTask();
                }
            });
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvTrending = view.findViewById(R.id.rv_trending);
        rvPopular = view.findViewById(R.id.rv_popular);
        rvNowPlaying = view.findViewById(R.id.rv_nowplaying);
        rvUpcoming = view.findViewById(R.id.rv_upcoming);
        progressBar = view.findViewById(R.id.progressBar);

        rvTrending.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvPopular.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvNowPlaying.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvUpcoming.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        trendingAdapter = new TrendingAdapter(getContext(), trendingMovieList);
        popularAdapter = new MovieAdapter(getContext(), popularMovieList);
        nowPlayingAdapter = new MovieAdapter(getContext(), nowPlayingMovieList);
        upcomingAdapter = new MovieAdapter(getContext(), upcomingMovieList);

        rvTrending.setAdapter(trendingAdapter);
        rvPopular.setAdapter(popularAdapter);
        rvNowPlaying.setAdapter(nowPlayingAdapter);
        rvUpcoming.setAdapter(upcomingAdapter);

        tvTrending = view.findViewById(R.id.tv_trending);
        tvPopular = view.findViewById(R.id.tv_popular);
        tvNowPlaying = view.findViewById(R.id.tv_nowplaying);
        tvUpcoming = view.findViewById(R.id.tv_upcoming);
        tvName = view.findViewById(R.id.tv_name);
        tvHi = view.findViewById(R.id.tv_hi);

        if (!isNetworkAvailable()) {
            showNetworkAlertDialog();
            return;
        }

        DBHelper dbHelper = new DBHelper(getContext());
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("data_pref", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("username", "");
        User user = dbHelper.getUser(username);
        if (user != null) {
            tvName.setText(user.getName());
        } else {
            tvName.setVisibility(View.GONE);
            tvHi.setVisibility(View.GONE);
        }

        loadDataWithCountDownLatch();
    }

    private void loadDataWithCountDownLatch() {
        final int count = 4;
        final CountDownLatch latch = new CountDownLatch(count);

        progressBar.setVisibility(View.VISIBLE);
        tvTrending.setVisibility(View.GONE);
        tvPopular.setVisibility(View.GONE);
        tvNowPlaying.setVisibility(View.GONE);
        tvUpcoming.setVisibility(View.GONE);
        rvTrending.setVisibility(View.GONE);
        rvPopular.setVisibility(View.GONE);
        rvUpcoming.setVisibility(View.GONE);
        rvNowPlaying.setVisibility(View.GONE);

        APIService apiService = APIConfig.getAPIService();

        apiService.getTrendingMoviesPerDay(key).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getMovieList();
                    if (movies != null) {
                        trendingMovieList.clear();
                        trendingMovieList.addAll(movies);
                        trendingAdapter.notifyItemRangeInserted(0, movies.size());
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load trending movies", Toast.LENGTH_SHORT).show();
                }
                latch.countDown();
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                latch.countDown();
            }
        });

        apiService.getPopular(key).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getMovieList();
                    if (movies != null) {
                        popularMovieList.clear();
                        popularMovieList.addAll(movies);
                        popularAdapter.notifyItemRangeInserted(0, movies.size());
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load popular movies", Toast.LENGTH_SHORT).show();
                }
                latch.countDown();
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                latch.countDown();
            }
        });

        apiService.getNowPlaying(key).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getMovieList();
                    if (movies != null) {
                        nowPlayingMovieList.clear();
                        nowPlayingMovieList.addAll(movies);
                        nowPlayingAdapter.notifyItemRangeInserted(0, movies.size());
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load now playing movies", Toast.LENGTH_SHORT).show();
                }
                latch.countDown();
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                latch.countDown();
            }
        });

        apiService.getUpcoming(key).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getMovieList();
                    if (movies != null) {
                        upcomingMovieList.clear();
                        upcomingMovieList.addAll(movies);
                        upcomingAdapter.notifyItemRangeInserted(0, movies.size());
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load upcoming movies", Toast.LENGTH_SHORT).show();
                }
                latch.countDown();
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                latch.countDown();
            }
        });

        new Thread(() -> {
            try {
                latch.await();
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvTrending.setVisibility(View.VISIBLE);
                    rvTrending.setVisibility(View.VISIBLE);
                    tvPopular.setVisibility(View.VISIBLE);
                    rvPopular.setVisibility(View.VISIBLE);
                    tvNowPlaying.setVisibility(View.VISIBLE);
                    rvNowPlaying.setVisibility(View.VISIBLE);
                    tvUpcoming.setVisibility(View.VISIBLE);
                    rvUpcoming.setVisibility(View.VISIBLE);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
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
