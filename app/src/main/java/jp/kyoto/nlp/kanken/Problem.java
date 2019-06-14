package jp.kyoto.nlp.kanken;

import java.util.HashMap;
import java.util.Set;

abstract class Problem {

    public enum Topic {
        POLITICS("politics"),
        BUSINESS("business"),
        CULTURE_ARTS("culture_arts"),
        HEALTH_MEDICINE("health_medicine"),
        SCIENCE_EDUCATION("science_education"),
        HOME_LIVING("home_living"),
        SPORTS("sports"),
        OTHER("other"),
        ALL("all");

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
            topicsByJapStr.put("健康・医学", Problem.Topic.HEALTH_MEDICINE);
            topicsByJapStr.put("家庭・暮らし", Problem.Topic.HOME_LIVING);
            topicsByJapStr.put("政治", Problem.Topic.POLITICS);
            topicsByJapStr.put("科学・教育", Problem.Topic.SCIENCE_EDUCATION);
            topicsByJapStr.put("スポーツ", Problem.Topic.SPORTS);
            topicsByJapStr.put("その他", Problem.Topic.OTHER);
            topicsByJapStr.put("一括選択",Problem.Topic.ALL);
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

    public Set<Problem.Topic> getTopics() {
        return topics;
    }

    public abstract Problem.Type getType();

    public String getStatement() {
        return statement;
    }

    public String getJumanInfo() {
        return jumanInfo;
    }

    public String getRightAnswer() {
        return rightAnswer;
    }

    public String getArticleUrl() {
        return articleUrl;
    }

    public boolean isArticleLinkAlive() {
        return isArticleLinkAlive;
    }

    public String getAltArticleUrl() {
        return altArticleUrl;
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("id: ").append(id);
        str.append("; level: ").append(level);
        str.append("; topics: ").append(topics);
        str.append("; type: ").append(getType().getLabelId());
        str.append("; stmt: ").append(statement);
        str.append("; answer: ").append(rightAnswer);
        str.append("; articleUrl: ").append(articleUrl);
        str.append("; isArticleLinkAlive: ").append(isArticleLinkAlive);
        str.append("; altArticleUrl: ").append(altArticleUrl);
        return str.toString();
    }

    public int hashCode() {
        return id.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Problem))
            return false;
        return obj == this || this.getId() == ((Problem) obj).getId();
    }

    protected String id;
    protected int level;
    protected Set<Problem.Topic> topics;
    protected String statement;
    protected String jumanInfo;
    protected String rightAnswer;
    protected String articleUrl;
    protected boolean isArticleLinkAlive;
    protected String altArticleUrl;

}
