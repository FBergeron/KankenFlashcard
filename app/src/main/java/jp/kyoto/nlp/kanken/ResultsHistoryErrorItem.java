package jp.kyoto.nlp.kanken;

import java.util.Date;

public class ResultsHistoryErrorItem {

    private Date date;
    private String problem;
    private String userAnswer;
    private String rightAnswer;

    public ResultsHistoryErrorItem(Date date) {
        this.date = date;
    }

    public ResultsHistoryErrorItem(Date date, String problem, String userAnswer, String rightAnswer) {
        this.date = date;
        this.problem = problem;
        this.userAnswer = userAnswer;
        this.rightAnswer = rightAnswer;
    }

    public Date getDate() {
        return date;
    }

    public String getProblem() {
        return problem;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public String getRightAnswer() {
        return rightAnswer;
    }

    @Override
    public String toString() {
        return "ResultsHistoryErrorItem{" +
                "date='" + date + '\'' +
                ", problem='" + problem + '\'' +
                ", userAnswer=" + userAnswer +
                ", rightAnswer='" + rightAnswer + "'}";
    }
}

