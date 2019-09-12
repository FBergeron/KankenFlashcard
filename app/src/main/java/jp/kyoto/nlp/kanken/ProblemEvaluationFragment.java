package jp.kyoto.nlp.kanken;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.app.AlertDialog.Builder;

public class ProblemEvaluationFragment extends Fragment {

    public void reportProblemAsErroneous(android.view.View view) {
        final QuizProblemActivity parentActivity = (QuizProblemActivity)getActivity();
        Builder builder = new Builder(parentActivity);
        builder.setTitle(getResources().getString(R.string.info_confirm_report_title))
        .setMessage(getResources().getString(R.string.info_confirm_report_msg))
        .setPositiveButton(R.string.button_report, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Builder builder = new Builder(parentActivity);
                builder.setTitle(getResources().getString(R.string.info_report_title))
                .setMessage(getResources().getString(R.string.info_report_msg))
                .setPositiveButton(R.string.button_continue, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        appl.getQuiz().reportAsIncorrect();

                        goNextProblem();
                    }
                 })
                .setIcon(android.R.drawable.ic_dialog_info)
                .setCancelable(true)
                .show();
            }
         })
        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
         })
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setCancelable(true)
        .show();
    }

    public void goNextProblem() {
        Problem nextProblem = appl.getQuiz().nextProblem();
        if (nextProblem == null)
            storeResults(false);
        else {
            appl.getQuiz().setCurrentMode(Quiz.Mode.MODE_ASK);
            appl.getQuiz().setCurrentAnswer("");
            QuizProblemActivity parentActivity = (QuizProblemActivity)getActivity();
            parentActivity.showProblemStatement();
            parentActivity.askProblem();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_problem_evaluation, container, false);

        ImageButton buttonSetProblemFamiliarity0 = view.findViewById(R.id.buttonFamiliarity0);
        buttonSetProblemFamiliarity0.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appl.getQuiz().addFamiliarity(0);
                    goNextProblem();
                }
            }
        );

        ImageButton buttonSetProblemFamiliarity1 = view.findViewById(R.id.buttonFamiliarity1);
        buttonSetProblemFamiliarity1.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appl.getQuiz().addFamiliarity(1);
                    goNextProblem();
                }
            }
        );

        ImageButton buttonSetProblemFamiliarity2 = view.findViewById(R.id.buttonFamiliarity2);
        buttonSetProblemFamiliarity2.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appl.getQuiz().addFamiliarity(2);
                    goNextProblem();
                }
            }
        );

        ImageButton buttonSetProblemFamiliarity3 = view.findViewById(R.id.buttonFamiliarity3);
        buttonSetProblemFamiliarity3.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appl.getQuiz().addFamiliarity(3);
                    goNextProblem();
                }
            }
        );

        ImageButton buttonSetProblemFamiliarity4 = view.findViewById(R.id.buttonFamiliarity4);
        buttonSetProblemFamiliarity4.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appl.getQuiz().addFamiliarity(4);
                    goNextProblem();
                }
            }
        );

        ImageButton buttonReportErroneousProblem = view.findViewById(R.id.buttonReportErroneousProblem);
        buttonReportErroneousProblem.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reportProblemAsErroneous(v);
                }
            }
        );

        ImageButton buttonQuit = view.findViewById(R.id.buttonQuit);
        buttonQuit.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View view = inflater.inflate(R.layout.dialog_quit, null);
                    builder.setView(view);
                    builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog dialog = builder.create();

                    TextView dialogTextViewProblemFamiliarity = view.findViewById(R.id.textViewProblemFamiliarity);
                    Problem currProb = appl.getQuiz().getCurrentProblem();
                    String wordInKanjis = getWordInKanjis(currProb.getJumanInfo());
                    String text = String.format(getResources().getString(R.string.label_enter_problem_familiarity), wordInKanjis);
                    dialogTextViewProblemFamiliarity.setText(text);

                    ImageButton buttonSetProblemFamiliarity0 = view.findViewById(R.id.buttonFamiliarity0);
                    buttonSetProblemFamiliarity0.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                appl.getQuiz().addFamiliarity(0);
                                // Save data and quit the application.
                                storeResults(true);
                            }
                        }
                    );

                    ImageButton buttonSetProblemFamiliarity1 = view.findViewById(R.id.buttonFamiliarity1);
                    buttonSetProblemFamiliarity1.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                appl.getQuiz().addFamiliarity(1);
                                // Save data and quit the application.
                                storeResults(true);
                            }
                        }
                    );

                    ImageButton buttonSetProblemFamiliarity2 = view.findViewById(R.id.buttonFamiliarity2);
                    buttonSetProblemFamiliarity2.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                appl.getQuiz().addFamiliarity(2);
                                // Save data and quit the application.
                                storeResults(true);
                            }
                        }
                    );

                    ImageButton buttonSetProblemFamiliarity3 = view.findViewById(R.id.buttonFamiliarity3);
                    buttonSetProblemFamiliarity3.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                appl.getQuiz().addFamiliarity(3);
                                // Save data and quit the application.
                                storeResults(true);
                            }
                        }
                    );

                    ImageButton buttonSetProblemFamiliarity4 = view.findViewById(R.id.buttonFamiliarity4);
                    buttonSetProblemFamiliarity4.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                appl.getQuiz().addFamiliarity(4);
                                // Save data and quit the application.
                                storeResults(true);
                            }
                        }
                    );

                    builder.show();
                }
            }
        );

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageViewResult = view.findViewById(R.id.imageViewResult);
        textViewAnswer = view.findViewById(R.id.textViewAnswer);
        textViewDetailedAnswer = view.findViewById(R.id.textViewDetailedAnswer);
        textViewProblemFamiliarity = view.findViewById(R.id.textViewProblemFamiliarity);
    }

    public void showProblemEvaluation() {
        Problem currProb = appl.getQuiz().getCurrentProblem();

        Pattern problemPattern = Pattern.compile(".*\\[(.*)\\].*");
        String statement = appl.getQuiz().getCurrentProblem().getStatement();

        Matcher problemMatcher = problemPattern.matcher(statement);
        String problemWord = "";
        if (problemMatcher.matches())
            problemWord = problemMatcher.group(1);

        String answer = appl.getQuiz().getCurrentAnswer();
        String rightAnswer = currProb.getRightAnswer();
        textViewAnswer.setText(answer);
        textViewDetailedAnswer.setText(rightAnswer);

        int resultResId = appl.getQuiz().isCurrentAnswerRight() ? R.drawable.text_result02 : R.drawable.text_result01;

        imageViewResult.setImageResource(resultResId);

        String wordInKanjis = getWordInKanjis(currProb.getJumanInfo());
        String text = String.format(getResources().getString(R.string.label_enter_problem_familiarity), wordInKanjis);
        textViewProblemFamiliarity.setText(text);
    }

    private void storeResults(boolean quitAppl) {
        URL storeResultsUrl;
        try {
            storeResultsUrl = new URL(appl.getServerBaseUrl() + storeResultsReqPath);

            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getResources().getString(R.string.label_sending_results_data));
            progressDialog.setCancelable(false);
            progressDialog.show();

            new SendResultsTask(quitAppl).execute(storeResultsUrl);
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

    private String getWordInKanjis(String jumanInfo) {
        StringBuilder wordInKanjis = new StringBuilder();

        // Handle each part separated by a plus mark.
        String[] parts = jumanInfo.split("\\+");
        for (int i = 0; i < parts.length; i++) {
            // Remove part after question mark.
            int indexOfQuestionMark = parts[i].indexOf("?");
            if (indexOfQuestionMark != -1)
                parts[i] = parts[i].substring(0, indexOfQuestionMark);

            // Append the string that is left to the slash.
            int indexOfSlash = parts[i].indexOf("/");
            if (indexOfSlash != -1)
                wordInKanjis.append(parts[i], 0, indexOfSlash);
        }

        return wordInKanjis.toString();
    }

    public void showArticle(android.view.View view) {
        String articleUrl = appl.getQuiz().getCurrentProblem().getArticleUrl();
        if (articleUrl != null) {
            Intent httpIntent = new Intent(Intent.ACTION_VIEW);
            httpIntent.setData(Uri.parse(articleUrl));
            startActivity(httpIntent);
        }
    }

    private class SendResultsTask extends AsyncTask {

        SendResultsTask(boolean quitAppl) {
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
                Log.d(tag, "status=" + status);
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
                Log.e(tag, "An exception has occurred: " + exception);

                Builder builder = new Builder(getActivity());
                builder.setTitle(getResources().getString(R.string.error_server_unreachable_title))
                .setMessage(getResources().getString(R.string.error_server_unreachable_msg))
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
                Intent quizSummaryActivity = new Intent(getActivity(), QuizSummaryActivity.class);
                startActivity(quizSummaryActivity);
                getActivity().finish();
            }
        }

        private Exception exception;

        private boolean quitAppl;

    }

    private ProgressDialog progressDialog;

    private KankenApplication appl = KankenApplication.getInstance();

    private static final String storeResultsReqPath = "/cgi-bin/store_results.cgi";

    private static final String tag = "ProblemEvalFragment";

    private ImageView imageViewResult;
    private TextView textViewAnswer;
    private TextView textViewDetailedAnswer;
    private TextView textViewProblemFamiliarity;

}

