package jp.kyoto.nlp.kanken;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class QuizSummaryActivity extends ActionActivity {

    @Override
    public void onBackPressed() {
        doLeaveSummary();
    }

    public void leaveSummary(android.view.View view) {
        doLeaveSummary();
    }

    public void playAgain(android.view.View view) {
        appl.playAgain();
        fetchProblems(appl.getQuiz().getLevel(), appl.getQuiz().getTopics(), appl.getQuiz().getType());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_summary);

        ListView listView = findViewById(R.id.listView);
        SummaryListViewAdapter listViewAdapter = new SummaryListViewAdapter(this, getLayoutInflater());
        listView.setAdapter(listViewAdapter);
        List<SummaryItem> summaryItems = new ArrayList<>();

        int topicCount = Problem.Topic.values().length;
        String[] labelTopics = new String[topicCount];
        for (int i = 0; i < topicCount; i++) {
            String strResName = "label_topic_" + Problem.Topic.values()[i].getLabelId();
            int labelId = getResources().getIdentifier(strResName, "string", QuizSummaryActivity.this.getPackageName());
            labelTopics[i] = getResources().getString(labelId);
        }

        String strResName = "label_quiz_type_" + appl.getQuiz().getType().getLabelId();
        int labelId = getResources().getIdentifier(strResName, "string", QuizSummaryActivity.this.getPackageName());
        String strType = String.format(getResources().getString(R.string.label_problem_info_type), getResources().getString(labelId));

        String strLevel = String.format(getResources().getString(R.string.label_problem_info_level), appl.getQuiz().getLevel());

        StringBuilder strTopics = new StringBuilder();
        String delimiter = "";
        for (int i = 0; i < topicCount; i++) {
            Log.d(TAG, "topic="+Problem.Topic.values()[i]);
            if (appl.getQuiz().getTopics().contains(Problem.Topic.values()[i])) {
                strTopics.append(delimiter);
                strTopics.append(labelTopics[i]);
                delimiter = ", ";
            }
        }

        Quiz quiz = appl.getQuiz();
        int length = quiz.getLength();
        Iterator<Problem> itProblem = quiz.getProblems();
        Iterator<String> itAnswer = quiz.getAnswers();
        Iterator<Boolean> itRightAnswer = quiz.getRightAnswers();
        Iterator<Integer> itFamiliarity = quiz.getFamiliarities();
        Iterator<Boolean> itReported = quiz.getReportedAsIncorrects();
        for (int i = 0; i < length; i++) {
            Problem problem = itProblem.next();
            String answer = itAnswer.next();
            Boolean isRightAnswer = itRightAnswer.next();
            Integer familiarity = itFamiliarity.next();
            Boolean isReportedAsIncorrect = itReported.next();

            String strTopic = "";
            // Just show the first pertinent topic.
            for (Problem.Topic topic : problem.getTopics()) {
                if (appl.getQuiz().getTopics().contains(topic)) {
                    strResName = "label_topic_" + topic.getLabelId() + "_short";
                    labelId = getResources().getIdentifier(strResName, "string", QuizSummaryActivity.this.getPackageName());
                    strTopic = getResources().getString(labelId);
                    break;
                }
            }

            int strFamiliarityId = 0;
            if (isReportedAsIncorrect) {
                strFamiliarityId = R.string.nothing_familiarity;
            } else {
                strFamiliarityId = getResources().getIdentifier("label_familiarity_long_" + familiarity, "string", QuizSummaryActivity.this.getPackageName());
            }
            String strFamiliarity = getResources().getString(strFamiliarityId);


            String problemLabel = String.format(getResources().getString(R.string.label_problem_number), (i+1), length);
            String link = (problem.isArticleLinkAlive() ? problem.getArticleUrl() : problem.getAltArticleUrl());
            String statement =  problem.getStatement();
            String rightAnswer = problem.getRightAnswer();

            SummaryItem summaryItem = new SummaryItem(
                    answer,
                    rightAnswer,
                    isRightAnswer,
                    strFamiliarity,
                    link,
                    strTopic,
                    strLevel,
                    problemLabel,
                    statement
            );
            summaryItems.add(summaryItem);
        }

        listViewAdapter.setItems(summaryItems);
    }

    private void doLeaveSummary() {
        Intent quizSettingsActivity = new Intent(QuizSummaryActivity.this, QuizSettingsActivity.class);
        startActivity(quizSettingsActivity);
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

            getNextProblemsUrl = new URL(appl.getServerBaseUrl() + KankenApplication.getNextProblemsReqPath +
                    "?type=" + URLEncoder.encode(type.toString().toLowerCase()) +
                    "&level=" + URLEncoder.encode(level + "", "UTF-8") +
                    "&topics=" + URLEncoder.encode(topicsParam.toString(), "UTF-8"));

            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getResources().getString(R.string.label_fetching_quiz_data));
            progressDialog.setCancelable(false);
            progressDialog.show();

            new FetchProblemsTask(appl, progressDialog, QuizSummaryActivity.this).execute(getNextProblemsUrl);
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

    private ProgressDialog progressDialog;

    private KankenApplication appl = KankenApplication.getInstance();

    private static final String TAG = "QuizSummaryActivity";

}
