import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

public class main {


    //Viterbi code adapted from Paul Fodor <pfodor@cs.sunysb.edu> Stony Brook University, 2007

    private static class TNode {
        public int[] v_path;
        public double v_prob;

        public TNode(int[] v_path, double v_prob) {
            this.v_path = copyIntArray(v_path);
            this.v_prob = v_prob;
        }
    }

    private static int[] copyIntArray(int[] ia) {
        int[] newIa = new int[ia.length];
        for (int i = 0; i < ia.length; i++) {
            newIa[i] = ia[i];
        }
        return newIa;
    }

    private static int[] copyIntArray(int[] ia, int newInt) {
        int[] newIa = new int[ia.length + 1];
        for (int i = 0; i < ia.length; i++) {
            newIa[i] = ia[i];
        }
        newIa[ia.length] = newInt;
        return newIa;
    }

    public static int[] forwardViterbi(String[] y, String[] X, Double[] sp,
                                       Double[][] tp, Double[][] ep) {
        TNode[] T = new TNode[X.length];
        for (int state = 0; state < X.length; state++) {
            int[] intArray = new int[1];
            intArray[0] = state;
            T[state] = new TNode(intArray, sp[state] * ep[state][0]);
        }

        for (int output = 1; output < y.length; output++) {
            TNode[] U = new TNode[X.length];
            for (int next_state = 0; next_state < X.length; next_state++) {

                int[] argmax = new int[0];
                double valmax = 0;
                for (int state = 0; state < X.length; state++) {
                    int[] v_path = copyIntArray(T[state].v_path);
                    double v_prob = T[state].v_prob;
                    double p = ep[next_state][output] * tp[state][next_state];
                    v_prob *= p;
                    if (v_prob > valmax) {
                        if (v_path.length == y.length) {
                            argmax = copyIntArray(v_path);
                        } else {
                            argmax = copyIntArray(v_path, next_state);
                        }
                        valmax = v_prob;

                    }
                }
                U[next_state] = new TNode(argmax, valmax);
            }
            T = U;
        }
        // apply sum/max to the final states:
        int[] argmax = new int[0];
        double valmax = 0;
        for (int state = 0; state < X.length; state++) {

            int[] v_path = copyIntArray(T[state].v_path);
            double v_prob = T[state].v_prob;
            if (v_prob > valmax) {

                argmax = copyIntArray(v_path);
                valmax = v_prob;
            }
        }
        try {
            PrintWriter writer = new PrintWriter(new FileWriter("output.txt", true));
            for (int i = 0; i < argmax.length; i++) {
                writer.println(y[i] + " " + X[argmax[i]]);
            }
            writer.println("");
            writer.close();
        } catch (IOException e) {
            System.out.println("fail");
        }


        return argmax;
    }

    public static void main(String[] args) {

        Hashtable<String, Integer> tag_frequency = new Hashtable<String, Integer>();
        Hashtable<String, Integer> words_and_tags = new Hashtable<String, Integer>();
        Hashtable<String, Double> emissionProbability = new Hashtable<String, Double>();
        Hashtable<String, Double> prior_probability = new Hashtable<String, Double>();
        Hashtable<String, Integer> transition_frequency = new Hashtable<String, Integer>();
        int totalTags = 0;

        int numLocStart = 0;
        int numPerStart = 0;
        int numDateStart = 0;
        int numOrgStart = 0;
        int numOtherStart = 0;


        int numLocTotal = 0;
        int numDateTotal = 0;
        int numOrgTotal = 0;
        int numPersTotal = 0;
        int numOtherTotal = 0;


        String[] states = {"location", "person", "date", "org", "other"};

        int numSentences = 0;

        String previousTag = "";

        boolean firstLoop = true;
        int wordsOnceLoc = 0;
        int wordsOncePer = 0;
        int wordsOnceDate = 0;
        int wordsOnceOrg = 0;
        int wordsOnceOther = 0;
        File folder = new File(args[0]);
        File[] listOfFiles = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (!name.equals(".DS_Store") && !name.equals("wsj_0026.conll")) {
                    return true;
                }
                return false;
            }
        });
        for (File file : listOfFiles) {
            if (file.isFile()) {
                int locationIndex = 0;
                int personIndex = 0;
                int dateIndex = 0;
                int orgIndex = 0;
                int lineLength = 0;
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;

                    while ((line = reader.readLine()) != null) {
                        String[] result = line.split("\\s");
                        for (int i = 5; i < result.length; i++) {
                            if (result[i].contains("location")) {
                                locationIndex = i;
                            } else if (result[i].contains("person")) {
                                personIndex = i;
                            } else if (result[i].contains("date")) {
                                dateIndex = i;
                            } else if (result[i].contains("org")) {
                                orgIndex = i;
                            }
                        }
                    }
                    reader.close();
                }
                catch (IOException e) {
                    System.out.println("Error1");
                }
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] result = line.split("\\s");
                        // 8 = location, 9 = person, 10 = date, 11 = org
                        if (result.length > 4) {
                            if (result[0].equals("1")) {
                                numSentences++;
                                if (result[locationIndex].contains("location")) {

                                    numLocStart++;
                                } else if (result[personIndex].contains("person")) {
                                    numPerStart++;
                                } else if (result[dateIndex].contains("date")) {
                                    numDateStart++;
                                } else if (result[orgIndex].contains("org")) {
                                    numOrgStart++;
                                } else {
                                    numOtherStart++;
                                }
                            }

                            if (firstLoop == true) {
                                if (result[locationIndex].contains("location")) {
                                    previousTag = "location";
                                    numLocTotal++;
                                } else if (result[personIndex].contains("person")) {
                                    previousTag = "person";
                                    numPersTotal++;
                                } else if (result[dateIndex].contains("date")) {
                                    previousTag = "date";
                                    numDateTotal++;
                                } else if (result[orgIndex].contains("org")) {
                                    previousTag = "org";
                                    numOrgTotal++;
                                } else {
                                    previousTag = "other";
                                    numOtherTotal++;
                                }
                            } else {
                                if (result[locationIndex].contains("location")) {
                                    if (transition_frequency.containsKey(previousTag + " location")) {
                                        transition_frequency.put(previousTag + " location", transition_frequency.get(previousTag + " location"));
                                    } else {
                                        transition_frequency.put(previousTag + " location", 1);
                                    }
                                    previousTag = "location";
                                    numLocTotal++;
                                } else if (result[personIndex].contains("person")) {
                                    if (transition_frequency.containsKey(previousTag + " person")) {
                                        transition_frequency.put(previousTag + " person", transition_frequency.get(previousTag + " person"));
                                    } else {
                                        transition_frequency.put(previousTag + " person", 1);
                                    }
                                    previousTag = "person";
                                    numPersTotal++;
                                } else if (result[dateIndex].contains("date")) {
                                    if (transition_frequency.containsKey(previousTag + " date")) {
                                        transition_frequency.put(previousTag + " date", transition_frequency.get(previousTag + " date"));
                                    } else {
                                        transition_frequency.put(previousTag + " date", 1);
                                    }
                                    previousTag = "date";
                                    numDateTotal++;
                                } else if (result[orgIndex].contains("org")) {
                                    if (transition_frequency.containsKey(previousTag + " org")) {
                                        transition_frequency.put(previousTag + " org", transition_frequency.get(previousTag + " org"));
                                    } else {
                                        transition_frequency.put(previousTag + " org", 1);
                                    }
                                    previousTag = "org";
                                    numOrgTotal++;
                                } else {
                                    if (transition_frequency.containsKey(previousTag + " other")) {
                                        transition_frequency.put(previousTag + " other", transition_frequency.get(previousTag + " other"));
                                    } else {
                                        transition_frequency.put(previousTag + " other", 1);
                                    }
                                    previousTag = "other";
                                    numOtherTotal++;
                                }
                            }
                            String tempString = "";
                            if (result[locationIndex].contains("location")) {
                                tempString = "location";
                            } else if (result[personIndex].contains("person")) {
                                tempString = "person";
                            } else if (result[dateIndex].contains("date")) {
                                tempString = "date";
                            } else if (result[orgIndex].contains("org")) {
                                tempString = "org";
                            } else {
                                tempString = "other";
                            }

                            String newString = result[3] + " " + tempString;


                            if (words_and_tags.containsKey(newString)) {
                                words_and_tags.put(newString, words_and_tags.get(newString) + 1);
                            } else {
                                words_and_tags.put(newString, 1);
                            }




                            firstLoop = false;
                        }
                    }

                    Set<String> a = words_and_tags.keySet();
                    for (String b : a) {
                        String[] result = b.split("\\s");
                        String tag = result[1];
                        if (words_and_tags.get(b) == 1) {
                            if (tag.equals("location")) {
                                wordsOnceLoc++;
                            } else if (tag.equals("person")) {
                                wordsOncePer++;
                            } else if (tag.equals("date")) {
                                wordsOnceDate++;
                            } else if (tag.equals("org")) {
                                wordsOnceOrg++;
                            } else {
                                wordsOnceOther++;
                            }
                        }
                    }
                    //likelihood
                    Set<String> keys = words_and_tags.keySet();
                    for (String key : keys) {
                        String[] temp = key.split("\\s");
                        String tag = temp[1];
                        int tag_freq;
                        if (tag.equals("location")) {
                            tag_freq = numLocTotal;
                        } else if (tag.equals("person")) {
                            tag_freq = numPersTotal;
                        } else if (tag.equals("date")) {
                            tag_freq = numDateTotal;
                        } else if (tag.equals("org")) {
                            tag_freq = numOrgTotal;
                        } else {
                            tag_freq = numOtherTotal;
                        }

                        double newValue = ((double) words_and_tags.get(key)) / tag_freq;

                        emissionProbability.put(key, newValue);

                    }

                    //Prior Probability
                    Set<String> keys2 = transition_frequency.keySet();
                    for (String key : keys2) {

                        String[] temp = key.split("\\s");
                        String tag = temp[0];
                        int tag_freq;
                        if (tag.equals("location")) {
                            tag_freq = numLocTotal;
                        } else if (tag.equals("person")) {
                            tag_freq = numPersTotal;
                        } else if (tag.equals("date")) {
                            tag_freq = numDateTotal;
                        } else if (tag.equals("org")) {
                            tag_freq = numOrgTotal;
                        } else {
                            tag_freq = numOtherTotal;
                        }


                        double bd = (double) transition_frequency.get(key) / tag_freq;


                        prior_probability.put(key, bd);
                    }
                    br.close();
                } catch (IOException e) {
                    System.out.println("IOException");
                }
            }

            double startProbLoc = (double) numLocStart / numSentences;
            double startProbPer = (double) numPerStart / numSentences;
            double startProbDate = (double) numDateStart / numSentences;
            double startProbOrg = (double) numOrgStart / numSentences;
            double startProbOther = (double) numOtherStart / numSentences;


            Double[] start_probability = new Double[states.length];

            start_probability[0] = startProbLoc;
            start_probability[1] = startProbPer;
            start_probability[2] = startProbDate;
            start_probability[3] = startProbOrg;
            start_probability[4] = startProbOther;

            Double[][] transition_probability = new Double[states.length][states.length];
            String firstTag;
            String secondTag;
            for (int i = 0; i < states.length; i++) {
                firstTag = states[i];
                for (int j = 0; j < states.length; j++) {
                    secondTag = states[j];
                    String tempTransString = firstTag + " " + secondTag;
                    if (prior_probability.containsKey(tempTransString)) {
                        transition_probability[i][j] = prior_probability.get(tempTransString);
                    } else {
                        transition_probability[i][j] = 0.0;
                    }
                }
            }


            File test_input = new File(args[1]);
            File output = new File("output.txt");
            if (output.exists()) {
                output.delete();
            }
            try (BufferedReader br = new BufferedReader(new FileReader(test_input))) {
                String line;
                List<String> observedWords = new ArrayList<String>();
                while ((line = br.readLine()) != null) {
                    String[] result = line.split("\\s");
                    if (result.length > 4) {
                        observedWords.add(result[3]);
                    }
                }
                String[] observations = observedWords.toArray(new String[0]);
                Double[][] emission_probability = new Double[states.length][observations.length];

                for (int i = 0; i < states.length; i++) {
                    String tag = states[i];
                    for (int j = 0; j < observations.length; j++) {
                        String word = observations[j];
                        String temp = word + " " + tag;

                        if (emissionProbability.containsKey(temp)) {
                            emission_probability[i][j] = emissionProbability.get(temp);
                        } else {
                            emission_probability[i][j] = 0.0;

                        }
                    }
                }
                forwardViterbi(observations, states, start_probability, transition_probability, emission_probability);
            } catch (IOException e) {
                System.out.println("Error2");
            }



        }
    }
}


