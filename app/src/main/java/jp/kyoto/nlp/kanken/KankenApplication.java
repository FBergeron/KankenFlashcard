package jp.kyoto.nlp.kanken;

import android.app.Application;

import com.leafdigital.kanji.android.MultiAssetInputStream;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class KankenApplication extends Application {

    public static KankenApplication getInstance() {
        return KankenApplication.instance;
    }

    public void onCreate() {
        super.onCreate();
        KankenApplication.instance = this;
    }

    public void startQuiz(Problem.Type type, Set<Problem.Topic> topics, int level) {
        quiz = new Quiz(type, topics, level);
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserIdToken() {
        return userIdToken;
    }

    public void setUserIdToken(String userIdToken) {
        this.userIdToken = userIdToken;
    }

    public String getSessionCookie() {
        return sessionCookie;
    }

    public void setSessionCookie(String sessionCookie) {
        this.sessionCookie = sessionCookie;
    }

    public String getServerBaseUrl() throws IOException, JSONException {
        if (serverBaseUrl == null) {
            InputStream is = new MultiAssetInputStream(getAssets(), new String[] { "config.json" });
            JSONObject jsonConfig = Util.readJson(is);
            serverBaseUrl = jsonConfig.getString("server");
        }
        return serverBaseUrl;
    }

    public void initKanaFreq() throws IOException {
        InputStream is = new MultiAssetInputStream(getAssets(), new String[] { "kana-freq.dat" });
        Util.initKanaFreq(is);
    }

    private static KankenApplication instance;

    private Quiz quiz;

    private String userName;
    private String userEmail;
    private String userIdToken;
    private String sessionCookie;

    private String serverBaseUrl;

}
