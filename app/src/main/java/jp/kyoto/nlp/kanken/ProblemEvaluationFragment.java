package jp.kyoto.nlp.kanken;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
        if (nextProblem == null) {
            Intent quizSummaryActivity = new Intent(getActivity(), QuizSummaryActivity.class);
            startActivity(quizSummaryActivity);
            getActivity().finish();
        }
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
                    storeResult(false);
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
                    storeResult(false);
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
                    storeResult(false);
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
                    storeResult(false);
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
                    storeResult(false);
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
                                storeResult(true);
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
                                storeResult(true);
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
                                storeResult(true);
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
                                storeResult(true);
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
                                storeResult(true);
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

    private void storeResult(boolean quitAppl) {
        URL storeResultUrl;
        try {
            storeResultUrl = new URL(appl.getServerBaseUrl() + KankenApplication.storeResultReqPath);

            new SendResultTask(appl, getContext(), quitAppl).execute(storeResultUrl);
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

    private ProgressDialog progressDialog;

    private KankenApplication appl = KankenApplication.getInstance();

    private static final String tag = "ProblemEvalFragment";

    private ImageView imageViewResult;
    private TextView textViewAnswer;
    private TextView textViewDetailedAnswer;
    private TextView textViewProblemFamiliarity;

}

