package jp.kyoto.nlp.kanken;

import java.util.HashSet;

class KankenApplication {

    public static KankenApplication getInstance() {
        return SingletonHelper.instance;
    }

    public void startQuiz(int level, HashSet<Problem.Topic> topics, Problem.Type type) {
        quiz = new Quiz(level, topics, type);
    }

    public Quiz getQuiz() {
        return quiz;
    }

    private KankenApplication() {
    }

    private static class SingletonHelper {
        private static final KankenApplication instance = new KankenApplication();
    }

    private Quiz quiz;

}
