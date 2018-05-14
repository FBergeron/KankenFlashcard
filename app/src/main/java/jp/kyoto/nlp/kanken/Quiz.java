package jp.kyoto.nlp.kanken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

class Quiz {

    public Quiz(int level, HashSet<Problem.Topic> topics, Problem.Type type, int length) {
        this.level = level;
        this.topics = topics;
        this.type = type;
        this.length = length;
    }

    public Quiz(int level, HashSet<Problem.Topic> topics, Problem.Type type) {
        this(level, topics, type, 3);
    }

    public int getLevel() {
        return level;
    }

    public HashSet<Problem.Topic> getTopics() {
        return topics;
    }

    public Problem.Type getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public void clear() {
        problems.clear();
        answers.clear();
        rightAnswers.clear();
        currentProblem = -1;
    }

    public int getCurrentProblemIndex() {
        return currentProblem;
    }

    public Problem getCurrentProblem() {
        if (currentProblem == -1) {
            Problem problem = ProblemStore.getInstance().getNextProblem(level, topics, type);
            problems.add(problem);
            currentProblem++;
        }
        return problems.get(currentProblem);
    }
    
    public Problem nextProblem() {
        if (currentProblem >= this.length - 1)
            return null;

        Problem problem = ProblemStore.getInstance().getNextProblem(level, topics, type);
        problems.add(problem);
        currentProblem++;
        return problem;
    }

    public Iterator<Problem> getProblems() {
        return problems.iterator();
    }

    public Iterator<String> getAnswers() {
        return answers.iterator();
    }

    public boolean validateAnswer(String answer) {
        answers.add(answer);
        if (answer != null && answer.equals(getCurrentProblem().getRightAnswer())) {
            rightAnswers.add(Boolean.TRUE);
            return true;
        }
        else {
            rightAnswers.add(Boolean.FALSE);
            return false;
        }
    }

    public Boolean isCurrentAnswerRight() {
        if (currentProblem == -1 || currentProblem >= rightAnswers.size())
            return null;
        return rightAnswers.get(currentProblem);
    }

    private int level;
    private HashSet<Problem.Topic> topics;
    private Problem.Type type;
    private int length;

    private int currentProblem = -1;

    private ArrayList<Problem> problems = new ArrayList<Problem>();
    private ArrayList<String> answers = new ArrayList<String>();
    private ArrayList<Boolean> rightAnswers = new ArrayList<Boolean>();

}
