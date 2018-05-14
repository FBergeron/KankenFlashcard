package jp.kyoto.nlp.kanken;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ProblemEvaluationActivity extends AppCompatActivity {

    public void goNextPage() {
        Problem nextProblem = appl.getQuiz().nextProblem();
        if (nextProblem == null) {
            Intent quizSummaryActivity = new Intent(ProblemEvaluationActivity.this, QuizSummaryActivity.class);
            startActivity(quizSummaryActivity);
        }
        else {
            Intent problemActivity = (Problem.Type.READING.equals(nextProblem.getType()) ?
                    new Intent(ProblemEvaluationActivity.this, ReadingProblemActivity.class) :
                    new Intent(ProblemEvaluationActivity.this, WritingProblemActivity.class));
            startActivity(problemActivity);
        }
    }

    public void setProblemFamiliarity(int familiarity) {
        System.out.println("familiarity="+familiarity);

        goNextPage();
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
    
    public void setProblemFamiliarity5(android.view.View view) {
        setProblemFamiliarity(5); 
    }
   
    public void reportProblemAsErroneous(android.view.View view) {
        System.out.println( "This problem is incorrect." );        

        goNextPage();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_problem_evaluation);

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

    }

    KankenApplication appl = KankenApplication.getInstance();

}
