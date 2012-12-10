class StrictEqualityMetric extends DummyEqualityMetric {
    public float getSimilarity(String string1, String string2) {
        return string1.equals(string2) ? 1.0f : 0.0f;
    }
}