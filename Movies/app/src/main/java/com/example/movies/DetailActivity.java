package com.example.movies;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.movies.adapter.CastAdapter;
import com.example.movies.db.FavoriteMovieHelper;
import com.example.movies.db.FavoriteTvShowHelper;
import com.example.movies.model.Cast;
import com.example.movies.model.Movie;
import com.example.movies.model.TvShow;
import com.example.movies.viewmodel.FavoriteViewModel;
import com.example.movies.viewmodel.MainViewModel;

import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {

    private ImageView moviePoster, movieCover, layerHide, addFav;
    private TextView tv_title, tv_description, tv_popular, tv_genre, tv_addFav;
    private Movie movie;
    private TvShow tvShow;
    private ProgressBar pb_detail;
    private CastAdapter castAdapter;
    private ActionBar toolbar;
    private MainViewModel mainViewModel;
    public static final String EXTRA_MOVIE = "extra_movie";
    public static final String EXTRA_SHOW = "extra_show";
    private static final String API_KEY = "68eff651539ae197e48884a6d31d2059";
    private FavoriteMovieHelper movieHelper;
    private FavoriteTvShowHelper tvShowHelper;
    private FavoriteViewModel favoriteViewModel;
    private long IS_FAVORITE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        pb_detail = findViewById(R.id.pb_detail);
        layerHide = findViewById(R.id.layerHide);
        toolbar = getSupportActionBar();
        showLoading(true);
        moviePoster = findViewById(R.id.detail_poster);
        movieCover = findViewById(R.id.detail_cover);
        addFav = findViewById(R.id.img_fav);
        tv_title = findViewById(R.id.detail_title);
        tv_description = findViewById(R.id.detail_description);
        tv_popular = findViewById(R.id.detail_popular);
        tv_genre = findViewById(R.id.detail_genre);
        tv_addFav = findViewById(R.id.tv_addFav);

        RecyclerView rv_cast = findViewById(R.id.rv_cast);
        rv_cast.setHasFixedSize(true);
        castAdapter = new CastAdapter(this);
        castAdapter.notifyDataSetChanged();

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mainViewModel.getGenre().observe(this, getGenre);
        mainViewModel.getCast().observe(this, getCast);

        favoriteViewModel = ViewModelProviders.of(this).get(FavoriteViewModel.class);
        movieHelper = FavoriteMovieHelper.getInstance(getApplicationContext());
        tvShowHelper = FavoriteTvShowHelper.getInstance(getApplicationContext());

        if (getIntent().getParcelableExtra(EXTRA_MOVIE) != null){
            movie = getIntent().getParcelableExtra(EXTRA_MOVIE);
            String url = "https://api.themoviedb.org/3/movie/" + movie.getId_movie() + "?api_key=" + API_KEY + "&language=en-US";
            String castUrl = "https://api.themoviedb.org/3/movie/" + movie.getId_movie() + "/credits?api_key=" + API_KEY;
            setDetails();
            addOrRemoveFav(movie.getIsFav());
            setAttribute(movie.getTitle(), url, castUrl);
        }else{
            tvShow = getIntent().getParcelableExtra(EXTRA_SHOW);
            String url = "https://api.themoviedb.org/3/tv/" + tvShow.getId_show() + "?api_key=" + API_KEY + "&language=en-US";
            String castUrl = "https://api.themoviedb.org/3/tv/" + tvShow.getId_show() + "/credits?api_key=" + API_KEY;
            setShowDetails();
            addOrRemoveFav(tvShow.getIsFav());
            setAttribute(tvShow.getTitle(), url, castUrl);
        }

        rv_cast.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        rv_cast.setAdapter(castAdapter);
    }

    private void setAttribute(String title, String url, String castUrl){
        toolbar.setTitle(title);
        mainViewModel.setGenre(url);
        mainViewModel.setCast(castUrl);
    }

    private Observer<ArrayList<String>> getGenre = new Observer<ArrayList<String>>() {
        @Override
        public void onChanged(ArrayList<String> movieGenres) {
            if (movieGenres != null) {
                String temps = TextUtils.join(" | ", movieGenres);
                setGenre(temps);
            }
        }
    };

    private void setGenre(String temps) {
        tv_genre.setText(temps);
    }

    private Observer<ArrayList<Cast>> getCast = new Observer<ArrayList<Cast>>() {
        @Override
        public void onChanged(@Nullable ArrayList<Cast> casts) {
            if (casts != null){
                castAdapter.setCasts(casts);
                showLoading(false);
            }
        }
    };

    private void setShowDetails() {
        Glide.with(this).load(tvShow.getCover()).into(movieCover);
        Glide.with(this).load(tvShow.getPoster()).into(moviePoster);
        tv_title.setText(tvShow.getTitle());
        tv_popular.setText(tvShow.getPopularity());
        tv_description.setText(tvShow.getDescription());
    }

    private void setDetails() {
        Glide.with(this).load(movie.getCover()).into(movieCover);
        Glide.with(this).load(movie.getPoster()).into(moviePoster);
        tv_title.setText(movie.getTitle());
        tv_popular.setText(movie.getPopularity());
        tv_description.setText(movie.getDescription());
    }

    private void addOrRemoveFav(int response){
        if (response == 1){
            favChanger(1);
            addFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    long result;
                    if (movie != null) {
                        result = movieHelper.deleteFavMovie(movie.getId_movie());
                    } else {
                        result = tvShowHelper.deleteFavShow(tvShow.getId_show());
                    }
                    if (result > 0) {
                        favChanger(0);
                        showToast(getString(R.string.rmed_favorite));
                    }else {
                        favChanger(1);
                        showToast(getString(R.string.failrm_favorite));
                    }
                }
            });
        } else if (response == 0){
            favChanger(0);
            addFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    long result;
                    if (movie != null){
                        result = movieHelper.addFavMovie(movie);
                    } else {
                        result = tvShowHelper.addFavShow(tvShow);
                    }
                    if (result > 0){
                        favChanger(1);
                        showToast(getString(R.string.added_favorite));
                    } else if (result == IS_FAVORITE) {
                        favChanger(0);
                        showToast(getString(R.string.ald_favorite));
                    } else {
                        favChanger(0);
                        showToast(getString(R.string.failed_favorite));
                    }
                }
            });
        } else {
            favChanger(0);
        }
    }

    private void favChanger(int state){
        if (state == 1) {
            Glide.with(this).load("").placeholder(R.drawable.ic_favorite).into(addFav);
            tv_addFav.setText(getString(R.string.rm_favorite));
        } else {
            Glide.with(this).load("").placeholder(R.drawable.ic_outline_favorite).into(addFav);
            tv_addFav.setText(getString(R.string.add_to_favorite));
        }
    }

    private void showLoading(Boolean state) {
        if (state) {
            pb_detail.setVisibility(View.VISIBLE);
            layerHide.setVisibility(View.VISIBLE);
            toolbar.hide();
        } else {
            pb_detail.setVisibility(View.GONE);
            layerHide.setAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out));
            layerHide.setVisibility(View.GONE);
            movieCover.setAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_animation));
            moviePoster.setAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_transition));
            toolbar.show();
        }
    }

    private void showToast (String msg){
        Toast.makeText(DetailActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}
