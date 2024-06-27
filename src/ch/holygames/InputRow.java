package ch.holygames;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record InputRow(
    String remark,
    int reservationIndexOffset,
    String reservationIndex,
    String lastName,
    String firstName,
    String foodConstraint,
    String email,
    String babyphone,
    String hostelRemark,
    String age,
    String customRoomGroup,
    String roomNeighboring) {

    public static InputRow ofInputLine(InputLine inputLine) {
        String[] fields = inputLine.line().split("\t");
        String remark = safeGet(fields, "A");
        String reservationIndex = safeGet(fields, "D");
        String lastName = safeGet(fields, "F");
        String firstName = safeGet(fields, "G");
        String foodConstraint = safeGet(fields, "I");
        String email = safeGet(fields, "J");
        String babyphone = null;
        String hostelRemark = safeGet(fields, "O");
        String age = safeGet(fields, "R");
        String customRoomGroup = safeGet(fields, "T");
        String roomNeighboring = safeGet(fields, "V");
        return new InputRow(
                remark,
                inputLine.offset(),
                reservationIndex,
                lastName,
                firstName,
                foodConstraint,
                email,
                babyphone,
                sanitizeHostelRemark(hostelRemark),
                age,
                customRoomGroup,
                roomNeighboring
        );
    }

    private static String sanitizeHostelRemark(String hostelRemark) {
        if (Stream.<Predicate<String>>of(
                s -> s == null,
                s -> s.toLowerCase().contains("sparrow"),
                s -> s.toLowerCase().contains("oui"),
                s -> s.toLowerCase().contains("Je désire être regroupé en chambre et bénéficier d'un prix dégressif"),
                s -> s.toLowerCase().contains("En chambre à 4 avec Ramoni Florence et Manon"),
                s -> s.toLowerCase().contains("Je désire être regroupé en chambre commune. Merci")
        ).anyMatch(p -> p.test(hostelRemark))) {
            return null;
        } else {
            return hostelRemark;
        }
    }

    private static String safeGet(String[] fromArray, String column) {
        int index = columnToIndex(column);
        if (index < fromArray.length) {
            String string = fromArray[index];
            if ("".equals(string)) {
                return null;
            } else {
                return string;
            }
        } else {
            System.out.println("index " + index + " is out of bound for array " + Arrays.stream(fromArray).collect(Collectors.joining(",")));
            return null;
        }
    }
    private static int columnToIndex(String column) {
        return column.chars()
                .reduce(0, (accumulated, added) -> accumulated*26+(added - 'A' + 1))
                - 1;
    }

    public String computeReservationKey() {
        if (customRoomGroup == null) {
            return reservationIndexOffset + "." + reservationIndex;
        } else {
            return customRoomGroup;
        }
    }

    public boolean computeIsCancelled() {
        return "annulé".equals(remark);
    }

    public boolean computeIsCancelledInBatch_1() {
        return "Annulé (new)".equals(remark);
    }
    public boolean computeIsCancelledInBatch_2() {
        return "ANNULÉ NEW2".equals(remark);
    }

    public boolean computeIsNewInBatch_1() {
        return "NEW".equals(remark);
    }

    public boolean computeIsNewInBatch_2() {
        return "NEW2".equals(remark);
    }

    public String computeNeighborhoodKey(long index) {
        // no neighborhood for late reservations
        return roomNeighboring() == null || computeIsNewInBatch_1() || computeIsNewInBatch_2() ? "" + index : roomNeighboring();
    }
}
