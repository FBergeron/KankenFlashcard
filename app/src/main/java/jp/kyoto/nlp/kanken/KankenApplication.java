package jp.kyoto.nlp.kanken;

import android.app.Activity;
import android.app.Application;
import android.net.Uri;
import android.util.Log;

import com.leafdigital.kanji.android.MultiAssetInputStream;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class KankenApplication extends Application {

    public static final String getNextProblemsReqPath = "/cgi-bin/get_next_problems.cgi";
    public static final String getErrorHistoryReqPath = "/cgi-bin/get_errors_history.cgi";
    public static final String getResultHistoryReqPath = "/cgi-bin/get_results_history.cgi";
    public static final String signOutReqPath = "/cgi-bin/sign_out.cgi";
    public static final String signInReqPath = "/cgi-bin/sign_in.cgi";
    public static final String storeResultReqPath = "/cgi-bin/store_result.cgi";

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

    public void playAgain() {
        if (quiz != null)
            quiz.clear();
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

    public Uri getUserPictureUrl() {
        return userPictureUrl;
    }

    public void setUserPictureUrl(Uri pictureUri) {
        this.userPictureUrl = pictureUri;
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

    public void setFirstActivity(Activity activity) {
        if (firstActivity == null)
            firstActivity = activity;
    }

    public void quit() {
        if (firstActivity != null) {
            firstActivity.finishAndRemoveTask();
            firstActivity = null;
        }
    }

    private static KankenApplication instance;

    private Quiz    quiz;

    private String  userName;
    private String  userEmail;
    private Uri     userPictureUrl;
    private String  userIdToken;
    private String  sessionCookie;

    private String  serverBaseUrl;

    private final static String tag = "KankenApplication";

    private Activity firstActivity = null;

}
