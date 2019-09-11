package jp.kyoto.nlp.kanken;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.CheckBox;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QuizSettingsActivity extends ActionActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {
    public class CustomAdapter extends ArrayAdapter<String> {
        private LayoutInflater inflater;

        public CustomAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            String item = getItem(position);
            if (null == v) v = inflater.inflate(R.layout.custom_listview, null);

            CheckBox ch = v.findViewById(R.id.checkBox);
            ch.setText(item);
            ch.setChecked(checkedTopics[position]);
            return v;
        }
    }

    public void showResultsHistory(android.view.View view) {
        Intent resultsHistoryActivity = new Intent(QuizSettingsActivity.this, ResultsHistoryActivity.class);
        startActivity(resultsHistoryActivity);
    }

    public void signOut(android.view.View view) {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        URL signOutUrl;
                        try {
                            signOutUrl = new URL(appl.getServerBaseUrl() + signOutReqPath);

                            progressDialog = new ProgressDialog(QuizSettingsActivity.this);
                            progressDialog.setMessage(getResources().getString(R.string.label_signing_out));
                            progressDialog.setCancelable(false);
                            progressDialog.show();

                            new SignOutTask().execute(signOutUrl);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        } catch (JSONException e3) {
                            e3.printStackTrace();
                        }
                    }
                }
        );
    }

    public void invokeTopicChooser(android.view.View view) {
        for (int i = 0; i < checkedTopics.length; i++)
            checkedTopics[i] = selectedTopics.contains(i);


        CustomAdapter adapter = new CustomAdapter(getApplicationContext(), 0, Arrays.asList(labelTopics));
        ListView listView = new ListView(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int lastNum = checkedTopics.length - 1;
                boolean last = checkedTopics[lastNum];
                if (position != lastNum) {
                    checkedTopics[lastNum] = false;
                    checkedTopics[position] = !checkedTopics[position];
                    boolean isAllChecked = true;
                    for (int i = 0; i < checkedTopics.length - 1; i++) {
                        if (!checkedTopics[i]) {
                            isAllChecked = false;
                            break;
                        }
                    }
                    checkedTopics[lastNum] = isAllChecked;
                    labelTopics[lastNum] = isAllChecked ? getString(R.string.label_topic_all_unselect) : getString(R.string.label_topic_all);
                } else {
                    boolean temp = !last;
                    for (int i = 0; i < checkedTopics.length; i++) {
                        checkedTopics[i] = temp;
                    }
                    labelTopics[position] = checkedTopics[position] ? getString(R.string.label_topic_all_unselect) : getString(R.string.label_topic_all);
                    adapter.notifyDataSetChanged();
                }
                CustomAdapter tmp = (CustomAdapter) listView.getAdapter();
                tmp.notifyDataSetChanged();
            }
        });
        AlertDialog.Builder builderTopicChooser = new AlertDialog.Builder(QuizSettingsActivity.this);
        builderTopicChooser.setTitle(getResources().getString(R.string.label_topic_chooser_title));
        builderTopicChooser.setView(listView);
        // builderTopicChooser.setMultiChoiceItems(labelTopics, checkedTopics, new DialogInterface.OnMultiChoiceClickListener() {
        //     @Override
        //     public void onClick(DialogInterface dialog, int topicIndex, boolean isChecked) {
        //         checkedTopics[topicIndex] = false;
        //         checkedTopics[topicIndex+1] = isChecked;
        //     }
        // });
        builderTopicChooser.setCancelable(false);
        builderTopicChooser.setPositiveButton(getResources().getString(R.string.button_select), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < checkedTopics.length; i++) {
                    if (checkedTopics[i])
                        selectedTopics.add(i);
                    else
                        selectedTopics.remove(i);
                }
                showSelectedTopics();
            }
        });
        builderTopicChooser.setNegativeButton(getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showSelectedTopics();
            }
        });
        AlertDialog dialogTopicChooser = builderTopicChooser.create();
        dialogTopicChooser.show();

    }

    public void showTermsOfUsage(android.view.View view) {
        Intent httpIntent = new Intent(Intent.ACTION_VIEW);
        String termsOfUsageLink = getResources().getString(R.string.link_terms_of_usage);
        httpIntent.setData(Uri.parse(termsOfUsageLink));
        startActivity(httpIntent);
    }

    public void showDirections(android.view.View view) {
        Intent httpIntent = new Intent(Intent.ACTION_VIEW);
        String directionsLink = getResources().getString(R.string.link_directions);
        httpIntent.setData(Uri.parse(directionsLink));
        startActivity(httpIntent);
    }

    public void startReadingQuiz(android.view.View view) {
        startQuiz(Problem.Type.READING);
    }

    public void startWritingQuiz(android.view.View view) {
        startQuiz(Problem.Type.WRITING);
    }

    public void startQuiz(Problem.Type type) {
        Set<Problem.Topic> quizTopics = new HashSet<Problem.Topic>();
        for (Integer selectedTopic : selectedTopics)
            quizTopics.add(Problem.Topic.values()[selectedTopic]);

        if (quizTopics.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(QuizSettingsActivity.this);
            builder.setTitle(getResources().getString(R.string.error_no_topics_selected_title))
                    .setMessage(getResources().getString(R.string.error_no_topics_selected_msg))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setCancelable(true)
                    .show();
            return;
        }

        SharedPreferences sharedPref = getSharedPreferences(Util.PREFS_GENERAL, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        SeekBar seekbarQuizLevel = findViewById(R.id.seekBarQuizLevel);
        int level = seekbarQuizLevel.getProgress() + 1;
        editor.putInt(Util.PREF_KEY_QUIZ_LEVEL, level);

        StringBuilder strPrefTopics = new StringBuilder();
        String delimiter = "";
        for (Problem.Topic topic : quizTopics) {
            strPrefTopics.append(delimiter);
            strPrefTopics.append(topic.getLabelId());
            delimiter = ",";
        }
        editor.putString(Util.PREF_KEY_QUIZ_TOPICS, strPrefTopics.toString());

        editor.putString(Util.PREF_KEY_QUIZ_TYPE, type.getLabelId());

        editor.apply();

        appl.startQuiz(type, quizTopics, level);

        fetchProblems(level, quizTopics, type);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_settings);

        appl = KankenApplication.getInstance();

        TextView userName = findViewById(R.id.userName);
        userName.setText(appl.getUserName());

        seekBarE = findViewById(R.id.seekBarE);

        int topicCount = Problem.Topic.values().length;
        labelTopics = new String[topicCount];
        for (int i = 0; i < topicCount; i++) {
            String strResName = "label_topic_" + Problem.Topic.values()[i].getLabelId();
            int labelId = getResources().getIdentifier(strResName, "string", QuizSettingsActivity.this.getPackageName());
            labelTopics[i] = getResources().getString(labelId);
        }

        SharedPreferences sharedPref = getSharedPreferences(Util.PREFS_GENERAL, Context.MODE_PRIVATE);

        int prefLevel = sharedPref.getInt(Util.PREF_KEY_QUIZ_LEVEL, 1);
        SeekBar seekbarQuizLevel = findViewById(R.id.seekBarQuizLevel);
        seekbarQuizLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setSeekBarImageWidth(progress, seekBar);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekbarQuizLevel.setProgress(prefLevel - 1);
        setSeekBarImageWidth(prefLevel - 1, seekbarQuizLevel);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);


        checkedTopics = new boolean[labelTopics.length];

        String prefTopics = sharedPref.getString(Util.PREF_KEY_QUIZ_TOPICS, "");
        HashSet<String> prefTopicLabels = new HashSet<String>(Arrays.asList(prefTopics.split(",")));
        for (int i = 0; i < Problem.Topic.values().length; i++) {
            if (prefTopicLabels.contains(Problem.Topic.values()[i].getLabelId()))
                selectedTopics.add(Integer.valueOf(i));
        }
        showSelectedTopics();

        String prefType = sharedPref.getString(Util.PREF_KEY_QUIZ_TYPE, "reading");

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(Util.googleClientId).requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions).build();
    }

    private void setSeekBarImageWidth(int progress, SeekBar seekBar) {
        float x = (float) (progress) / (float) seekBar.getMax();
        if (progress == 0) {
            x = 0.01f;
        } else if (progress == 3) {
            x = 0.7f;
        }
        ViewGroup.LayoutParams params = seekBarE.getLayoutParams();
        float seekBarMaxWidth = getResources().getDimension(R.dimen.seek_bar_width);
        params.width = (int) (seekBarMaxWidth * x);
        seekBarE.setLayoutParams(params);
    }

    private void showSelectedTopics() {
        StringBuilder str = new StringBuilder();
        String delimiter = "";
        int topicCount = Problem.Topic.values().length;
        for (int i = 0; i < topicCount - 1; i++) {
            if (selectedTopics.contains(i)) {
                str.append(delimiter);
                str.append(labelTopics[i]);
                delimiter = ", ";
            }
        }
        TextView textViewSelectedTopics = findViewById(R.id.textViewSelectedTopics);
        textViewSelectedTopics.setText(str.toString());
    }

    private void fetchProblems(int level, Set<Problem.Topic> topics, Problem.Type type) {
        Log.d(TAG, "fetchProblems level=" + level + " topics=" + topics + " type=" + type);
        URL getNextProblemsUrl;
        try {
            String delim = "";
            StringBuilder topicsParam = new StringBuilder();
            List<Problem.Topic> sortedTopics = new ArrayList<Problem.Topic>(topics);
            Collections.sort(sortedTopics);
            for (Problem.Topic topic : sortedTopics) {
                topicsParam.append(delim);
                topicsParam.append(topic.toString().toLowerCase());
                delim = ",";
            }

            getNextProblemsUrl = new URL(appl.getServerBaseUrl() + getNextProblemsReqPath +
                    "?type=" + URLEncoder.encode(type.toString().toLowerCase()) +
                    "&level=" + URLEncoder.encode(level + "", "UTF-8") +
                    "&topics=" + URLEncoder.encode(topicsParam.toString(), "UTF-8"));

            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getResources().getString(R.string.label_fetching_quiz_data));
            progressDialog.setCancelable(false);
            progressDialog.show();

            new FetchProblemsTask(appl, progressDialog, QuizSettingsActivity.this).execute(getNextProblemsUrl);
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View v) {
    }

    // private class FetchProblemsTask extends AsyncTask {

    //     protected Object doInBackground(Object... objs) {
    //         JSONArray jsonProblems = null;
    //         URL getNextProblemsUrl = (URL) objs[0];
    //         try {
    //             HttpURLConnection con = (HttpURLConnection) getNextProblemsUrl.openConnection();
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
    //             jsonProblems = jsonResponse.getJSONArray("problems");
    //         } catch (IOException e) {
    //             e.printStackTrace();
    //             this.exception = e;
    //         } catch (JSONException e2) {
    //             e2.printStackTrace();
    //             this.exception = e2;
    //         }

    //         return jsonProblems;
    //     }

    //     protected void onPostExecute(final Object obj) {
    //         if (progressDialog != null) {
    //             progressDialog.dismiss();
    //             progressDialog = null;
    //         }

    //         if (exception != null || obj == null) {
    //             if (exception != null)
    //                 Log.e(TAG, "An exception has occurred: " + exception);
    //             if (obj == null)
    //                 Log.e(TAG, "Cannot retrieve problems.");

    //             AlertDialog.Builder builder = new AlertDialog.Builder(QuizSettingsActivity.this);
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

    //         ArrayList<Problem> problems = new ArrayList<Problem>();
    //         Quiz quiz = appl.getQuiz();

    //         JSONArray jsonProblems = (JSONArray) obj;

    //         if (jsonProblems.length() < Quiz.DEFAULT_LENGTH) {
    //             AlertDialog.Builder builder = new AlertDialog.Builder(QuizSettingsActivity.this);
    //             builder.setTitle(getResources().getString(R.string.error_not_enough_problems_found_title))
    //                     .setMessage(getResources().getString(R.string.error_not_enough_problems_found_msg))
    //                     .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
    //                         public void onClick(DialogInterface dialog, int which) {
    //                         }
    //                     })
    //                     .setIcon(android.R.drawable.ic_dialog_alert)
    //                     .setCancelable(true)
    //                     .show();
    //             return;
    //         }

    //         for (int i = 0; i < jsonProblems.length(); i++) {
    //             try {
    //                 JSONArray jsonProblemData = jsonProblems.getJSONArray(i);

    //                 JSONArray jsonProblem = jsonProblemData.getJSONArray(0);
    //                 JSONArray jsonProblemTopics = jsonProblemData.getJSONArray(1);

    //                 String id = jsonProblem.getString(0);
    //                 String jumanInfo = jsonProblem.getString(1);
    //                 String statement = jsonProblem.getString(2);
    //                 String rightAnswer = jsonProblem.getString(3);
    //                 String articleUrl = jsonProblem.getString(4);
    //                 Object objIsLinkAlive = jsonProblem.get(5);

    //                 boolean isLinkAlive = objIsLinkAlive != null && objIsLinkAlive instanceof Integer && (Integer) objIsLinkAlive == 1;

    //                 String altArticleUrl = null;
    //                 try {
    //                     if (!isLinkAlive && objIsLinkAlive != null && objIsLinkAlive instanceof JSONArray) {
    //                         StringBuilder strParams = new StringBuilder();
    //                         String paramDelim = "";
    //                         JSONArray searchTerms = (JSONArray) objIsLinkAlive;
    //                         for (int t = 0; t < searchTerms.length(); t++) {
    //                             strParams.append(paramDelim);
    //                             strParams.append(URLEncoder.encode(searchTerms.getString(t), "UTF-8"));
    //                             paramDelim = "+";
    //                         }
    //                         if (strParams.length() > 0)
    //                             altArticleUrl = String.format(getResources().getString(R.string.search_engine_url), strParams);
    //                     }
    //                 } catch (UnsupportedEncodingException ignore) {
    //                     ignore.printStackTrace();
    //                 }

    //                 // Log.d(TAG, "id="+id);
    //                 // Log.d(TAG, "jumanInfo="+jumanInfo);
    //                 // Log.d(TAG, "statement="+statement);
    //                 // Log.d(TAG, "rightAnswer="+rightAnswer);
    //                 // Log.d(TAG, "articleUrl="+articleUrl);
    //                 // Log.d(TAG, "objIsLinkAlive="+objIsLinkAlive);
    //                 // Log.d(TAG, "isLinkAlive="+isLinkAlive);
    //                 // Log.d(TAG, "altArticleUrl="+altArticleUrl);

    //                 Set<Problem.Topic> topics = new HashSet<Problem.Topic>();
    //                 for (int j = 0; j < jsonProblemTopics.length(); j++) {
    //                     String topic = jsonProblemTopics.getString(j);
    //                     topics.add(Problem.Topic.valueOf(topic.toUpperCase()));
    //                 }

    //                 Problem problem = null;
    //                 try {
    //                     if (Problem.Type.READING == quiz.getType())
    //                         problem = new ReadingProblem(id, quiz.getLevel(), topics, statement, jumanInfo, rightAnswer, articleUrl, isLinkAlive, altArticleUrl);
    //                     else
    //                         problem = new WritingProblem(id, quiz.getLevel(), topics, statement, jumanInfo, rightAnswer, articleUrl, isLinkAlive, altArticleUrl);
    //                 } catch (NumberFormatException e) {
    //                     e.printStackTrace();
    //                 }
    //                 if (problem != null)
    //                     problems.add(problem);
    //             } catch (JSONException e) {
    //                 e.printStackTrace();
    //             }
    //         }

    //         quiz.setProblems(problems);
    //         quiz.setCurrentAnswer("");
    //         quiz.setCurrentMode(Quiz.Mode.MODE_ASK);
    //         Problem currProb = quiz.getCurrentProblem();

    //         Intent problemActivity = (Problem.Type.READING.equals(currProb.getType()) ?
    //                 new Intent(QuizSettingsActivity.this, ReadingProblemActivity.class) :
    //                 new Intent(QuizSettingsActivity.this, WritingProblemActivity.class));
    //         startActivity(problemActivity);
    //     }

    //     private Exception exception;

    // }

    private class SignOutTask extends AsyncTask {

        protected Object doInBackground(Object... objs) {
            URL signOutUrl = (URL) objs[0];
            try {
                HttpURLConnection con = (HttpURLConnection) signOutUrl.openConnection();
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestMethod("POST");
                con.setFixedLengthStreamingMode(0);
                con.connect();

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                String status = jsonResponse.getString("status");
                Log.d(TAG, "status=" + status);
                if (!"ok".equals(status))
                    exception = new Exception("Server responded with status=" + status + ". Something is probably wrong.");

                return null;
            } catch (IOException e) {
                e.printStackTrace();
                exception = e;
            } catch (JSONException e2) {
                e2.printStackTrace();
                exception = e2;
            }

            return null;
        }

        protected void onPostExecute(final Object obj) {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            if (exception != null) {
                Log.e(TAG, "An exception has occurred: " + exception);

                AlertDialog.Builder builder = new AlertDialog.Builder(QuizSettingsActivity.this);
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

            appl.setUserName(null);
            appl.setUserEmail(null);
            appl.setUserIdToken(null);
            appl.setSessionCookie(null);

            Intent authenticationActivity = new Intent(QuizSettingsActivity.this, AuthenticationActivity.class);
            authenticationActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(authenticationActivity);
        }

        private Exception exception;

    }

    private KankenApplication appl;

    private String[] labelTopics;
    private boolean[] checkedTopics;
    private HashSet<Integer> selectedTopics = new HashSet<Integer>();

    private ProgressDialog progressDialog;

    private GoogleApiClient googleApiClient;

    private View seekBarE;

    private static final String getNextProblemsReqPath = "/cgi-bin/get_next_problems.cgi";
    private static final String signOutReqPath = "/cgi-bin/sign_out.cgi";

    private static final String TAG = "QuizSettingsActivity";

}
