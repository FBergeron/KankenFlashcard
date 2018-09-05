package jp.kyoto.nlp.kanken;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ReadingProblemActivity extends AppCompatActivity {

    private final static int MAX_ANSWER_LENGTH = 16;

    public void goNextProblem() {
        Problem nextProblem = appl.getQuiz().nextProblem();
        if (nextProblem == null) {
            URL storeResultsUrl = null;
            try {
                storeResultsUrl = new URL(appl.getServerBaseUrl() + storeResultsReqPath);

                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage(getResources().getString(R.string.label_sending_results_data));
                progressDialog.setCancelable(false);
                progressDialog.show();

                new SendResultsTask().execute(storeResultsUrl);
            }
            catch(MalformedURLException e1) {
                e1.printStackTrace();
            }
            catch(IOException e2) {
                e2.printStackTrace();
            }
            catch(JSONException e3) {
                e3.printStackTrace();
            }
        }
        else
            showProblemStatement();
    }

    public void setProblemFamiliarity(int familiarity) {
        appl.getQuiz().addFamiliarity(familiarity);

        goNextProblem();
    }

    public void setProblemFamiliarity0(android.view.View view) {
        setProblemFamiliarity(0); 
    }
    
    public void setProblemFamiliarity1(android.view.View view) {
        setProblemFamiliarity(1); 
    }
    
    public void setProblemFamiliarity2(android.view.View view) {
        setProblemFamiliarity(2); 
    }
    
    public void setProblemFamiliarity3(android.view.View view) {
        setProblemFamiliarity(3); 
    }
    
    public void setProblemFamiliarity4(android.view.View view) {
        setProblemFamiliarity(4); 
    }
    
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
System.out.println( "altArticleUrl="+altArticleUrl );        
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
        TextView textViewReadingProblemUserAnswer = (TextView)findViewById(R.id.textViewReadingProblemUserAnswer);
        String answer = textViewReadingProblemUserAnswer.getText().toString();
        String foundKana = null;
        String subword = null;
        if (answer.length() > 1) {
            subword = answer.substring(answer.length() - 2, answer.length());
            foundKana = Util.findKana(subword);
            if (foundKana != null) {
                textViewReadingProblemUserAnswer.setText(answer.substring(0, answer.length() - 2));
                findViewById(R.id.buttonDeleteKana).setEnabled(textViewReadingProblemUserAnswer.getText().toString().length() > 0);
                return;
            }
        }
        if (answer.length() > 0) {
            subword = answer.substring(answer.length() - 1, answer.length()); 
            foundKana = Util.findKana(subword);
            if (foundKana != null) {
                textViewReadingProblemUserAnswer.setText(answer.substring(0, answer.length() - 1));
                findViewById(R.id.buttonDeleteKana).setEnabled(textViewReadingProblemUserAnswer.getText().toString().length() > 0);
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
                if (textViewReadingProblemUserAnswer.getText().toString().length() < MAX_ANSWER_LENGTH) {
                    textViewReadingProblemUserAnswer.setText(textViewReadingProblemUserAnswer.getText() + buttonKanas.get(i));
                    findViewById(R.id.buttonDeleteKana).setEnabled(true);
                }
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
        showProblemEvaluation();
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_problem);

        showProblemStatement();
    }

    private void showProblemStatement() {
        Problem currProb = appl.getQuiz().getCurrentProblem();
        int currProbIndex = appl.getQuiz().getCurrentProblemIndex();

        findViewById(R.id.imageButtonViewReadingProblemArticle).setVisibility(GONE);
        findViewById(R.id.imageButtonSearchReadingProblemArticle).setVisibility(GONE);

        findViewById(R.id.tableLayoutReadingProblemAnswerButtons).setVisibility(VISIBLE);
        findViewById(R.id.layoutReadingProblemUserAnswer).setVisibility(VISIBLE);
        findViewById(R.id.fragmentProblemEvaluation).setVisibility(GONE);

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
        stmt.append("em { color: red; font-weight: bold; font-style: normal;}");
        stmt.append("</style>");
        stmt.append("</head>");
        stmt.append("<body>" + currProb.getStatement().replace("[", "<em>").replace("]", "</em>")  + "</body>");
        stmt.append("</html>");

        WebView webViewProblemStatement = (WebView)findViewById(R.id.webViewProblemStatement);
        webViewProblemStatement.loadData(stmt.toString(), "text/html; charset=utf-8", "utf-8");

        TextView textViewReadingProblemUserAnswer = (TextView)findViewById(R.id.textViewReadingProblemUserAnswer);
        textViewReadingProblemUserAnswer.setText("");

        findViewById(R.id.imageButtonViewReadingProblemArticle).setVisibility(GONE);
        findViewById(R.id.imageButtonSearchReadingProblemArticle).setVisibility(GONE);

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
            Button button = (Button)findViewById(buttonId);
            button.setText(buttonKanas.get(i));
        }

        findViewById(R.id.buttonDeleteKana).setEnabled(false);
    }

    private void showProblemEvaluation() {
        Problem currProb = appl.getQuiz().getCurrentProblem();
        int currProbIndex = appl.getQuiz().getCurrentProblemIndex();

        ImageButton imageButtonViewArticle = (ImageButton)findViewById(R.id.imageButtonViewReadingProblemArticle);
        ImageButton imageButtonSearchReadingProblemArticle = (ImageButton)findViewById(R.id.imageButtonSearchReadingProblemArticle);
        if (currProb.isArticleLinkAlive()) 
            imageButtonViewArticle.setVisibility(VISIBLE);
        else
            imageButtonSearchReadingProblemArticle.setVisibility(VISIBLE);

        findViewById(R.id.tableLayoutReadingProblemAnswerButtons).setVisibility(GONE);
        findViewById(R.id.layoutReadingProblemUserAnswer).setVisibility(GONE);
        findViewById(R.id.fragmentProblemEvaluation).setVisibility(VISIBLE);

        TextView textViewAnswerValue = (TextView)findViewById(R.id.textViewAnswerValue);
        textViewAnswerValue.setText(appl.getQuiz().getAnswer(currProbIndex));

        TextView textViewAnswerRightValue = (TextView)findViewById(R.id.textViewAnswerRightValue);
        textViewAnswerRightValue.setText(currProb.getRightAnswer());

        TextView textViewEvaluationResult = (TextView)findViewById(R.id.textViewEvaluationResult);
        if (appl.getQuiz().isCurrentAnswerRight()) {
            String strRightAnswer = getResources().getString(R.string.label_right_answer);
            textViewEvaluationResult.setText(strRightAnswer);
            textViewEvaluationResult.setTextColor(Color.GREEN);
        }
        else {
            String strWrongAnswer = getResources().getString(R.string.label_wrong_answer);
            textViewEvaluationResult.setText(strWrongAnswer);
            textViewEvaluationResult.setTextColor(Color.RED);
        }

        String jumanInfo = currProb.getJumanInfo();
        int indexOfSlash = jumanInfo.indexOf("/");
        String wordInKanjis = (indexOfSlash == -1 ? jumanInfo : jumanInfo.substring(0, indexOfSlash));
        String text = String.format(getResources().getString(R.string.label_enter_problem_familiarity), wordInKanjis);
        TextView textViewProblemFamiliarity = (TextView)findViewById(R.id.textViewProblemFamiliarity);
        textViewProblemFamiliarity.setText(text);
    }

    private class SendResultsTask extends AsyncTask {

        protected Object doInBackground(Object... objs) {
            URL storeResultsUrl = (URL)objs[0];
            try {
                Map<String, String> params = new HashMap<String, String>();

                int length = appl.getQuiz().getLength();
                Iterator<Problem> itProblem = appl.getQuiz().getProblems();
                Iterator<String> itAnswer = appl.getQuiz().getAnswers();
                Iterator<Boolean> itRightAnswer = appl.getQuiz().getRightAnswers();
                Iterator<Integer> itFamiliarities = appl.getQuiz().getFamiliarities();
                Iterator<Boolean> itReported = appl.getQuiz().getReportedAsIncorrects();
                for (int i = 0; i < length; i++) {
                    Problem problem = itProblem.next();
                    String answer = itAnswer.next();
                    Boolean isRightAnswer = itRightAnswer.next();
                    Integer familiarity = itFamiliarities.next();
                    Boolean isReportedAsIncorrect = itReported.next();

                    params.put("problemId_" + i, problem.getId());
                    params.put("problemJuman_" + i, problem.getJumanInfo());
                    params.put("problemRightAnswer_" + i, (isRightAnswer.booleanValue() ? 1 : 0) + ""); 
                    params.put("problemFamiliarity_" + i, familiarity + "");
                    params.put("problemAnswer_" + i, answer);
                    params.put("problemReportedAsIncorrect_" + i, (isReportedAsIncorrect.booleanValue() ? 1 : 0) + "");
                }

                StringBuilder builder = new StringBuilder();
                String delimiter = "";
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    builder.append(delimiter).append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                    delimiter = "&";
                }
                byte[] data = builder.toString().getBytes("UTF-8");

                HttpURLConnection con = (HttpURLConnection) storeResultsUrl.openConnection();
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                con.setRequestProperty("Accept", "application/json");
                String cookie = appl.getSessionCookie();
                if (cookie != null)
                    con.setRequestProperty("Cookie", cookie);
                con.setRequestMethod("POST");
                con.setFixedLengthStreamingMode(data.length);
                con.connect();

                OutputStream writer = con.getOutputStream();
                writer.write(data);
                writer.flush();
                writer.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(final Object obj) {
            if (exception != null) {
                System.out.println("An exception has occured: " + exception);
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
                return;
            }

            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            Intent quizSummaryActivity = new Intent(ReadingProblemActivity.this, QuizSummaryActivity.class);
            startActivity(quizSummaryActivity);
        }

        private Exception exception;

    }

    private KankenApplication appl = KankenApplication.getInstance();
    
    private ProgressDialog progressDialog;

    private List<String> buttonKanas = new ArrayList<String>();

    private static final String storeResultsReqPath = "/cgi-bin/store_results.cgi";

}
