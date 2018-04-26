package jp.kyoto.nlp.kanken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;

class Util {

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

}

