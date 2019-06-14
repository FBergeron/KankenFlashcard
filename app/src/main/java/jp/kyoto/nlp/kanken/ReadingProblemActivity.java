package jp.kyoto.nlp.kanken;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ReadingProblemActivity extends QuizProblemActivity {

    private final static int MAX_ANSWER_LENGTH = 10;

    public void showArticle(android.view.View view) {
        String articleUrl = appl.getQuiz().getCurrentProblem().getArticleUrl();
        if (articleUrl != null) {
            Intent httpIntent = new Intent(Intent.ACTION_VIEW);
            httpIntent.setData(Uri.parse(articleUrl));
            startActivity(httpIntent);
        }
    }

    public void search(android.view.View view) {
        String altArticleUrl = appl.getQuiz().getCurrentProblem().getAltArticleUrl();
        if (altArticleUrl != null) {
            Intent httpIntent = new Intent(Intent.ACTION_VIEW);
            httpIntent.setData(Uri.parse(altArticleUrl));
            startActivity(httpIntent);
        }
    }

    @Override
    public void onBackPressed() {
        Util.goBackToSettings(ReadingProblemActivity.this);
    }

    public void deleteKana(android.view.View view) {
        TextView textViewProblemUserAnswer = findViewById(R.id.textViewProblemUserAnswer);
        String answer = textViewProblemUserAnswer.getText().toString();
        String foundKana = null;
        String subword = null;
        if (answer.length() > 1) {
            subword = answer.substring(answer.length() - 2, answer.length());
            foundKana = Util.findKana(subword);
            if (foundKana != null) {
                String newAnswer = answer.substring(0, answer.length() - 2);
                textViewProblemUserAnswer.setText(newAnswer);
                appl.getQuiz().setCurrentAnswer(newAnswer);
                findViewById(R.id.buttonDeleteKana).setEnabled(newAnswer.length() > 0);
                return;
            }
        }
        if (answer.length() > 0) {
            subword = answer.substring(answer.length() - 1, answer.length()); 
            foundKana = Util.findKana(subword);
            if (foundKana != null) {
                String newAnswer = answer.substring(0, answer.length() - 1);
                textViewProblemUserAnswer.setText(newAnswer);
                appl.getQuiz().setCurrentAnswer(newAnswer);
                findViewById(R.id.buttonDeleteKana).setEnabled(newAnswer.length() > 0);
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
                TextView textViewProblemUserAnswer = findViewById(R.id.textViewProblemUserAnswer);
                if (textViewProblemUserAnswer.getText().toString().length() < MAX_ANSWER_LENGTH) {
                    String newAnswer = textViewProblemUserAnswer.getText() + buttonKanas.get(i);
                    textViewProblemUserAnswer.setText(newAnswer);
                    appl.getQuiz().setCurrentAnswer(newAnswer);
                    findViewById(R.id.buttonDeleteKana).setEnabled(true);
                }
            }
        }
    }
    
    public void validateAnswer(android.view.View view) {
        TextView textViewProblemUserAnswer = findViewById(R.id.textViewProblemUserAnswer);
        final String answer = textViewProblemUserAnswer.getText().toString();

        if (answer.trim().equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ReadingProblemActivity.this);
            builder.setTitle(getResources().getString(R.string.error_empty_answer_title))
            .setMessage(getResources().getString(R.string.error_empty_answer_msg))
            .setPositiveButton(R.string.button_next, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) { 
                    appl.getQuiz().validateAnswer(answer);
                    appl.getQuiz().setCurrentMode(Quiz.Mode.MODE_EVALUATION);
                    showProblemEvaluation();
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
        appl.getQuiz().setCurrentMode(Quiz.Mode.MODE_EVALUATION);
        showProblemEvaluation();
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_problem);

        showProblemStatement();

        if (appl.getQuiz().getCurrentMode() == Quiz.Mode.MODE_ASK)
            askProblem();
        else
            showProblemEvaluation();
    }

    protected void askProblem() {
        super.askProblem();

        Problem currProb = appl.getQuiz().getCurrentProblem();

        findViewById(R.id.contentBody).setVisibility(VISIBLE);
        findViewById(R.id.fragmentProblemEvaluation).setVisibility(GONE);

        TextView textViewProblemUserAnswer = findViewById(R.id.textViewProblemUserAnswer);
        textViewProblemUserAnswer.setText(appl.getQuiz().getCurrentAnswer());

        buttonKanas = new ArrayList<String>();

        List<String> answerKanas = Util.findKanasFrom(currProb.getRightAnswer(), true);
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
            Button button = findViewById(buttonId);
            button.setText(buttonKanas.get(i));
        }

        findViewById(R.id.buttonDeleteKana).setEnabled(false);
    }

    protected void showProblemEvaluation() {
        super.showProblemEvaluation();

        findViewById(R.id.contentBody).setVisibility(GONE);
        findViewById(R.id.fragmentProblemEvaluation).setVisibility(VISIBLE);
    }

    private KankenApplication appl = KankenApplication.getInstance();
    
    private List<String> buttonKanas = new ArrayList<String>();

}
