package jp.kyoto.nlp.kanken;

import java.util.Date;

public class ErrorsHistoryItem {

    private Date date;
    private String problem;
    private String userAnswer;
    private String rightAnswer;

    public ErrorsHistoryItem(Date date) {
        this.date = date;
    }

    public ErrorsHistoryItem(Date date, String problem, String userAnswer, String rightAnswer) {
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
        return "ErrorsHistoryItem{" +
                "date='" + date + '\'' +
                ", problem='" + problem + '\'' +
                ", userAnswer=" + userAnswer +
                ", rightAnswer='" + rightAnswer + "'}";
    }
}

