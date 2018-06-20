package jp.kyoto.nlp.kanken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

class Util {

    public static ArrayList<String> kanas = new ArrayList<String>();

    static {
        kanas.add("\u3042"); // あ
        kanas.add("\u3044"); // い
        kanas.add("\u3046"); // う
        kanas.add("\u3048"); // え
        kanas.add("\u304a"); // お

        kanas.add("\u304b"); // か
        kanas.add("\u304d"); // き
        kanas.add("\u304f"); // く
        kanas.add("\u3051"); // け
        kanas.add("\u3053"); // こ

        kanas.add("\u3055"); // さ
        kanas.add("\u3057"); // し
        kanas.add("\u3059"); // す
        kanas.add("\u305b"); // せ
        kanas.add("\u305d"); // そ

        kanas.add("\u305f"); // た
        kanas.add("\u3062"); // ち
        kanas.add("\u3064"); // つ
        kanas.add("\u3066"); // て
        kanas.add("\u3068"); // と

        kanas.add("\u306a"); // な
        kanas.add("\u306b"); // に
        kanas.add("\u306c"); // ぬ
        kanas.add("\u306d"); // ね
        kanas.add("\u306e"); // の

        kanas.add("\u306f"); // は
        kanas.add("\u3072"); // ひ
        kanas.add("\u3075"); // ふ
        kanas.add("\u3078"); // へ
        kanas.add("\u307b"); // ほ

        kanas.add("\u307e"); // ま
        kanas.add("\u307f"); // み
        kanas.add("\u3080"); // む
        kanas.add("\u3081"); // め
        kanas.add("\u3082"); // も

        kanas.add("\u3084"); // や
        kanas.add("\u3086"); // ゆ
        kanas.add("\u3088"); // よ

        kanas.add("\u3089"); // ら
        kanas.add("\u308a"); // り
        kanas.add("\u308b"); // る
        kanas.add("\u308c"); // れ
        kanas.add("\u308d"); // ろ

        kanas.add("\u308f"); // わ
        kanas.add("\u3092"); // を

        kanas.add("\u3093"); // ん

        kanas.add("\u304c"); // が
        kanas.add("\u304e"); // ぎ
        kanas.add("\u3050"); // ぐ
        kanas.add("\u3052"); // げ
        kanas.add("\u3054"); // ご

        kanas.add("\u3056"); // ざ
        kanas.add("\u3058"); // じ
        kanas.add("\u305a"); // ず
        kanas.add("\u305c"); // ぜ
        kanas.add("\u305e"); // ぞ

        kanas.add("\u3060"); // だ
        kanas.add("\u3062"); // ぢ
        kanas.add("\u3065"); // づ
        kanas.add("\u3067"); // で
        kanas.add("\u3069"); // ど

        kanas.add("\u3070"); // ば
        kanas.add("\u3073"); // び
        kanas.add("\u3076"); // ぶ
        kanas.add("\u3079"); // べ
        kanas.add("\u307c"); // ぼ

        kanas.add("\u3071"); // ぱ
        kanas.add("\u3074"); // ぴ
        kanas.add("\u3077"); // ぷ
        kanas.add("\u307a"); // ぺ
        kanas.add("\u307d"); // ぽ

        kanas.add("\u3041"); // ぁ
        kanas.add("\u3043"); // ぃ
        kanas.add("\u3045"); // ぅ
        kanas.add("\u3047"); // ぇ
        kanas.add("\u3049"); // ぉ

        kanas.add("\u3063"); // っ
    }

    public static ArrayList<String> longKanas = new ArrayList<String>();

    static {
        longKanas.add("\u304d\u3083"); // きゃ 
        longKanas.add("\u304d\u3085"); // きゅ 
        longKanas.add("\u304d\u3087"); // きょ

        longKanas.add("\u3057\u3083"); // しゃ
        longKanas.add("\u3057\u3085"); // しゅ
        longKanas.add("\u3057\u3087"); // しょ

        longKanas.add("\u3061\u3083"); // ちゃ
        longKanas.add("\u3061\u3085"); // ちゅ
        longKanas.add("\u3061\u3087"); // ちょ

        longKanas.add("\u306b\u3083"); // にゃ
        longKanas.add("\u306b\u3085"); // にゅ
        longKanas.add("\u306b\u3087"); // にょ

        longKanas.add("\u3072\u3083"); // ひゃ
        longKanas.add("\u3072\u3085"); // ひゅ
        longKanas.add("\u3072\u3087"); // ひょ

        longKanas.add("\u307f\u3083"); // みゃ
        longKanas.add("\u307f\u3085"); // みゅ
        longKanas.add("\u307f\u3087"); // みょ

        longKanas.add("\u308a\u3083"); // りゃ
        longKanas.add("\u308a\u3085"); // りゅ
        longKanas.add("\u308a\u3087"); // りょ

        longKanas.add("\u304e\u3083"); // ぎゃ
        longKanas.add("\u304e\u3085"); // ぎゅ
        longKanas.add("\u304e\u3087"); // ぎょ

        longKanas.add("\u3058\u3083"); // じゃ
        longKanas.add("\u3058\u3085"); // じゅ
        longKanas.add("\u3058\u3087"); // じょ

        longKanas.add("\u3062\u3083"); // ぢゃ
        longKanas.add("\u3062\u3085"); // ぢゅ
        longKanas.add("\u3062\u3087"); // ぢょ

        longKanas.add("\u3073\u3083"); // びゃ
        longKanas.add("\u3073\u3085"); // びゅ
        longKanas.add("\u3073\u3087"); // びょ

        longKanas.add("\u3074\u3083"); // ぴゃ
        longKanas.add("\u3074\u3085"); // ぴゅ
        longKanas.add("\u3074\u3087"); // ぴょ
    }

    public static JSONObject readJson(URL url) throws IOException, JSONException {
        InputStream is = url.openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } 
        finally {
            is.close();
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int counter = 0;
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
            counter++;
            // This seems to help downloading the data file.
            // Maybe there is a buffering issue?
            if (counter % 80 == 0)
                System.out.print(".");

        }
        System.out.println("!");
        return sb.toString();
    }

    public static String findKana(String k) {
        ArrayList<String> kanasToSearch = (k.length() == 2 ? Util.longKanas : Util.kanas);

        for (Iterator it = kanasToSearch.iterator(); it.hasNext();) {
            String kana = (String)it.next();
            if (kana.equals(k))
                return(kana);
        }

        return null;
    }

    public static ArrayList<String> findKanasFrom(String word) {
        ArrayList<String> chars = new ArrayList<String>();
        int c = 0;
        while (c < word.length()) {
            String foundKana = null;
            String subword;
            if (c + 2 < word.length()) {
                subword = word.substring(c, c + 2);
                foundKana = Util.findKana(subword);
                if (foundKana != null)
                    c += 2;
            }
            if (foundKana == null) {
                subword = word.substring(c, c + 1);
                foundKana = Util.findKana(subword);
                if (foundKana != null)
                    c += 1;
            }
            if (foundKana != null && !chars.contains(foundKana))
                chars.add(foundKana);
        }
        return chars;
    }

    public static final String PREFS_GENERAL = "KankenAppPrefsGeneral";

}

