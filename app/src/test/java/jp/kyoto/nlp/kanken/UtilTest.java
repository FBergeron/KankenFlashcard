package jp.kyoto.nlp.kanken;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class UtilTest {

    @Test
    public void checkFindKanasFrom() {
        String[][] words = {
            {"か", "た", "か", "な" },
            {"じ", "て", "ん", "しゃ"},
            {"れ", "ん", "ぱ", "い"},
            {"せ", "つ", "じょ", "く"},
            {"か"},
            {"と", "う", "しょ"},
            {"お", "う", "じゃ"},
            {"ば", "っ", "そ", "く"},
            {"じゅ", "ん", "え", "ん"},
            {"と", "じょ", "う", "こ", "く"}
        };
        
        for (int w = 0; w < words.length; w++) {
            List<String> kanas = new ArrayList<String>();
            for (int k = 0; k < words[w].length; k++)
                kanas.add(words[w][k]);
            assertEquals(kanas, Util.findKanasFrom(String.join("", words[w]), false));
        }
    }

}

