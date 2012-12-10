import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;

import java.io.Serializable;

public abstract class DummyEqualityMetric extends AbstractStringMetric implements Serializable {

    @Override
    public String getShortDescriptionString() {
        return null;
    }

    @Override
    public String getLongDescriptionString() {
        return null;
    }

    @Override
    public float getSimilarityTimingEstimated(String string1, String string2) {
        return 0;
    }

    @Override
    public float getUnNormalisedSimilarity(String string1, String string2) {
        return getSimilarity(string1, string2);
    }

    @Override
    public String getSimilarityExplained(String string1, String string2) {
        return null;
    }
}
