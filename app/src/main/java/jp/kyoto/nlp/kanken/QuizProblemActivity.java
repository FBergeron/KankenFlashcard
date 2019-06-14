package jp.kyoto.nlp.kanken;

import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public abstract class QuizProblemActivity extends ActionActivity {

    protected void showProblemStatement() {
        Problem currProb = appl.getQuiz().getCurrentProblem();
        int currProbIndex = appl.getQuiz().getCurrentProblemIndex();

        TextView textViewProblemInfoLevel = findViewById(R.id.textViewProblemInfoLevel);
        String strLevel = String.format(getResources().getString(R.string.label_problem_info_level), currProb.getLevel());
        textViewProblemInfoLevel.setText(strLevel);

        TextView textViewProblemInfoTopic = findViewById(R.id.textViewProblemInfoTopic);
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

        TextView textViewProblemNumber = findViewById(R.id.textViewProblemNumber);
        String strProblemNumber = String.format(getResources().getString(R.string.label_problem_number), currProbIndex + 1, Quiz.DEFAULT_LENGTH);
        textViewProblemNumber.setText(strProblemNumber);

        TextView statement = findViewById(R.id.statement);
        statement.setText(Html.fromHtml(currProb.getStatement().replace("[", "<u><font color=\"red\">").replace("]", "</font></u>")));

    }

    protected void askProblem() {
    }

    protected void showProblemEvaluation() {
        Problem currProb = appl.getQuiz().getCurrentProblem();

        ImageButton imageButtonViewArticle = findViewById(R.id.imageButtonViewProblemArticle);
        if (currProb.isArticleLinkAlive()) {
            imageButtonViewArticle.setVisibility(VISIBLE);
        }
        else {
            imageButtonViewArticle.setVisibility(GONE);
        }

        ProblemEvaluationFragment problemEvaluationFragment = (ProblemEvaluationFragment)getFragmentManager().findFragmentById(R.id.fragmentProblemEvaluation);
        problemEvaluationFragment.showProblemEvaluation();
    }

    private KankenApplication appl = KankenApplication.getInstance();

}

