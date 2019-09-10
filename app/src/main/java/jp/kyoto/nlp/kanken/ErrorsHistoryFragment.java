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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ErrorsHistoryFragment extends Fragment {

    public ErrorsHistoryFragment() {
    }

    public static ErrorsHistoryFragment newInstance(String param1, String param2) {
        ErrorsHistoryFragment fragment = new ErrorsHistoryFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_error_history, container, false);

        ListView listViewResultEntries = view.findViewById(R.id.listViewResultEntries);
        listViewAdapter = new ErrorsHistoryListViewAdapter(getContext(), inflater);
        listViewResultEntries.setAdapter(listViewAdapter);

        initErrors();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void initErrors() {
        URL getErrorsHistoryUrl;
        try {
            getErrorsHistoryUrl = new URL(appl.getServerBaseUrl() + getErrorHistoryReqPath);

            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage(getResources().getString(R.string.label_fetching_data));
            progressDialog.setCancelable(false);
            progressDialog.show();

            new FetchErrorHistoryTask().execute(getErrorsHistoryUrl);
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

    private class FetchErrorHistoryTask extends AsyncTask {

        protected Object doInBackground(Object... objs) {
            JSONObject jsonErrorHistory = null;
            URL getErrorHistoryUrl = (URL) objs[0];
            try {
                HttpURLConnection con = (HttpURLConnection) getErrorHistoryUrl.openConnection();
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
                jsonErrorHistory = jsonResponse;
            } catch (IOException e) {
                e.printStackTrace();
                this.exception = e;
            } catch (JSONException e2) {
                e2.printStackTrace();
                this.exception = e2;
            }

            return jsonErrorHistory;
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

            Log.d(tag, "errorsHistory=" + obj);

            List<ErrorsHistoryItem> errorsHistoryItems = new ArrayList<>();
            JSONObject jsonErrorsHistory = (JSONObject)obj;
            if (jsonErrorsHistory.has("errors_history")) {
                JSONArray jsonErrors = null;
                try {
                    jsonErrors = (JSONArray)jsonErrorsHistory.get("errors_history");
                }
                catch(JSONException e) {
                    e.printStackTrace();
                }
                if (jsonErrors != null) {
                    String strPrevDate = null;
                    for (int i = 0; i < jsonErrors.length(); i++) {
                        try {
                            JSONObject error = (JSONObject)jsonErrors.get(i);
                            String strDateKey = null;
                            for (Iterator<String> keys = error.keys(); keys.hasNext(); ) {
                                strDateKey = keys.next();
                                try {
                                    Date date = formatter.parse(strDateKey);
                                    JSONArray dailyErrors = (JSONArray)error.get(strDateKey);
                                    for (int j = 0; j < dailyErrors.length(); j++) {
                                        JSONObject err = (JSONObject)dailyErrors.get(j);

                                        String problemId = (String)err.get("problem_id");
                                        String problemStmt = err.has("problem_statement") ? (String)err.get("problem_statement") : null;
                                        String problemWord = err.has("problem_word") ? (String)err.get("problem_word") : null;
                                        String problemRightAnswer = err.has("problem_right_answer") ? (String)err.get("problem_right_answer") : null;
                                        String userAnswer = (String)err.get("user_answer");

                                        if (!strDateKey.equals(strPrevDate)) {
                                            ErrorsHistoryItem dateItem = new ErrorsHistoryItem(date);
                                            errorsHistoryItems.add(dateItem);
                                            strPrevDate = strDateKey;
                                        }
                                        ErrorsHistoryItem errorsHistoryItem = new ErrorsHistoryItem(
                                            date,
                                            problemWord,
                                            userAnswer,
                                            problemRightAnswer
                                        );
                                        errorsHistoryItems.add(errorsHistoryItem);
                                    }
                                }
                                catch(ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        catch(JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    listViewAdapter.setItems(errorsHistoryItems);
                }
            }
        }

        private Exception exception;

    }

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    private ErrorsHistoryListViewAdapter listViewAdapter;

    private ProgressDialog progressDialog;

    private KankenApplication appl = KankenApplication.getInstance();

    private static final String getErrorHistoryReqPath = "/cgi-bin/get_errors_history.cgi";

    private static final String tag = "ErrorsHistoryFragment";

}


