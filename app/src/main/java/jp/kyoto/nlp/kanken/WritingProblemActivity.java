package jp.kyoto.nlp.kanken;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leafdigital.kanji.InputStroke;
import com.leafdigital.kanji.KanjiInfo;
import com.leafdigital.kanji.KanjiList;
import com.leafdigital.kanji.KanjiMatch;
import com.leafdigital.kanji.android.KanjiDrawing;
import com.leafdigital.kanji.android.KanjiDrawing.DrawnStroke;
import com.leafdigital.kanji.android.MultiAssetInputStream;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Random;

import static android.view.View.*;

public class WritingProblemActivity extends AppCompatActivity {

    private final static int MAX_ANSWER_LENGTH = 10;

    private final static long SHOW_KANJIS_DELAY = 1200; // In ms.

    private final static int[] ALL_IDS_a_w600dp = {
        R.id.no1_a, R.id.no2_a, R.id.no3_a, R.id.no4_a, R.id.no5_a, R.id.no6_a, R.id.no7_a
    };

    private final static int[] ALL_IDS_b_w600dp = {
        R.id.no1_b, R.id.no2_b, R.id.no3_b, R.id.no4_b, R.id.no5_b, R.id.no6_b, R.id.no7_b, R.id.no8_b, R.id.no9_b, R.id.no10_b, R.id.no11_b, R.id.no12_b
    };

    private final static int[] ALL_IDS_a = {
        R.id.no1_a, R.id.no2_a, R.id.no3_a, R.id.no4_a, R.id.no5_a, R.id.no6_a
    };

    private final static int[] ALL_IDS_b = {
        R.id.no1_b, R.id.no2_b, R.id.no3_b, R.id.no4_b, R.id.no5_b, R.id.no6_b, R.id.no7_b, R.id.no8_b, R.id.no9_b
    };

    private final static int KANJIS_MAX_COUNT = 60;

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
        if (altArticleUrl != null) {
            Intent httpIntent = new Intent(Intent.ACTION_VIEW);
            httpIntent.setData(Uri.parse(altArticleUrl));
            startActivity(httpIntent);
        }
    }

    @Override
    public void onBackPressed() {
        Util.goBackToSettings(WritingProblemActivity.this);
    }

    public void deleteKanji(android.view.View view) {
        TextView textViewWritingProblemUserAnswer = (TextView)findViewById(R.id.textViewWritingProblemUserAnswer);
        String origText = textViewWritingProblemUserAnswer.getText().toString();
        if (origText.length() > 0) {
            textViewWritingProblemUserAnswer.setText(origText.substring(0, origText.length() - 1));
            findViewById(R.id.buttonDeleteKanji).setEnabled(textViewWritingProblemUserAnswer.getText().toString().length() > 0);
        }
    }

    public void enterCharacter(android.view.View view) {
        if (kanjiTimer != null)
            kanjiTimer.cancel();

        findViewById(R.id.buttonEnterWritingProblemCharacter).setEnabled(false);
        KanjiDrawing kanjiCanvas = (KanjiDrawing)findViewById(R.id.kanjiDrawing);
        new MatchThread(this, kanjiCanvas.getStrokes(), R.string.label_finding_characters, true);
    }

    public void undoCanvas(android.view.View view) {
        KanjiDrawing kanjiCanvas = (KanjiDrawing)findViewById(R.id.kanjiDrawing);
        kanjiCanvas.undo();
        
        kanjiPage = 0;
        kanjis = null;
    }

    public void clearCanvas(android.view.View view) {
        if (kanjiTimer != null)
            kanjiTimer.cancel();

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        KanjiDrawing kanjiCanvas = (KanjiDrawing)findViewById(R.id.kanjiDrawing);
        kanjiCanvas.clear();

        findViewById(R.id.buttonUndoWritingProblemCanvas).setEnabled(false);
        findViewById(R.id.buttonClearWritingProblemCanvas).setEnabled(false);
        findViewById(R.id.buttonEnterWritingProblemCharacter).setEnabled(false);
        findViewById(R.id.buttonShowNextPage_a).setVisibility(GONE);

        layoutKanjiInputRight_a.setVisibility(VISIBLE);
        layoutKanjiInputRight_b.setVisibility(GONE);

        int[] buttonAIds = (dpWidth >= 600 ? ALL_IDS_a_w600dp : ALL_IDS_a);
        for (int i = 0; i < buttonAIds.length; i++) {
            Button button = (Button)findViewById(buttonAIds[i]);
            if (button != null) {
                button.setText("");
                button.setEnabled(false);
            }
        }

        kanjiPage = 0;
        kanjis = null;
    }

    public void validateAnswer(android.view.View view) {
        TextView textViewWritingProblemUserAnswer = (TextView)findViewById(R.id.textViewWritingProblemUserAnswer);
        final String answer = textViewWritingProblemUserAnswer.getText().toString();

        if (answer.trim().equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(WritingProblemActivity.this);
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
        setContentView(R.layout.activity_writing_problem);

        // Make sure the list gets loaded
        new LoadThread();

        showProblemStatement();
    }

    private void showProblemStatement() {
        Problem currProb = appl.getQuiz().getCurrentProblem();
        int currProbIndex = appl.getQuiz().getCurrentProblemIndex();

        findViewById(R.id.imageButtonViewWritingProblemArticle).setVisibility(GONE);
        findViewById(R.id.imageButtonSearchWritingProblemArticle).setVisibility(GONE);

        findViewById(R.id.layoutKanjiInput).setVisibility(VISIBLE);
        findViewById(R.id.layoutWritingProblemUserAnswer).setVisibility(VISIBLE);
        findViewById(R.id.fragmentProblemEvaluation).setVisibility(GONE);

        TextView textViewProblemInfoLevel = (TextView)findViewById(R.id.textViewWritingProblemInfoLevel);
        String strLevel = String.format(getResources().getString(R.string.label_problem_info_level), currProb.getLevel());
        textViewProblemInfoLevel.setText(strLevel);

        TextView textViewProblemInfoTopic = (TextView)findViewById(R.id.textViewWritingProblemInfoTopic);
        // Just show the first pertinent topic.
        for (Problem.Topic topic : currProb.getTopics()) {
            if (appl.getQuiz().getTopics().contains(topic)) {
                String strResName = "label_topic_" + topic.getLabelId();
                int labelId = getResources().getIdentifier(strResName, "string", WritingProblemActivity.this.getPackageName());
                String strTopic = String.format(getResources().getString(R.string.label_problem_info_topic), getResources().getString(labelId));
                textViewProblemInfoTopic.setText(strTopic);
                break;
            }
        }

        TextView textViewProblemInfoType = (TextView)findViewById(R.id.textViewWritingProblemInfoType);
        String strResName = "label_quiz_type_" + currProb.getType().getLabelId();
        int labelId = getResources().getIdentifier(strResName, "string", WritingProblemActivity.this.getPackageName());
        String strType = String.format(getResources().getString(R.string.label_problem_info_type), getResources().getString(labelId));
        textViewProblemInfoType.setText(strType);

        TextView textViewWritingProblemNumber = (TextView)findViewById(R.id.textViewWritingProblemNumber);
        String strProblemNumber = String.format(getResources().getString(R.string.label_problem_number), currProbIndex + 1, Quiz.DEFAULT_LENGTH);
        textViewWritingProblemNumber.setText(strProblemNumber);

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

        TextView textViewWritingProblemUserAnswer = (TextView)findViewById(R.id.textViewWritingProblemUserAnswer);
        textViewWritingProblemUserAnswer.setText("");

        layoutKanjiInputRight_a = (LinearLayout)findViewById(R.id.layoutKanjiInputRight_a); 
        layoutKanjiInputRight_a.setVisibility(VISIBLE);
        layoutKanjiInputRight_b = (LinearLayout)findViewById(R.id.layoutKanjiInputRight_b); 
        layoutKanjiInputRight_b.setVisibility(GONE);

        KanjiDrawing kanjiCanvas = (KanjiDrawing)findViewById(R.id.kanjiDrawing);
        kanjiCanvas.setListener(
            new KanjiDrawing.Listener() {
                @Override
                public void strokes(DrawnStroke[] strokes) {
                    System.out.println("strokes="+strokes+" max="+KanjiDrawing.MAX_STROKES);
                    findViewById(R.id.buttonUndoWritingProblemCanvas).setEnabled(strokes.length > 0);
                    findViewById(R.id.buttonClearWritingProblemCanvas).setEnabled(strokes.length > 0);
                    findViewById(R.id.buttonEnterWritingProblemCharacter).setEnabled(strokes.length > 0);
                    if (strokes != null && strokes.length > 0)
                        startTimer(SHOW_KANJIS_DELAY);
                }
            }
        );

        clearCanvas(null);
        findViewById(R.id.buttonDeleteKanji).setEnabled(false);
    }

    private void showProblemEvaluation() {
        Problem currProb = appl.getQuiz().getCurrentProblem();
        int currProbIndex = appl.getQuiz().getCurrentProblemIndex();

        ImageButton imageButtonViewArticle = (ImageButton)findViewById(R.id.imageButtonViewWritingProblemArticle);
        ImageButton imageButtonSearchWritingProblemArticle = (ImageButton)findViewById(R.id.imageButtonSearchWritingProblemArticle);
        if (currProb.isArticleLinkAlive()) 
            imageButtonViewArticle.setVisibility(VISIBLE);
        else
            imageButtonSearchWritingProblemArticle.setVisibility(VISIBLE);

        findViewById(R.id.layoutKanjiInput).setVisibility(GONE);
        findViewById(R.id.layoutWritingProblemUserAnswer).setVisibility(GONE);
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

    private void startTimer(long time) {
        if (kanjiTimer == null) {
            kanjiTimer = new CountDownTimer(time, SHOW_KANJIS_DELAY / 3) {
                public void onTick(long millisUntilDone) {
                }

                public void onFinish() {
                    enterCharacter(null);
                }
            };
        }
        kanjiTimer.cancel();
        kanjiTimer.start();
    }

    private void initializeKanjiButtons() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        if (kanjiPage == 0) {
            layoutKanjiInputRight_a.setVisibility(VISIBLE);
            layoutKanjiInputRight_b.setVisibility(GONE);

            int[] buttonAIds = (dpWidth >= 600 ? ALL_IDS_a_w600dp : ALL_IDS_a);
            int k = 0;
            while (k < buttonAIds.length && k < kanjis.length) {
                final Button button = (Button)findViewById(buttonAIds[k]);
                if (button != null) {
                    button.setText(kanjis[k]);
                    button.setEnabled(true);
                    button.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TextView textViewWritingProblemUserAnswer = (TextView)findViewById(R.id.textViewWritingProblemUserAnswer);
                                if (textViewWritingProblemUserAnswer.getText().toString().length() < MAX_ANSWER_LENGTH) { 
                                    textViewWritingProblemUserAnswer.setText(textViewWritingProblemUserAnswer.getText().toString() + button.getText().toString());
                                    findViewById(R.id.buttonDeleteKanji).setEnabled(true);
                                    clearCanvas(v);
                                }
                            }
                        }
                    );
                }

                k++;
            }
            while (k < buttonAIds.length) {
                Button button = (Button)findViewById(buttonAIds[k]);
                if (button != null) {
                    button.setText(" ");
                    button.setEnabled(false);
                }
                k++;
            }

            Button buttonNextPage = (Button)findViewById(R.id.buttonShowNextPage_a);
            if (buttonNextPage != null) {
                if (kanjis.length > 7) {
                    buttonNextPage.setVisibility(VISIBLE);
                    buttonNextPage.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                kanjiPage++;
                                initializeKanjiButtons();
                             }
                        }
                    );
                }
                else
                    buttonNextPage.setVisibility(GONE);
            }
        }
        else {
            layoutKanjiInputRight_a.setVisibility(GONE);
            layoutKanjiInputRight_b.setVisibility(VISIBLE);

            int[] buttonBIds = (dpWidth >= 600 ? ALL_IDS_b_w600dp : ALL_IDS_b);
            int k = 12 * (kanjiPage - 1) + 7; 
            int b = 0;
            while (b < buttonBIds.length && k < kanjis.length) {
                final Button button = (Button)findViewById(buttonBIds[b]);
                if (button != null) {
                    button.setText(kanjis[k]);
                    button.setEnabled(true);
                    button.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TextView textViewWritingProblemUserAnswer = (TextView)findViewById(R.id.textViewWritingProblemUserAnswer);
                                if (textViewWritingProblemUserAnswer.getText().toString().length() < MAX_ANSWER_LENGTH) { 
                                    textViewWritingProblemUserAnswer.setText(textViewWritingProblemUserAnswer.getText().toString() + button.getText().toString());
                                    findViewById(R.id.buttonDeleteKanji).setEnabled(true);
                                    clearCanvas(v);
                                }
                            }
                        }
                    );
                }

                b++;
                k++;
            }
            while (b < buttonBIds.length) {
                Button button = (Button)findViewById(buttonBIds[b]);
                if (button != null) {
                    button.setText(" ");
                    button.setEnabled(false);
                }
                b++;
            }
            
            Button buttonPrevPage = (Button)findViewById(R.id.buttonShowPrevPage_b);
            if (buttonPrevPage != null) {
                buttonPrevPage.setVisibility(VISIBLE);
                buttonPrevPage.setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            kanjiPage--;
                            initializeKanjiButtons();
                         }
                    }
                );
            }
            Button buttonNextPage = (Button)findViewById(R.id.buttonShowNextPage_b);
            if (buttonNextPage != null) {
                if (kanjis.length > 12 * kanjiPage + 7) {
                    buttonNextPage.setVisibility(VISIBLE);
                    buttonNextPage.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                kanjiPage++;
                                initializeKanjiButtons();
                             }
                        }
                    );
                }
                else
                    buttonNextPage.setVisibility(GONE);
            }
        }
    }

    /**
     * Called once the kanji list has been loaded so that it enables the button
     * if needed.
     */
    private void loaded() {
        KanjiDrawing kanjiCanvas = (KanjiDrawing)findViewById(R.id.kanjiDrawing);
        DrawnStroke[] strokes = kanjiCanvas.getStrokes();
    }

    /**
     * Converts from drawn strokes to the KanjiInfo object that
     * com.leafdigital.kanji classes expect.
     * @param strokes Strokes
     * @return Equivalent KanjiInfo object
     */
    static KanjiInfo getKanjiInfo(DrawnStroke[] strokes) {
        KanjiInfo info = new KanjiInfo("?");
        for(DrawnStroke stroke : strokes) {
            InputStroke inputStroke = new InputStroke( stroke.getStartX(), stroke.getStartY(), stroke.getEndX(), stroke.getEndY());
            info.addStroke(inputStroke);
        }
        info.finish();
        return info;
    }

    /**
     * Thread that loads the kanji list in the background.
     */
    private class LoadThread extends Thread {

        private LoadThread() {
            setPriority(MIN_PRIORITY);
            // Start loading the kanji list but only if it wasn't loaded already
            synchronized(listSynch) {
                if(list==null) {
                    waitingActivities.add(WritingProblemActivity.this);
                    if (!listLoading) {
                        listLoading = true;
                        start();
                    }
                }
            }
        }

        @Override
        public void run() {
            try {
                long start = System.currentTimeMillis();
                Log.d(WritingProblemActivity.class.getName(), "Kanji drawing dictionary loading");
                InputStream input = new MultiAssetInputStream(getAssets(), new String[] { "strokes-20100823.xml.1", "strokes-20100823.xml.2" });
                KanjiList loaded = new KanjiList(input);
                synchronized(listSynch) {
                    list = loaded;
                    for(WritingProblemActivity listening : waitingActivities) {
                        final WritingProblemActivity current = listening;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                current.loaded();
                            }
                        });
                    }
                    waitingActivities = null;
                }
                long time = System.currentTimeMillis() - start;
                Log.d(WritingProblemActivity.class.getName(), "Kanji drawing dictionary loaded (" + time + "ms)");
            }
            catch(IOException e) {
                Log.e(WritingProblemActivity.class.getName(), "Error loading dictionary", e);
            }
            finally {
                synchronized(listSynch) {
                    listLoading = false;
                }
            }
        }
    }


   /**
     * Do the match on another thread.
     */
    class MatchThread extends Thread {

        /**
         * @param owner Owning activity
         * @param waitString String (R.string) to display in wait dialog
         * @param showMore Show more kanji (smaller grid)
         *   show them again
         */
        MatchThread(Activity owner, DrawnStroke[] strokes, int waitString, boolean showMore) {
            this.activity = owner;
            this.strokes = strokes;
            progressDialog = new ProgressDialog(activity);
            progressDialog.setMessage(activity.getString(waitString));
            progressDialog.setCancelable(false);
            progressDialog.show();

            info = getKanjiInfo(strokes);

            start();
        }

        public void run() {
            boolean closedDialog = false;
            try {
                final Quiz quiz = appl.getQuiz();
                final Problem currProb = quiz.getCurrentProblem();
                final String rightAnswer = currProb.getRightAnswer();

                TextView textViewWritingProblemUserAnswer = (TextView)findViewById(R.id.textViewWritingProblemUserAnswer);
                String answer = textViewWritingProblemUserAnswer.getText().toString();
                final String rightChar = (answer.length() < rightAnswer.length() ? rightAnswer.charAt(answer.length()) + "" : null);

                final KanjiMatch[] exactMatches = list.getTopMatches(info, KanjiInfo.MatchAlgorithm.STRICT, null);
                boolean isRightKanjiFound = false;
                if (rightChar != null) {
                    // System.out.println("Looking in exactMatches exact.l=" + exactMatches.length);                    
                    for (int i = 0; i < exactMatches.length; i++ ) {
                        if (exactMatches[i].getKanji().getKanji().equals(rightChar)) {
                            isRightKanjiFound = true;
                            // System.out.println("Found! No need to compute fuzzy.");
                            break;
                        }
                    }
                }

                final KanjiMatch[] fuzzyMatches = (isRightKanjiFound ? new KanjiMatch[0] : list.getTopMatches(info, KanjiInfo.MatchAlgorithm.FUZZY, null));
                if (rightChar != null) {
                    // System.out.println("Looking in fuzzyMatches fuzzy.l="+fuzzyMatches.length);
                    for (int i = 0; i < fuzzyMatches.length; i++ ) {
                        if (fuzzyMatches[i].getKanji().getKanji().equals(rightChar)) {
                            isRightKanjiFound = true;
                            // System.out.println("Found! No need to compute fuzzier1.");
                            break;
                        }
                    }
                }

                final KanjiMatch[] fuzzier1Matches = (isRightKanjiFound ? new KanjiMatch[0] : list.getTopMatches(info, KanjiInfo.MatchAlgorithm.FUZZY_1OUT, null));
                if (rightChar != null) {
                    // System.out.println("Looking in fuzzier1Matches fuzzy.l="+fuzzier1Matches.length);
                    for (int i = 0; i < fuzzier1Matches.length; i++ ) {
                        if (fuzzier1Matches[i].getKanji().getKanji().equals(rightChar)) {
                            isRightKanjiFound = true;
                            // System.out.println("Found! No need to compute fuzzier2.");
                            break;
                        }
                    }
                }

                final KanjiMatch[] fuzzier2Matches = (isRightKanjiFound ? new KanjiMatch[0] : list.getTopMatches(info, KanjiInfo.MatchAlgorithm.FUZZY_2OUT, null));

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();

                        List<String> exactChars = new ArrayList<String>();
                        // List<Float> exactScores = new ArrayList<Float>();
                        List<String> fuzzyChars = new ArrayList<String>();
                        // List<Float> fuzzyScores = new ArrayList<Float>();
                        List<String> fuzzier1Chars = new ArrayList<String>();
                        // List<Float> fuzzier1Scores = new ArrayList<Float>();
                        List<String> fuzzier2Chars = new ArrayList<String>();
                        // List<Float> fuzzier2Scores = new ArrayList<Float>();

                        for (int i = 0; i < exactMatches.length; i++) {
                            String kanji = exactMatches[i].getKanji().getKanji();
                            // float score = exactMatches[i].getScore();
                            exactChars.add(kanji);
                            // exactScores.add(new Float(score));
                        }

                        for (int i = 0; i < fuzzyMatches.length; i++) {
                            String kanji = fuzzyMatches[i].getKanji().getKanji();
                            // float score = fuzzyMatches[i].getScore();
                            fuzzyChars.add(kanji);
                            // fuzzyScores.add(score);
                        }

                        for (int i = 0; i < fuzzier1Matches.length; i++) {
                            String kanji = fuzzier1Matches[i].getKanji().getKanji();
                            // float score = fuzzier1Matches[i].getScore();
                            fuzzier1Chars.add(kanji);
                            // fuzzier1Scores.add(score);
                        }

                        for (int i = 0; i < fuzzier2Matches.length; i++) {
                            String kanji = fuzzier2Matches[i].getKanji().getKanji();
                            // float score = fuzzier2Matches[i].getScore();
                            fuzzier2Chars.add(kanji);
                            // fuzzier2Scores.add(score);
                        }

                        // Show the 4 kanji lists .
                        // int c = 0;
                        // while (c < exactChars.size() || c < fuzzyChars.size() || c < fuzzier1Chars.size() || c < fuzzier2Chars.size()) {
                        //     StringBuilder line = new StringBuilder();
                        //     if (c < exactChars.size()) 
                        //         line.append("c="+c+" EXACT="+exactChars.get(c)+" ("+exactScores.get(c)+")   ");
                        //     else
                        //         line.append("                          ");
                        //     if (c < fuzzyChars.size()) 
                        //         line.append("c="+c+" fuzzy="+fuzzyChars.get(c)+" ("+fuzzyScores.get(c)+")   ");
                        //     else
                        //         line.append("                          ");
                        //     if (c < fuzzier1Chars.size()) 
                        //         line.append("c="+c+" fuzzier="+fuzzier1Chars.get(c)+" ("+fuzzierScores.get(c)+")   ");
                        //     else
                        //         line.append("                          ");
                        //     if (c < fuzzier2Chars.size()) 
                        //         line.append("c="+c+" fuzzier="+fuzzier2Chars.get(c)+" ("+fuzzierScores.get(c)+")   ");
                        //     else
                        //         line.append("                          ");
                        //     System.out.println(line.toString());
                        //     c++;
                        // }

                        List<String> mixedChars = new ArrayList<String>();
                        int i = 0;
                        while (mixedChars.size() < KANJIS_MAX_COUNT && (i < exactChars.size() || i < fuzzyChars.size() || i < fuzzier1Chars.size() || i < fuzzier2Chars.size())) {
                            if (i < exactChars.size()) {
                                String exactKanji = exactChars.get(i);
                                if (!mixedChars.contains(exactKanji))
                                    mixedChars.add(exactKanji);
                            }
                            
                            if (i < fuzzyChars.size()) {
                                String fuzzyKanji = fuzzyChars.get(i);
                                if (!mixedChars.contains(fuzzyKanji))
                                    mixedChars.add(fuzzyKanji);
                            }

                            if (i < fuzzier1Chars.size()) {
                                String fuzzier1Kanji = fuzzier1Chars.get(i);
                                if (!mixedChars.contains(fuzzier1Kanji))
                                    mixedChars.add(fuzzier1Kanji);
                            }

                            if (i < fuzzier2Chars.size()) {
                                String fuzzier2Kanji = fuzzier2Chars.get(i);
                                if (!mixedChars.contains(fuzzier2Kanji))
                                    mixedChars.add(fuzzier2Kanji);
                            }

                            i++;
                        }
               
                        // For user's convenience, if the right kanji is in the list, bring it to the first pages.
                        if (rightChar != null) {
                            int indexOfRightChar = mixedChars.indexOf(rightChar);
                            // System.out.println("The right character " + rightChar + "'s index in the mixed chars is " + indexOfRightChar);
                            if (indexOfRightChar != -1 && indexOfRightChar >= 7) {
                                int newPos = rand.nextInt(6) + 1;
                                mixedChars.remove(rightChar);
                                mixedChars.add(newPos, rightChar);
                                // System.out.println("Let's move it to index=" + newPos);
                            }
                        }

                        kanjis = mixedChars.toArray(new String[mixedChars.size()]);

                        // Show mixed kanji list.
                        // for (int m = 0; m < kanjis.length; m++)
                        // System.out.println("m="+m+" MIXED="+kanjis[m]);
                        // out.println("index exact="+exactChars.indexOf(rightChar) + " fuzzy="+fuzzyChars.indexOf(rightChar)+" fuzzier1="+fuzzier1Chars.indexOf(rightChar)+" fuzzier2="+fuzzier2Chars.indexOf(rightChar));

                        ((WritingProblemActivity)activity).initializeKanjiButtons();
                    }
                });
            }
            finally {
                if(!closedDialog) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                        }
                    });
                }
            }
        }

        private KanjiInfo info;
        private ProgressDialog progressDialog;
        private Activity activity;
        private DrawnStroke[] strokes;

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

            Intent quizSummaryActivity = new Intent(WritingProblemActivity.this, QuizSummaryActivity.class);
            startActivity(quizSummaryActivity);
        }

        private Exception exception;

    }

    private KankenApplication appl = KankenApplication.getInstance();

    private ProgressDialog progressDialog;

    private LinearLayout layoutKanjiInputRight_a; 
    private LinearLayout layoutKanjiInputRight_b; 

    private int kanjiPage = 0;
    private String[] kanjis;

    private static KanjiList list;
    private static boolean listLoading;
    private static LinkedList<WritingProblemActivity> waitingActivities = new LinkedList<WritingProblemActivity>();
    private static Object listSynch = new Object();

    private Random rand = new Random();

    private CountDownTimer kanjiTimer = null;

    private static final String storeResultsReqPath = "/cgi-bin/store_results.cgi";

}
