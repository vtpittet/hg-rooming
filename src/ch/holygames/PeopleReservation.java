package ch.holygames;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record PeopleReservation(
        int reservationIndex,
        String lastName,
        String firstName,
        int age,
        String email,
        String foodRequest,
        boolean arrivesOnSaturday,
        boolean newInBatch2,
        boolean cancelledOnBatch_1,
        boolean cancelledOnBatch_2
) implements Row
{
    public static PeopleReservation fromInputRow(int reservationIndex, InputRow inputRow) {
        return new PeopleReservation(
                reservationIndex,
                inputRow.lastName(),
                inputRow.firstName(),
                Integer.valueOf(inputRow.age()),
                inputRow.email(),
                sanitizeFoodConstraint(inputRow.foodConstraint()),
                "Monte samedi matin".equals(inputRow.remark()),
                inputRow.computeIsNewInBatch_2(),
                inputRow.computeIsCancelledInBatch_1(),
                inputRow.computeIsCancelledInBatch_2()
        );
    }

    private static String sanitizeFoodConstraint(String input) {
        if (input == null || List.of(
                "Rien",
                "N/a",
                "N/A",
                "-",
                "Aucun",
                "aucun",
                "Aucune",
                "Aucune",
                "Non",
                "",
                "non",
                "Neantz",
                "Neant",
                "Ras",
                "Néant",
                "Miam :)",
                "/"
        ).stream().map(String::toLowerCase).anyMatch(ignored -> ignored.compareToIgnoreCase(input.trim()) == 0)) {
            return null;
        } else {
            return input;
        }
    }

    public Row.ComparisonKey getComparisonKey() {
        return new Row.ComparisonKey(lastName, firstName, email);
    }

    public OutputRow toOutputRow(RoomReservation context) {
        return new OutputRow(
                "'" + context.reservationId() + "." + reservationIndex,
                lastName,
                firstName,
                email,
                getArrivalDateString(),
                getDepartureDateString(),
                isAdult() ?1:0,
                isChild05() ?1:0,
                isChild69() ?1:0,
                isChild1015() ?1:0,
                computeHostelRemark()
        );
    }

    public String computeHostelRemark() {
        if (cancelledOnBatch_1) {
            return "***Annulé au 07.11.2023***";
        } else if (cancelledOnBatch_2) {
            return "***Annulé au 14.11.2023***";
        } {
            List<String> remarks = Stream.of(
                            Optional.ofNullable(newInBatch2 ? "***nouvelle inscription au 14.11.2023***" : null),
                            Optional.ofNullable(getFoodRequestString())
                    ).filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
            if (remarks.isEmpty()) {
                return null;
            } else {
                return remarks.stream().collect(Collectors.joining("  ----  "));
            }
        }
    }

    public String getFoodRequestString() {
        return foodRequest!=null? "restriction alimentaire: " + foodRequest : null;
    }

    public String getArrivalDateString() {
        return arrivesOnSaturday ? "***18.11.2023***" : "17.11.2023";
    }

    public String getDepartureDateString() {
        return "19.11.2023";
    }

    public boolean isChild1015() {
        return age >= 10 && age <= 15;
    }

    public boolean isChild69() {
        return age >= 6 && age <= 9;
    }

    public boolean isChild05() {
        return age < 6;
    }

    public boolean isAdult() {
        return age > 15;
    }

    public boolean isCancelled() {
        return cancelledOnBatch_1 || cancelledOnBatch_2;
    }

    public boolean isActive() {
        return !isCancelled();
    }
}
