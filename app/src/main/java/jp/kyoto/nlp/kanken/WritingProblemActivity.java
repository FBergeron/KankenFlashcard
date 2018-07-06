package jp.kyoto.nlp.kanken;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static android.view.View.*;

public class WritingProblemActivity extends AppCompatActivity {

    private final static int[] ALL_IDS_a = {
        R.id.no1_a, R.id.no2_a, R.id.no3_a, R.id.no4_a, R.id.no5_a, R.id.no6_a, R.id.no7_a
    };

    private final static int[] ALL_IDS_b = {
        R.id.no1_b, R.id.no2_b, R.id.no3_b, R.id.no4_b, R.id.no5_b, R.id.no6_b, R.id.no7_b, R.id.no8_b, R.id.no9_b, R.id.no10_b, R.id.no11_b, R.id.no12_b
    };

    public void deleteKanji(android.view.View view) {
        TextView textViewWritingProblemUserAnswer = (TextView)findViewById(R.id.textViewWritingProblemUserAnswer);
        String origText = textViewWritingProblemUserAnswer.getText().toString();
        if (origText.length() > 0) {
            textViewWritingProblemUserAnswer.setText(origText.substring(0, origText.length() - 1));
            findViewById(R.id.buttonDeleteKanji).setEnabled(textViewWritingProblemUserAnswer.getText().toString().length() > 0);
        }
    }

    public void enterCharacter(android.view.View view) {
        findViewById(R.id.buttonEnterWritingProblemCharacter).setEnabled(false);
        KanjiDrawing kanjiCanvas = (KanjiDrawing)findViewById(R.id.kanjiDrawing);
        new MatchThread(this, kanjiCanvas.getStrokes(), R.string.label_finding_characters, true);
    }

    public void undoCanvas(android.view.View view) {
        KanjiDrawing kanjiCanvas = (KanjiDrawing)findViewById(R.id.kanjiDrawing);
        kanjiCanvas.undo();
    }

    public void clearCanvas(android.view.View view) {
        KanjiDrawing kanjiCanvas = (KanjiDrawing)findViewById(R.id.kanjiDrawing);
        kanjiCanvas.clear();

        findViewById(R.id.buttonUndoWritingProblemCanvas).setEnabled(false);
        findViewById(R.id.buttonClearWritingProblemCanvas).setEnabled(false);
        findViewById(R.id.buttonEnterWritingProblemCharacter).setEnabled(false);
        findViewById(R.id.buttonShowNextPage_a).setVisibility(GONE);

        layoutKanjiInputRight_a.setVisibility(VISIBLE);
        layoutKanjiInputRight_b.setVisibility(GONE);

        for (int i = 0; i < ALL_IDS_a.length; i++) {
            Button button = (Button)findViewById(ALL_IDS_a[i]);
            button.setText("");
            button.setEnabled(false);
        }

        kanjiPage = 0;
        kanjis = null;
    }

    public void showArticle(android.view.View view) {
        String articleUrl = appl.getQuiz().getCurrentProblem().getArticleUrl();
        if (articleUrl != null) {
            Intent httpIntent = new Intent(Intent.ACTION_VIEW);
            httpIntent.setData(Uri.parse(articleUrl));
            startActivity(httpIntent);
        }
    }
    
    public void validateAnswer(android.view.View view) {
        TextView textViewWritingProblemUserAnswer = (TextView)findViewById(R.id.textViewWritingProblemUserAnswer);
        String answer = textViewWritingProblemUserAnswer.getText().toString();

        if (answer.trim().equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(WritingProblemActivity.this);
            builder.setTitle(getResources().getString(R.string.error_empty_answer_title))
            .setMessage(getResources().getString(R.string.error_empty_answer_msg))
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
             })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setCancelable(true)
            .show();
            return;
        }

        appl.getQuiz().validateAnswer(answer);
        
        Intent problemEvaluationActivity = new Intent(WritingProblemActivity.this, ProblemEvaluationActivity.class);
        startActivity(problemEvaluationActivity);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writing_problem);
       
        // Make sure the list gets loaded
        new LoadThread();

        Problem currProb = appl.getQuiz().getCurrentProblem();

        TextView textViewProblemInfoLevel = (TextView)findViewById(R.id.textViewWritingProblemInfoLevel);
        String strLevel = String.format(getResources().getString(R.string.label_problem_info_level), currProb.getLevel());
        textViewProblemInfoLevel.setText(strLevel);

        TextView textViewProblemInfoTopic = (TextView)findViewById(R.id.textViewWritingProblemInfoTopic);
        List<String> strTopics = new ArrayList<String>();
        Set<Problem.Topic> topics = currProb.getTopics();
        for (Problem.Topic topic : topics) {
            String strResName = "label_topic_" + topic.getLabelId();
            int labelId = getResources().getIdentifier(strResName, "string", WritingProblemActivity.this.getPackageName());

            String strTopic = String.format(getResources().getString(R.string.label_problem_info_topic), getResources().getString(labelId));
            strTopics.add(strTopic);
        }
        Collections.sort(strTopics);
        textViewProblemInfoTopic.setText(TextUtils.join(",", strTopics));

        TextView textViewProblemInfoType = (TextView)findViewById(R.id.textViewWritingProblemInfoType);
        String strResName = "label_quiz_type_" + currProb.getType().getLabelId();
        int labelId = getResources().getIdentifier(strResName, "string", WritingProblemActivity.this.getPackageName());
        String strType = String.format(getResources().getString(R.string.label_problem_info_type), getResources().getString(labelId));
        textViewProblemInfoType.setText(strType);

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

        WebView webViewProblemStatement = (WebView)findViewById(R.id.webViewWritingProblemStatement);
        webViewProblemStatement.loadData(stmt.toString(), "text/html; charset=utf-8", "utf-8");

        Button buttonViewArticle = (Button)findViewById(R.id.buttonViewWritingProblemArticle);
        buttonViewArticle.setVisibility(currProb.isArticleLinkAlive() ? VISIBLE : GONE);

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
                }
            }
        );

        clearCanvas(null);
        findViewById(R.id.buttonDeleteKanji).setEnabled(false);
    }

    private void initializeKanjiButtons() {
        if (kanjiPage == 0) {
            layoutKanjiInputRight_a.setVisibility(VISIBLE);
            layoutKanjiInputRight_b.setVisibility(GONE);

            int k = 0;
            while (k < ALL_IDS_a.length && k < kanjis.length) {
                final Button button = (Button)findViewById(ALL_IDS_a[k]);
                button.setText(kanjis[k]);
                button.setEnabled(true);
                button.setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            TextView textViewWritingProblemUserAnswer = (TextView)findViewById(R.id.textViewWritingProblemUserAnswer);
                            textViewWritingProblemUserAnswer.setText(textViewWritingProblemUserAnswer.getText().toString() + button.getText().toString());
                            findViewById(R.id.buttonDeleteKanji).setEnabled(true);
                            clearCanvas(v);
                        }
                    }
                );

                k++;
            }
            while (k < ALL_IDS_a.length) {
                Button button = (Button)findViewById(ALL_IDS_a[k]);
                button.setText(" ");
                button.setEnabled(false);
                k++;
            }

            Button buttonNextPage = (Button)findViewById(R.id.buttonShowNextPage_a);
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
        else {
            layoutKanjiInputRight_a.setVisibility(GONE);
            layoutKanjiInputRight_b.setVisibility(VISIBLE);

            int k = 12 * (kanjiPage - 1) + 7; 
            int b = 0;
            while (b < ALL_IDS_b.length && k < kanjis.length) {
                final Button button = (Button)findViewById(ALL_IDS_b[b]);
                button.setText(kanjis[k]);
                button.setEnabled(true);
                button.setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            TextView textViewWritingProblemUserAnswer = (TextView)findViewById(R.id.textViewWritingProblemUserAnswer);
                            textViewWritingProblemUserAnswer.setText(textViewWritingProblemUserAnswer.getText().toString() + button.getText().toString());
                            findViewById(R.id.buttonDeleteKanji).setEnabled(true);
                            clearCanvas(v);
                        }
                    }
                );

                b++;
                k++;
            }
            while (b < ALL_IDS_b.length) {
                Button button = (Button)findViewById(ALL_IDS_b[b]);
                button.setText(" ");
                button.setEnabled(false);
                b++;
            }
            
            Button buttonPrevPage = (Button)findViewById(R.id.buttonShowPrevPage_b);
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
            Button buttonNextPage = (Button)findViewById(R.id.buttonShowNextPage_b);
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
                final KanjiMatch[] exactMatches = list.getTopMatches(info, KanjiInfo.MatchAlgorithm.STRICT, null);
                final KanjiMatch[] fuzzyMatches = list.getTopMatches(info, KanjiInfo.MatchAlgorithm.FUZZY_1OUT, null);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();

                        List<String> exactChars = new ArrayList<String>();
                        List<Float> exactScores = new ArrayList<Float>();
                        List<String> fuzzyChars = new ArrayList<String>();
                        List<Float> fuzzyScores = new ArrayList<Float>();

                        for (int i = 0; i < exactMatches.length; i++) {
                            String kanji = exactMatches[i].getKanji().getKanji();
                            float score = exactMatches[i].getScore();
                            exactChars.add(kanji);
                            exactScores.add(new Float(score));
                        }

                        for (int i = 0; i < fuzzyMatches.length; i++) {
                            String kanji = fuzzyMatches[i].getKanji().getKanji();
                            float score = fuzzyMatches[i].getScore();
                            fuzzyChars.add(kanji);
                            fuzzyScores.add(score);
                        }

                        // Trace 1.
                        int c = 0;
                        while (c < exactChars.size() && c < fuzzyChars.size()) {
                            System.out.println("c="+c+" EXACT="+exactChars.get(c)+" ("+exactScores.get(c)+")   FUZZY="+fuzzyChars.get(c)+" ("+fuzzyScores.get(c)+")");
                            c++;
                        }
                        while (c < exactChars.size()) {
                            System.out.println("c="+c+" EXACT="+exactChars.get(c)+" ("+exactScores.get(c));
                            c++;
                        }
                        while (c < fuzzyChars.size()) {
                            System.out.println("c="+c+"                  FUZZY="+fuzzyChars.get(c)+" ("+fuzzyScores.get(c)+")");
                            c++;
                        }

                        List<String> mixedChars = new ArrayList<String>();
                        List<Float> mixedScores = new ArrayList<Float>();

                        int i = 0;
                        while (i < exactChars.size() && i < fuzzyChars.size()) {
                            String exactKanji = exactChars.get(i);
                            if (!mixedChars.contains(exactKanji))
                                mixedChars.add(exactKanji);
                            
                            String fuzzyKanji = fuzzyChars.get(i);
                            if (!mixedChars.contains(fuzzyKanji))
                                mixedChars.add(fuzzyKanji);

                            i++;
                        }
                        if (i >= exactChars.size()) {
                            while (i < fuzzyChars.size()) {
                                String fuzzyKanji = fuzzyChars.get(i);
                                if (!mixedChars.contains(fuzzyKanji))
                                    mixedChars.add(fuzzyKanji);
                                i++;
                            }
                        }
                        else {
                            while (i < exactChars.size()) {
                                String exactKanji = exactChars.get(i);
                                if (!mixedChars.contains(exactKanji))
                                    mixedChars.add(exactKanji);
                                i++;
                            }
                        }
               
                        kanjis = mixedChars.toArray(new String[mixedChars.size()]);

                        // Trace 2.
                        for (int m = 0; m < kanjis.length; m++)
                            System.out.println("m="+m+" MIXED="+kanjis[m]);

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

    private KankenApplication appl = KankenApplication.getInstance();

    private LinearLayout layoutKanjiInputRight_a; 
    private LinearLayout layoutKanjiInputRight_b; 

    private int kanjiPage = 0;
    private String[] kanjis;

    private static KanjiList list;
    private static boolean listLoading;
    private static LinkedList<WritingProblemActivity> waitingActivities = new LinkedList<WritingProblemActivity>();
    private static Object listSynch = new Object();

}
