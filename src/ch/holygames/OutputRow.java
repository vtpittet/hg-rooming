package ch.holygames;


public record OutputRow(
        String reservationId,
        String lastName,
        String firstName,
        String email,
        String arrivalDate,
        String departureDate,
        long adultCount,
        long childCount,
        String childAges,
        long child05Count,
        long child69Count,
        long child1015Count,

        String specificRequests
) {
    public String printRow() {
        StringBuilder row = new StringBuilder();
        row.append(reservationId);

        appendOptional(lastName, row);
        appendOptional(firstName, row);
        appendOptional(arrivalDate, row);
        appendOptional(departureDate, row);
        appendOptional(null, row);
        appendOptional(adultCount, row);
        appendOptional(childCount, row);
        appendOptional(childAges, row);
        appendOptional(specificRequests, row);
        return row.toString();
    }

    private void appendOptional(String string, StringBuilder stringBuilder) {
        stringBuilder.append(";");
        if (string != null) {
            stringBuilder.append(string);
        }
    }
    private void appendOptional(long integer, StringBuilder stringBuilder) {
        stringBuilder.append(";");
        if (integer != 0) {
            stringBuilder.append(integer);
        }
    }
}
