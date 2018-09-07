package jp.kyoto.nlp.kanken;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

import java.util.Iterator;

public class QuizSummaryActivity extends AppCompatActivity {

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

        int topicCount = Problem.Topic.values().length;
        labelTopics = new String[topicCount];
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
            System.out.println("topic="+Problem.Topic.values()[i]);
            if (appl.getQuiz().getTopics().contains(Problem.Topic.values()[i])) {
                strTopics.append(delimiter);
                strTopics.append(labelTopics[i]);
                delimiter = ", ";
            }
        }

        TextView textViewSummaryTypeAndLevel = (TextView)findViewById(R.id.textViewSummaryTypeAndLevel);
        textViewSummaryTypeAndLevel.setText(strType + "; " + strLevel + "; " + strTopics);
        
        StringBuffer summary = new StringBuffer();
        summary.append("<html>\n");
        summary.append("<head>\n");
        summary.append("<head>\n");
        summary.append("<style type\"text/css\">\n");
        summary.append(".stmt { text-align: left; border-bottom: 1px solid; }\n");
        summary.append(".link { text-align: center; border-bottom: 1px solid; }\n");
        summary.append(".rotate { text-align: center; white-space: nowrap; vertical-align: middle; width: 1.5em; }\n");
        summary.append(".rotate div { -webkit-transform: rotate(-90.0deg); margin-left: -10em; margin-right: -10em; }\n");
        summary.append("body { font-size: 24px; }\n");
        summary.append("em { color: red; font-weight: bold; font-style: normal; }\n");
        summary.append("table { width: 95%; border: 1px solid; margin: 6px 6px 12px 6px; border-collapse: collapse; }\n");
        summary.append("table th.problem { border: 1px solid #333333; background-color: #6666ff; color: #ffffff; padding: 6px; }\n");
        summary.append("table th.topic { border: 1px solid #333333; background-color: #9999ff; color: #ffffff; padding: 6px; font-size: smaller;}\n");
        summary.append("table td { background-color: #ffffff; color: #000000; padding: 6px; text-align: center; }\n");
        summary.append("table td.label { border: 1px solid #333333; background-color: #ccccff; color: #000000; padding: 6px; }\n");
        summary.append("table td.reported { border: 1px solid #333333; background-color: #f8c461; color: #ff0000; padding: 6px; text-align: center;}\n");
        summary.append("</style>\n");
        summary.append("</head>\n");
        summary.append("<body>\n");

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
                    strResName = "label_topic_" + topic.getLabelId();
                    labelId = getResources().getIdentifier(strResName, "string", QuizSummaryActivity.this.getPackageName());
                    strTopic = String.format(getResources().getString(R.string.label_problem_info_topic), getResources().getString(labelId));
                    break;
                }
            }

            summary.append("<table class=\"problem\">\n");
            String problemLabel = String.format(getResources().getString(R.string.label_summary_problem), (i+1));
            String link = (problem.isArticleLinkAlive() ? problem.getArticleUrl() : problem.getAltArticleUrl());
            String buttonImg = (problem.isArticleLinkAlive() ? "view-article" : "search");
            String statementLabel = getResources().getString(R.string.label_summary_statement);
            summary.append("<tr>");
            summary.append("<th width=\"6%\" class=\"rotate problem\" rowspan=\"2\"><div>" + problemLabel + "</div></th>");
            summary.append("<th width=\"6%\" class=\"rotate topic\" rowspan=\"2\"><div>" + strTopic + "</div></th>");
            summary.append("<td width=\"12%\" class=\"label\">" + statementLabel + "</td>");
            summary.append("<td width=\"72%\" colspan=\"6\" class=\"stmt\">" + problem.getStatement().replace("[", "<em>").replace("]", "</em>") + "</td>\n");
            summary.append("<td width=\"12%\" align=\"center\" class=\"link\"><a target=\"_blank\" href=\"" + link + "\"><img width=\"32\" height=\"32\" src=\"" + buttonImg + ".svg\"/></a></td>\n");
            summary.append("</tr>\n");
            summary.append("<tr>\n");
            String userAnswerLabel = getResources().getString(R.string.label_summary_user_answer);
            summary.append("<td class=\"label\" width=\"12%\">" + userAnswerLabel + "</td>\n");
            summary.append("<td width=\"16%\">" + answer + "</td>\n");
            String rightAnswerLabel = getResources().getString(R.string.label_summary_right_answer);
            summary.append("<td class=\"label\" width=\"12%\">" + rightAnswerLabel + "</td>\n");
            summary.append("<td width=\"16%\">" + problem.getRightAnswer() + "</td>\n");
            String outcomeLabel = getResources().getString(R.string.label_summary_outcome);
            summary.append("<td class=\"label\" width=\"8%\">" + outcomeLabel + "</td>\n");
            summary.append("<td width=\"10%\" align=\"center\"><img width=\"32\" height\"32\" src=\"" + (isRightAnswer ? "right" : "wrong") + ".svg\"/></td>\n");
            if (isReportedAsIncorrect.booleanValue()) {
                String reportedLabel = getResources().getString(R.string.label_summary_reported);
                summary.append("<td colspan=\"2\" class=\"reported\" width=\"20%\">" + reportedLabel + "</td>\n");
            }
            else {
                String familiarityLabel = getResources().getString(R.string.label_summary_familiarity);
                summary.append("<td class=\"label\" width=\"8%\">" + familiarityLabel + "</td>\n");
                summary.append("<td width=\"6%\">" + familiarity + "</td>\n");
            }
            summary.append("</tr>\n");
            summary.append("</table>\n");
        }

        summary.append("</body>\n");
        summary.append("</html>\n");

        // System.out.println("html="+summary.toString());

        WebView webViewSummary = (WebView)findViewById(R.id.webViewSummary);
        webViewSummary.loadDataWithBaseURL("file:///android_asset/", summary.toString(), "text/html; charset=utf-8", "utf-8", null);
    }

    private void doLeaveSummary() {
        Intent quizSettingsActivity = new Intent(QuizSummaryActivity.this, QuizSettingsActivity.class);
        startActivity(quizSettingsActivity);
    }

    private String[] labelTopics;

    private KankenApplication appl = KankenApplication.getInstance();

}
