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
        String foodConstraint = safeGet(fields, "J");
        String email = safeGet(fields, "K");
        String babyphone = safeGet(fields, "N");
        String hostelRemark = safeGet(fields, "Q");
        String age = safeGet(fields, "T");
        String customRoomGroup = safeGet(fields, "V");
        String roomNeighboring = safeGet(fields, "Z");
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
        if (Stream.<Predicate<String>>concat(
                Stream.of(s -> s == null),
                Stream.of("Merci pour une chambre double pour nous 2!",
                                "Nous souhaitons être tous les 5 dans la même chambre.",
                                "Les chiens sont-ils acceptés à l'hôtel ?",
                                "Marc-Antoine se joint à notre chambre avec William Bernard et Josselin Moosburgger",
                                "Je souhaite prendre la formule adulte en chambre quintuple",
                                "Je souhaite être logée en chambre triple avec mes amies déjà inscrites : Laura Suter et Letitia Frei. Vu et validée avec elles. Merci beaucoup!",
                                "Chambre individuel",
                                "Je suis en conversation avec d'autres personnes qui vont a l'évènement pour voir si cest possible de partager la chambre...",
                                "Nous souhaitons dans la mesure du possible être dans une chambre la moins chère possible, même si cela implique d'être séparés",
                                "Je souhaite être regroupée en chambre avec mes parents : Stéphan Bratschi",
                                "no",
                                "Regroupement avec MASUR LUU Christelle et LUU Jérémie : tous les 4 dans la même chambre, SVP",
                                "Mme Monnier fait famille d'accueil pour les deux enfants qui sont placés en foyer. Elle ne peut pas certifier leur présence à 100%, mais sans retour de notre part par mail, tout est en ordre.",
                                "Chambre individuelle à 178.-",
                                "En chambre individuelle",
                                "Je souhaite être regroupée en chambre avec mes parents : Stéphan Bratschi  Je ne veux pas une chambre pour moi seule.")
                        .map(String::toLowerCase)
                        .map(comparison -> s -> s.toLowerCase(Locale.ROOT).equals(comparison))
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
                return string.trim();
            }
        } else {
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
        return remark != null && Stream.of(
                "ANNULATION",
                "LISTE D'ATTENTE INDIVIDUELLE",
                "à confirmer"
        ).anyMatch(remark::contains);
    }

    public boolean computeIsCancelledInBatch_1() {
        return doesRemarkContains("ANNULE_NEW");
    }
    public boolean computeIsCancelledInBatch_2() {
        return doesRemarkContains("ANNULE_NEW2");
    }

    public boolean computeIsNewInBatch_1() {
        return doesRemarkContains("NEW");
    }

    public boolean computeIsNewInBatch_2() {
        return doesRemarkContains("NEW2");
    }

    private boolean doesRemarkContains(String... search) {
        return remark != null && Arrays.stream(search).anyMatch(remark::contains);
    }

    public String computeNeighborhoodKey(long index) {
        // no neighborhood for late reservations
        return roomNeighboring() == null || computeIsNewInBatch_1() || computeIsNewInBatch_2() ? "" + index : roomNeighboring();
    }
}
