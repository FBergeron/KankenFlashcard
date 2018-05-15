package jp.kyoto.nlp.kanken;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import java.util.Iterator;

public class QuizSummaryActivity extends AppCompatActivity {

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
        summary.append("body { font-size: x-large;}");
        summary.append("em { color: red; font-weight: bold;}");
        summary.append("</style>");
        summary.append("</head>");
        summary.append("<body>");

        int length = appl.getQuiz().getLength();
        Iterator itProblem = appl.getQuiz().getProblems();
        Iterator itAnswer = appl.getQuiz().getAnswers();
        for (int i = 0; i < length; i++) {
            Problem problem = (Problem)itProblem.next();
            String answer = (String)itAnswer.next();
            summary.append("<u>Problem " + i + "</u><br/>");
            summary.append("Statement: " + problem.getStatement().replace("[", "<em>").replace("]", "</em>") + "<br/>");
            summary.append("Your answer: " + answer + "<br/>");
            summary.append("Link: <a target=\"_blank\" href=\"" + problem.getArticleUrl() + "\">" + problem.getArticleUrl() + "</a><br/><br/>");
        }

        summary.append("</body>");
        summary.append("</html>");

        WebView webViewSummary = (WebView)findViewById(R.id.webViewSummary);
        webViewSummary.loadData(summary.toString(), "text/html; charset=utf-8", "utf-8");

    }

    KankenApplication appl = KankenApplication.getInstance();

}
