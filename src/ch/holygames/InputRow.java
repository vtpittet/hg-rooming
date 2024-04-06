package ch.holygames;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record InputRow(
    String remark,
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

    public static InputRow ofInputLine(String inputLine) {
        String[] fields = inputLine.split("\t");
        String remark = safeGet(fields, 0);
        String reservationIndex = safeGet(fields, 3);
        String lastName = safeGet(fields, 5);
        String firstName = safeGet(fields, 6);
        String foodConstraint = safeGet(fields, 8);
        String email = safeGet(fields, 9);
        String babyphone = safeGet(fields, 12);
        String hostelRemark = safeGet(fields, 16);
        String age = safeGet(fields, 17);
        String customRoomGroup = safeGet(fields, 20);
        String roomNeighboring = safeGet(fields, 22);
        return new InputRow(
                remark,
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
                s -> s.toLowerCase().contains("Je désire être regroupé en chambre et bénéficier d'un prix dégressif"),
                s -> s.toLowerCase().contains("En chambre à 4 avec Ramoni Florence et Manon")
        ).anyMatch(p -> p.test(hostelRemark))) {
            return null;
        } else {
            return hostelRemark;
        }
    }

    private static String safeGet(String[] fromArray, int index) {
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

    public String computeReservationKey() {
        if (customRoomGroup == null) {
            return reservationIndex;
        } else {
            return customRoomGroup;
        }
    }

    public boolean computeIsCancelled() {
        return "ANNULÉ".equals(remark);
    }

    public boolean computeIsCancelledInBatch_1() {
        return "ANNULÉ NEW".equals(remark);
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
