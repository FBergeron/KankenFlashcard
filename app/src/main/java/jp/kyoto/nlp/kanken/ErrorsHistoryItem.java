package jp.kyoto.nlp.kanken;

import java.util.Date;

public class ErrorsHistoryItem {

    private Date date;
    private String statement;
    private String problem;
    private int level;
    private Problem.Topic[] topics;
    private String userAnswer;
    private String rightAnswer;
    private boolean showDate;

    public ErrorsHistoryItem(Date date, String statement, String problem, int level, Problem.Topic[] topics, String userAnswer, String rightAnswer, boolean showDate) {
        this.date = date;
        this.statement = statement;
        this.problem = problem;
        this.level = level;
        this.topics = topics;
        this.userAnswer = userAnswer;
        this.rightAnswer = rightAnswer;
        this.showDate = showDate;
    }

    public Date getDate() {
        return date;
    }

    public String getStatement() {
        return statement;
    }

    public String getProblem() {
        return problem;
    }

    public int getLevel() {
        return level;
    }

    public Problem.Topic[] getTopics() {
        return topics;
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
                ", statement='" + statement + '\'' +
                ", problem='" + problem + '\'' +
                ", level='" + level + '\'' +
                ", topics='" + topics + '\'' +
                ", userAnswer=" + userAnswer +
                ", rightAnswer='" + rightAnswer + "'}";
    }
}

