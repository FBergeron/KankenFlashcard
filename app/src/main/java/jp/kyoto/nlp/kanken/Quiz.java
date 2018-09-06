package jp.kyoto.nlp.kanken;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import static jp.kyoto.nlp.kanken.Quiz.Mode.MODE_EVALUATION;

class Quiz {

    public static final int DEFAULT_LENGTH = 5;

    public enum Mode {
        MODE_ASK,
        MODE_EVALUATION
    }

    public Quiz(Problem.Type type, Set<Problem.Topic> topics, int level, int length) {
        this.type = type;
        this.topics = topics;
        this.level = level;
        this.length = length;
    }

    public Quiz(Problem.Type type, Set<Problem.Topic> topics, int level) {
        this(type, topics, level, DEFAULT_LENGTH);
    }

    public int getLength() {
        return length;
    }

    public void clear() {
        problems.clear();
        answers.clear();
        rightAnswers.clear();
        familiarities.clear();
        reportedAsIncorrects.clear();
        currentProblem = -1;
    }

    public Problem.Type getType() {
        return type;
    }

    public Set<Problem.Topic> getTopics() {
        return topics;
    }

    public int getLevel() {
        return level;
    }

    public int getCurrentProblemIndex() {
        return currentProblem;
    }

    public Problem getCurrentProblem() {
        if (currentProblem == -1)
            currentProblem++;
        return problems.get(currentProblem);
    }
    
    public Problem nextProblem() {
        if (currentProblem >= this.length - 1)
            return null;

        currentProblem++;
        return problems.get(currentProblem);
    }

    public Iterator<Problem> getProblems() {
        return problems.iterator();
    }

    public void setProblems(ArrayList<Problem> problems) {
        this.problems = problems;
    }

    public Iterator<String> getAnswers() {
        return answers.iterator();
    }

    public String getAnswer(int index) {
        return answers.get(index);
    }

    public Iterator<Boolean> getRightAnswers() {
        return rightAnswers.iterator();
    }

    public Iterator<Integer> getFamiliarities() {
        return familiarities.iterator();
    }

    public Iterator<Boolean> getReportedAsIncorrects() {
        return reportedAsIncorrects.iterator();
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

    public void reportAsIncorrect() {
        reportedAsIncorrects.add(Boolean.TRUE);
        familiarities.add(null);
    }

    public void addFamiliarity(int familiarity) {
        familiarities.add(new Integer(familiarity));
        reportedAsIncorrects.add(Boolean.FALSE);
    }

    public Boolean isCurrentAnswerRight() {
        if (currentProblem == -1 || currentProblem >= rightAnswers.size())
            return null;
        return rightAnswers.get(currentProblem);
    }

    public Boolean isCurrentProblemReportedAsIncorrect() {
        if (currentProblem == -1 || currentProblem >= reportedAsIncorrects.size())
            return null;
        return reportedAsIncorrects.get(currentProblem);
    }

    public String getCurrentAnswer() {
        return currentAnswer;
    }

    public void setCurrentAnswer(String currAns) {
        currentAnswer = currAns;
    }

    public int getCurrentResultStringNumber() {
        return currentResultStringNumber;
    }

    public Mode getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(Mode currMode) {
        currentMode = currMode;
        if (currentMode == MODE_EVALUATION) {
            Random r = new Random();
            currentResultStringNumber = r.nextInt(4);
        }
    }

    private int length;

    private int currentProblem = -1;
    private String currentAnswer;
    private int currentResultStringNumber;
    private Mode currentMode = Mode.MODE_ASK;

    private Problem.Type type;
    private Set<Problem.Topic> topics;
    private int level;

    private ArrayList<Problem> problems = new ArrayList<Problem>();
    private ArrayList<String> answers = new ArrayList<String>();
    private ArrayList<Boolean> rightAnswers = new ArrayList<Boolean>();
    private ArrayList<Integer> familiarities = new ArrayList<Integer>();
    private ArrayList<Boolean> reportedAsIncorrects = new ArrayList<Boolean>();

}
