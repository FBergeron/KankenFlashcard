package jp.kyoto.nlp.kanken;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

public class ReadingProblemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_problem);

        KankenApplication appl = KankenApplication.getInstance();
        Problem currProb = appl.getQuiz().getCurrentProblem();
System.out.println( "currProb2="+currProb );

        TextView textViewProblemInfoLevel = (TextView)findViewById(R.id.textViewProblemInfoLevel);
        String strLevel = String.format(getResources().getString(R.string.label_problem_info_level), currProb.getLevel());
        textViewProblemInfoLevel.setText(strLevel);

        TextView textViewProblemInfoTopic = (TextView)findViewById(R.id.textViewProblemInfoTopic);
        String strResName = "label_topic_" + currProb.getTopic().getLabelId();
        int labelId = getResources().getIdentifier(strResName, "string", ReadingProblemActivity.this.getPackageName());
        String strTopic = String.format(getResources().getString(R.string.label_problem_info_topic), getResources().getString(labelId));
        textViewProblemInfoTopic.setText(strTopic);

        TextView textViewProblemInfoType = (TextView)findViewById(R.id.textViewProblemInfoType);
        strResName = "label_quiz_type_" + currProb.getType().getLabelId();
        labelId = getResources().getIdentifier(strResName, "string", ReadingProblemActivity.this.getPackageName());
        String strType = String.format(getResources().getString(R.string.label_problem_info_type), getResources().getString(labelId));
        textViewProblemInfoType.setText(strType);

        StringBuffer stmt = new StringBuffer();
        stmt.append("<html>");
        stmt.append("<head>");
        stmt.append("<style type\"text/css\">");
        stmt.append("em { color: red; font-weight: bold;}");
        stmt.append("</style>");
        stmt.append("</head>");
        stmt.append("<body>" + currProb.getStatement() + "</body>");
        stmt.append("</html>");

        WebView webViewProblemStatement = (WebView)findViewById(R.id.webViewProblemStatement);
        webViewProblemStatement.loadData(stmt.toString(), "text/html; charset=utf-8", "utf-8");
    }
}
