package jp.kyoto.nlp.kanken;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
                        storeResult(false);
                    }
                 })
                .setIcon(android.R.drawable.ic_dialog_info)
                .setCancelable(false)
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
        view = inflater.inflate(R.layout.fragment_problem_evaluation, container, false);

        buttonSetProblemFamiliarity0 = view.findViewById(R.id.buttonFamiliarity0);
        buttonSetProblemFamiliarity1 = view.findViewById(R.id.buttonFamiliarity1);
        buttonSetProblemFamiliarity2 = view.findViewById(R.id.buttonFamiliarity2);
        buttonSetProblemFamiliarity3 = view.findViewById(R.id.buttonFamiliarity3);
        buttonSetProblemFamiliarity4 = view.findViewById(R.id.buttonFamiliarity4);

        buttonSetProblemFamiliarity0.setOnClickListener(new FamiliarityButtonAdapter(1, false));
        buttonSetProblemFamiliarity1.setOnClickListener(new FamiliarityButtonAdapter(2, false));
        buttonSetProblemFamiliarity2.setOnClickListener(new FamiliarityButtonAdapter(3, false));
        buttonSetProblemFamiliarity3.setOnClickListener(new FamiliarityButtonAdapter(4, false));
        buttonSetProblemFamiliarity4.setOnClickListener(new FamiliarityButtonAdapter(5, false));

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
                    quitDialog = builder.create();

                    TextView dialogTextViewProblemFamiliarity = view.findViewById(R.id.textViewProblemFamiliarity);
                    Problem currProb = appl.getQuiz().getCurrentProblem();
                    String wordInKanjis = getWordInKanjis(currProb.getJumanInfo());
                    String text = String.format(getResources().getString(R.string.label_enter_problem_familiarity), wordInKanjis);
                    dialogTextViewProblemFamiliarity.setText(text);

                    buttonDialogSetProblemFamiliarity0 = view.findViewById(R.id.buttonFamiliarity0);
                    buttonDialogSetProblemFamiliarity1 = view.findViewById(R.id.buttonFamiliarity1);
                    buttonDialogSetProblemFamiliarity2 = view.findViewById(R.id.buttonFamiliarity2);
                    buttonDialogSetProblemFamiliarity3 = view.findViewById(R.id.buttonFamiliarity3);
                    buttonDialogSetProblemFamiliarity4 = view.findViewById(R.id.buttonFamiliarity4);

                    buttonDialogSetProblemFamiliarity0.setOnClickListener(new FamiliarityButtonDialogAdapter(1));
                    buttonDialogSetProblemFamiliarity1.setOnClickListener(new FamiliarityButtonDialogAdapter(2));
                    buttonDialogSetProblemFamiliarity2.setOnClickListener(new FamiliarityButtonDialogAdapter(3));
                    buttonDialogSetProblemFamiliarity3.setOnClickListener(new FamiliarityButtonDialogAdapter(4));
                    buttonDialogSetProblemFamiliarity4.setOnClickListener(new FamiliarityButtonDialogAdapter(5));

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

        buttonSetProblemFamiliarity0.setEnabled(true);
        buttonSetProblemFamiliarity1.setEnabled(true);
        buttonSetProblemFamiliarity2.setEnabled(true);
        buttonSetProblemFamiliarity3.setEnabled(true);
        buttonSetProblemFamiliarity4.setEnabled(true);
        if (buttonDialogSetProblemFamiliarity0 != null)
            buttonDialogSetProblemFamiliarity0.setEnabled(true);
        if (buttonDialogSetProblemFamiliarity1 != null)
            buttonDialogSetProblemFamiliarity1.setEnabled(true);
        if (buttonDialogSetProblemFamiliarity2 != null)
            buttonDialogSetProblemFamiliarity2.setEnabled(true);
        if (buttonDialogSetProblemFamiliarity3 != null)
            buttonDialogSetProblemFamiliarity3.setEnabled(true);
        if (buttonDialogSetProblemFamiliarity4 != null)
            buttonDialogSetProblemFamiliarity4.setEnabled(true);
    }

    void storeResult(boolean quitAppl) {
        URL storeResultUrl;
        try {
            storeResultUrl = new URL(appl.getServerBaseUrl() + KankenApplication.storeResultReqPath);

            new SendResultTask(appl, getActivity(), this, quitAppl).execute(storeResultUrl);
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

    class FamiliarityButtonAdapter implements View.OnClickListener {

        FamiliarityButtonAdapter(int familiarity, boolean quitAppl) {
            this.familiarity = familiarity;
            this.quitAppl = quitAppl;
        }

        @Override
        public void onClick(View v) {
            buttonSetProblemFamiliarity0.setEnabled(false);
            buttonSetProblemFamiliarity1.setEnabled(false);
            buttonSetProblemFamiliarity2.setEnabled(false);
            buttonSetProblemFamiliarity3.setEnabled(false);
            buttonSetProblemFamiliarity4.setEnabled(false);

            appl.getQuiz().addFamiliarity(familiarity);
            storeResult(quitAppl);
        }

        private int familiarity;
        private boolean quitAppl;

    }

    class FamiliarityButtonDialogAdapter implements View.OnClickListener {

        FamiliarityButtonDialogAdapter(int familiarity) {
            this.familiarity = familiarity;
        }

        @Override
        public void onClick(View v) {
            buttonDialogSetProblemFamiliarity0.setEnabled(false);
            buttonDialogSetProblemFamiliarity1.setEnabled(false);
            buttonDialogSetProblemFamiliarity2.setEnabled(false);
            buttonDialogSetProblemFamiliarity3.setEnabled(false);
            buttonDialogSetProblemFamiliarity4.setEnabled(false);

            if (quitDialog != null) {
                appl.getQuiz().addFamiliarity(familiarity);
                quitDialog.dismiss();
                quitDialog = null;
                // Save data and quit the application.
                storeResult(true);
            }
        }

        private int familiarity;

    }

    private ProgressDialog progressDialog;

    private AlertDialog quitDialog;

    private KankenApplication appl = KankenApplication.getInstance();

    private static final String tag = "ProblemEvalFragment";

    private ImageView imageViewResult;
    private TextView textViewAnswer;
    private TextView textViewDetailedAnswer;
    private TextView textViewProblemFamiliarity;

    private ImageButton buttonSetProblemFamiliarity0;
    private ImageButton buttonSetProblemFamiliarity1;
    private ImageButton buttonSetProblemFamiliarity2;
    private ImageButton buttonSetProblemFamiliarity3;
    private ImageButton buttonSetProblemFamiliarity4;

    private ImageButton buttonDialogSetProblemFamiliarity0;
    private ImageButton buttonDialogSetProblemFamiliarity1;
    private ImageButton buttonDialogSetProblemFamiliarity2;
    private ImageButton buttonDialogSetProblemFamiliarity3;
    private ImageButton buttonDialogSetProblemFamiliarity4;

    private View view;

}

