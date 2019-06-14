package jp.kyoto.nlp.kanken;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.leafdigital.kanji.InputStroke;
import com.leafdigital.kanji.KanjiInfo;
import com.leafdigital.kanji.KanjiList;
import com.leafdigital.kanji.KanjiMatch;
import com.leafdigital.kanji.android.KanjiDrawing;
import com.leafdigital.kanji.android.KanjiDrawing.DrawnStroke;
import com.leafdigital.kanji.android.MultiAssetInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

//import android.support.v7.app.ActionBar;

public class WritingProblemActivity extends QuizProblemActivity {

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
        TextView textViewProblemUserAnswer = findViewById(R.id.textViewProblemUserAnswer);
        String origText = textViewProblemUserAnswer.getText().toString();
        if (origText.length() > 0) {
            String newAnswer = origText.substring(0, origText.length() - 1);
            textViewProblemUserAnswer.setText(newAnswer);
            appl.getQuiz().setCurrentAnswer(newAnswer);
            findViewById(R.id.buttonDeleteKanji).setEnabled(newAnswer.length() > 0);
        }
    }

    public void enterCharacter(android.view.View view) {
        if (kanjiTimer != null)
            kanjiTimer.cancel();

        KanjiDrawing kanjiCanvas = findViewById(R.id.kanjiDrawing);
        if (matchThread != null) {
            matchThread.stop();
            matchThread = null;
        }
        matchThread = new MatchThread(this, kanjiCanvas.getStrokes(), R.string.label_finding_characters, true);
    }

    public void undoCanvas(android.view.View view) {
        stopSearchingForKanjis();

        KanjiDrawing kanjiCanvas = findViewById(R.id.kanjiDrawing);
        kanjiCanvas.undo();
    }

    public void clearCanvas(android.view.View view) {
        stopSearchingForKanjis();

        KanjiDrawing kanjiCanvas = findViewById(R.id.kanjiDrawing);
        kanjiCanvas.clear();

        for (Button button : kanjiButtons) {
            button.setEnabled(false);
            button.setText("");
        }

        btnNextItems.setEnabled(false);
    }

    public void stopSearchingForKanjis() {
        if (matchThread != null) {
            matchThread.stop();
            matchThread = null;
        }
    }

    public void validateAnswer(android.view.View view) {
        TextView textViewProblemUserAnswer = findViewById(R.id.textViewProblemUserAnswer);
        final String answer = textViewProblemUserAnswer.getText().toString();

        if (answer.trim().equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(WritingProblemActivity.this);
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
        setContentView(R.layout.activity_writing_problem);

        findViewById(R.id.kanjiDrawing).setEnabled(false);

        // Make sure the list gets loaded
        new LoadThread();

        textViewProblemUserAnswer = findViewById(R.id.textViewProblemUserAnswer);

        showProblemStatement();

        int[] kanjiButtonIds = {
                R.id.kanji1,
                R.id.kanji2,
                R.id.kanji3,
                R.id.kanji4,
                R.id.kanji5,
                R.id.kanji6,
                R.id.kanji7
        };

        for (int kanjiButtonId : kanjiButtonIds) {
            Button button = findViewById(kanjiButtonId);
            button.setEnabled(false);
            kanjiButtons.add(button);
        }

        btnNextItems = findViewById(R.id.btnNextItems);

        if (appl.getQuiz().getCurrentMode() == Quiz.Mode.MODE_ASK)
            askProblem();
        else
            showProblemEvaluation();

    }

    protected void askProblem() {
        super.askProblem();

        findViewById(R.id.contentBody).setVisibility(VISIBLE);
        findViewById(R.id.fragmentProblemEvaluation).setVisibility(GONE);

        textViewProblemUserAnswer.setText(appl.getQuiz().getCurrentAnswer());

        KanjiDrawing kanjiCanvas = findViewById(R.id.kanjiDrawing);
        kanjiCanvas.setListener(
            new KanjiDrawing.Listener() {
                @Override
                public void strokes(DrawnStroke[] strokes) {
                    if (strokes.length > 0) {
                        startTimer(SHOW_KANJIS_DELAY);

                        findViewById(R.id.buttonUndoWritingProblemCanvas).setEnabled(true);
                        findViewById(R.id.buttonClearWritingProblemCanvas).setEnabled(true);
                    }
                    else {
                        if (kanjiTimer != null)
                            kanjiTimer.cancel();

                        findViewById(R.id.buttonUndoWritingProblemCanvas).setEnabled(false);
                        findViewById(R.id.buttonClearWritingProblemCanvas).setEnabled(false);

                        kanjis = null;
                    }
                    kanjiPage = 0;
                }
            }
        );

        clearCanvas(null);
        findViewById(R.id.buttonDeleteKanji).setEnabled(false);
    }

    protected void showProblemEvaluation() {
        super.showProblemEvaluation();

        findViewById(R.id.contentBody).setVisibility(GONE);
        findViewById(R.id.fragmentProblemEvaluation).setVisibility(VISIBLE);
    }

    private void startTimer(long time) {
        if (time > 0) {
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
        } else {
            enterCharacter(null);
        }
    }

    private void initializeKanjiButtons() {
        setKanjiButton();
    }

    /**
     * Called once the kanji list has been loaded so that it enables the button
     * if needed.
     */
    private void loaded() {
        KanjiDrawing kanjiCanvas = findViewById(R.id.kanjiDrawing);
        DrawnStroke[] strokes = kanjiCanvas.getStrokes();
        kanjiCanvas.setEnabled(true);
        dissmissLoadingDialog();
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
                if(listExact==null) {
                    showLoadingDialog();
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
                String[] files = new String[] { "strokes-20160426.xml.1", "strokes-20160426.xml.2" };
                listExact = loadList(getAssets(), files);

                listFuzzy = loadList(getAssets(), files);

                listFuzzier1 = loadList(getAssets(), files);

                listFuzzier2 = loadList(getAssets(), files);
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
            catch(IOException e) {
                Log.e(TAG, "Error loading dictionary", e);
            }
            finally {
                synchronized(listSynch) {
                    listLoading = false;
                }
            }
        }

        synchronized private KanjiList loadList(AssetManager assetManager, String[] files) throws IOException {
            InputStream input = new MultiAssetInputStream(assetManager, files);
            return new KanjiList(input);
        }
    }


   /**
     * Do the match on another thread.
     */
    class MatchThread implements Runnable, Runner {

        /**
         * @param owner Owning activity
         * @param waitString String (R.string) to display in wait dialog
         * @param showMore Show more kanji (smaller grid)
         *   show them again
         */
        MatchThread(Activity owner, DrawnStroke[] strokes, int waitString, boolean showMore) {
            this.activity = owner;
            this.strokes = strokes;

            info = getKanjiInfo(strokes);
            infoExact = getKanjiInfo(strokes);
            infoFuzzy = getKanjiInfo(strokes);
            infoFuzzier1 = getKanjiInfo(strokes);
            infoFuzzier2 = getKanjiInfo(strokes);

            worker = new Thread(this);
            worker.start();
        }

        public boolean isRunning() {
            return isRunning;
        }

        public void stop() {
            isRunning = false;
            worker = null;
        }

        private KanjiMatch[] exactMatches;
        private KanjiMatch[] fuzzyMatches;
        private KanjiMatch[] fuzzier1Matches;
        private KanjiMatch[] fuzzier2Matches;

        public void run() {
            isRunning = true;

            final Quiz quiz = appl.getQuiz();
            final Problem currProb = quiz.getCurrentProblem();
            final String rightAnswer = currProb.getRightAnswer();

            String answer = textViewProblemUserAnswer.getText().toString();
            final String rightChar = (answer.length() < rightAnswer.length() ? rightAnswer.charAt(answer.length()) + "" : null);

            if (true) {
                if (!isRunning) {
                    Log.d(TAG,  "out0!" );
                    return;
                }
                long startTimeExact = System.currentTimeMillis();
                exactMatches = listExact.getTopMatches(info, KanjiInfo.MatchAlgorithm.STRICT, null, this);
                long stopTimeExact = System.currentTimeMillis();

                boolean isRightKanjiFound = false;
                // It's better not to set isRightKanjiFound to true too early because the list of kanji might be too suspiciously short.
                // For example, in the case where the right answer contains the kanji ichi.  The list will contain only 1 kanji.
                //
                // if (rightChar != null) {
                //     Log.d(TAG, "Looking in exactMatches exact.l=" + exactMatches.length);
                //     for (KanjiMatch exactMatch : exactMatches) {
                //         if (exactMatch.getKanji().getKanji().equals(rightChar)) {
                //             isRightKanjiFound = true;
                //             Log.d(TAG, "Found! No need to compute fuzzy.");
                //             break;
                //         }
                //     }
                // }

                long startTimeFuzzy = System.currentTimeMillis();
                if (!isRunning) {
                    Log.d(TAG,  "out1!" );
                    return;
                }
                fuzzyMatches = new KanjiMatch[0];
                long stopTimeFuzzy = System.currentTimeMillis();
                if (rightChar != null) {
                    Log.d(TAG, "Looking in fuzzyMatches fuzzy.l="+fuzzyMatches.length);
                    for (KanjiMatch fuzzyMatch : fuzzyMatches) {
                        if (fuzzyMatch.getKanji().getKanji().equals(rightChar)) {
                            isRightKanjiFound = true;
                            Log.d(TAG, "Found! No need to compute fuzzier1.");
                            break;
                        }
                    }
                }

                if (!isRunning) {
                    Log.d(TAG,  "out2!" );
                    return;
                }
                long startTimeFuzzier1 = System.currentTimeMillis();
                fuzzier1Matches = new KanjiMatch[0];
                long stopTimeFuzzier1 = System.currentTimeMillis();
                if (rightChar != null) {
                    Log.d(TAG, "Looking in fuzzier1Matches fuzzy.l="+fuzzier1Matches.length);
                    for (KanjiMatch fuzzier1Match : fuzzier1Matches) {
                        if (fuzzier1Match.getKanji().getKanji().equals(rightChar)) {
                            isRightKanjiFound = true;
                            Log.d(TAG, "Found! No need to compute fuzzier2.");
                            break;
                        }
                    }
                }

                if (!isRunning) {
                    Log.d(TAG,  "out3!" );
                    return;
                }
                long startTimeFuzzier2 = System.currentTimeMillis();
                fuzzier2Matches = new KanjiMatch[0];
                long stopTimeFuzzier2 = System.currentTimeMillis();
                Log.d(TAG, "fuzzier2Matches fuzzy.l="+fuzzier2Matches.length);

                Log.d(TAG, "Exact time="+(stopTimeExact-startTimeExact)+" ms");
                Log.d(TAG, "Fuzzy time="+(stopTimeFuzzy-startTimeFuzzy)+" ms");
                Log.d(TAG, "Fuzzier1 time="+(stopTimeFuzzier1-startTimeFuzzier1)+" ms");
                Log.d(TAG, "Fuzzier2 time="+(stopTimeFuzzier2-startTimeFuzzier2)+" ms");
                Log.d(TAG, "Total time="+((stopTimeExact-startTimeExact)+(stopTimeFuzzy-startTimeFuzzy)+(stopTimeFuzzier1-startTimeFuzzier1)+(stopTimeFuzzier2-startTimeFuzzier2))+" ms");
                if (!isRunning) {
                    Log.d(TAG,  "out4!" );
                    return;
                }
            }

            if (isRunning) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long startTime = System.currentTimeMillis();

                        List<String> exactChars = new ArrayList<String>();
                        // List<Float> exactScores = new ArrayList<Float>();
                        List<String> fuzzyChars = new ArrayList<String>();
                        // List<Float> fuzzyScores = new ArrayList<Float>();
                        List<String> fuzzier1Chars = new ArrayList<String>();
                        // List<Float> fuzzier1Scores = new ArrayList<Float>();
                        List<String> fuzzier2Chars = new ArrayList<String>();
                        // List<Float> fuzzier2Scores = new ArrayList<Float>();

                        for (KanjiMatch exactMatch : exactMatches) {
                            String kanji = exactMatch.getKanji().getKanji();
                            // float score = exactMatches[i].getScore();
                            exactChars.add(kanji);
                            // exactScores.add(new Float(score));
                        }

                        for (KanjiMatch fuzzyMatch : fuzzyMatches) {
                            String kanji = fuzzyMatch.getKanji().getKanji();
                            // float score = fuzzyMatches[i].getScore();
                            fuzzyChars.add(kanji);
                            // fuzzyScores.add(score);
                        }

                        for (KanjiMatch fuzzier1Match : fuzzier1Matches) {
                            String kanji = fuzzier1Match.getKanji().getKanji();
                            // float score = fuzzier1Matches[i].getScore();
                            fuzzier1Chars.add(kanji);
                            // fuzzier1Scores.add(score);
                        }

                        for (KanjiMatch fuzzier2Match : fuzzier2Matches) {
                            String kanji = fuzzier2Match.getKanji().getKanji();
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
                        //     Log.d(TAG, line.toString());
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
                            // Log.d(TAG, "The right character " + rightChar + "'s index in the mixed chars is " + indexOfRightChar);
                            if (indexOfRightChar != -1 && indexOfRightChar >= 7) {
                                int newPos = rand.nextInt(6) + 1;
                                mixedChars.remove(rightChar);
                                mixedChars.add(newPos, rightChar);
                                // Log.d(TAG, "Let's move it to index=" + newPos);
                            }
                        }

                        kanjis = mixedChars.toArray(new String[mixedChars.size()]);

                        // Show mixed kanji list.
                        // for (int m = 0; m < kanjis.length; m++)
                        // Log.d(TAG, "m="+m+" MIXED="+kanjis[m]);
                        // out.println("index exact="+exactChars.indexOf(rightChar) + " fuzzy="+fuzzyChars.indexOf(rightChar)+" fuzzier1="+fuzzier1Chars.indexOf(rightChar)+" fuzzier2="+fuzzier2Chars.indexOf(rightChar));

                        ((WritingProblemActivity)activity).initializeKanjiButtons();
                        
                        long stopTime = System.currentTimeMillis();
                        Log.d(TAG, "Update time="+(stopTime-startTime)+" ms");

                    }
                });
            }
        }

        private KanjiInfo info;
        private KanjiInfo infoExact;
        private KanjiInfo infoFuzzy;
        private KanjiInfo infoFuzzier1;
        private KanjiInfo infoFuzzier2;

        private Activity activity;
        private DrawnStroke[] strokes;

        private boolean isRunning; 

        private Thread worker;

    }

    public void onClickNextItems(View view) {
        kanjiPage += MAX_KANJI_BUTTON_NUM;
        if (kanjiPage >= kanjis.length) {
            kanjiPage = 0;
        }
        setKanjiButton();
    }

    private void setKanjiButton() {
        if (kanjis.length < MAX_KANJI_BUTTON_NUM) {
            btnNextItems.setEnabled(false);
        } else {
            btnNextItems.setEnabled(true);
        }
        for (int i = 0; i < MAX_KANJI_BUTTON_NUM; i++) {
            Button button = kanjiButtons.get(i);
            int kanjiIndex = i + kanjiPage;
            if (kanjiIndex >= kanjis.length) {
                button.setEnabled(false);
                button.setVisibility(View.INVISIBLE);
            } else {
                button.setEnabled(true);
                button.setVisibility(View.VISIBLE);
                String kanji = kanjis[kanjiIndex];
                button.setText(kanji);
            }
        }
    }

    public void onClickKanji(View view) {
        Button button = (Button) view;
        if (textViewProblemUserAnswer.getText().toString().length() < MAX_ANSWER_LENGTH) {
            String newAnswer = textViewProblemUserAnswer.getText().toString() + button.getText();
            textViewProblemUserAnswer.setText(newAnswer);
            appl.getQuiz().setCurrentAnswer(newAnswer);
            findViewById(R.id.buttonDeleteKanji).setEnabled(true);
            clearCanvas(view);
        }
    }

    private void showLoadingDialog() {
        runOnUiThread(() -> {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setCancelable(false);
            progressDialog.show();
        });
    }

    private void dissmissLoadingDialog() {
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        });
    }


    private KankenApplication appl = KankenApplication.getInstance();

    private TextView textViewProblemUserAnswer;

    private int kanjiPage = 0;
    private String[] kanjis;

    private static KanjiList listExact;
    private static KanjiList listFuzzy;
    private static KanjiList listFuzzier1;
    private static KanjiList listFuzzier2;
    private static boolean listLoading;
    private static LinkedList<WritingProblemActivity> waitingActivities = new LinkedList<WritingProblemActivity>();
    private static final Object listSynch = new Object();
    private static final int MAX_KANJI_BUTTON_NUM = 7;

    private Random rand = new Random();

    private CountDownTimer kanjiTimer = null;

    private MatchThread matchThread = null;

    private final static int MAX_ANSWER_LENGTH = 10;

    //private final static long SHOW_KANJIS_DELAY = 1200; // In ms.
    //private final static long SHOW_KANJIS_DELAY = 600; // In ms.
    //private final static long SHOW_KANJIS_DELAY = 200; // In ms.
    private final static long SHOW_KANJIS_DELAY = 0; // In ms.

    private final static int KANJIS_MAX_COUNT = 80;

    private final static String TAG = "WritingProblemActivity";

    private List<Button> kanjiButtons = new ArrayList<>();
    private ImageButton btnNextItems;

    private ProgressDialog progressDialog;

}
