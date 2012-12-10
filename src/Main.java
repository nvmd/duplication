import uk.ac.shef.wit.simmetrics.similaritymetrics.*;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.*;
import java.util.*;

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
        String entityMetricName = args[1];
        String keyMetricName = args[2];

        float threshold = Float.parseFloat(args[3]);
        boolean outputData = args.length > 4;

        InterfaceStringMetric entityMetric = entityMetricMap.get(entityMetricName);
        InterfaceStringMetric keyMetric = keyMetricMap.get(keyMetricName);

        Instances data = loadData(filename);
        List<String> refinedValues = getValues(data);
        List<String> refinedIds = getIds(data);

        List<List<Integer>> processedIds = process(refinedIds, keyMetric, 1.0f);

        List<List<Integer>> processedValues = process(refinedValues, entityMetric, threshold);

        if (outputData) {
            printMatching(data, ATTR_0_NUM, processedValues);
        }

        Float precision = 0f;
        Float recall = 0f;

        calc(data, processedValues, processedIds, precision, recall);

        System.out.println(filename + " " + entityMetricName+ " " + keyMetricName
                                    + " " + threshold + " " + precision + " " + recall);
    }

    private static Instances loadData(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader);
        Instances data = arff.getData();
        data.setClassIndex(data.numAttributes() - 1);

        return data;
    }

    private static List<String> getValues(Instances data) throws IOException {
        Enumeration values = data.enumerateInstances();
        List<String> refinedValues = new ArrayList<String>(data.numInstances());
        while (values.hasMoreElements()) {
            String value = ((Instance) values.nextElement()).stringValue(ATTR_0_NUM);
            refinedValues.add(value.replaceAll("[-,.]", "").replaceAll("\\s+", " ").toLowerCase());
        }

        return refinedValues;
    }

    private static List<String> getIds(Instances data) {
        Enumeration ids = data.enumerateInstances();
        List<String> refinedIds = new ArrayList<String>(data.numInstances());
        while (ids.hasMoreElements()) {
            String value = ((Instance) ids.nextElement()).stringValue(ID_ATTRIBUTE_NUM);
            refinedIds.add(value);
        }

        return refinedIds;
    }

    private static void calc(Instances data, List<List<Integer>> processedValues, List<List<Integer>> processedIds, Float precision, Float recall) {
        int truePositive = 0;
        int falseNegative = 0;
        int falsePositive = 0;
        int trueNegative = 0;

        for (int i = 0; i < processedValues.size(); ++i) {
            List<Integer> indices = processedValues.get(i);
            List<Integer> bar = processedIds.get(i);
            String id = data.instance(i).stringValue(ID_ATTRIBUTE_NUM);

            int intersec = 0;
            for (Integer index : indices) {
                String id2 = data.instance(index).stringValue(ID_ATTRIBUTE_NUM);
                if (id.equals(id2)) {
                    ++truePositive;
                } else {
                    ++falseNegative;
                }
                if (bar.contains(index))
                    intersec++;
            }

            falsePositive += bar.size() - intersec;
        }

        precision = (float) truePositive / (truePositive + falsePositive);
        recall = (float) truePositive / (truePositive + falseNegative);
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

    static Map<String, InterfaceStringMetric> entityMetricMap = new HashMap<String, InterfaceStringMetric>() {{
        put("levenshtein", new Levenshtein());
        put("jaro-winkler", new JaroWinkler());
        put("monge-elkan", new MongeElkan());
        put("soundex", new Soundex());
        put("cosine", new CosineSimilarity());
        put("jaccard", new JaccardSimilarity());
    }};

    static Map<String, InterfaceStringMetric> keyMetricMap = new HashMap<String, InterfaceStringMetric>() {{
        put("strict", new StrictEqualityMetric());
        put("substring", new SubstringEqualityMetric());
    }};
}
