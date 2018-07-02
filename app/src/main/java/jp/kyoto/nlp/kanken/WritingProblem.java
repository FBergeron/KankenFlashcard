package jp.kyoto.nlp.kanken;

class WritingProblem extends Problem {

    public WritingProblem(String id, int level, Problem.Topic topic, String statement, String jumanInfo, String rightAnswer, String articleUrl, boolean isArticleLinkAlive) {
        this.id = id;
        this.level = level;
        this.topic = topic;
        this.statement = statement;
        this.jumanInfo = jumanInfo;
        this.rightAnswer = rightAnswer;
        this.articleUrl = articleUrl;
        this.isArticleLinkAlive = isArticleLinkAlive;
    }

    public Problem.Type getType() {
        return Problem.Type.WRITING;
    }

    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if (!(obj instanceof WritingProblem))
            return false;
        if (obj == this)
            return true;
        return this.getId() == ((WritingProblem)obj).getId();
    }

}


