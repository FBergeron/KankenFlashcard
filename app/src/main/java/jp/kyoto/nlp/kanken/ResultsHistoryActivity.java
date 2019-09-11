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

    // I have moved this to TextResultsHistoryActivityFragment but it's not sure yet if it's a good idea.
    // If the text and graphic views use the same data, it's probably better to load the data in ResultsHistoryActivity.
    // If they use different data, then it's better that each view has its own data fetcher.

    // private void initResultsHistory() {
    //     URL getResultHistoryUrl;
    //     try {
    //         getResultHistoryUrl = new URL(appl.getServerBaseUrl() + getResultHistoryReqPath);

    //         progressDialog = new ProgressDialog(this);
    //         progressDialog.setMessage(getResources().getString(R.string.label_fetching_data));
    //         progressDialog.setCancelable(false);
    //         progressDialog.show();

    //         new FetchResultHistoryTask().execute(getResultHistoryUrl);
    //     } catch (MalformedURLException e1) {
    //         e1.printStackTrace();
    //     } catch (UnsupportedEncodingException e2) {
    //         e2.printStackTrace();
    //     } catch (IOException e3) {
    //         e3.printStackTrace();
    //     } catch (JSONException e4) {
    //         e4.printStackTrace();
    //     }
    // }

    // private class FetchResultHistoryTask extends AsyncTask {

    //     protected Object doInBackground(Object... objs) {
    //         JSONObject jsonResultHistory = null;
    //         URL getResultHistoryUrl = (URL) objs[0];
    //         try {
    //             HttpURLConnection con = (HttpURLConnection) getResultHistoryUrl.openConnection();
    //             con.setRequestProperty("Accept", "application/json");
    //             String cookie = appl.getSessionCookie();
    //             if (cookie != null)
    //                 con.setRequestProperty("Cookie", cookie);
    //             con.setRequestMethod("GET");
    //             con.connect();

    //             BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    //             String inputLine;
    //             StringBuilder response = new StringBuilder();
    //             while ((inputLine = in.readLine()) != null) {
    //                 response.append(inputLine);
    //             }
    //             in.close();

    //             JSONObject jsonResponse = new JSONObject(response.toString());
    //             jsonResultHistory = jsonResponse;
    //         } catch (IOException e) {
    //             e.printStackTrace();
    //             this.exception = e;
    //         } catch (JSONException e2) {
    //             e2.printStackTrace();
    //             this.exception = e2;
    //         }

    //         return jsonResultHistory;
    //     }

    //     protected void onPostExecute(final Object obj) {
    //         if (progressDialog != null) {
    //             progressDialog.dismiss();
    //             progressDialog = null;
    //         }

    //         if (exception != null || obj == null) {
    //             if (exception != null)
    //                 Log.e(tag, "An exception has occurred: " + exception);
    //             if (obj == null)
    //                 Log.e(tag, "Cannot retrieve problems.");

    //             AlertDialog.Builder builder = new AlertDialog.Builder(ResultsHistoryActivity.this);
    //             builder.setTitle(getResources().getString(R.string.error_server_unreachable_title))
    //                     .setMessage(getResources().getString(R.string.error_server_unreachable_msg))
    //                     .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
    //                         public void onClick(DialogInterface dialog, int which) {
    //                         }
    //                     })
    //                     .setIcon(android.R.drawable.ic_dialog_alert)
    //                     .setCancelable(true)
    //                     .show();

    //             return;
    //         }

    //         Log.d(tag, "resultsHistory=" + obj);

    //         List<ResultsHistoryItem> resultsHistoryItems = new ArrayList<>();
    //         JSONObject jsonResultsHistory = (JSONObject)obj;
    //         if (jsonResultsHistory.has("results_history")) {
    //             JSONArray jsonResults = null;
    //             try {
    //                 jsonResults = (JSONArray)jsonResultsHistory.get("results_history");
    //             }
    //             catch(JSONException e) {
    //                 e.printStackTrace();
    //             }
    //             if (jsonResults != null) {
    //                 for (int i = 0; i < jsonResults.length(); i++) {
    //                     try {
    //                         JSONObject result = (JSONObject)jsonResults.get(i);
    //                         Integer readingRights = null;
    //                         Integer readingWrongs = null;
    //                         Integer writingRights = null;
    //                         Integer writingWrongs = null;

    //                         String strDateKey = null;
    //                         for (Iterator<String> keys = result.keys(); keys.hasNext(); ) {
    //                             strDateKey = keys.next();
    //                             JSONObject dailyResult = (JSONObject)result.get(strDateKey);
    //                             if (dailyResult.has("reading")) {
    //                                 JSONObject readingData = (JSONObject)dailyResult.get("reading");
    //                                 readingRights = (Integer)readingData.get("rights");
    //                                 readingWrongs = (Integer)readingData.get("wrongs");
    //                             }
    //                             if (dailyResult.has("writing")) {
    //                                 JSONObject writingData = (JSONObject)dailyResult.get("writing");
    //                                 writingRights = (Integer)writingData.get("rights");
    //                                 writingWrongs = (Integer)writingData.get("wrongs");
    //                             }
    //                         }

    //                         if (strDateKey != null) {
    //                             SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    //                             try {
    //                                 Date date = formatter.parse(strDateKey);
    //                                 ResultsHistoryItem resultsHistoryItem = new ResultsHistoryItem(
    //                                     date,
    //                                     readingRights.intValue(),
    //                                     readingWrongs.intValue(),
    //                                     writingRights.intValue(),
    //                                     writingWrongs.intValue()
    //                                );
    //                                resultsHistoryItems.add(resultsHistoryItem);
    //                             }
    //                             catch(ParseException e) {
    //                                 e.printStackTrace();
    //                             }
    //                         }
    //                     }
    //                     catch(JSONException e) {
    //                         e.printStackTrace();
    //                     }
    //                 }
    //                 listViewAdapter.setItems(resultsHistoryItems);
    //             }
    //         }
    //     }

    //     private Exception exception;

    // }

    private ResultsHistoryListViewAdapter listViewAdapter;

    private View fragmentErrorView;
    private View fragmentGraphicView;
    private View fragmentTextView;

    private ProgressDialog progressDialog;

    private KankenApplication appl = KankenApplication.getInstance();

    //private static final String getResultHistoryReqPath = "/cgi-bin/get_results_history.cgi";

    private static final String tag = "ResultsHistoryActivity";

}
