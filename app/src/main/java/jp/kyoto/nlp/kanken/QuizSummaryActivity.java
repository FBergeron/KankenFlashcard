package jp.kyoto.nlp.kanken;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import java.util.Iterator;

public class QuizSummaryActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        // Prevent the user to come back once the quiz is started.
    }

    public void leaveSummary(android.view.View view) {
        Intent quizSettingsActivity = new Intent(QuizSummaryActivity.this, QuizSettingsActivity.class);
        startActivity(quizSettingsActivity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_summary);

        StringBuffer summary = new StringBuffer();
        summary.append("<html>");
        summary.append("<head>");
        summary.append("<head>");
        summary.append("<style type\"text/css\">");
        summary.append(".rotate { text-align: center; white-space: nowrap; vertical-align: middle; width: 1.5em; }");
        summary.append(".rotate div { -webkit-transform: rotate(-90.0deg); margin-left: -10em; margin-right: -10em; }");
        summary.append("body { font-size: 24px; }");
        summary.append("em { color: red; font-weight: bold; }");
        summary.append("table { width: 95%; border: 1px solid; margin: 10px 10px 20px 10px; border-collapse: collapse; }");
        summary.append("table th { border: 1px solid #333333; background-color: #6666ff; color: #ffffff; padding: 10px; }");
        summary.append("table td { border: 1px solid #333333; background-color: #ffffff; color: #000000; padding: 10px; }");
        summary.append("table td.label { border: 1px solid #333333; background-color: #ccccff; color: #000000; padding: 10px; }");
        summary.append("</style>");
        summary.append("</head>");
        summary.append("<body>");

        Quiz quiz = appl.getQuiz();
        int length = quiz.getLength();
        Iterator<Problem> itProblem = quiz.getProblems();
        Iterator<String> itAnswer = quiz.getAnswers();
        Iterator<Boolean> itRightAnswer = quiz.getRightAnswers();
        Iterator<Integer> itFamiliarity = quiz.getFamiliarities();
        for (int i = 0; i < length; i++) {
            Problem problem = itProblem.next();
            String answer = itAnswer.next();
            Boolean isRightAnswer = itRightAnswer.next();
            Integer familiarity = itFamiliarity.next();
            summary.append("<table class=\"problem\">");
            String problemLabel = String.format(getResources().getString(R.string.label_summary_problem), (i+1));
            String link = (problem.isArticleLinkAlive() ? problem.getArticleUrl() : "");
            String buttonImg = (problem.isArticleLinkAlive() ? "view-article" : "search");
            String statementLabel = getResources().getString(R.string.label_summary_statement);
            summary.append("<tr><th width=\"4%\" class=\"rotate\" rowspan=\"2\"><div width=\"12%\">" + problemLabel + "</div></th><td width=\"12%\" class=\"label\">" + statementLabel + "</td><td width=\"72%\" colspan=\"6\">" + problem.getStatement().replace("[", "<em>").replace("]", "</em>") + "</td>");
            summary.append("<td align=\"center\"><a target=\"_blank\" href=\"" + link + "\"><img width=\"32\" height=\"32\" src=\"" + buttonImg + ".svg\"/></a></td>");
            summary.append("</tr>");
            summary.append("<tr>");
            String userAnswerLabel = getResources().getString(R.string.label_summary_user_answer);
            summary.append("<td class=\"label\" width=\"12%\">" + userAnswerLabel + "</td>");
            summary.append("<td width=\"12%\">" + answer + "</td>");
            String rightAnswerLabel = getResources().getString(R.string.label_summary_right_answer);
            summary.append("<td class=\"label\" width=\"12%\">" + rightAnswerLabel + "</td>");
            summary.append("<td width=\"12%\">" + problem.getRightAnswer() + "</td>");
            String outcomeLabel = getResources().getString(R.string.label_summary_outcome);
            summary.append("<td class=\"label\" width=\"12%\">" + outcomeLabel + "</td>");
            summary.append("<td width=\"12%\" align=\"center\"><img width=\"32\" height\"32\" src=\"" + (isRightAnswer ? "right" : "wrong") + ".svg\"/></td>");
            String familiarityLabel = getResources().getString(R.string.label_summary_familiarity);
            summary.append("<td class=\"label\" width=\"12%\">" + familiarityLabel + "</td>");
            summary.append("<td width=\"12%\">" + familiarity + "</td>");
            summary.append("</tr>");
            summary.append("</table>");
        }

        summary.append("</body>");
        summary.append("</html>");

        WebView webViewSummary = (WebView)findViewById(R.id.webViewSummary);
        webViewSummary.loadDataWithBaseURL("file:///android_asset/", summary.toString(), "text/html; charset=utf-8", "utf-8", null);
    }

    KankenApplication appl = KankenApplication.getInstance();

}
