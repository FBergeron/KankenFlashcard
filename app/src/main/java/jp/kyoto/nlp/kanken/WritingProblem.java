public class WritingProblem extends Problem {

    public WritingProblem(int level, Problem.Topic topic, String statement, String rightAnswer) {
        this.level = level;
        this.topic = topic;
        this.statement = statement;
        this.rightAnswer = rightAnswer;
    }

    public Problem.Type getType() {
        return Problem.Type.WRITING;
    }

}


