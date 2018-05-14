package jp.kyoto.nlp.kanken;

class ReadingProblem extends Problem {

    public ReadingProblem(String id, int level, Problem.Topic topic, String statement, String rightAnswer) {
        this.id = id;
        this.level = level;
        this.topic = topic;
        this.statement = statement;
        this.rightAnswer = rightAnswer;
    }

    public Problem.Type getType() {
        return Problem.Type.READING;
    }

    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if (!(obj instanceof ReadingProblem))
            return false;
        if (obj == this)
            return true;
        return this.getId() == ((ReadingProblem)obj).getId();
    }

}

