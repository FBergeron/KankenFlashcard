package jp.kyoto.nlp.kanken;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

class ProblemStore {

    public static ProblemStore getInstance() {
        return SingletonHelper.instance;
    }

    public Problem getNextProblem(int level, HashSet<Problem.Topic> topics, Problem.Type type) {
        Problem.Topic[] topicArray = (Problem.Topic[])topics.toArray(new Problem.Topic[topics.size()]);
        Random rand = new Random();
        int topicIndex = rand.nextInt(topicArray.length);

        HashMap<String, HashMap<Problem.Type, HashMap<String, Problem>>> problemsByLevel = problemsByTopic.get(topicArray[topicIndex]);
        HashMap<Problem.Type, HashMap<String, Problem>> problemsByType = problemsByLevel.get(level + "");
        HashMap<String, Problem> problemsById = problemsByType.get(type);

        String[] problemIds = (String[])problemsById.keySet().toArray(new String[problemsById.size()]);
        int problemIndex = rand.nextInt(problemIds.length);
        Problem problem = problemsById.get(problemIds[problemIndex]);
        return problem;
    }

    private ProblemStore() {
        new Thread() {
            public void run() {
                int problemCount = 0;
                URL problemsUrl = null;
                try {
                    System.out.println("Retrieving JSON data...");
                    problemsUrl = new URL("http://lotus.kuee.kyoto-u.ac.jp/~frederic/Kanken/problems.json");
                    JSONObject jsonProblemsByTopic = Util.readJson(problemsUrl);
                    for (Iterator itTopic = jsonProblemsByTopic.keys(); itTopic.hasNext(); ) {
                        String strTopic = (String)itTopic.next();
                        Problem.Topic topic = Problem.getTopicFromJapaneseString(strTopic);
                        // System.out.println("topic=" + strTopic);
                        
                        HashMap<String, HashMap<Problem.Type, HashMap<String, Problem>>> problemsByLevel = null;
                        if (problemsByTopic.containsKey(topic))
                            problemsByLevel = problemsByTopic.get(topic);
                        else {
                            problemsByLevel = new HashMap<String, HashMap<Problem.Type, HashMap<String, Problem>>>();
                            problemsByTopic.put(topic, problemsByLevel);
                        }

                        JSONObject jsonProblemsByLevel = jsonProblemsByTopic.getJSONObject(strTopic);
                        for (Iterator itLevel = jsonProblemsByLevel.keys(); itLevel.hasNext(); ) {
                            String level = (String)itLevel.next();
                            // System.out.println("level=" + level);
                            try {
                                Integer intLevel = Integer.parseInt(level);

                                HashMap<Problem.Type, HashMap<String, Problem>> problemsByType = null;
                                if (problemsByLevel.containsKey(level))
                                    problemsByType = problemsByLevel.get(level);
                                else {
                                    problemsByType = new HashMap<Problem.Type, HashMap<String, Problem>>();
                                    problemsByLevel.put(level, problemsByType);
                                }

                                JSONObject jsonProblemsByType = jsonProblemsByLevel.getJSONObject(level);
                                for (Iterator itType = jsonProblemsByType.keys(); itType.hasNext(); ) {
                                    String strType = (String)itType.next();
                                    Problem.Type type = Problem.getTypeFromString(strType);
                                    // System.out.println("type=" + strType);

                                    HashMap<String, Problem> problemsById = null;
                                    if (problemsByType.containsKey(strType))
                                        problemsById = problemsByType.get(strType);
                                    else {
                                        problemsById = new HashMap<String, Problem>();
                                        problemsByType.put(type, problemsById);
                                    }

                                    JSONArray jsonProblems = jsonProblemsByType.getJSONArray(strType);
                                    for (int i = 0; i < jsonProblems.length(); i++) {
                                        JSONArray jsonProblem = jsonProblems.getJSONArray(i);
                                        String id = jsonProblem.getString(0);
                                        String statement = jsonProblem.getString(1);
                                        String rightAnswer = jsonProblem.getString(2);
                                        String articleUrl = jsonProblem.getString(3);

                                        // System.out.println("id="+id);
                                        // System.out.println("statement="+statement);
                                        // System.out.println("rightAnswer="+rightAnswer);
                                        // System.out.println("articleUrl="+articleUrl);

                                        Problem problem = null;
                                        if ("yomi".equals(strType))
                                            problem = new ReadingProblem(id, intLevel.intValue(), topic, statement, rightAnswer, articleUrl); 
                                        else if ("kaki".equals(strType))
                                            problem = new WritingProblem(id, intLevel.intValue(), topic, statement, rightAnswer, articleUrl);
                                        if (problem != null) {
                                            problemsById.put(id, problem);
                                            problemCount++;
                                        }
                                    }
                                }
                            }
                            catch(NumberFormatException nfe) {
                                nfe.printStackTrace();
                            }
                        }
                    }
                    System.out.println(problemCount + " problems have been inserted in the database.");

                }
                catch(MalformedURLException e1) {
                    e1.printStackTrace();
                }
                catch(IOException e2) {
                    e2.printStackTrace();
                }
                catch(JSONException e3) {
                    e3.printStackTrace();
                }

            }
        }.start();
    }

    private static class SingletonHelper {
        private static final ProblemStore instance = new ProblemStore();
    }

    HashMap<Problem.Topic, HashMap<String, HashMap<Problem.Type, HashMap<String, Problem>>>> problemsByTopic = new HashMap<Problem.Topic, HashMap<String, HashMap<Problem.Type, HashMap<String, Problem>>>>();
    
}
