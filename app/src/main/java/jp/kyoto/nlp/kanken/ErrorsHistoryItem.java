package jp.kyoto.nlp.kanken;

import java.util.Date;

public class ErrorsHistoryItem {

    private Date date;
    private String problem;
    private String userAnswer;
    private String rightAnswer;
    private boolean showDate;

    public ErrorsHistoryItem(Date date, String problem, String userAnswer, String rightAnswer, boolean showDate) {
        this.date = date;
        this.problem = problem;
        this.userAnswer = userAnswer;
        this.rightAnswer = rightAnswer;
        this.showDate = showDate;
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

    public boolean isShowDate() {
        return showDate;
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

