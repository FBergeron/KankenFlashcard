package jp.kyoto.nlp.kanken;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.leafdigital.kanji.android.KanjiDrawing;
import com.leafdigital.kanji.android.KanjiDrawing.DrawnStroke;

public class WritingProblemActivity extends AppCompatActivity {

    public void clearAnswer(android.view.View view) {
        System.out.println("clear answer");
    }

    public void enterCharacter(android.view.View view) {
        System.out.println("enter");
    }

    public void clearCanvas(android.view.View view) {
        // CanvasView canvas1 = (CanvasView)findViewById(R.id.canvasViewWritingProblemCanvas1);
        // canvas1.clearCanvas();

        // CanvasView canvas2 = (CanvasView)findViewById(R.id.canvasViewWritingProblemCanvas2);
        // canvas2.clearCanvas();
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
    
    KankenApplication appl = KankenApplication.getInstance();

    private KanjiDrawing kanjiCanvas;

}
