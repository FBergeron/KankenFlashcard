package jp.kyoto.nlp.kanken;

import java.util.Set;

import android.app.Application;
import android.content.Context;

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

    private static KankenApplication instance;

    private Quiz quiz;

    private String userName;
    private String userEmail;
    private String userIdToken;
    private String sessionCookie;

}
