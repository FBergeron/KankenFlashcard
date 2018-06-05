package jp.kyoto.nlp.kanken;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
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
import java.util.LinkedList;

public class WritingProblemActivity extends AppCompatActivity {

    public void clearAnswer(android.view.View view) {
        System.out.println("clear answer");
    }

    public void enterCharacter(android.view.View view) {
        new MatchThread(this, kanjiCanvas.getStrokes(), KanjiInfo.MatchAlgorithm.STRICT, R.string.label_finding_exact_matches, 
            kanjiCanvas.getStrokes().length == 1 ? R.string.label_exact_matches_1_stroke : R.string.label_exact_matches_n_strokes, 
                R.string.label_inexact_matches, STAGE_EXACT, false, new String[0]);
    }

    public void undoCanvas(android.view.View view) {
        kanjiCanvas.undo();
    }

    public void clearCanvas(android.view.View view) {
        kanjiCanvas.clear();
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
        // EditText editTextAnswer = (EditText)findViewById(R.id.editTextWritingProblemAnswer);
        // String answer = editTextAnswer.getText().toString();
        String answer = "";

        // if (answer.trim().equals("")) {
        //     AlertDialog.Builder builder = new AlertDialog.Builder(WritingProblemActivity.this);
        //     builder.setTitle(getResources().getString(R.string.error_empty_answer_title))
        //     .setMessage(getResources().getString(R.string.error_empty_answer_msg))
        //     .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
        //         public void onClick(DialogInterface dialog, int which) { 
        //         }
        //      })
        //     .setIcon(android.R.drawable.ic_dialog_alert)
        //     .setCancelable(true)
        //     .show();
        //     return;
        // }

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
        String strResName = "label_topic_" + currProb.getTopic().getLabelId();
        int labelId = getResources().getIdentifier(strResName, "string", WritingProblemActivity.this.getPackageName());
        String strTopic = String.format(getResources().getString(R.string.label_problem_info_topic), getResources().getString(labelId));
        textViewProblemInfoTopic.setText(strTopic);

        TextView textViewProblemInfoType = (TextView)findViewById(R.id.textViewWritingProblemInfoType);
        strResName = "label_quiz_type_" + currProb.getType().getLabelId();
        labelId = getResources().getIdentifier(strResName, "string", WritingProblemActivity.this.getPackageName());
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

        kanjiCanvas = new KanjiDrawing(this);
        LinearLayout layoutKanjiInput = (LinearLayout)findViewById(R.id.layoutKanjiInput);
        layoutKanjiInput.addView(kanjiCanvas);
        
        kanjiCanvas.setListener(
            new KanjiDrawing.Listener() {
                @Override
                public void strokes(DrawnStroke[] strokes) {
                    System.out.println("strokes="+strokes);
                    //findViewById(R.id.undo).setEnabled(strokes.length > 0);
                    //findViewById(R.id.clear).setEnabled(strokes.length > 0);

                    //boolean gotList;
                    //synchronized(listSynch) {
                    //    gotList = list != null;
                    //}
                    //findViewById(R.id.done).setEnabled(strokes.length > 0 && gotList);

                    //TextView strokesText = (TextView)findViewById(R.id.strokes);
                    //strokesText.setText(strokes.length + "");
                    //if(strokes.length == KanjiDrawing.MAX_STROKES)
                    //    strokesText.setTextColor(Color.RED);
                    //else
                    //    strokesText.setTextColor(normalRgb);
                }
            }
        );
    }

    /**
     * Called once the kanji list has been loaded so that it enables the button
     * if needed.
     */
    private void loaded() {
        DrawnStroke[] strokes = kanjiCanvas.getStrokes();
        //findViewById(R.id.done).setEnabled(strokes.length > 0);
    }

    /**
     * Converts from drawn strokes to the KanjiInfo object that
     * com.leafdigital.kanji classes expect.
     * @param strokes Strokes
     * @return Equivalent KanjiInfo object
     */
    static KanjiInfo getKanjiInfo(DrawnStroke[] strokes) {
        KanjiInfo info = new KanjiInfo("?");
        for(DrawnStroke stroke : strokes)
        {
            InputStroke inputStroke = new InputStroke(
                stroke.getStartX(), stroke.getStartY(),
                stroke.getEndX(), stroke.getEndY());
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
    static class MatchThread extends Thread {

        /**
         * @param owner Owning activity
         * @param algo Algorithm to use to do match
         * @param waitString String (R.string) to display in wait dialog
         * @param labelString String to use for activity label
         * @param otherString String to use for 'nope not that' button
         * @param stageCode Code to use for activity result
         * @param showMore Show more kanji (smaller grid)
         * @param alreadyShown Array of kanji that were already shown so don't
         *   show them again
         */
        MatchThread(Activity owner, DrawnStroke[] strokes, KanjiInfo.MatchAlgorithm algo,
                    int waitString, int labelString, int otherString, int stageCode,
                    boolean showMore, String[] alreadyShown) {
            this.activity = owner;
            //dialog = new ProgressDialog(activity);
            //dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            //dialog.setMessage(activity.getString(waitString));
            //dialog.setCancelable(false);
            //dialog.show();
            //progress = new KanjiList.Progress() {
            //    @Override
            //    public void progress(final int done, final int max) {
            //        activity.runOnUiThread(new Runnable() {
            //            @Override
            //            public void run() {
            //                if(done == 0)
            //                    dialog.setMax(max);
            //                dialog.setProgress(done);
            //            }
            //        });
            //    }
            //};
            this.algo = algo;

            // Build info
            info = getKanjiInfo(strokes);

            // Build intent
            // intent = new Intent(activity, TopResultsActivity.class);
            // intent.putExtra(EXTRA_LABEL, labelString);
            // intent.putExtra(EXTRA_OTHERLABEL, otherString);
            // intent.putExtra(EXTRA_SHOWMORE, showMore);
            // intent.putExtra(EXTRA_ALREADYSHOWN, alreadyShown);
            // intent.putExtra(EXTRA_STAGE, stageCode);
            // intent.putExtra(EXTRA_ALGO, algo.toString());
            // DrawnStroke.saveToIntent(intent, strokes);

            start();
        }

        public void run() {
            boolean closedDialog = false;
            try {
                final KanjiMatch[] matches = list.getTopMatches(info, algo, progress);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //dialog.dismiss();
                        String[] chars = new String[matches.length];
                        for(int i=0; i<matches.length; i++) {
                            chars[i] = matches[i].getKanji().getKanji();
                            System.out.println("chars["+i+"]="+chars[i]);
                        }
                        //intent.putExtra(EXTRA_MATCHES, chars);
                        //activity.startActivityForResult(intent, 0);
                    }
                });
            }
            finally {
                if(!closedDialog) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // dialog.dismiss();
                        }
                    });
                }
            }
        }


        private KanjiInfo info;
        //private ProgressDialog dialog;
        private KanjiInfo.MatchAlgorithm algo;
        private Intent intent;
        private KanjiList.Progress progress;
        private Activity activity;

    }

    KankenApplication appl = KankenApplication.getInstance();

    private static KanjiList list;
    private static boolean listLoading;
    private static LinkedList<WritingProblemActivity> waitingActivities = new LinkedList<WritingProblemActivity>();
    private static Object listSynch = new Object();

    private KanjiDrawing kanjiCanvas;

    private final static int STAGE_EXACT = 1, STAGE_FUZZY = 2,
        STAGE_MOREFUZZY = 3, STAGE_PLUSMINUS1 = 4,
        STAGE_MOREPLUSMINUS1 = 5, STAGE_EVENMOREPLUSMINUS1 = 6;

}
