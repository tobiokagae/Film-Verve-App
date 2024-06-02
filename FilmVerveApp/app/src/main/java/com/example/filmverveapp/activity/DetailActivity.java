package com.example.filmverveapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.filmverveapp.R;
import com.example.filmverveapp.database.DBHelper;
import com.example.filmverveapp.fragment.HomeFragment;
import com.example.filmverveapp.model.Genre;
import com.example.filmverveapp.model.Movie;
import com.example.filmverveapp.api.APIConfig;
import com.example.filmverveapp.api.APIService;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    private ImageView ivPoster, ivBackdrop, ivBack;
    private TextView tvTitle, tvSinopsis, tvAge, tvRating, tvRelease, tvGenre, tvLanguage;
    private Button btnAddToWatchlist, btnDeleteToWatchlist;
    private DBHelper dbHelper;

    private String key = "e4a25dbab6f3e22322f148efa2b77218";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT, () -> navigateToMainActivity());
        } else {
            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    navigateToMainActivity();
                }
            });
        }

        ivPoster = findViewById(R.id.iv_poster);
        ivBackdrop = findViewById(R.id.iv_backdrop);
        tvTitle = findViewById(R.id.tv_title);
        tvSinopsis = findViewById(R.id.tv_sinopsis);
        tvAge = findViewById(R.id.tv_age);
        tvRating = findViewById(R.id.tv_rating);
        tvRelease = findViewById(R.id.tv_release);
        tvGenre = findViewById(R.id.tv_genre);
        tvLanguage = findViewById(R.id.tv_language);
        btnAddToWatchlist = findViewById(R.id.btn_add_to_watchlist);
        btnDeleteToWatchlist = findViewById(R.id.btn_delete_to_watchlist);
        ivBack = findViewById(R.id.iv_back);

        Intent intent = getIntent();
        dbHelper = new DBHelper(this);

        if (!isNetworkAvailable()) {
            showNetworkAlertDialog();
            return;
        }

        ivBack.setOnClickListener(v -> {
            Intent intent1 = new Intent(DetailActivity.this, MainActivity.class);
            intent1.putExtra("navigateTo", "HomeFragment");
            startActivity(intent1);
            finish();
        });



        btnDeleteToWatchlist.setOnClickListener(v -> {
            int userId = intent.getIntExtra("user_id", -1);
            int movieId = intent.getIntExtra("movie_id", -1);

            if (userId != -1 && movieId != -1) {
                boolean success = dbHelper.deleteMovieFromWatchlist(userId, movieId);
                if (success) {
                    Toast.makeText(DetailActivity.this, "Movie removed from watchlist", Toast.LENGTH_SHORT).show();
                    btnDeleteToWatchlist.setVisibility(View.GONE);
                    btnAddToWatchlist.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(DetailActivity.this, "Failed to remove movie from watchlist", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnAddToWatchlist.setOnClickListener(v -> {
            int userId = intent.getIntExtra("user_id", -1);

            if (userId != -1) {
                int movieId = intent.getIntExtra("movie_id", -1);
                if (movieId != -1) {
                    boolean success = dbHelper.addMovieToWatchlist(userId, movieId);
                    if (success) {
                        Toast.makeText(this, "Movie added to watchlist", Toast.LENGTH_SHORT).show();
                        btnAddToWatchlist.setVisibility(View.GONE);
                        btnDeleteToWatchlist.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(this, "Failed to add movie to watchlist", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        int movieId = intent.getIntExtra("movie_id", -1);
        if (movieId != -1) {
            fetchMovieDetails(movieId);
        }

        SharedPreferences sharedPreferences = getSharedPreferences("data_pref", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);
        Boolean isLogged = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLogged) {
            btnAddToWatchlist.setVisibility(View.VISIBLE);
            int userId = 0;

            if (userId != -1) {
                userId = dbHelper.getUserIdByUsername(username);
                boolean isInWatchlist = dbHelper.isMovieInWatchlist(userId, movieId);

                if (isInWatchlist) {
                    btnAddToWatchlist.setVisibility(View.GONE);
                    btnDeleteToWatchlist.setVisibility(View.VISIBLE);
                }
            } else {
                showLoginAlertDialog();
            }
        }

        handleBackPressed();

    }


    private void fetchMovieDetails(int movieId) {
        StringBuilder genresBuilder = new StringBuilder();

        APIService apiService = APIConfig.getAPIService();
        apiService.getMovieDetails(movieId, key).enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(@NonNull Call<Movie> call, @NonNull Response<Movie> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Movie movie = response.body();
                    for (Genre genre : movie.getGenres()) {
                        genresBuilder.append(genre.getName()).append(", ");
                    }
                    if (genresBuilder.length() > 0) {
                        genresBuilder.setLength(genresBuilder.length() - 2);
                    }
                    tvGenre.setText(genresBuilder.toString());
                    tvTitle.setText(movie.getTitle());
                    tvAge.setText(movie.getAge() ? "18+" : "PG");
                    Glide.with(DetailActivity.this).load(movie.getPoster()).into(ivPoster);
                    Glide.with(DetailActivity.this).load(movie.getBackdrop()).into(ivBackdrop);
                    tvSinopsis.setText(movie.getSinopsis());

                    String formattedRating = String.format(Locale.US, "%.1f", movie.getRating());
                    tvRating.setText(formattedRating);

                    tvRelease.setText(movie.getRelease_date());
                    tvLanguage.setText(movie.getOriginal_language());
                } else {
                    Log.e("DetailActivity", "Failed to fetch movie genre details");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Movie> call, @NonNull Throwable t) {
                Log.e("DetailActivity", "Genre network error: " + t.getMessage());
            }
        });
    }

    private void handleBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT, () -> navigateToMainActivity());
        } else {
            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    navigateToMainActivity();
                }
            });
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(DetailActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("fragmentToLoad", "home");
        startActivity(intent);
        finish();
    }

    private void showLoginAlertDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Login Required")
                .setMessage("You need to log in to view the movie details.")
                .setPositiveButton("Ok", null)
                .show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showNetworkAlertDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Network Connection")
                .setMessage("You need an active network connection to view the app. Please check your network settings.")
                .setPositiveButton("Settings", (dialog, which) -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
