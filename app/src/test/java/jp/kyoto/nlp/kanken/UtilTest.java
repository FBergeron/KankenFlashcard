package jp.kyoto.nlp.kanken;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

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

