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

    String[] labelTopics;
    boolean[] checkedTopics;
    HashSet<Integer> selectedTopics = new HashSet<Integer>();

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
        System.out.println("BEFORE checkedTopicsByPosition="+selectedTopics);
        dialogTopicChooser.show();
        System.out.println("AFTER checkedTopicsByPosition="+selectedTopics);
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
        SeekBar seekbarQuizLevel = (SeekBar) findViewById(R.id.seekBarQuizLevel);
        int level = seekbarQuizLevel.getProgress();

        String topics = "???";

        RadioGroup radioGroupQuizLevel = (RadioGroup) findViewById(R.id.radioGroupQuizType);
        int selectedRadioButtonId = radioGroupQuizLevel.getCheckedRadioButtonId();
        String type = (selectedRadioButtonId == R.id.radioButtonQuizTypeReading ? "reading" : "writing");

        System.out.println("startQuiz level="+level+" topics="+topics+" type="+type);

        Intent problemActivity = ("reading".equals(type) ?
                new Intent(QuizSettingsActivity.this, ReadingProblemActivity.class) :
                new Intent(QuizSettingsActivity.this, WritingProblemActivity.class));
        startActivity(problemActivity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_settings);

        labelTopics = new String[]{
                getResources().getString(R.string.label_topic_business),
                getResources().getString(R.string.label_topic_cooking),
                getResources().getString(R.string.label_topic_culture),
                getResources().getString(R.string.label_topic_health),
                getResources().getString(R.string.label_topic_medecine),
                getResources().getString(R.string.label_topic_politics),
                getResources().getString(R.string.label_topic_sports),
                getResources().getString(R.string.label_topic_transportation)
        };
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
}
