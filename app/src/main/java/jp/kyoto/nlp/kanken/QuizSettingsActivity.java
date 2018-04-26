package jp.kyoto.nlp.kanken;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.HashSet;

public class QuizSettingsActivity extends AppCompatActivity {

    public static final String termsOfUsageLink = "http://www.bbc.co.uk";
    public static final String directionsLink = "http://www.radio-canada.ca";

    public void invokeTopicChooser(android.view.View view) {
        for (int i = 0; i < checkedTopics.length; i++)
            checkedTopics[i] = selectedTopics.contains(i);

        AlertDialog.Builder builderTopicChooser = new AlertDialog.Builder(QuizSettingsActivity.this);
        builderTopicChooser.setTitle(getResources().getString(R.string.label_topic_chooser_title));
        builderTopicChooser.setMultiChoiceItems(labelTopics, checkedTopics, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int topicIndex, boolean isChecked) {
            }
        });
        builderTopicChooser.setCancelable(false);
        builderTopicChooser.setPositiveButton(getResources().getString(R.string.button_select), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < checkedTopics.length; i++) {
                    if (checkedTopics[i])
                        selectedTopics.add(i);
                    else
                        selectedTopics.remove(i);
                }
                showSelectedTopics();
            }
        });
        builderTopicChooser.setNegativeButton(getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showSelectedTopics();
            }
        });
        AlertDialog dialogTopicChooser = builderTopicChooser.create();
        dialogTopicChooser.show();
    }

    public void showTermsOfUsage(android.view.View view) {
        Intent httpIntent = new Intent(Intent.ACTION_VIEW);
        httpIntent.setData(Uri.parse(termsOfUsageLink));
        startActivity(httpIntent);
    }

    public void showDirections(android.view.View view) {
        Intent httpIntent = new Intent(Intent.ACTION_VIEW);
        httpIntent.setData(Uri.parse(directionsLink));
        startActivity(httpIntent);
    }

    public void startQuiz(android.view.View view) {
        HashSet<Problem.Topic> quizTopics = new HashSet<Problem.Topic>();
        for (Integer selectedTopic : selectedTopics)
            quizTopics.add(Problem.Topic.values()[selectedTopic.intValue()]);
        
        if (quizTopics.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(QuizSettingsActivity.this);
            builder.setTitle(getResources().getString(R.string.error_no_topics_selected_title))
            .setMessage(getResources().getString(R.string.error_no_topics_selected_msg))
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) { 
                }
             })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setCancelable(true)
            .show();
            return;    
        }

        SeekBar seekbarQuizLevel = (SeekBar) findViewById(R.id.seekBarQuizLevel);
        int level = seekbarQuizLevel.getProgress();

        RadioGroup radioGroupQuizLevel = (RadioGroup) findViewById(R.id.radioGroupQuizType);
        int selectedRadioButtonId = radioGroupQuizLevel.getCheckedRadioButtonId();
        Problem.Type type = (selectedRadioButtonId == R.id.radioButtonQuizTypeReading ? 
            Problem.Type.READING : Problem.Type.WRITING);

        KankenApplication appl = KankenApplication.getInstance();
        appl.startQuiz(level, quizTopics, type);
        Problem currProb = appl.getQuiz().getCurrentProblem();

        Intent problemActivity = (Problem.Type.READING.equals(currProb.getType()) ?
                new Intent(QuizSettingsActivity.this, ReadingProblemActivity.class) :
                new Intent(QuizSettingsActivity.this, WritingProblemActivity.class));
        startActivity(problemActivity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_settings);

        int topicCount = Problem.Topic.values().length;
        labelTopics = new String[topicCount];
        for (int i = 0; i < topicCount; i++) {
            String strResName = "label_topic_" + Problem.Topic.values()[i].getLabelId();
            int labelId = getResources().getIdentifier(strResName, "string", QuizSettingsActivity.this.getPackageName());
            labelTopics[i] = getResources().getString(labelId);
        }
        checkedTopics = new boolean[labelTopics.length];
    }

    private void showSelectedTopics() {
        StringBuilder str = new StringBuilder();
        String delimiter = "";
        for (Integer selectedTopic : selectedTopics) {
            str.append(delimiter);
            str.append(labelTopics[selectedTopic.intValue()]);
            delimiter = ", ";
        }
        TextView textViewSelectedTopics = (TextView)findViewById(R.id.textViewSelectedTopics);
        textViewSelectedTopics.setText(str.toString());
    }

    private String[] labelTopics;
    private boolean[] checkedTopics;
    private HashSet<Integer> selectedTopics = new HashSet<Integer>();

    private String sharedPrefFile = "jp.kyoto.nlp.kanken.KankenApplPrefs";

}
