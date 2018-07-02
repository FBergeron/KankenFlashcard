package jp.kyoto.nlp.kanken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

class Quiz {

    public static final int DEFAULT_LENGTH = 1;

    public Quiz(int length) {
        this.length = length;
    }

    public Quiz() {
        this(DEFAULT_LENGTH);
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

    public Iterator<Boolean> getRightAnswers() {
        return rightAnswers.iterator();
    }

    public Iterator<Integer> getFamiliarities() {
        return familiarities.iterator();
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

    public void addFamiliarity(int familiarity) {
        familiarities.add(new Integer(familiarity));
    }

    public Boolean isCurrentAnswerRight() {
        if (currentProblem == -1 || currentProblem >= rightAnswers.size())
            return null;
        return rightAnswers.get(currentProblem);
    }

    private int length;

    private int currentProblem = -1;

    private ArrayList<Problem> problems = new ArrayList<Problem>();
    private ArrayList<String> answers = new ArrayList<String>();
    private ArrayList<Boolean> rightAnswers = new ArrayList<Boolean>();
    private ArrayList<Integer> familiarities = new ArrayList<Integer>();

}
