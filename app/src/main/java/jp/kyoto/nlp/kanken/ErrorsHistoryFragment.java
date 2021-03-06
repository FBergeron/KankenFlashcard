package jp.kyoto.nlp.kanken;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
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

        panelErrorDetails = view.findViewById(R.id.linearLayoutErrorDetailsPanel);
        panelErrorDetails.setVisibility(View.GONE);

        buttonClosePanelErrorDetails = view.findViewById(R.id.buttonCloseErrorDetailsPanel);
        buttonClosePanelErrorDetails.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    panelErrorDetails.setVisibility(View.GONE);
                    hideSelector();
                }
            }
        );

        textViewProblemLevel = view.findViewById(R.id.textViewProblemLevel);
        textViewProblemTopic = view.findViewById(R.id.textViewProblemTopic);
        problemStatement = view.findViewById(R.id.problemStatement);
        textViewUserAnswer = view.findViewById(R.id.textViewUserAnswer);
        textViewProblemAnswer = view.findViewById(R.id.textViewProblemAnswer);

        listViewResultEntries = view.findViewById(R.id.listViewResultEntries);
        listViewAdapter = new ErrorsHistoryListViewAdapter(getContext(), inflater);
        listViewResultEntries.setAdapter(listViewAdapter);

        listViewResultEntries.setOnItemClickListener(
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    showSelector();
                    ErrorsHistoryListViewAdapter listAdapter = (ErrorsHistoryListViewAdapter)adapterView.getAdapter();
                    ErrorsHistoryItem item = listAdapter.getItem(i);

                    String strLevel = item.getLevel() == -1 ? getResources().getString(R.string.label_no_data) : String.format(getResources().getString(R.string.label_problem_info_level), item.getLevel());
                    textViewProblemLevel.setText(strLevel);

                    Problem.Topic[] topics = item.getTopics();
                    StringBuilder strTopics = new StringBuilder();
                    if (topics == null)
                        strTopics.append(getResources().getString(R.string.label_no_data));
                    else {
                        String delim = "";
                        for (int t = 0; t < topics.length; t++) {
                            strTopics.append(delim);
                            String strResName = "label_topic_" + topics[t].getLabelId();
                            int labelId = getResources().getIdentifier(strResName, "string", getContext().getPackageName());
                            strTopics.append(getResources().getString(labelId));
                            delim = "\n";
                        }
                    }

                    textViewProblemTopic.setText(strTopics.toString());
                    if (item.getStatement() == null)
                        problemStatement.setText(getResources().getString(R.string.label_no_data));
                    else
                        problemStatement.setText(Html.fromHtml(item.getStatement().replace("[", "<u><font color=\"red\">").replace("]", "</font></u>")));
                    textViewUserAnswer.setText(item.getUserAnswer());
                    textViewProblemAnswer.setText(item.getRightAnswer() == null ? getResources().getString(R.string.label_no_data) : item.getRightAnswer());

                    panelErrorDetails.setVisibility(View.VISIBLE);
                    ((ListView)adapterView).post(
                        new Runnable() {
                            public void run() {
                                ((ListView)adapterView).smoothScrollToPosition(i);
                            }
                        }
                    );
                }
            }
        );

        radioButtonProblemTypeReading = view.findViewById(R.id.radioButtonProblemTypeReading);
        radioButtonProblemTypeWriting = view.findViewById(R.id.radioButtonProblemTypeWriting);
        SharedPreferences sharedPref = getContext().getSharedPreferences(Util.PREFS_GENERAL, Context.MODE_PRIVATE);
        String prefProblemType = sharedPref.getString(Util.PREF_KEY_HISTORY_ERRORS_PROBLEM_TYPE, "Reading");
        if ("Reading".equals(prefProblemType))
            radioButtonProblemTypeReading.setChecked(true);
        else
            radioButtonProblemTypeWriting.setChecked(true);

        radioGroupProblemType = view.findViewById(R.id.radioGroupProblemType);
        radioGroupProblemType.setOnCheckedChangeListener(
            new RadioGroup.OnCheckedChangeListener() {
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    // Hack to remove the Listview selection.
                    // https://stackoverflow.com/questions/9754170/listview-selection-remains-persistent-after-exiting-choice-mode
                    listViewResultEntries.setAdapter(listViewResultEntries.getAdapter());

                    panelErrorDetails.setVisibility(View.GONE);

                    String problemType = null;
                    int selectedId = radioGroupProblemType.getCheckedRadioButtonId();
                    if (selectedId == R.id.radioButtonProblemTypeReading)
                        problemType = "Reading";
                    else
                        problemType = "Writing";

                    SharedPreferences sharedPref = getContext().getSharedPreferences(Util.PREFS_GENERAL, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(Util.PREF_KEY_HISTORY_ERRORS_PROBLEM_TYPE, problemType);
                    editor.apply();

                    updateErrors();
                }
            }
        );

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void updateErrors() {
        if (jsonErrorsHistory != null)
            rebuildErrors();
        else {
            URL getErrorsHistoryUrl;
            try {
                getErrorsHistoryUrl = new URL(appl.getServerBaseUrl() + KankenApplication.getErrorHistoryReqPath);

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
    }

    private void hideSelector() {
        listViewResultEntries.getSelector().setAlpha(0);
    }

    private void showSelector() {
        Drawable selector = listViewResultEntries.getSelector();
        if (selector.getAlpha() == 0)
            selector.setAlpha(255);
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
            jsonErrorsHistory = (JSONObject)obj;
            rebuildErrors();
        }

        private Exception exception;

    }

    private void rebuildErrors() {
        if (jsonErrorsHistory.has("errors_history")) {
            List<ErrorsHistoryItem> errorsHistoryItems = new ArrayList<>();
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

                                    boolean isReadingProblem = problemId.endsWith("-y");
                                    if ((isReadingProblem && radioButtonProblemTypeWriting.isChecked()) ||
                                        (!isReadingProblem && radioButtonProblemTypeReading.isChecked()))
                                        continue;

                                    String problemStmt = err.has("problem_statement") ? (String)err.get("problem_statement") : null;
                                    String problemWord = err.has("problem_word") ? (String)err.get("problem_word") : null;
                                    String problemRightAnswer = err.has("problem_right_answer") ? (String)err.get("problem_right_answer") : null;
                                    String userAnswer = (String)err.get("user_answer");

                                    int problemLevel = -1;
                                    if (err.has("problem_level")) {
                                        try {
                                            problemLevel = Integer.parseInt((String)err.get("problem_level"));
                                        }
                                        catch(NumberFormatException shoudNeverHappen) {
                                            shoudNeverHappen.printStackTrace();
                                        }
                                    }
                                    Problem.Topic[] topics = null;
                                    if (err.has("problem_topics")) {
                                        JSONArray jsonTopics = (JSONArray)err.get("problem_topics");
                                        topics = new Problem.Topic[jsonTopics.length()];
                                        for (int t = 0; t < jsonTopics.length(); t++)
                                            topics[t] = Problem.Topic.valueOf((jsonTopics.get(t) + "").toUpperCase());
                                    }

                                    ErrorsHistoryItem errorsHistoryItem = new ErrorsHistoryItem(
                                        date,
                                        problemStmt,
                                        problemWord,
                                        problemLevel,
                                        topics,
                                        userAnswer,
                                        problemRightAnswer,
                                        !strDateKey.equals(strPrevDate)
                                    );
                                    errorsHistoryItems.add(errorsHistoryItem);
                                    if (!strDateKey.equals(strPrevDate))
                                        strPrevDate = strDateKey;
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

    private JSONObject jsonErrorsHistory = null;

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    private RadioGroup radioGroupProblemType;
    private RadioButton radioButtonProblemTypeReading;
    private RadioButton radioButtonProblemTypeWriting;

    private View panelErrorDetails;
    private Button buttonClosePanelErrorDetails;

    private TextView textViewProblemLevel;
    private TextView textViewProblemTopic;
    private TextView problemStatement;
    private TextView textViewUserAnswer;
    private TextView textViewProblemAnswer;

    private ListView listViewResultEntries;

    private ErrorsHistoryListViewAdapter listViewAdapter;

    private ProgressDialog progressDialog;

    private KankenApplication appl = KankenApplication.getInstance();

    private static final String tag = "ErrorsHistoryFragment";

}


