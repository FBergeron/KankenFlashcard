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
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Space;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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

public class QuizSettingsActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

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
                    }
                    catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    catch(IOException e2) {
                        e2.printStackTrace();
                    }
                    catch(JSONException e3) {
                        e3.printStackTrace();
                    }
                }
            }
        );
    }

    public void invokeTopicChooser(android.view.View view) {
        for (int i = 0; i < checkedTopics.length; i++)
            checkedTopics[i] = selectedTopics.contains(i);

        AlertDialog.Builder builderTopicChooser = new AlertDialog.Builder(QuizSettingsActivity.this);
        builderTopicChooser.setTitle(getResources().getString(R.string.label_topic_chooser_title));
        builderTopicChooser.setMultiChoiceItems(labelTopics, checkedTopics, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int topicIndex, boolean isChecked) {
            }
        });
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

    public void startQuiz(android.view.View view) {
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
        editor.putInt("QuizLevel", level); 

        StringBuilder strPrefTopics = new StringBuilder();
        String delimiter = "";
        for (Problem.Topic topic : quizTopics) {
            strPrefTopics.append(delimiter);
            strPrefTopics.append(topic.getLabelId());
            delimiter = ",";
        }
        editor.putString("QuizTopics", strPrefTopics.toString());

        RadioGroup radioGroupQuizType = findViewById(R.id.radioGroupQuizType);
        int selectedRadioButtonId = radioGroupQuizType.getCheckedRadioButtonId();
        Problem.Type type = (selectedRadioButtonId == R.id.radioButtonQuizTypeReading ? 
            Problem.Type.READING : Problem.Type.WRITING);
        editor.putString("QuizType", type.getLabelId());

        editor.apply();
        
        appl.startQuiz(type, quizTopics, level);

        fetchProblems(level, quizTopics, type);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_settings);

        appl = KankenApplication.getInstance();
        
        TextView textViewUserName = findViewById(R.id.textViewUserName);
        textViewUserName.setText(appl.getUserName());
        TextView textViewUserEmail = findViewById(R.id.textViewUserEmail);
        textViewUserEmail.setText(appl.getUserEmail());
        ImageView imageViewUserPicture = findViewById(R.id.imageViewUserPicture);
        Glide.with(this).load(appl.getUserPictureUrl()).into(imageViewUserPicture);

        int topicCount = Problem.Topic.values().length;
        labelTopics = new String[topicCount];
        for (int i = 0; i < topicCount; i++) {
            String strResName = "label_topic_" + Problem.Topic.values()[i].getLabelId();
            int labelId = getResources().getIdentifier(strResName, "string", QuizSettingsActivity.this.getPackageName());
            labelTopics[i] = getResources().getString(labelId);
        }

        SharedPreferences sharedPref = getSharedPreferences(Util.PREFS_GENERAL, Context.MODE_PRIVATE);

        int prefLevel = sharedPref.getInt("QuizLevel", 1);
        SeekBar seekbarQuizLevel = findViewById(R.id.seekBarQuizLevel);
        seekbarQuizLevel.setProgress(prefLevel - 1);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        // Try to adjust the width of the space objects.
        // Not perfect but looks good enough.
        Space spaceQuizLevel12 = findViewById(R.id.spaceQuizLevel12);
        Space spaceQuizLevel23 = findViewById(R.id.spaceQuizLevel23);
        Space spaceQuizLevel34 = findViewById(R.id.spaceQuizLevel34);
        Space spaceQuizLevel45 = findViewById(R.id.spaceQuizLevel45);
        ViewGroup.LayoutParams spaceParams = spaceQuizLevel12.getLayoutParams();
        spaceParams.width = (width >= 1024 ? 200 : 30);
        spaceQuizLevel12.setLayoutParams(spaceParams);
        spaceQuizLevel23.setLayoutParams(spaceParams);
        spaceQuizLevel34.setLayoutParams(spaceParams);
        spaceQuizLevel45.setLayoutParams(spaceParams);

        checkedTopics = new boolean[labelTopics.length];

        String prefTopics = sharedPref.getString("QuizTopics", "");
        HashSet<String> prefTopicLabels = new HashSet<String>(Arrays.asList(prefTopics.split(",")));
        for (int i = 0; i < Problem.Topic.values().length; i++) {
            if (prefTopicLabels.contains(Problem.Topic.values()[i].getLabelId()))
                selectedTopics.add(Integer.valueOf(i));
        }
        showSelectedTopics();

        String prefType = sharedPref.getString("QuizType", "reading");
        RadioGroup radioGroupQuizType = findViewById(R.id.radioGroupQuizType);
        if (Problem.Type.READING.getLabelId().equals(prefType)) 
            radioGroupQuizType.check(R.id.radioButtonQuizTypeReading);
        else
            radioGroupQuizType.check(R.id.radioButtonQuizTypeWriting);

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(Util.googleClientId).requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions).build();
    }

    private void showSelectedTopics() {
        StringBuilder str = new StringBuilder();
        String delimiter = "";
        int topicCount = Problem.Topic.values().length;
        for (int i = 0; i < topicCount; i++) {
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
        Log.d(tag, "fetchProblems level="+level+" topics="+topics+" type="+type);
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

            new FetchProblemsTask().execute(getNextProblemsUrl);
        }
        catch(MalformedURLException e1) {
            e1.printStackTrace();
        }
        catch(UnsupportedEncodingException e2) {
            e2.printStackTrace();
        }
        catch(IOException e3) {
            e3.printStackTrace();
        }
        catch(JSONException e4) {
            e4.printStackTrace();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View v) {
    }

    private class FetchProblemsTask extends AsyncTask {

        protected Object doInBackground(Object... objs) {
            JSONArray jsonProblems = null;
            URL getNextProblemsUrl = (URL)objs[0];
            try {
                HttpURLConnection con = (HttpURLConnection) getNextProblemsUrl.openConnection();
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
                jsonProblems = jsonResponse.getJSONArray("problems");
            }
            catch(IOException e) {
                e.printStackTrace();
                this.exception = e;
            }
            catch(JSONException e2) {
                e2.printStackTrace(); 
                this.exception = e2;
            }

            return jsonProblems;
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

            ArrayList<Problem> problems = new ArrayList<Problem>(); 
            Quiz quiz = appl.getQuiz();

            JSONArray jsonProblems = (JSONArray)obj;
            
            if (jsonProblems.length() < Quiz.DEFAULT_LENGTH) {
                AlertDialog.Builder builder = new AlertDialog.Builder(QuizSettingsActivity.this);
                builder.setTitle(getResources().getString(R.string.error_not_enough_problems_found_title))
                .setMessage(getResources().getString(R.string.error_not_enough_problems_found_msg))
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(true)
                .show();
                return;
            }

            for (int i = 0; i < jsonProblems.length(); i++) {
                try {
                    JSONArray jsonProblemData = jsonProblems.getJSONArray(i);

                    JSONArray jsonProblem = jsonProblemData.getJSONArray(0);
                    JSONArray jsonProblemTopics = jsonProblemData.getJSONArray(1);

                    String id = jsonProblem.getString(0);
                    String jumanInfo = jsonProblem.getString(1);
                    String statement = jsonProblem.getString(2);
                    String rightAnswer = jsonProblem.getString(3);
                    String articleUrl = jsonProblem.getString(4);
                    Object objIsLinkAlive = jsonProblem.get(5);
                    
                    boolean isLinkAlive = objIsLinkAlive != null && objIsLinkAlive instanceof Integer && (Integer) objIsLinkAlive == 1;

                    String altArticleUrl = null;
                    try {
                        if (!isLinkAlive && objIsLinkAlive != null && objIsLinkAlive instanceof JSONArray) {
                            StringBuilder strParams = new StringBuilder();
                            String paramDelim = "";
                            JSONArray searchTerms = (JSONArray)objIsLinkAlive;
                            for (int t = 0; t < searchTerms.length(); t++) {
                                strParams.append(paramDelim);
                                strParams.append(URLEncoder.encode(searchTerms.getString(t), "UTF-8"));
                                paramDelim = "+";
                            }
                            if (strParams.length() > 0)
                                altArticleUrl = String.format(getResources().getString(R.string.search_engine_url), strParams);
                        }
                    }
                    catch (UnsupportedEncodingException ignore) {
                        ignore.printStackTrace();
                    }

                    // Log.d(tag, "id="+id);
                    // Log.d(tag, "jumanInfo="+jumanInfo);
                    // Log.d(tag, "statement="+statement);
                    // Log.d(tag, "rightAnswer="+rightAnswer);
                    // Log.d(tag, "articleUrl="+articleUrl);
                    // Log.d(tag, "objIsLinkAlive="+objIsLinkAlive);
                    // Log.d(tag, "isLinkAlive="+isLinkAlive);
                    // Log.d(tag, "altArticleUrl="+altArticleUrl);

                    Set<Problem.Topic> topics = new HashSet<Problem.Topic>();
                    for (int j = 0; j < jsonProblemTopics.length(); j++) {
                        String topic = jsonProblemTopics.getString(j);
                        topics.add(Problem.Topic.valueOf(topic.toUpperCase()));
                    }

                    Problem problem = null;
                    try {
                        if (Problem.Type.READING == quiz.getType())
                            problem = new ReadingProblem(id, quiz.getLevel(), topics, statement, jumanInfo, rightAnswer, articleUrl, isLinkAlive, altArticleUrl);
                        else
                            problem = new WritingProblem(id, quiz.getLevel(), topics, statement, jumanInfo, rightAnswer, articleUrl, isLinkAlive, altArticleUrl);
                    }
                    catch(NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if (problem != null)
                        problems.add(problem);
                }
                catch(JSONException e) {
                    e.printStackTrace();
                }
            }

            quiz.setProblems(problems);
            quiz.setCurrentAnswer("");
            quiz.setCurrentMode(Quiz.Mode.MODE_ASK);
            Problem currProb = quiz.getCurrentProblem();

            Intent problemActivity = (Problem.Type.READING.equals(currProb.getType()) ?
                    new Intent(QuizSettingsActivity.this, ReadingProblemActivity.class) :
                    new Intent(QuizSettingsActivity.this, WritingProblemActivity.class));
            startActivity(problemActivity);
        }

        private Exception exception;

    }

    private class SignOutTask extends AsyncTask {

        protected Object doInBackground(Object... objs) {
            URL signOutUrl = (URL)objs[0];
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
                Log.d(tag,  "status="+status );            
            
                return null;
            }
            catch(IOException e) {
                e.printStackTrace(); 
                exception = e;
            }
            catch(JSONException e2) {
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
                Log.e(tag, "An exception has occurred: " + exception);

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

    private static final String getNextProblemsReqPath = "/cgi-bin/get_next_problems.cgi";
    private static final String signOutReqPath = "/cgi-bin/sign_out.cgi";

    private static final String tag = "QuizSettingsActivity";

}
