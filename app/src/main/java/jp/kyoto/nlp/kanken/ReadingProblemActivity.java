package jp.kyoto.nlp.kanken;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ReadingProblemActivity extends AppCompatActivity {

    private final static int MAX_ANSWER_LENGTH = 16;

    @Override
    public void onBackPressed() {
        // Prevent the user to come back once the quiz is started.
    }

    public void deleteKana(android.view.View view) {
        TextView textViewReadingProblemUserAnswer = (TextView)findViewById(R.id.textViewReadingProblemUserAnswer);
        String answer = textViewReadingProblemUserAnswer.getText().toString();
        String foundKana = null;
        String subword = null;
        if (answer.length() > 1) {
            subword = answer.substring(answer.length() - 2, answer.length());
            foundKana = Util.findKana(subword);
            if (foundKana != null) {
                textViewReadingProblemUserAnswer.setText(answer.substring(0, answer.length() - 2));
                return;
            }
        }
        if (answer.length() > 0) {
            subword = answer.substring(answer.length() - 1, answer.length()); 
            foundKana = Util.findKana(subword);
            if (foundKana != null) {
                textViewReadingProblemUserAnswer.setText(answer.substring(0, answer.length() - 1));
                return;
            }
        }
    }

    public void enterKana(android.view.View view) {
        for (int i = 0; i < buttonKanas.size(); i++) {
            int buttonNumber = i +  1;
            String buttonName = "buttonKana" + (buttonNumber < 10 ? "0" : "") + buttonNumber;
            int buttonId = getResources().getIdentifier(buttonName, "id", ReadingProblemActivity.this.getPackageName());
            if (buttonId == view.getId()) {
                TextView textViewReadingProblemUserAnswer = (TextView)findViewById(R.id.textViewReadingProblemUserAnswer);
                if (textViewReadingProblemUserAnswer.getText().toString().length() < MAX_ANSWER_LENGTH)
                    textViewReadingProblemUserAnswer.setText(textViewReadingProblemUserAnswer.getText() + buttonKanas.get(i));
            }
        }
    }
    
    public void validateAnswer(android.view.View view) {
        TextView textViewReadingProblemUserAnswer = (TextView)findViewById(R.id.textViewReadingProblemUserAnswer);
        final String answer = textViewReadingProblemUserAnswer.getText().toString();

        if (answer.trim().equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ReadingProblemActivity.this);
            builder.setTitle(getResources().getString(R.string.error_empty_answer_title))
            .setMessage(getResources().getString(R.string.error_empty_answer_msg))
            .setPositiveButton(R.string.button_continue, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) { 
                    appl.getQuiz().validateAnswer(answer);
                   
                    Intent problemEvaluationActivity = new Intent(ReadingProblemActivity.this, ProblemEvaluationActivity.class);
                    startActivity(problemEvaluationActivity);
                }
             })
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
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
        int currProbIndex = appl.getQuiz().getCurrentProblemIndex();

        TextView textViewProblemInfoLevel = (TextView)findViewById(R.id.textViewReadingProblemInfoLevel);
        String strLevel = String.format(getResources().getString(R.string.label_problem_info_level), currProb.getLevel());
        textViewProblemInfoLevel.setText(strLevel);

        TextView textViewProblemInfoTopic = (TextView)findViewById(R.id.textViewReadingProblemInfoTopic);
        // Just show the first pertinent topic.
        for (Problem.Topic topic : currProb.getTopics()) {
            if (appl.getQuiz().getTopics().contains(topic)) {
                String strResName = "label_topic_" + topic.getLabelId();
                int labelId = getResources().getIdentifier(strResName, "string", ReadingProblemActivity.this.getPackageName());
                String strTopic = String.format(getResources().getString(R.string.label_problem_info_topic), getResources().getString(labelId));
                textViewProblemInfoTopic.setText(strTopic);
                break;
            }
        }

        TextView textViewProblemInfoType = (TextView)findViewById(R.id.textViewReadingProblemInfoType);
        String strResName = "label_quiz_type_" + currProb.getType().getLabelId();
        int labelId = getResources().getIdentifier(strResName, "string", ReadingProblemActivity.this.getPackageName());
        String strType = String.format(getResources().getString(R.string.label_problem_info_type), getResources().getString(labelId));
        textViewProblemInfoType.setText(strType);

        TextView textViewReadingProblemNumber = (TextView)findViewById(R.id.textViewReadingProblemNumber);
        String strProblemNumber = String.format(getResources().getString(R.string.label_problem_number), currProbIndex + 1, Quiz.DEFAULT_LENGTH);
        textViewReadingProblemNumber.setText(strProblemNumber);

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

        WebView webViewProblemStatement = (WebView)findViewById(R.id.webViewReadingProblemStatement);
        webViewProblemStatement.loadData(stmt.toString(), "text/html; charset=utf-8", "utf-8");
        
        buttonKanas = new ArrayList<String>();

        ArrayList<String> answerKanas = Util.findKanasFrom(currProb.getRightAnswer());
        buttonKanas.addAll(answerKanas);

        while (buttonKanas.size() < 9) {
            String fillerKana = null;
            try {
                fillerKana = Util.findRandomKana();
            }
            catch (IOException ignore) {
                ignore.printStackTrace();
            }
            if (fillerKana != null && !buttonKanas.contains(fillerKana))
                buttonKanas.add(fillerKana);
        }

        Random r = new Random();
        ArrayList<String> shuffledButtonKanas = new ArrayList<String>();
        while (!buttonKanas.isEmpty())
            shuffledButtonKanas.add(buttonKanas.remove(r.nextInt(buttonKanas.size())));
        buttonKanas = shuffledButtonKanas;

        for (int i = 0; i < buttonKanas.size(); i++) {
            int buttonNumber = i +  1;
            String buttonName = "buttonKana" + (buttonNumber < 10 ? "0" : "") + buttonNumber;
            int buttonId = getResources().getIdentifier(buttonName, "id", ReadingProblemActivity.this.getPackageName());
            Button button = (Button)findViewById(buttonId);
            button.setText(buttonKanas.get(i));
        }
    }

    KankenApplication appl = KankenApplication.getInstance();

    private ArrayList<String> buttonKanas;

}
