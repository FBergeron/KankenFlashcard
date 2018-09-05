package jp.kyoto.nlp.kanken;

import android.app.Fragment;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public abstract class QuizProblemActivity extends AppCompatActivity {

    protected void showProblemStatement() {
        Problem currProb = appl.getQuiz().getCurrentProblem();
        int currProbIndex = appl.getQuiz().getCurrentProblemIndex();

        TextView textViewProblemInfoLevel = (TextView)findViewById(R.id.textViewProblemInfoLevel);
        String strLevel = String.format(getResources().getString(R.string.label_problem_info_level), currProb.getLevel());
        textViewProblemInfoLevel.setText(strLevel);

        TextView textViewProblemInfoTopic = (TextView)findViewById(R.id.textViewProblemInfoTopic);
        // Just show the first pertinent topic.
        for (Problem.Topic topic : currProb.getTopics()) {
            if (appl.getQuiz().getTopics().contains(topic)) {
                String strResName = "label_topic_" + topic.getLabelId();
                int labelId = getResources().getIdentifier(strResName, "string", QuizProblemActivity.this.getPackageName());
                String strTopic = String.format(getResources().getString(R.string.label_problem_info_topic), getResources().getString(labelId));
                textViewProblemInfoTopic.setText(strTopic);
                break;
            }
        }

        TextView textViewProblemInfoType = (TextView)findViewById(R.id.textViewProblemInfoType);
        String strResName = "label_quiz_type_" + currProb.getType().getLabelId();
        int labelId = getResources().getIdentifier(strResName, "string", QuizProblemActivity.this.getPackageName());
        String strType = String.format(getResources().getString(R.string.label_problem_info_type), getResources().getString(labelId));
        textViewProblemInfoType.setText(strType);

        TextView textViewProblemNumber = (TextView)findViewById(R.id.textViewProblemNumber);
        String strProblemNumber = String.format(getResources().getString(R.string.label_problem_number), currProbIndex + 1, Quiz.DEFAULT_LENGTH);
        textViewProblemNumber.setText(strProblemNumber);

        StringBuffer stmt = new StringBuffer();
        stmt.append("<html>");
        stmt.append("<head>");
        stmt.append("<style type\"text/css\">");
        stmt.append("body { font-size: x-large;}");
        stmt.append("em { color: red; font-weight: bold; font-style: normal;}");
        stmt.append("</style>");
        stmt.append("</head>");
        stmt.append("<body>" + currProb.getStatement().replace("[", "<em>").replace("]", "</em>")  + "</body>");
        stmt.append("</html>");

        WebView webViewProblemStatement = (WebView)findViewById(R.id.webViewProblemStatement);
        webViewProblemStatement.loadData(stmt.toString(), "text/html; charset=utf-8", "utf-8");
    }

    protected void askProblem() {
        Problem currProb = appl.getQuiz().getCurrentProblem();
        int currProbIndex = appl.getQuiz().getCurrentProblemIndex();
        
        findViewById(R.id.imageButtonViewProblemArticle).setVisibility(GONE);
        findViewById(R.id.imageButtonSearchProblemArticle).setVisibility(GONE);
    }

    protected void showProblemEvaluation() {
        Problem currProb = appl.getQuiz().getCurrentProblem();
        int currProbIndex = appl.getQuiz().getCurrentProblemIndex();

        ImageButton imageButtonViewArticle = (ImageButton)findViewById(R.id.imageButtonViewProblemArticle);
        ImageButton imageButtonSearchProblemArticle = (ImageButton)findViewById(R.id.imageButtonSearchProblemArticle);
        if (currProb.isArticleLinkAlive()) { 
            imageButtonViewArticle.setVisibility(VISIBLE);
            imageButtonSearchProblemArticle.setVisibility(GONE);
        }
        else {
            imageButtonViewArticle.setVisibility(GONE);
            imageButtonSearchProblemArticle.setVisibility(VISIBLE);
        }

        ProblemEvaluationFragment problemEvaluationFragment = (ProblemEvaluationFragment)getFragmentManager().findFragmentById(R.id.fragmentProblemEvaluation);
        problemEvaluationFragment.showProblemEvaluation();
    }

    private KankenApplication appl = KankenApplication.getInstance();

}

