package jp.kyoto.nlp.kanken;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringJoiner;

public class ProblemEvaluationActivity extends AppCompatActivity {

    public void goNextPage() {
        Problem nextProblem = appl.getQuiz().nextProblem();
        if (nextProblem == null) {
            URL storeResultsUrl = null;
            try {
                storeResultsUrl = new URL(storeResultsUrlStr);

                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage(getResources().getString(R.string.label_sending_results_data));
                progressDialog.setCancelable(false);
                progressDialog.show();

                new SendResultsTask().execute(storeResultsUrl);
            }
            catch(MalformedURLException e1) {
                e1.printStackTrace();
            }
        }
        else {
            Intent problemActivity = (Problem.Type.READING.equals(nextProblem.getType()) ?
                    new Intent(ProblemEvaluationActivity.this, ReadingProblemActivity.class) :
                    new Intent(ProblemEvaluationActivity.this, WritingProblemActivity.class));
            startActivity(problemActivity);
        }
    }

    public void setProblemFamiliarity(int familiarity) {
        appl.getQuiz().addFamiliarity(familiarity);

        goNextPage();
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
    
    public void setProblemFamiliarity5(android.view.View view) {
        setProblemFamiliarity(5); 
    }
   
    public void reportProblemAsErroneous(android.view.View view) {
        System.out.println( "This problem is incorrect." );        

        goNextPage();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_problem_evaluation);

        Problem currProb = appl.getQuiz().getCurrentProblem();
        int currProbIndex = appl.getQuiz().getCurrentProblemIndex();

        TextView textViewProblemNumber = (TextView)findViewById(R.id.textViewProblemNumber);
        String strProblemNumber = String.format(getResources().getString(R.string.label_problem_number), currProbIndex + 1, Quiz.DEFAULT_LENGTH);
        textViewProblemNumber.setText(strProblemNumber);

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

        String text = String.format(getResources().getString(R.string.label_enter_problem_familiarity), appl.getQuiz().getCurrentProblem().getRightAnswer());
        TextView textViewProblemFamiliarity = (TextView)findViewById(R.id.textViewProblemFamiliarity);
        textViewProblemFamiliarity.setText(text);
    }

    private class SendResultsTask extends AsyncTask {

        protected Object doInBackground(Object... objs) {
            URL storeResultsUrl = (URL)objs[0];
            try {
                Map<String, String> params = new HashMap<String, String>();
                //params.put("user", appl.getUserEmail());

                int length = appl.getQuiz().getLength();
                Iterator<Problem> itProblem = appl.getQuiz().getProblems();
                Iterator<String> itAnswer = appl.getQuiz().getAnswers();
                Iterator<Boolean> itRightAnswer = appl.getQuiz().getRightAnswers();
                Iterator<Integer> itFamiliarities = appl.getQuiz().getFamiliarities();
                for (int i = 0; i < length; i++) {
                    Problem problem = itProblem.next();
                    String answer = itAnswer.next();
                    Boolean isRightAnswer = itRightAnswer.next();
                    Integer familiarity = itFamiliarities.next();

                    params.put("problemId_" + i, problem.getId());
                    params.put("problemJuman_" + i, problem.getJumanInfo());
                    params.put("problemRightAnswer_" + i, (isRightAnswer.booleanValue() ? 1 : 0) + ""); 
                    params.put("problemFamiliarity_" + i, familiarity + "");
                    params.put("problemAnswer_" + i, answer);
                }

                StringJoiner joiner = new StringJoiner("&");
                for (Map.Entry<String, String> entry : params.entrySet())
                    joiner.add(entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
                byte[] data = joiner.toString().getBytes("UTF-8");

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

            Intent quizSummaryActivity = new Intent(ProblemEvaluationActivity.this, QuizSummaryActivity.class);
            startActivity(quizSummaryActivity);
        }

        private Exception exception;

    }

    private KankenApplication appl = KankenApplication.getInstance();

    private ProgressDialog progressDialog;

    private static final String storeResultsUrlStr = "https://lotus.kuee.kyoto-u.ac.jp/~frederic/KankenFlashcardServer/cgi-bin/store_results.cgi";

}
