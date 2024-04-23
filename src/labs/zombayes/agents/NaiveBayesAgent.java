package src.labs.zombayes.agents;

// SYSTEM IMPORTS
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


// JAVA PROJECT IMPORTS
import edu.bu.labs.zombayes.agents.SurvivalAgent;
import edu.bu.labs.zombayes.features.Features.FeatureType;
import edu.bu.labs.zombayes.linalg.Matrix;
import edu.bu.labs.zombayes.utils.Pair;



public class NaiveBayesAgent
    extends SurvivalAgent
{

    public static class NaiveBayes
        extends Object
    {

        public static final FeatureType[] FEATURE_HEADER = {FeatureType.CONTINUOUS,
                                                            FeatureType.CONTINUOUS,
                                                            FeatureType.DISCRETE,
                                                            FeatureType.DISCRETE};

        private Map<Integer, Double> classProbs; 
        private Map<Integer, Map<Integer, Integer>> cFeatureCounts;
        private Map<Integer, Map<Integer, Double>> cFeatureMeans;
        private Map<Integer, Map<Integer, Double>> cFeatureSTD;
        private Map<Integer, Map<Integer, Map<Integer, Double>>> dFeatureProbs;
        private Map<Integer, Map<Integer, Integer>> dFeatureCounts;

        // TODO: complete me!
        public NaiveBayes()
        {
            classProbs = new HashMap<>();
            cFeatureCounts = new HashMap<>();
            cFeatureMeans = new HashMap<>();
            cFeatureSTD = new HashMap<>();
            dFeatureProbs = new HashMap<>();
            dFeatureCounts = new HashMap<>();
        }

        // TODO: complete me!
        public void fit(Matrix X, Matrix y_gt)
        {
            
            int samples = X.getShape().getNumRows();

            for (int i = 0; i < samples; i++) {
                int value = (int) y_gt.get(i, 0);
                if (classProbs.containsKey(value)) {
                    classProbs.put(value, classProbs.get(value) + 1);
                } else {
                    classProbs.put(value, 1.0);
                }
            }
            
            // Probability of each class happening 
            for (Integer key: classProbs.keySet()) {
                classProbs.put(key, classProbs.get(key) / samples);
            }

            // Iterate over all samples
            for (int i = 0; i < samples; i++) {
                int truthValue = (int) y_gt.get(i, 0);

                // Continuous Features
                for (int j = 0; j < 2; j++) {
                    double fValue = X.get(j, i); // Feature value
                    // Increase the number of counts for this class and this feature

                    if (cFeatureCounts.containsKey(truthValue)) {
                        if (cFeatureCounts.get(truthValue).containsKey(j)) {
                            cFeatureCounts.get(truthValue).put(j, cFeatureCounts.get(truthValue).get(j) + 1);
                            cFeatureMeans.get(truthValue).put(j, cFeatureMeans.get(truthValue).get(j) + fValue);
                        } else {
                            cFeatureCounts.get(truthValue).put(j, 1);
                            cFeatureMeans.get(truthValue).put(j, fValue);
                        }
                    } else {
                        cFeatureCounts.put(truthValue, new HashMap<>());
                        cFeatureCounts.get(truthValue).put(j, 1);
                        cFeatureMeans.put(truthValue, new HashMap<>());
                        cFeatureMeans.get(truthValue).put(j, fValue);
                    }
                }

                // Discrete Features
                for (int j = 2; j < 4; j++) {
                    int fValue = (int) X.get(j, i); // Feature value
                    if (dFeatureProbs.containsKey(truthValue)) {
                        if (dFeatureProbs.get(truthValue).containsKey(j)) {
                            if (dFeatureProbs.get(truthValue).get(j).containsKey(fValue)) {
                                dFeatureProbs.get(truthValue).get(j).put(fValue, dFeatureProbs.get(truthValue).get(j).get(fValue) + 1);
                            } else {
                                dFeatureProbs.get(truthValue).get(j).put(fValue, 1.0);
                            }
                        } else {
                            dFeatureProbs.get(truthValue).put(j, new HashMap<>());
                            dFeatureProbs.get(truthValue).get(j).put(fValue, 1.0);
                        }
                    } else {
                        dFeatureProbs.put(truthValue, new HashMap<>());
                        dFeatureProbs.get(truthValue).put(j, new HashMap<>());
                        dFeatureProbs.get(truthValue).get(j).put(fValue, 1.0);
                    }

                    // Increase the number of counts for this class and this feature
                    if (dFeatureCounts.containsKey(truthValue)) {
                        if (dFeatureCounts.get(truthValue).containsKey(j)) {
                            dFeatureCounts.get(truthValue).put(j, dFeatureCounts.get(truthValue).get(j) + 1);
                        } else {
                            dFeatureCounts.get(truthValue).put(j, 1);
                        }
                    } else {
                        dFeatureCounts.put(truthValue, new HashMap<>());
                        dFeatureCounts.get(truthValue).put(j, 1);
                    }
                }
            }
            
            // Divide out all probs by counts
            for (int c : dFeatureProbs.keySet()) {
                for (int f : dFeatureProbs.get(c).keySet()) {
                    for (int v : dFeatureProbs.get(c).get(f).keySet()) {
                        dFeatureProbs.get(c).get(f).put(v, dFeatureProbs.get(c).get(f).get(v) / dFeatureCounts.get(c).get(f));
                    }
                }
            }

            // Calculate the mean for each class and feature (continuous)
            for (int c : cFeatureMeans.keySet()) {
                for (int f : cFeatureMeans.get(c).keySet()) {
                    cFeatureMeans.get(c).put(f, cFeatureMeans.get(c).get(f) / cFeatureCounts.get(c).get(f));
                }
            }

            // Smooth out zero probabiltiiy values
            for (int c : dFeatureProbs.keySet()) {
                for (int f : dFeatureProbs.get(c).keySet()) {
                    for (int v : dFeatureProbs.get(c).get(f).keySet()) {
                        if (dFeatureProbs.get(c).get(f).get(v) == 0) {
                            dFeatureProbs.get(c).get(f).put(v, 0.0001);
                        }
                    }
                }
            }

            // Calculate the sum of the difference between mean and data value squared for each class and feature (continuous)
            for (int i = 0; i < samples; i++) {
                for (int j = 0; j < 2; j++) {
                    double fValue = X.get(i,j);
                    int truthValue = (int) y_gt.get(i, 0);

                    if (cFeatureSTD.containsKey(truthValue)) {
                        if (cFeatureSTD.get(truthValue).containsKey(j)) {
                            cFeatureSTD.get(truthValue).put(j, cFeatureSTD.get(truthValue).get(j) + Math.pow(fValue - cFeatureMeans.get(truthValue).get(j), 2));
                        } else {
                            cFeatureSTD.get(truthValue).put(j, Math.pow(fValue - cFeatureMeans.get(truthValue).get(j), 2));
                        }
                    } else {
                        cFeatureSTD.put(truthValue, new HashMap<>());
                        cFeatureSTD.get(truthValue).put(j, Math.pow(fValue - cFeatureMeans.get(truthValue).get(j), 2));
                    }
                }
            }
            
            // For each std divide by feature count to get variance and square root it for std
            for (int c : cFeatureSTD.keySet()) {
                for (int f : cFeatureSTD.get(c).keySet()) {
                    cFeatureSTD.get(c).put(f, Math.sqrt(cFeatureSTD.get(c).get(f) / cFeatureCounts.get(c).get(f)));
                }
            }

            return;
        }

        // TODO: complete me!
        public int predict(Matrix x)
        {
            double maxProb = Double.NEGATIVE_INFINITY;
            int maxClass = -1;

            for (int c : classProbs.keySet()) {
                double classProb = classProbs.get(c); // The probability of this class happening 

                // Loop through the continuous Features
                for (int i = 0; i < 2; i++) {
                    double featureValue = x.get(0, i);
                    classProb *= calculateProb(featureValue, cFeatureMeans.get(c).get(i), cFeatureSTD.get(c).get(i));
                }

                // Loop through discrete features
                for (int i = 2; i < 4; i++) {
                    int featureValue = (int) x.get(0, i);
                    classProb *= dFeatureProbs.get(c).get(i).get(featureValue);
                }

                if (classProb > maxProb) {
                    maxProb = classProb;
                    maxClass = c;
                }
            }

            return maxClass;
        }

        private double calculateProb(double v, double mean, double std) {
            // Probability for Gaussian distribution
            double probabiltiiy = (1 / Math.sqrt(2 * Math.PI * Math.pow(std, 2))) * Math.pow(Math.E, (-(Math.pow(v - mean, 2))/ (2 * Math.pow(std, 2))));
            return probabiltiiy;
        }

    }
    
    private NaiveBayes model;

    public NaiveBayesAgent(int playerNum, String[] args)
    {
        super(playerNum, args);
        this.model = new NaiveBayes();
    }

    public NaiveBayes getModel() { return this.model; }

    @Override
    public void train(Matrix X, Matrix y_gt)
    {
        System.out.println(X.getShape() + " " + y_gt.getShape());
        this.getModel().fit(X, y_gt);
    }

    @Override
    public int predict(Matrix featureRowVector)
    {
        return this.getModel().predict(featureRowVector);
    }

}
