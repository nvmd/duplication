import uk.ac.shef.wit.simmetrics.similaritymetrics.InterfaceStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Main {

    private static final int ID_ATTRIBUTE_NUM = 2;
    private static final int ATTR_0_NUM = 0;

    private static List<List<Integer>> process(List<String> attrs, InterfaceStringMetric metric, float metricThreshold) {
        List<List<Integer>> result = new ArrayList<List<Integer>>();
        for (String value : attrs) {
            result.add(process(value, attrs, metric, metricThreshold));
        }
        return result;
    }

    private static List<Integer> process(String attr, List<String> attrs, InterfaceStringMetric metric, float metricThreshold) {
        List<Integer> indices = new ArrayList<Integer>();
        for (int i = 0; i < attrs.size(); ++i) {
            String currentAttr = attrs.get(i);
            float similarity = metric.getSimilarity(attr, currentAttr);
            if (similarity >= metricThreshold) {
                indices.add(i);
            }
        }
        return indices;
    }

    public static void main(String[] args) throws IOException {
        String filename = args[0];
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader);
        Instances data = arff.getData();
        data.setClassIndex(data.numAttributes() - 1);

//        System.out.println("\nDataset:\n");
//        System.out.println(data);

//        System.out.println(data.attribute(ATTR_0).value(17));
//        System.out.println(data.attribute(ATTR_0).numValues());

        Enumeration values = data.enumerateInstances();
        List<String> refinedValues = new ArrayList<String>(data.numInstances());
        while (values.hasMoreElements()) {
            String value = ((Instance) values.nextElement()).stringValue(ATTR_0_NUM);
            refinedValues.add(value.replaceAll("[-,.]", "").replaceAll("\\s+", " ").toLowerCase());
//            System.out.println(value);
        }
        float threshold = 0.99f;
        List<List<Integer>> processedValues = process(refinedValues, new Levenshtein(), threshold);
//        System.out.println(processedValues);

        printMatching(data, ATTR_0_NUM, processedValues);

        int truePositive = 0;
        int falseNegative = 0;
        int falsePositive = 0;
        int trueNegative = 0;

        for (int i = 0; i < processedValues.size(); ++i) {
            List<Integer> indices = processedValues.get(i);
            String id = data.instance(i).stringValue(ID_ATTRIBUTE_NUM);
            for (Integer index : indices) {
                String id2 = data.instance(index).stringValue(ID_ATTRIBUTE_NUM);
                if (id.equals(id2)) {
                    ++truePositive;
                } else {
                    ++falseNegative;
                }
            }
        }

        Enumeration ids = data.enumerateInstances();
        List<String> refinedIds = new ArrayList<String>(data.numInstances());
        while (ids.hasMoreElements()) {
            String value = ((Instance) ids.nextElement()).stringValue(ID_ATTRIBUTE_NUM);
            refinedIds.add(value);
        }

        List<List<Integer>> processedIds = process(refinedIds, new InterfaceStringMetric() {
            @Override
            public String getShortDescriptionString() {
                return null;
            }

            @Override
            public String getLongDescriptionString() {
                return null;
            }

            @Override
            public long getSimilarityTimingActual(String string1, String string2) {
                return 0;
            }

            @Override
            public float getSimilarityTimingEstimated(String string1, String string2) {
                return 0;
            }

            @Override
            public float getSimilarity(String string1, String string2) {
                return (string1.equals(string2) ? 1.0f : 0.0f);
            }

            @Override
            public String getSimilarityExplained(String string1, String string2) {
                return null;
            }
        }, 1.0f);
//        System.out.println(processedIds);
//        printMatching(data, ID_ATTRIBUTE_NUM, processedIds);

        for (int i = 0; i < processedValues.size(); ++i) {
            List<Integer> foo = processedValues.get(i);
            List<Integer> bar = processedIds.get(i);

            int intersec = 0;
            for (Integer index : foo) {
                if (bar.contains(index))
                    intersec++;
            }

            falsePositive += bar.size() - intersec;
        }


        float precision = (float) truePositive / (truePositive + falsePositive);
        float recall = (float) truePositive / (truePositive + falseNegative);

        System.out.println("Precision: " + precision);
        System.out.println("Recall: " + recall);

    }

    private static void printMatching(Instances data, int attribute, List<List<Integer>> processedValues) {
        for (int i = 0; i < processedValues.size(); ++i) {
            List<Integer> indices = processedValues.get(i);
            System.out.print(data.instance(i).stringValue(attribute) + " -> ");
            for (Integer index : indices) {
                System.out.print(data.instance(index).stringValue(attribute) + " | ");
            }
            System.out.println();
        }
    }
}
