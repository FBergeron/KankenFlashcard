package jp.kyoto.nlp.kanken;

public class SummaryItem {
    private String answer;
    private String rightAnswer;
    private boolean isRight;
    private String familiarity;
    private String link;
    private String topic;
    private String level;
    private String number;
    private String statement;

    public SummaryItem(String answer, String rightAnswer, boolean isRight, String familiarity, String link, String topic, String level, String number, String statement) {
        this.answer = answer;
        this.rightAnswer = rightAnswer;
        this.isRight = isRight;
        this.familiarity = familiarity;
        this.link = link;
        this.topic = topic;
        this.level = level;
        this.number = number;
        this.statement = statement;
    }

    public String getAnswer() {
        return answer;
    }

    public String getRightAnswer() {
        return rightAnswer;
    }

    public boolean isRight() {
        return isRight;
    }

    public String getFamiliarity() {
        return familiarity;
    }

    public String getLink() {
        return link;
    }

    public String getTopic() {
        return topic;
    }

    public String getLevel() {
        return level;
    }

    public String getNumber() {
        return number;
    }

    public String getStatement() {
        return statement;
    }

    @Override
    public String toString() {
        return "SummaryItem{" +
                "answer='" + answer + '\'' +
                ", rightAnswer='" + rightAnswer + '\'' +
                ", isRight=" + isRight +
                ", familiarity='" + familiarity + '\'' +
                ", link='" + link + '\'' +
                ", topic='" + topic + '\'' +
                ", level=" + level +
                ", number=" + number +
                ", statement='" + statement + '\'' +
                '}';
    }
}
