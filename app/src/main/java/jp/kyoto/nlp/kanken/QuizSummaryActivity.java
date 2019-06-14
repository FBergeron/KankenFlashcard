package jp.kyoto.nlp.kanken;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class QuizSummaryActivity extends ActionActivity {

    @Override
    public void onBackPressed() {
        doLeaveSummary();
    }

    public void leaveSummary(android.view.View view) {
        doLeaveSummary();
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
            Log.d(tag, "topic="+Problem.Topic.values()[i]);
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
        finish();
    }

    private KankenApplication appl = KankenApplication.getInstance();

    private static final String tag = "QuizSummaryActivity";

}
