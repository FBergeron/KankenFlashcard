package jp.kyoto.nlp.kanken;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class TextResultsHistoryFragment extends Fragment {

    public TextResultsHistoryFragment() {
    }

    public static TextResultsHistoryFragment newInstance(String param1, String param2) {
        TextResultsHistoryFragment fragment = new TextResultsHistoryFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_text_results_history, container, false);

        ListView listViewResultEntries = view.findViewById(R.id.listViewResultEntries);
        listViewAdapter = new ResultsHistoryListViewAdapter(getContext(), inflater);
        listViewResultEntries.setAdapter(listViewAdapter);

        initResultsHistory();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void initResultsHistory() {
        URL getResultHistoryUrl;
        try {
            getResultHistoryUrl = new URL(appl.getServerBaseUrl() + getResultHistoryReqPath);

            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage(getResources().getString(R.string.label_fetching_data));
            progressDialog.setCancelable(false);
            progressDialog.show();

            new FetchResultHistoryTask().execute(getResultHistoryUrl);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
        } catch (IOException e3) {
            e3.printStackTrace();
        } catch (JSONException e4) {
            e4.printStackTrace();
        }
    }

    private class FetchResultHistoryTask extends AsyncTask {

        protected Object doInBackground(Object... objs) {
            JSONObject jsonResultHistory = null;
            URL getResultHistoryUrl = (URL) objs[0];
            try {
                HttpURLConnection con = (HttpURLConnection) getResultHistoryUrl.openConnection();
                con.setRequestProperty("Accept", "application/json");
                String cookie = appl.getSessionCookie();
                if (cookie != null)
                    con.setRequestProperty("Cookie", cookie);
                con.setRequestMethod("GET");
                con.connect();

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                jsonResultHistory = jsonResponse;
            } catch (IOException e) {
                e.printStackTrace();
                this.exception = e;
            } catch (JSONException e2) {
                e2.printStackTrace();
                this.exception = e2;
            }

            return jsonResultHistory;
        }

        protected void onPostExecute(final Object obj) {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            if (exception != null || obj == null) {
                if (exception != null)
                    Log.e(tag, "An exception has occurred: " + exception);
                if (obj == null)
                    Log.e(tag, "Cannot retrieve problems.");

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getResources().getString(R.string.error_server_unreachable_title))
                        .setMessage(getResources().getString(R.string.error_server_unreachable_msg))
                        .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setCancelable(true)
                        .show();

                return;
            }

            Log.d(tag, "resultsHistory=" + obj);

            List<ResultsHistoryItem> resultsHistoryItems = new ArrayList<>();
            JSONObject jsonResultsHistory = (JSONObject)obj;
            if (jsonResultsHistory.has("results_history")) {
                JSONArray jsonResults = null;
                try {
                    jsonResults = (JSONArray)jsonResultsHistory.get("results_history");
                }
                catch(JSONException e) {
                    e.printStackTrace();
                }
                if (jsonResults != null) {
                    Calendar now = Calendar.getInstance();
                    String strDateTemp = dateFormatter.format(now.getTime());

                    Calendar periodStart = Calendar.getInstance();
                    periodStart.add(Calendar.MONTH, -1);
                    String strDateMin = dateFormatter.format(periodStart.getTime());

                    String strDateResult = null;
                    JSONObject result = null;
                    Integer readingRights = Integer.valueOf(0);
                    Integer readingWrongs = Integer.valueOf(0);
                    Integer writingRights = Integer.valueOf(0);
                    Integer writingWrongs = Integer.valueOf(0);
                    int entryIndex = 0;
                    int resultIndex = 0;
                    while (strDateTemp.compareTo(strDateMin) > 0) {
                        if (jsonResults.length() > resultIndex && (result == null || strDateResult == null || strDateResult.compareTo(strDateTemp) > 0)) {
                            try {
                                result = (JSONObject)jsonResults.get(resultIndex);
                                for (Iterator<String> keys = result.keys(); keys.hasNext(); ) {
                                    strDateResult = keys.next();
                                    JSONObject dailyResult = (JSONObject)result.get(strDateResult);
                                    if (dailyResult.has("reading")) {
                                        JSONObject readingData = (JSONObject)dailyResult.get("reading");
                                        readingRights = (Integer)readingData.get("rights");
                                        readingWrongs = (Integer)readingData.get("wrongs");
                                    }
                                    if (dailyResult.has("writing")) {
                                        JSONObject writingData = (JSONObject)dailyResult.get("writing");
                                        writingRights = (Integer)writingData.get("rights");
                                        writingWrongs = (Integer)writingData.get("wrongs");
                                    }

                                    // There always is 1 key.
                                    break;
                                }
                                resultIndex++;
                            }
                            catch(JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        try {
                            Date date = dateFormatter.parse(strDateTemp);
                            if (strDateResult != null && strDateResult.compareTo(strDateTemp) == 0) {
                                ResultsHistoryItem resultsHistoryItem = new ResultsHistoryItem(
                                    date,
                                    readingRights.intValue(),
                                    readingWrongs.intValue(),
                                    writingRights.intValue(),
                                    writingWrongs.intValue()
                                );
                                resultsHistoryItems.add(resultsHistoryItem);
                            }
                            else {
                                ResultsHistoryItem resultsHistoryItem = new ResultsHistoryItem(date, 0, 0, 0, 0);
                                resultsHistoryItems.add(resultsHistoryItem);
                            }
                        }
                        catch(ParseException e) {
                            e.printStackTrace();
                        }

                        entryIndex++;
                        now.add(Calendar.DAY_OF_MONTH, -1);
                        strDateTemp = dateFormatter.format(now.getTime());
                    }

                    listViewAdapter.setItems(resultsHistoryItems);
                }
            }
        }

        private Exception exception;

    }

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    private ResultsHistoryListViewAdapter listViewAdapter;

    private ProgressDialog progressDialog;

    private KankenApplication appl = KankenApplication.getInstance();

    private static final String getResultHistoryReqPath = "/cgi-bin/get_results_history.cgi";

    private static final String tag = "TextResultsHistoryFragment";

}
