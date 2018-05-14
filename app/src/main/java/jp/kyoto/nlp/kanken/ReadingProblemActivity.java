package jp.kyoto.nlp.kanken;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

public class ReadingProblemActivity extends AppCompatActivity {

    public void showArticle(android.view.View view) {
        String articleUrl = appl.getQuiz().getCurrentProblem().getArticleUrl();
        if (articleUrl != null) {
            Intent httpIntent = new Intent(Intent.ACTION_VIEW);
            httpIntent.setData(Uri.parse(articleUrl));
            startActivity(httpIntent);
        }
    }
    
    public void validateAnswer(android.view.View view) {
        EditText editTextAnswer = (EditText)findViewById(R.id.editTextAnswer);
        String answer = editTextAnswer.getText().toString();

        if (answer.trim().equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ReadingProblemActivity.this);
            builder.setTitle(getResources().getString(R.string.error_empty_answer_title))
            .setMessage(getResources().getString(R.string.error_empty_answer_msg))
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) { 
                }
             })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setCancelable(true)
            .show();
            return;
        }

        appl.getQuiz().validateAnswer(answer);
        
        Intent problemEvaluationActivity = new Intent(ReadingProblemActivity.this, ProblemEvaluationActivity.class);
        startActivity(problemEvaluationActivity);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_problem);

        Problem currProb = appl.getQuiz().getCurrentProblem();

        TextView textViewProblemInfoLevel = (TextView)findViewById(R.id.textViewProblemInfoLevel);
        String strLevel = String.format(getResources().getString(R.string.label_problem_info_level), currProb.getLevel());
        textViewProblemInfoLevel.setText(strLevel);

        TextView textViewProblemInfoTopic = (TextView)findViewById(R.id.textViewProblemInfoTopic);
        String strResName = "label_topic_" + currProb.getTopic().getLabelId();
        int labelId = getResources().getIdentifier(strResName, "string", ReadingProblemActivity.this.getPackageName());
        String strTopic = String.format(getResources().getString(R.string.label_problem_info_topic), getResources().getString(labelId));
        textViewProblemInfoTopic.setText(strTopic);

        TextView textViewProblemInfoType = (TextView)findViewById(R.id.textViewProblemInfoType);
        strResName = "label_quiz_type_" + currProb.getType().getLabelId();
        labelId = getResources().getIdentifier(strResName, "string", ReadingProblemActivity.this.getPackageName());
        String strType = String.format(getResources().getString(R.string.label_problem_info_type), getResources().getString(labelId));
        textViewProblemInfoType.setText(strType);

        StringBuffer stmt = new StringBuffer();
        stmt.append("<html>");
        stmt.append("<head>");
        stmt.append("<style type\"text/css\">");
        stmt.append("body { font-size: x-large;}");
        stmt.append("em { color: red; font-weight: bold;}");
        stmt.append("</style>");
        stmt.append("</head>");
        stmt.append("<body>" + currProb.getStatement().replace("[", "<em>").replace("]", "</em>")  + "</body>");
        stmt.append("</html>");

        WebView webViewProblemStatement = (WebView)findViewById(R.id.webViewProblemStatement);
        webViewProblemStatement.loadData(stmt.toString(), "text/html; charset=utf-8", "utf-8");
    }

    KankenApplication appl = KankenApplication.getInstance();

}
