package jp.kyoto.nlp.kanken;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public abstract class BaseActionActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActionBar();
    }

    protected void onStart() {
        super.onStart();
    }

    protected void initActionBar() {
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setTitle("");
        Drawable actionBarBackground = ResourcesCompat.getDrawable(getResources(), R.drawable.background_header, null);
        actionBar.setIcon(actionBarBackground);
        actionBar.show();
    }

    protected ActionBar actionBar;

    private final static String tag = "BaseActionActivity";

}

