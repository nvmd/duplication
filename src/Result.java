
public class Result {
    private float precision;
    private float recall;

    public Result(float precision, float recall) {
        this.precision = precision;
        this.recall = recall;
    }

    public float getPrecision() {
        return precision;
    }

    public float getRecall() {
        return recall;
    }
}
