package com.example.movies.db;

import android.provider.BaseColumns;

public class DatabaseContract{
    static final String TABLE_MOVIE = "Movie";
    static final String TABLE_TVSHOW = "TvShow";

    static final class TableColumns implements BaseColumns{
        static final String OBJECT_ID = "title";
        static final String TITLE = "title";
        static final String DESCRIPTION = "description";
        static final String POPULAR = "popular";
        static final String POSTER = "poster";
        static final String COVER = "cover";
        static final String RELEASE_YEAR = "release_year";
    }
}