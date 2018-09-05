package jp.kyoto.nlp.kanken;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ProblemEvaluationFragment extends Fragment {

    public void goNextProblem() {
        Problem nextProblem = appl.getQuiz().nextProblem();
        if (nextProblem == null) {
            URL storeResultsUrl = null;
            try {
                storeResultsUrl = new URL(appl.getServerBaseUrl() + storeResultsReqPath);

                progressDialog = new ProgressDialog(getContext());
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
        else {
            appl.getQuiz().setCurrentMode(Quiz.Mode.MODE_ASK);
            appl.getQuiz().setCurrentAnswer("");
            QuizProblemActivity parentActivity = (QuizProblemActivity)getActivity();
            parentActivity.showProblemStatement();
            parentActivity.askProblem();
        }
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
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_problem_evaluation, container, false);

        Button buttonSetProblemFamiliarity0 = (Button)view.findViewById(R.id.buttonFamiliarity0);
        buttonSetProblemFamiliarity0.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setProblemFamiliarity(0);
                }
            }
        );

        Button buttonSetProblemFamiliarity1 = (Button)view.findViewById(R.id.buttonFamiliarity1);
        buttonSetProblemFamiliarity1.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setProblemFamiliarity(1);
                }
            }
        );

        Button buttonSetProblemFamiliarity2 = (Button)view.findViewById(R.id.buttonFamiliarity2);
        buttonSetProblemFamiliarity2.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setProblemFamiliarity(2);
                }
            }
        );

        Button buttonSetProblemFamiliarity3 = (Button)view.findViewById(R.id.buttonFamiliarity3);
        buttonSetProblemFamiliarity3.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setProblemFamiliarity(3);
                }
            }
        );

        Button buttonSetProblemFamiliarity4 = (Button)view.findViewById(R.id.buttonFamiliarity4);
        buttonSetProblemFamiliarity4.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setProblemFamiliarity(4);
                }
            }
        );

        return view;
    }

    public void showProblemEvaluation() {
        Problem currProb = appl.getQuiz().getCurrentProblem();
        int currProbIndex = appl.getQuiz().getCurrentProblemIndex();

        QuizProblemActivity parentActivity = (QuizProblemActivity)getActivity();

        TextView textViewAnswerValue = (TextView)parentActivity.findViewById(R.id.textViewAnswerValue);
        textViewAnswerValue.setText(appl.getQuiz().getAnswer(currProbIndex));

        TextView textViewAnswerRightValue = (TextView)parentActivity.findViewById(R.id.textViewAnswerRightValue);
        textViewAnswerRightValue.setText(currProb.getRightAnswer());

        TextView textViewEvaluationResult = (TextView)parentActivity.findViewById(R.id.textViewEvaluationResult);
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
        TextView textViewProblemFamiliarity = (TextView)parentActivity.findViewById(R.id.textViewProblemFamiliarity);
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

            //Intent quizSummaryActivity = new Intent(ReadingProblemActivity.this, QuizSummaryActivity.class);
            Intent quizSummaryActivity = new Intent(getContext(), QuizSummaryActivity.class);
            startActivity(quizSummaryActivity);
        }

        private Exception exception;

    }

    private ProgressDialog progressDialog;

    private KankenApplication appl = KankenApplication.getInstance();

    private static final String storeResultsReqPath = "/cgi-bin/store_results.cgi";

}

