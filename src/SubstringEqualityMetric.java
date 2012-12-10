class SubstringEqualityMetric extends DummyEqualityMetric {
    public float getSimilarity(String string1, String string2) {
        return string1.contains(string2) || string2.contains(string1) ? 1.0f : 0.0f;
    }
}