package jp.kyoto.nlp.kanken;

import java.util.Set;

class ReadingProblem extends Problem {

    public ReadingProblem(String id, int level, Set<Problem.Topic> topics, String statement, String jumanInfo, String rightAnswer, String articleUrl, boolean isArticleLinkAlive, String altArticleUrl) {
        this.id = id;
        this.level = level;
        this.topics = topics;
        this.statement = statement;
        this.jumanInfo = jumanInfo;
        this.rightAnswer = rightAnswer;
        this.articleUrl = articleUrl;
        this.isArticleLinkAlive = isArticleLinkAlive;
        this.altArticleUrl = altArticleUrl;
    }

    public Problem.Type getType() {
        return Problem.Type.READING;
    }

}

