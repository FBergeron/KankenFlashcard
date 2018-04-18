package jp.kyoto.nlp.kanken;

abstract class Problem {

    public enum Topic {
        BUSINESS("business"),
        COOKING("cooking"),
        CULTURE("culture"),
        HEALTH("health"),
        MEDECINE("medecine"),
        POLITICS("politics"),
        SPORTS("sports"),
        TRANSPORTATION("transportation");

        Topic(String labelId) {
            this.labelId = labelId;
        }

        String getLabelId() {
            return labelId;
        }

        private String labelId;
    }

    public enum Type {
        READING("reading"),
        WRITING("writing");

        Type(String labelId) {
            this.labelId = labelId;
        }

        String getLabelId() {
            return labelId;
        }

        private String labelId;
    }

    public int getLevel() {
        return level;
    }

    public Problem.Topic getTopic() {
        return topic;
    }

    public abstract Problem.Type getType();

    public String getStatement() {
        return statement;
    }

    public String getRightAnswer() {
        return rightAnswer;
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("level: ").append(level);
        str.append("; topic: ").append(topic.getLabelId());
        str.append("; type: ").append(getType().getLabelId());
        str.append("; stmt: ").append(statement);
        str.append("; answer: ").append(rightAnswer);
        return str.toString();
    }

    protected int level;
    protected Problem.Topic topic;
    protected String statement;
    protected String rightAnswer;

}
