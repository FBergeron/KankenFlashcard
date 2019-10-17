package jp.kyoto.nlp.kanken;

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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class FetchProblemsTask extends AsyncTask {

    public FetchProblemsTask(KankenApplication appl, ProgressDialog progressDialog, Context context) {
        this.appl = appl;
        this.progressDialog = progressDialog;
        this.context = context;
    }

    protected Object doInBackground(Object... objs) {
        JSONArray jsonProblems = null;
        URL getNextProblemsUrl = (URL) objs[0];
        try {
            HttpURLConnection con = (HttpURLConnection) getNextProblemsUrl.openConnection();
            con.setRequestProperty("Accept", "application/json");
            String cookie = appl.getSessionCookie();
            if (cookie != null)
                con.setRequestProperty("Cookie", cookie);
            con.setRequestMethod("GET");
            con.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            jsonProblems = jsonResponse.getJSONArray("problems");
        } catch (IOException e) {
            e.printStackTrace();
            this.exception = e;
        } catch (JSONException e2) {
            e2.printStackTrace();
            this.exception = e2;
        }

        return jsonProblems;
    }

    protected void onPostExecute(final Object obj) {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        if (exception != null || obj == null) {
            if (exception != null)
                Log.e(TAG, "An exception has occurred: " + exception);
            if (obj == null)
                Log.e(TAG, "Cannot retrieve problems.");

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

        ArrayList<Problem> problems = new ArrayList<Problem>();
        Quiz quiz = appl.getQuiz();

        JSONArray jsonProblems = (JSONArray) obj;

        if (jsonProblems.length() < Quiz.DEFAULT_LENGTH) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getResources().getString(R.string.error_not_enough_problems_found_title))
                    .setMessage(context.getResources().getString(R.string.error_not_enough_problems_found_msg))
                    .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setCancelable(true)
                    .show();
            return;
        }

        for (int i = 0; i < jsonProblems.length(); i++) {
            try {
                JSONArray jsonProblemData = jsonProblems.getJSONArray(i);

                JSONArray jsonProblem = jsonProblemData.getJSONArray(0);
                JSONArray jsonProblemTopics = jsonProblemData.getJSONArray(1);

                String id = jsonProblem.getString(0);
                String jumanInfo = jsonProblem.getString(1);
                String statement = jsonProblem.getString(2);
                String rightAnswer = jsonProblem.getString(3);
                String articleUrl = jsonProblem.getString(4);
                Object objIsLinkAlive = jsonProblem.get(5);

                boolean isLinkAlive = objIsLinkAlive != null && objIsLinkAlive instanceof Integer && (Integer) objIsLinkAlive == 1;

                String altArticleUrl = null;
                try {
                    if (!isLinkAlive && objIsLinkAlive != null && objIsLinkAlive instanceof JSONArray) {
                        StringBuilder strParams = new StringBuilder();
                        String paramDelim = "";
                        JSONArray searchTerms = (JSONArray) objIsLinkAlive;
                        for (int t = 0; t < searchTerms.length(); t++) {
                            strParams.append(paramDelim);
                            strParams.append(URLEncoder.encode(searchTerms.getString(t), "UTF-8"));
                            paramDelim = "+";
                        }
                        if (strParams.length() > 0)
                            altArticleUrl = String.format(context.getResources().getString(R.string.search_engine_url), strParams);
                    }
                } catch (UnsupportedEncodingException ignore) {
                    ignore.printStackTrace();
                }

                // Log.d(TAG, "id="+id);
                // Log.d(TAG, "jumanInfo="+jumanInfo);
                // Log.d(TAG, "statement="+statement);
                // Log.d(TAG, "rightAnswer="+rightAnswer);
                // Log.d(TAG, "articleUrl="+articleUrl);
                // Log.d(TAG, "objIsLinkAlive="+objIsLinkAlive);
                // Log.d(TAG, "isLinkAlive="+isLinkAlive);
                // Log.d(TAG, "altArticleUrl="+altArticleUrl);

                Set<Problem.Topic> topics = new HashSet<Problem.Topic>();
                for (int j = 0; j < jsonProblemTopics.length(); j++) {
                    String topic = jsonProblemTopics.getString(j);
                    topics.add(Problem.Topic.valueOf(topic.toUpperCase()));
                }

                Problem problem = null;
                try {
                    if (Problem.Type.READING == quiz.getType())
                        problem = new ReadingProblem(id, quiz.getLevel(), topics, statement, jumanInfo, rightAnswer, articleUrl, isLinkAlive, altArticleUrl);
                    else
                        problem = new WritingProblem(id, quiz.getLevel(), topics, statement, jumanInfo, rightAnswer, articleUrl, isLinkAlive, altArticleUrl);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                if (problem != null)
                    problems.add(problem);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        quiz.setProblems(problems);
        quiz.setCurrentAnswer("");
        quiz.setCurrentMode(Quiz.Mode.MODE_ASK);
        Problem currProb = quiz.getCurrentProblem();

        Intent problemActivity = (Problem.Type.READING.equals(currProb.getType()) ?
                new Intent(context, ReadingProblemActivity.class) :
                new Intent(context, WritingProblemActivity.class));
        context.startActivity(problemActivity);
    }

    private Exception exception;

    private KankenApplication appl;

    private ProgressDialog progressDialog;

    private Context context;

    private static final String TAG = "FetchProblemsTask";

}

