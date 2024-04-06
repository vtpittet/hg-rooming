package ch.holygames;

public interface Row {
    record ComparisonKey(String lastName, String firstName, String email) {}
    ComparisonKey getComparisonKey();
}
