package jp.kyoto.nlp.kanken;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class QuizSettingsActivity extends AppCompatActivity {

    public static final String termsOfUsageLink = "http://www.bbc.co.uk";
    public static final String directionsLink = "http://www.radio-canada.ca";

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
        httpIntent.setData(Uri.parse(termsOfUsageLink));
        startActivity(httpIntent);
    }

    public void showDirections(android.view.View view) {
        Intent httpIntent = new Intent(Intent.ACTION_VIEW);
        httpIntent.setData(Uri.parse(directionsLink));
        startActivity(httpIntent);
    }

    public void startQuiz(android.view.View view) {
        HashSet<Problem.Topic> quizTopics = new HashSet<Problem.Topic>();
        for (Integer selectedTopic : selectedTopics)
            quizTopics.add(Problem.Topic.values()[selectedTopic.intValue()]);
        
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

        // Clear problemIndex values after 24-hours period.
        // After that period, it's expected that the problem data has been updated with
        // newer problems.
        if (sharedPref.contains("ProblemIndexLastModif")) {
            long problemIndexLastModifTime = sharedPref.getLong("ProblemIndexLastModif", 0);
            if (problemIndexLastModifTime != 0) {
                Date now = new Date();
                if (TimeUnit.HOURS.convert(now.getTime() - problemIndexLastModifTime, TimeUnit.MILLISECONDS) >= 24) {
                    for (String key : sharedPref.getAll().keySet()) {
                        if (key.startsWith("ProblemIndex")) {
                            editor.remove(key);
                            System.out.println("Remove key=" + key);
                        }
                    }

                }
            }
        }

        SeekBar seekbarQuizLevel = (SeekBar) findViewById(R.id.seekBarQuizLevel);
        int level = seekbarQuizLevel.getProgress() + 1;
        editor.putInt("QuizLevel", level); 

        StringBuffer strPrefTopics = new StringBuffer();
        String delimiter = "";
        for (Problem.Topic topic : quizTopics) {
            strPrefTopics.append(delimiter);
            strPrefTopics.append(topic.getLabelId());
            delimiter = ",";
        }
        editor.putString("QuizTopics", strPrefTopics.toString());

        RadioGroup radioGroupQuizType = (RadioGroup) findViewById(R.id.radioGroupQuizType);
        int selectedRadioButtonId = radioGroupQuizType.getCheckedRadioButtonId();
        Problem.Type type = (selectedRadioButtonId == R.id.radioButtonQuizTypeReading ? 
            Problem.Type.READING : Problem.Type.WRITING);
        editor.putString("QuizType", type.getLabelId());

        editor.commit();

        KankenApplication appl = KankenApplication.getInstance();
        fetchProblems(level, quizTopics, type);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_settings);

        int topicCount = Problem.Topic.values().length;
        labelTopics = new String[topicCount];
        for (int i = 0; i < topicCount; i++) {
            String strResName = "label_topic_" + Problem.Topic.values()[i].getLabelId();
            int labelId = getResources().getIdentifier(strResName, "string", QuizSettingsActivity.this.getPackageName());
            labelTopics[i] = getResources().getString(labelId);
        }

        SharedPreferences sharedPref = getSharedPreferences(Util.PREFS_GENERAL, Context.MODE_PRIVATE);

        int prefLevel = sharedPref.getInt("QuizLevel", 1);
        SeekBar seekbarQuizLevel = (SeekBar) findViewById(R.id.seekBarQuizLevel);
        seekbarQuizLevel.setProgress(prefLevel - 1);

        checkedTopics = new boolean[labelTopics.length];

        String prefTopics = sharedPref.getString("QuizTopics", "");
        HashSet<String> prefTopicLabels = new HashSet<String>(Arrays.asList(prefTopics.split(",")));
        for (int i = 0; i < Problem.Topic.values().length; i++) {
            if (prefTopicLabels.contains(Problem.Topic.values()[i].getLabelId()))
                selectedTopics.add(new Integer(i));
        }
        showSelectedTopics();

        String prefType = sharedPref.getString("QuizType", "reading");
        RadioGroup radioGroupQuizType = (RadioGroup) findViewById(R.id.radioGroupQuizType);
        if (Problem.Type.READING.getLabelId() == prefType) 
            radioGroupQuizType.check(R.id.radioButtonQuizTypeReading);
        else
            radioGroupQuizType.check(R.id.radioButtonQuizTypeWriting);
    }

    private void showSelectedTopics() {
        StringBuilder str = new StringBuilder();
        String delimiter = "";
        for (Integer selectedTopic : selectedTopics) {
            str.append(delimiter);
            str.append(labelTopics[selectedTopic.intValue()]);
            delimiter = ", ";
        }
        TextView textViewSelectedTopics = (TextView)findViewById(R.id.textViewSelectedTopics);
        textViewSelectedTopics.setText(str.toString());
    }

    private void fetchProblems(int level, HashSet<Problem.Topic> topics, Problem.Type type) {
        System.out.println("fetchProblems level="+level+" topics="+topics+" type="+type);
        URL getNextProblemsUrl = null;
        try {
            System.out.println("Retrieving problem batch...");

            String delim = "";
            StringBuffer topicsParam = new StringBuffer();
            List<Problem.Topic> sortedTopics = new ArrayList<Problem.Topic>(topics);
            Collections.sort(sortedTopics);
            for (Problem.Topic topic : sortedTopics) {
                topicsParam.append(delim);
                topicsParam.append(topic.toString().toLowerCase());
                delim = ",";
            }

            SharedPreferences sharedPref = getSharedPreferences(Util.PREFS_GENERAL, Context.MODE_PRIVATE);
            System.out.println("sharedPref0="+sharedPref.getAll());
            delim = "";
            StringBuffer indices = new StringBuffer();
            for (Problem.Topic topic : topics) {
                indices.append(delim);
                int problemIndex = sharedPref.getInt("ProblemIndex_" + type + "_" + topic + "_" + level, 0);   
                System.out.println("prefkey=ProblemIndex_" + type + "_" + topic + "_" + level+ " problemIndex="+problemIndex);
                indices.append("" + problemIndex);
                delim = ",";
            }
            
            getNextProblemsUrl = new URL(getNextProblemsBaseUrl + "?type=" + type.toString().toLowerCase() + "&level=" + level + "&topics=" + topicsParam + "&indices=" + indices);
            System.out.println("getNextProblemsUrl="+getNextProblemsUrl);
            new FetchProblemsTask().execute(getNextProblemsUrl);
        }
        catch(MalformedURLException e1) {
            e1.printStackTrace();
        }
    }

    public void getNextProblem(int level, HashSet<Problem.Topic> topics, Problem.Type type) {
        System.out.println("getNextProblem level="+level+" topics="+topics+" type="+type);
        URL getNextProblemsUrl = null;
        try {
            System.out.println("Retrieving problem batch...");

            String delim = "";
            StringBuffer topicsParam = new StringBuffer();
            List<Problem.Topic> sortedTopics = new ArrayList<Problem.Topic>(topics);
            Collections.sort(sortedTopics);
            for (Problem.Topic topic : sortedTopics) {
                topicsParam.append(delim);
                topicsParam.append(topic.toString().toLowerCase());
                delim = ",";
            }

            delim = "";
            StringBuffer indices = new StringBuffer();
            for (int i = 0; i < topics.size(); i++) {
                indices.append(delim);
                indices.append("0");
                delim = ",";
            }
            
            getNextProblemsUrl = new URL(getNextProblemsBaseUrl + "?type=" + type.toString().toLowerCase() + "&level=" + (level + 1) + "&topics=" + topicsParam + "&indices=" + indices);
            System.out.println("getNextProblemsUrl="+getNextProblemsUrl);
            new FetchProblemsTask().execute(getNextProblemsUrl);
        }
        catch(MalformedURLException e1) {
            e1.printStackTrace();
        }
    }

    private class FetchProblemsTask extends AsyncTask {

        protected Object doInBackground(Object... objs) {
            System.out.println("doInBackground url="+objs);

            JSONArray jsonProblems = null;
            try {
                JSONObject jsonResponse = Util.readJson((URL)objs[0]);
                jsonProblems = jsonResponse.getJSONArray("problems");
            }
            catch(IOException e1) {
                this.exception = e1;
            }
            catch(JSONException e2) {
                this.exception = e2;
            }

            return jsonProblems;
        }

        protected void onPostExecute(Object obj) {
            System.out.println("onPostExecute obj="+obj);

            if (exception != null) {
                System.out.println("An exception has occured: " + exception);
                return;
            }

            ArrayList<Problem> problems = new ArrayList<Problem>(); 

            JSONArray jsonProblems = (JSONArray)obj;
            for (int i = 0; i < jsonProblems.length(); i++) {
                try {
                    JSONArray jsonProblem = jsonProblems.getJSONArray(i);
                    String id = jsonProblem.getString(0);
                    String juman = jsonProblem.getString(1);
                    String statement = jsonProblem.getString(2);
                    String rightAnswer = jsonProblem.getString(3);
                    String articleUrl = jsonProblem.getString(4);
                    int isLinkAlive = jsonProblem.getInt(5);
                    String topic = jsonProblem.getString(6);
                    String type = jsonProblem.getString(7);
                    int level = jsonProblem.getInt(8);

                    // System.out.println("id="+id);
                    // System.out.println("juman="+juman);
                    // System.out.println("statement="+statement);
                    // System.out.println("rightAnswer="+rightAnswer);
                    // System.out.println("articleUrl="+articleUrl);
                    // System.out.println("isLinkAlive="+isLinkAlive);
                    // System.out.println("topic="+topic);
                    // System.out.println("type="+type);
                    // System.out.println("level="+level);

                    Problem problem = null;
                    try {
                        if (Problem.Type.READING.getLabelId().equals(type))
                            problem = new ReadingProblem(id, level, Problem.Topic.valueOf(topic.toUpperCase()), statement, rightAnswer, articleUrl);
                        else
                            problem = new WritingProblem(id, level, Problem.Topic.valueOf(topic.toUpperCase()), statement, rightAnswer, articleUrl);
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

            KankenApplication appl = KankenApplication.getInstance();
            appl.startQuiz();
            Quiz quiz = appl.getQuiz();
            quiz.setProblems(problems);
            Problem currProb = quiz.getCurrentProblem();

            Intent problemActivity = (Problem.Type.READING.equals(currProb.getType()) ?
                    new Intent(QuizSettingsActivity.this, ReadingProblemActivity.class) :
                    new Intent(QuizSettingsActivity.this, WritingProblemActivity.class));
            startActivity(problemActivity);
        }

        private Exception exception;

    }

    private String[] labelTopics;
    private boolean[] checkedTopics;
    private HashSet<Integer> selectedTopics = new HashSet<Integer>();

    private String sharedPrefFile = "jp.kyoto.nlp.kanken.KankenApplPrefs";

    private static final String getNextProblemsBaseUrl = "http://lotus.kuee.kyoto-u.ac.jp/~frederic/KankenFlashcardServer/cgi-bin/get_next_problems.cgi";

}
