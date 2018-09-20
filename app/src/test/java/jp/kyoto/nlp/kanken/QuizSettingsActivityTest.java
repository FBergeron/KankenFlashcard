package jp.kyoto.nlp.kanken;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class QuizSettingsActivityTest {

    @Before
    public void setUp() throws Exception {
        quizSettingsActivity = Robolectric.setupActivity(QuizSettingsActivity.class); 
    }

    @Test
    public void checkЅhowDirections() {
        Button buttonShowDirections = (Button)quizSettingsActivity.findViewById(R.id.buttonShowDirections);
        buttonShowDirections.performClick();
        ShadowActivity shadowActivity = shadowOf(quizSettingsActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        String directionsLink = RuntimeEnvironment.application.getResources().getString(R.string.link_directions);
        assertEquals(Intent.ACTION_VIEW, startedIntent.getAction());
        assertEquals(directionsLink, startedIntent.getData().toString());
    }

    @Test
    public void checkShowTermsOfUsage() {
        Button buttonShowTermsOfUsage = (Button)quizSettingsActivity.findViewById(R.id.buttonShowTermsOfUsage);
        buttonShowTermsOfUsage.performClick();
        ShadowActivity shadowActivity = shadowOf(quizSettingsActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        String termsOfUsageLink = RuntimeEnvironment.application.getResources().getString(R.string.link_terms_of_usage);
        assertEquals(Intent.ACTION_VIEW, startedIntent.getAction());
        assertEquals(termsOfUsageLink, startedIntent.getData().toString());
    }

    @Test
    public void checkQuizDoNotStartIfNoTopicIsSelected() {
        Button buttonStartQuiz = (Button)quizSettingsActivity.findViewById(R.id.buttonStartQuiz);
        buttonStartQuiz.performClick();
        ShadowActivity shadowActivity = shadowOf(quizSettingsActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertEquals(null, startedIntent);
    }

    private QuizSettingsActivity quizSettingsActivity;

}
