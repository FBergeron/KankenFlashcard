package jp.kyoto.nlp.kanken;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

class SendResultsTask extends AsyncTask {

    public SendResultsTask(KankenApplication appl, ProgressDialog progressDialog, Context context, boolean quitAppl) {
        this.appl = appl;
        this.progressDialog = progressDialog;
        this.context = context;
        this.quitAppl = quitAppl;
    }

    protected Object doInBackground(Object... objs) {
        URL storeResultsUrl = (URL)objs[0];
        try {
            Map<String, String> params = new HashMap<String, String>();

            //int length = appl.getQuiz().getLength();
            int length = appl.getQuiz().getAnswerCount();
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
                params.put("problemRightAnswer_" + i, (isRightAnswer ? 1 : 0) + "");
                params.put("problemFamiliarity_" + i, familiarity + "");
                params.put("problemAnswer_" + i, answer);
                params.put("problemReportedAsIncorrect_" + i, (isReportedAsIncorrect ? 1 : 0) + "");
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
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            String status = jsonResponse.getString("status");
            Log.d(TAG, "status=" + status);
            if (!"ok".equals(status))
                exception = new Exception("Server responded with status=" + status + ". Something is probably wrong.");
        }
        catch (IOException e) {
            e.printStackTrace();

            exception = e;
        }
        catch (JSONException e2) {
            e2.printStackTrace();

            exception = e2;
        }

        return null;
    }

    protected void onPostExecute(final Object obj) {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        if (exception != null) {
            Log.e(TAG, "An exception has occurred: " + exception);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getResources().getString(R.string.error_server_unreachable_title))
            .setMessage(context.getResources().getString(R.string.error_server_unreachable_msg))
            .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
             })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setCancelable(true)
            .show();

            return;
        }

        if (quitAppl)
            appl.getFirstActivity().finishAndRemoveTask();
        else {
            Intent quizSummaryActivity = new Intent(context, QuizSummaryActivity.class);
            context.startActivity(quizSummaryActivity);
            ((Activity)context).finish();
        }
    }

    private Exception exception;

    private KankenApplication appl;

    private ProgressDialog progressDialog;

    private Context context;

    private boolean quitAppl;

    private static final String TAG = "SendResultsTask";

}


