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
        ProblemStore.getInstance(); // Just to force database initialization.  Remove this later.
    }

    public void startQuiz(int level, HashSet<Problem.Topic> topics, Problem.Type type) {
        quiz = new Quiz(level, topics, type);
    }

    public Quiz getQuiz() {
        return quiz;
    }

    private static KankenApplication instance;

    private Quiz quiz;

}
