package jp.kyoto.nlp.kanken;

class ProblemStore {

    public static ProblemStore getInstance() {
        return SingletonHelper.instance;
    }

    public Problem getNextProblem(int level, Problem.Topic[] topics, Problem.Type type) {
        // For now, the input parameters are ignored.  Normally, the problem should be chosen in function of these parameters.
        Problem problem = problems[problemCount % 5];
        problemCount++;
        return problem;
    }

    private ProblemStore() {
        // Create 5 small problems as dummy data.
        problems = new Problem[5];
        problems[0] = new ReadingProblem(1, Problem.Topic.TRANSPORTATION, "京都<em>駅</em>はいつもにぎやかですね。", "えき");
        problems[1] = new ReadingProblem(1, Problem.Topic.TRANSPORTATION, "今年の夏に<em>飛行機</em>で北海道に行きます。", "ひこうき");
        problems[2] = new ReadingProblem(1, Problem.Topic.TRANSPORTATION, "現代の日本語では<em>自動車</em>（特に、乗用車）を指すことが多い。", "じどうしゃ");
        problems[3] = new ReadingProblem(1, Problem.Topic.TRANSPORTATION, "<em>新幹線</em>は早くてとても便利です。", "しんかんせん");
        problems[4] = new ReadingProblem(1, Problem.Topic.TRANSPORTATION, "昨日、<em>電車</em>の切符を買いました。", "でんしゃ");
    }

    private static class SingletonHelper {
        private static final ProblemStore instance = new ProblemStore();
    }

    private int problemCount = 0;

    private Problem[] problems;
    
}
