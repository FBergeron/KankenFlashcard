package jp.kyoto.nlp.kanken;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static android.view.View.GONE;

public class ResultsHistoryActivity extends ActionActivity {

    @Override
    public void onBackPressed() {
        doLeaveResultsHistory();
    }

    public void leaveResultsHistory(android.view.View view) {
        doLeaveResultsHistory();
    }

    public void showErrorView(android.view.View view) {
        fragmentErrorView.setVisibility(View.VISIBLE);
        fragmentGraphicView.setVisibility(View.GONE);
        fragmentTextView.setVisibility(View.GONE);

        SharedPreferences sharedPref = getSharedPreferences(Util.PREFS_GENERAL, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Util.PREF_KEY_HISTORY_VIEW, "Errors");
        editor.apply();
    }

    public void showTextView(android.view.View view) {
        fragmentErrorView.setVisibility(View.GONE);
        fragmentGraphicView.setVisibility(View.GONE);
        fragmentTextView.setVisibility(View.VISIBLE);

        SharedPreferences sharedPref = getSharedPreferences(Util.PREFS_GENERAL, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Util.PREF_KEY_HISTORY_VIEW, "Text");
        editor.apply();
    }

    public void showGraphicView(android.view.View view) {
        fragmentErrorView.setVisibility(View.GONE);
        fragmentGraphicView.setVisibility(View.VISIBLE);
        fragmentTextView.setVisibility(View.GONE);

        SharedPreferences sharedPref = getSharedPreferences(Util.PREFS_GENERAL, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Util.PREF_KEY_HISTORY_VIEW, "Graphic");
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results_history);

        fragmentErrorView = findViewById(R.id.fragmentErrorResultsHistory);
        fragmentGraphicView = findViewById(R.id.fragmentGraphicResultsHistory);
        fragmentTextView = findViewById(R.id.fragmentTextResultsHistory);

        SharedPreferences sharedPref = getSharedPreferences(Util.PREFS_GENERAL, Context.MODE_PRIVATE);
        String prefView = sharedPref.getString(Util.PREF_KEY_HISTORY_VIEW, "Text");

        if ("Text".equals(prefView)) {
            fragmentErrorView.setVisibility(View.GONE);
            fragmentGraphicView.setVisibility(View.GONE);
            fragmentTextView.setVisibility(View.VISIBLE);

            RadioGroup radioGroupHistoryViewType = findViewById(R.id.radioGroupHistoryViewType);
            radioGroupHistoryViewType.check(R.id.buttonTextView);
        }
        else if ("Graphic".equals(prefView)) {
            fragmentErrorView.setVisibility(View.GONE);
            fragmentGraphicView.setVisibility(View.VISIBLE);
            fragmentTextView.setVisibility(View.GONE);

            RadioGroup radioGroupHistoryViewType = findViewById(R.id.radioGroupHistoryViewType);
            radioGroupHistoryViewType.check(R.id.buttonGraphicView);
        }
        else if ("Errors".equals(prefView)) {
            fragmentErrorView.setVisibility(View.VISIBLE);
            fragmentGraphicView.setVisibility(View.GONE);
            fragmentTextView.setVisibility(View.GONE);

            RadioGroup radioGroupHistoryViewType = findViewById(R.id.radioGroupHistoryViewType);
            radioGroupHistoryViewType.check(R.id.buttonErrorView);
        }
    }

    private void doLeaveResultsHistory() {
        finish();
    }

    private ResultsHistoryListViewAdapter listViewAdapter;

    private View fragmentErrorView;
    private View fragmentGraphicView;
    private View fragmentTextView;

    private ProgressDialog progressDialog;

    private KankenApplication appl = KankenApplication.getInstance();

    private static final String tag = "ResultsHistoryActivity";

}
