package jp.kyoto.nlp.kanken;

import java.util.HashSet;

class Quiz {

    public Quiz(int level, HashSet<Problem.Topic> topics, Problem.Type type) {
        this.level = level;
        this.topics = topics;
        this.type = type;
    }

    public Problem getCurrentProblem() {
        if (currentProblem == null)
            currentProblem = ProblemStore.getInstance().getNextProblem(-1 /* level */, null /* topics */, null /* type */);
        return currentProblem;
    }
    
    public Problem nextProblem() {
        currentProblem = ProblemStore.getInstance().getNextProblem(-1 /* level */, null /* topics */, null /* type */);
        return currentProblem;
    }

    private int level;
    private HashSet<Problem.Topic> topics;
    private Problem.Type type;
    
    private Problem currentProblem;

}
