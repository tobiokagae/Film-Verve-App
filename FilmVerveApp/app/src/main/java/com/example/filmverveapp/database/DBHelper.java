package com.example.filmverveapp.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.filmverveapp.model.User;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "user.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_USER = "user";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    private static final String TABLE_WATCHLIST = "watchlist";
    private static final String COLUMN_WATCHLIST_ID = "id";
    public static final String COLUMN_MOVIE_ID = "movie_id";
    private static final String COLUMN_USER_ID = "user_id";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USER_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USER + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_USERNAME + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USER_TABLE);

        String CREATE_WATCHLIST_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_WATCHLIST + "("
                + COLUMN_WATCHLIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MOVIE_ID + " INTEGER,"
                + COLUMN_USER_ID + " INTEGER" + ")";
        db.execSQL(CREATE_WATCHLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    public boolean addUser(String name, String username, String password) {
        if (isUsernameExists(username)) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        long result = db.insert(TABLE_USER, null, values);
        db.close();
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = { COLUMN_ID };
        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = { username, password };
        Cursor cursor = db.query(TABLE_USER, columns, selection, selectionArgs,
                null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count > 0;
    }

    public boolean deleteUser(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_USER, COLUMN_USERNAME + " = ?", new String[]{username});
        db.close();
        return result > 0;
    }

    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = { COLUMN_ID };
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = { username };
        Cursor cursor = db.query(TABLE_USER, columns, selection, selectionArgs,
                null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count > 0;
    }

    public User getUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = { COLUMN_NAME, COLUMN_USERNAME };
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = { username };
        Cursor cursor = db.query(TABLE_USER, columns, selection, selectionArgs,
                null, null, null);
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
            @SuppressLint("Range") String userUsername = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
            user = new User(name, userUsername);
        }
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return user;
    }

    @SuppressLint("Range")
    public int getUserIdByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        int userId = -1; // Inisialisasi user_id dengan nilai default -1

        // Query untuk mencari user_id berdasarkan username
        String selectQuery = "SELECT " + COLUMN_ID + " FROM " + TABLE_USER +
                " WHERE " + COLUMN_USERNAME + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{username});

        // Jika cursor tidak kosong, ambil user_id
        if (cursor != null && cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
            cursor.close();
        }

        return userId;
    }



    public Cursor getWatchlistData(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_WATCHLIST,
                null,
                COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null
        );
    }

    public boolean isMovieInWatchlist(int userId, int movieId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean result = false;

        try {
            // Query untuk memeriksa apakah film ada di watchlist pengguna
            String query = "SELECT * FROM " + TABLE_WATCHLIST +
                    " WHERE " + COLUMN_USER_ID + " = ?" +
                    " AND " + COLUMN_MOVIE_ID + " = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(movieId)});

            // Jika cursor memiliki data, maka film ada di watchlist
            if (cursor != null && cursor.getCount() > 0) {
                result = true;
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error while checking if movie is in watchlist: " + e.getMessage());
        } finally {
            // Menutup cursor
            if (cursor != null) {
                cursor.close();
            }
        }

        return result;
    }

    public boolean addMovieToWatchlist(int userId, int movieId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_MOVIE_ID, movieId);
        long result = db.insert(TABLE_WATCHLIST, null, values);
        db.close();
        return result != -1;
    }


    public boolean deleteMovieFromWatchlist(int userId, int movieId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_USER_ID + " = ?" + " AND " + COLUMN_MOVIE_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId), String.valueOf(movieId)};
        int deletedRows = db.delete(TABLE_WATCHLIST, selection, selectionArgs);
        db.close();
        return deletedRows > 0;
    }

    public static String getColumnMovieId() {
        return COLUMN_MOVIE_ID;
    }


}