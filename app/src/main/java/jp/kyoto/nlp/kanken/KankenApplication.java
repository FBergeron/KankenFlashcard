package jp.kyoto.nlp.kanken;

import java.util.HashSet;

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

    public void startQuiz() {
        quiz = new Quiz();
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

    private static KankenApplication instance;

    private Quiz quiz;

    private String userName;
    private String userEmail;

}
