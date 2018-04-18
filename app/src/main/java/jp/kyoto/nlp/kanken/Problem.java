public abstract class Problem {

    public enum Topic {
        BUSINESS,
        COOKING,
        CULTURE,
        HEALTH,
        MEDECINE,
        POLITICS,
        SPORTS,
        TRANSPORTATION
    }

    public enum Type {
        READING,
        WRITING
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

    protected int level;
    protected Problem.Topic topic;
    protected String statement;
    protected String rightAnswer;

}
