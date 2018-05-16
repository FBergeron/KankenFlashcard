package jp.kyoto.nlp.kanken;

import java.util.HashMap;

abstract class Problem {

    public enum Topic {
        BUSINESS("business"),
        CULTURE_ARTS("culture_arts"),
        EDUCATION_LEARNING("education_learning"),
        HEALTH_MEDICINE("health_medicine"),
        HOME_LIVING("home_living"),
        POLITICS("politics"),
        SCIENCE_TECHNOLOGY("science_technology"),
        SPORTS("sports"),
        TRANSPORTATION("transportation"),
        OTHER("other");

        Topic(String labelId) {
            this.labelId = labelId;
        }

        String getLabelId() {
            return labelId;
        }

        private String labelId;
    }

    public static Problem.Topic getTopicFromJapaneseString(String str) {
        if (topicsByJapStr == null) {
            topicsByJapStr = new HashMap<String, Problem.Topic>();
            topicsByJapStr.put("ビジネス", Problem.Topic.BUSINESS);
            topicsByJapStr.put("文化・芸術", Problem.Topic.CULTURE_ARTS);
            topicsByJapStr.put("教育・学習", Problem.Topic.EDUCATION_LEARNING);
            topicsByJapStr.put("健康・医学", Problem.Topic.HEALTH_MEDICINE);
            topicsByJapStr.put("家庭・暮らし", Problem.Topic.HOME_LIVING);
            topicsByJapStr.put("政治", Problem.Topic.POLITICS);
            topicsByJapStr.put("科学・技術", Problem.Topic.SCIENCE_TECHNOLOGY);
            topicsByJapStr.put("スポーツ", Problem.Topic.SPORTS);
            topicsByJapStr.put("交通", Problem.Topic.TRANSPORTATION);
            topicsByJapStr.put("null", Problem.Topic.OTHER);
        }
        return topicsByJapStr.get(str);
    }

    private static HashMap<String, Problem.Topic> topicsByJapStr;

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

    public static Problem.Type getTypeFromString(String str) {
        if (typesByStr == null) {
            typesByStr = new HashMap<String, Problem.Type>();
            typesByStr.put("yomi", Problem.Type.READING);
            typesByStr.put("kaki", Problem.Type.WRITING);
        }
        return typesByStr.get(str);
    }

    private static HashMap<String, Problem.Type> typesByStr;

    public String getId() {
        return id;
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

    public String getArticleUrl() {
        return articleUrl;
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("id: ").append(id);
        str.append("; level: ").append(level);
        str.append("; topic: ").append(topic.getLabelId());
        str.append("; type: ").append(getType().getLabelId());
        str.append("; stmt: ").append(statement);
        str.append("; answer: ").append(rightAnswer);
        str.append("; articleUrl: ").append(articleUrl);
        return str.toString();
    }

    public int hashCode() {
        return id.hashCode();
    }

    protected String id;
    protected int level;
    protected Problem.Topic topic;
    protected String statement;
    protected String rightAnswer;
    protected String articleUrl;

}
