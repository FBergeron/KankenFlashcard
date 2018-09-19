package jp.kyoto.nlp.kanken;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class QuizSettingsActivityTest {

    @Test
    public void checkIfHelpWorks() {
        Activity activity = Robolectric.setupActivity(QuizSettingsActivity.class);
        Button buttonShowDirections = (Button)activity.findViewById(R.id.buttonShowDirections);
        assertThat("Help", equalTo(buttonShowDirections.getText().toString()));
    }

}
