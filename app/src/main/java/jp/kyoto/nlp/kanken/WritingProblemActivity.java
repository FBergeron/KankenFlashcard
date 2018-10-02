package jp.kyoto.nlp.kanken;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
import static android.view.View.INVISIBLE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;

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

        findViewById(R.id.buttonEnterWritingProblemCharacter).setEnabled(false);
        KanjiDrawing kanjiCanvas = findViewById(R.id.kanjiDrawing);
        if (matchThread != null) {
            matchThread.stop();
            matchThread = null;
        }
        imageViewSearchingWritingProblemCharacter.setVisibility(VISIBLE);
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
    }

    public void stopSearchingForKanjis() {
        if (matchThread != null) {
            matchThread.stop();
            matchThread = null;
        }
        imageViewSearchingWritingProblemCharacter.setVisibility(INVISIBLE);
    }

    public void validateAnswer(android.view.View view) {
        TextView textViewProblemUserAnswer = findViewById(R.id.textViewProblemUserAnswer);
        final String answer = textViewProblemUserAnswer.getText().toString();

        if (answer.trim().equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(WritingProblemActivity.this);
            builder.setTitle(getResources().getString(R.string.error_empty_answer_title))
            .setMessage(getResources().getString(R.string.error_empty_answer_msg))
            .setPositiveButton(R.string.button_continue, new DialogInterface.OnClickListener() {
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

        // Make sure the list gets loaded
        new LoadThread();

        textViewProblemUserAnswer = findViewById(R.id.textViewProblemUserAnswer);

        imageViewSearchingWritingProblemCharacter = findViewById(R.id.imageViewSearchingWritingProblemCharacter);

        showProblemStatement();

        if (appl.getQuiz().getCurrentMode() == Quiz.Mode.MODE_ASK)
            askProblem();
        else
            showProblemEvaluation();

        Glide.with(this).load("android_asset/searching.gif").into(imageViewSearchingWritingProblemCharacter);

        typefaceKanjiButton = Typeface.createFromAsset(getAssets(), "gyate-luminescence.otf");
        textViewProblemUserAnswer.setTypeface(typefaceKanjiButton);
    }

    protected void askProblem() {
        super.askProblem();

        findViewById(R.id.layoutKanjiInput).setVisibility(VISIBLE);
        findViewById(R.id.layoutWritingProblemUserAnswer).setVisibility(VISIBLE);
        findViewById(R.id.fragmentProblemEvaluation).setVisibility(GONE);

        textViewProblemUserAnswer.setText(appl.getQuiz().getCurrentAnswer());

        layoutKanjiInputRight_a = findViewById(R.id.layoutKanjiInputRight_a);
        layoutKanjiInputRight_a.setVisibility(VISIBLE);
        layoutKanjiInputRight_b = findViewById(R.id.layoutKanjiInputRight_b);
        layoutKanjiInputRight_b.setVisibility(GONE);

        KanjiDrawing kanjiCanvas = findViewById(R.id.kanjiDrawing);
        kanjiCanvas.setListener(
            new KanjiDrawing.Listener() {
                @Override
                public void strokes(DrawnStroke[] strokes) {
                    if (strokes.length > 0) {
                        startTimer(SHOW_KANJIS_DELAY);

                        findViewById(R.id.buttonUndoWritingProblemCanvas).setEnabled(true);
                        findViewById(R.id.buttonClearWritingProblemCanvas).setEnabled(true);
                        findViewById(R.id.buttonEnterWritingProblemCharacter).setEnabled(true);
                    }
                    else {
                        if (kanjiTimer != null)
                            kanjiTimer.cancel();

                        findViewById(R.id.buttonUndoWritingProblemCanvas).setEnabled(false);
                        findViewById(R.id.buttonClearWritingProblemCanvas).setEnabled(false);
                        findViewById(R.id.buttonEnterWritingProblemCharacter).setEnabled(false);
                        findViewById(R.id.buttonShowNextPage_a).setVisibility(GONE);

                        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
                        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

                        layoutKanjiInputRight_a.setVisibility(VISIBLE);
                        layoutKanjiInputRight_b.setVisibility(GONE);

                        int[] buttonAIds = (dpWidth >= 600 ? ALL_IDS_a_w600dp : ALL_IDS_a);
                        for (int buttonAId : buttonAIds) {
                            Button button = findViewById(buttonAId);
                            if (button != null) {
                                button.setText("");
                                button.setEnabled(false);
                            }
                        }
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

        findViewById(R.id.layoutKanjiInput).setVisibility(GONE);
        findViewById(R.id.layoutWritingProblemUserAnswer).setVisibility(GONE);
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
        }
        else
            enterCharacter(null);
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
                final Button button = findViewById(buttonAIds[k]);
                if (button != null) {
                    button.setText(kanjis[k]);
                    button.setEnabled(true);
                    button.setTypeface(typefaceKanjiButton);
                    button.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (textViewProblemUserAnswer.getText().toString().length() < MAX_ANSWER_LENGTH) { 
                                    stopSearchingForKanjis();
                                    String newAnswer = textViewProblemUserAnswer.getText().toString() + button.getText().toString();
                                    textViewProblemUserAnswer.setText(newAnswer);
                                    appl.getQuiz().setCurrentAnswer(newAnswer);
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
                Button button = findViewById(buttonAIds[k]);
                if (button != null) {
                    button.setText(" ");
                    button.setEnabled(false);
                }
                k++;
            }

            Button buttonNextPage = findViewById(R.id.buttonShowNextPage_a);
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
                final Button button = findViewById(buttonBIds[b]);
                if (button != null) {
                    button.setText(kanjis[k]);
                    button.setEnabled(true);
                    button.setTypeface(typefaceKanjiButton);
                    button.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (textViewProblemUserAnswer.getText().toString().length() < MAX_ANSWER_LENGTH) { 
                                    String newAnswer = textViewProblemUserAnswer.getText().toString() + button.getText().toString();
                                    textViewProblemUserAnswer.setText(newAnswer);
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
                Button button = findViewById(buttonBIds[b]);
                if (button != null) {
                    button.setText(" ");
                    button.setEnabled(false);
                }
                b++;
            }
            
            Button buttonPrevPage = findViewById(R.id.buttonShowPrevPage_b);
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
            Button buttonNextPage = findViewById(R.id.buttonShowNextPage_b);
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
        KanjiDrawing kanjiCanvas = findViewById(R.id.kanjiDrawing);
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
                if(listExact==null) {
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
                Log.d(tag, "Kanji drawing dictionary loading");
                InputStream input = new MultiAssetInputStream(getAssets(), new String[] { "strokes-20100823.xml.1", "strokes-20100823.xml.2" });
                KanjiList loaded = new KanjiList(input);
                synchronized(listSynch) {
                    listExact = loaded;
                    for(WritingProblemActivity listening : waitingActivities) {
                        final WritingProblemActivity current = listening;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                current.loaded();
                            }
                        });
                    }
                    //waitingActivities = null;
                }
                long time = System.currentTimeMillis() - start;
                Log.d(tag, "Kanji drawing dictionary loaded (" + time + "ms)");
                
                start = System.currentTimeMillis();
                Log.d(tag, "Kanji drawing dictionary loading");
                input = new MultiAssetInputStream(getAssets(), new String[] { "strokes-20100823.xml.1", "strokes-20100823.xml.2" });
                loaded = new KanjiList(input);
                synchronized(listSynch) {
                    listFuzzy = loaded;
                    for(WritingProblemActivity listening : waitingActivities) {
                        final WritingProblemActivity current = listening;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                current.loaded();
                            }
                        });
                    }
                    //waitingActivities = null;
                }
                time = System.currentTimeMillis() - start;
                Log.d(tag, "Kanji drawing dictionary loaded (" + time + "ms)");
                
                start = System.currentTimeMillis();
                Log.d(tag, "Kanji drawing dictionary loading");
                input = new MultiAssetInputStream(getAssets(), new String[] { "strokes-20100823.xml.1", "strokes-20100823.xml.2" });
                loaded = new KanjiList(input);
                synchronized(listSynch) {
                    listFuzzier1 = loaded;
                    for(WritingProblemActivity listening : waitingActivities) {
                        final WritingProblemActivity current = listening;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                current.loaded();
                            }
                        });
                    }
                    //waitingActivities = null;
                }
                time = System.currentTimeMillis() - start;
                Log.d(tag, "Kanji drawing dictionary loaded (" + time + "ms)");
                
                start = System.currentTimeMillis();
                Log.d(tag, "Kanji drawing dictionary loading");
                input = new MultiAssetInputStream(getAssets(), new String[] { "strokes-20100823.xml.1", "strokes-20100823.xml.2" });
                loaded = new KanjiList(input);
                synchronized(listSynch) {
                    listFuzzier2 = loaded;
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
                time = System.currentTimeMillis() - start;
                Log.d(tag, "Kanji drawing dictionary loaded (" + time + "ms)");
            }
            catch(IOException e) {
                Log.e(tag, "Error loading dictionary", e);
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

            if (strokes.length < 8 || strokes.length >= 20) {
                if (!isRunning) {
                    Log.d(tag,  "out0!" );                
                    return;
                }
                long startTimeExact = System.currentTimeMillis();
                exactMatches = listExact.getTopMatches(info, KanjiInfo.MatchAlgorithm.STRICT, null, this);
                long stopTimeExact = System.currentTimeMillis();

                boolean isRightKanjiFound = false;
                if (rightChar != null) {
                    Log.d(tag, "Looking in exactMatches exact.l=" + exactMatches.length);                    
                    for (KanjiMatch exactMatch : exactMatches) {
                        if (exactMatch.getKanji().getKanji().equals(rightChar)) {
                            isRightKanjiFound = true;
                            Log.d(tag, "Found! No need to compute fuzzy.");
                            break;
                        }
                    }
                }

                long startTimeFuzzy = System.currentTimeMillis();
                if (!isRunning) {
                    Log.d(tag,  "out1!" );
                    return;
                }
                fuzzyMatches = (!isRunning || isRightKanjiFound ? new KanjiMatch[0] : listFuzzy.getTopMatches(info, KanjiInfo.MatchAlgorithm.FUZZY, null, this));
                long stopTimeFuzzy = System.currentTimeMillis();
                if (rightChar != null) {
                    Log.d(tag, "Looking in fuzzyMatches fuzzy.l="+fuzzyMatches.length);
                    for (KanjiMatch fuzzyMatch : fuzzyMatches) {
                        if (fuzzyMatch.getKanji().getKanji().equals(rightChar)) {
                            isRightKanjiFound = true;
                            Log.d(tag, "Found! No need to compute fuzzier1.");
                            break;
                        }
                    }
                }

                if (!isRunning) {
                    Log.d(tag,  "out2!" );
                    return;
                }
                long startTimeFuzzier1 = System.currentTimeMillis();
                fuzzier1Matches = (!isRunning || isRightKanjiFound ? new KanjiMatch[0] : listFuzzier1.getTopMatches(info, KanjiInfo.MatchAlgorithm.FUZZY_1OUT, null, this));
                long stopTimeFuzzier1 = System.currentTimeMillis();
                if (rightChar != null) {
                    Log.d(tag, "Looking in fuzzier1Matches fuzzy.l="+fuzzier1Matches.length);
                    for (KanjiMatch fuzzier1Match : fuzzier1Matches) {
                        if (fuzzier1Match.getKanji().getKanji().equals(rightChar)) {
                            isRightKanjiFound = true;
                            Log.d(tag, "Found! No need to compute fuzzier2.");
                            break;
                        }
                    }
                }

                if (!isRunning) {
                    Log.d(tag,  "out3!" );                
                    return;
                }
                long startTimeFuzzier2 = System.currentTimeMillis();
                fuzzier2Matches = (!isRunning || isRightKanjiFound ? new KanjiMatch[0] : listFuzzier2.getTopMatches(info, KanjiInfo.MatchAlgorithm.FUZZY_2OUT, null, this));
                long stopTimeFuzzier2 = System.currentTimeMillis();
                Log.d(tag, "fuzzier2Matches fuzzy.l="+fuzzier2Matches.length);

                Log.d(tag, "Exact time="+(stopTimeExact-startTimeExact)+" ms");
                Log.d(tag, "Fuzzy time="+(stopTimeFuzzy-startTimeFuzzy)+" ms");
                Log.d(tag, "Fuzzier1 time="+(stopTimeFuzzier1-startTimeFuzzier1)+" ms");
                Log.d(tag, "Fuzzier2 time="+(stopTimeFuzzier2-startTimeFuzzier2)+" ms");
                Log.d(tag, "Total time="+((stopTimeExact-startTimeExact)+(stopTimeFuzzy-startTimeFuzzy)+(stopTimeFuzzier1-startTimeFuzzier1)+(stopTimeFuzzier2-startTimeFuzzier2))+" ms");
                if (!isRunning) {
                    Log.d(tag,  "out4!" );                
                    return;
                }
            }
            else {
                final MatchThread parent = this;
                Thread workerExact = new Thread(
                    new Runnable() {
                        public void run() {
                            long startTimeExact = System.currentTimeMillis();
                            exactMatches = listExact.getTopMatches(infoExact, KanjiInfo.MatchAlgorithm.STRICT, null, parent);
                            long stopTimeExact = System.currentTimeMillis();
                            Log.d(tag, "Exact time="+(stopTimeExact-startTimeExact)+" ms");
                        }
                    }
                );
                Thread workerFuzzy = new Thread(
                    new Runnable() {
                        public void run() {
                            long startTimeFuzzy = System.currentTimeMillis();
                            fuzzyMatches = listFuzzy.getTopMatches(infoFuzzy, KanjiInfo.MatchAlgorithm.FUZZY, null, parent);
                            long stopTimeFuzzy = System.currentTimeMillis();
                            Log.d(tag, "Fuzzy time="+(stopTimeFuzzy-startTimeFuzzy)+" ms");
                        }
                    }
                );
                Thread workerFuzzier1 = new Thread(
                    new Runnable() {
                        public void run() {
                            long startTimeFuzzier1 = System.currentTimeMillis();
                            fuzzier1Matches = listFuzzier1.getTopMatches(infoFuzzier1, KanjiInfo.MatchAlgorithm.FUZZY_1OUT, null, parent);
                            long stopTimeFuzzier1 = System.currentTimeMillis();
                            Log.d(tag, "Fuzzier1 time="+(stopTimeFuzzier1-startTimeFuzzier1)+" ms");
                        }
                    }
                );
                Thread workerFuzzier2 = new Thread(
                    new Runnable() {
                        public void run() {
                            long startTimeFuzzier2 = System.currentTimeMillis();
                            fuzzier2Matches = listFuzzier2.getTopMatches(infoFuzzier2, KanjiInfo.MatchAlgorithm.FUZZY_2OUT, null, parent);
                            long stopTimeFuzzier2 = System.currentTimeMillis();
                            Log.d(tag, "Fuzzier2 time="+(stopTimeFuzzier2-startTimeFuzzier2)+" ms");
                        }
                    }
                );
                long startTime = System.currentTimeMillis();
                if (!isRunning)
                    return;

                workerExact.start();
                workerFuzzy.start();
                workerFuzzier1.start();
                workerFuzzier2.start();

                try {
                    workerExact.join();
                }
                catch(InterruptedException ignore) {
                    ignore.printStackTrace();
                }
                try {
                    workerFuzzy.join();
                }
                catch(InterruptedException ignore) {
                    ignore.printStackTrace();
                }
                try {
                    workerFuzzier1.join();
                }
                catch(InterruptedException ignore) {
                    ignore.printStackTrace();
                }
                try {
                    workerFuzzier2.join();
                }
                catch(InterruptedException ignore) {
                    ignore.printStackTrace();
                }
                long stopTime = System.currentTimeMillis();
                Log.d(tag, "Search time="+(stopTime-startTime)+" ms");
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
                        //     Log.d(tag, line.toString());
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
                            // Log.d(tag, "The right character " + rightChar + "'s index in the mixed chars is " + indexOfRightChar);
                            if (indexOfRightChar != -1 && indexOfRightChar >= 7) {
                                int newPos = rand.nextInt(6) + 1;
                                mixedChars.remove(rightChar);
                                mixedChars.add(newPos, rightChar);
                                // Log.d(tag, "Let's move it to index=" + newPos);
                            }
                        }

                        kanjis = mixedChars.toArray(new String[mixedChars.size()]);

                        // Show mixed kanji list.
                        // for (int m = 0; m < kanjis.length; m++)
                        // Log.d(tag, "m="+m+" MIXED="+kanjis[m]);
                        // out.println("index exact="+exactChars.indexOf(rightChar) + " fuzzy="+fuzzyChars.indexOf(rightChar)+" fuzzier1="+fuzzier1Chars.indexOf(rightChar)+" fuzzier2="+fuzzier2Chars.indexOf(rightChar));

                        ((WritingProblemActivity)activity).initializeKanjiButtons();
                        
                        long stopTime = System.currentTimeMillis();
                        Log.d(tag, "Update time="+(stopTime-startTime)+" ms");

                        imageViewSearchingWritingProblemCharacter.setVisibility(INVISIBLE);
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

    private KankenApplication appl = KankenApplication.getInstance();

    private LinearLayout layoutKanjiInputRight_a; 
    private LinearLayout layoutKanjiInputRight_b; 

    private ImageView imageViewSearchingWritingProblemCharacter;
    private TextView textViewProblemUserAnswer;

    private Typeface typefaceKanjiButton;

    private int kanjiPage = 0;
    private String[] kanjis;

    private static KanjiList listExact;
    private static KanjiList listFuzzy;
    private static KanjiList listFuzzier1;
    private static KanjiList listFuzzier2;
    private static boolean listLoading;
    private static LinkedList<WritingProblemActivity> waitingActivities = new LinkedList<WritingProblemActivity>();
    private static final Object listSynch = new Object();

    private Random rand = new Random();

    private CountDownTimer kanjiTimer = null;

    private MatchThread matchThread = null;

    private final static int MAX_ANSWER_LENGTH = 10;

    //private final static long SHOW_KANJIS_DELAY = 1200; // In ms.
    //private final static long SHOW_KANJIS_DELAY = 600; // In ms.
    //private final static long SHOW_KANJIS_DELAY = 200; // In ms.
    private final static long SHOW_KANJIS_DELAY = 0; // In ms.

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

    private final static int KANJIS_MAX_COUNT = 80;

    private final static String tag = "WritingProblemActivity";

}
