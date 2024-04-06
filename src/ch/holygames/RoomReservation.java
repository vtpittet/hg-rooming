package ch.holygames;

import javax.swing.text.html.Option;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record RoomReservation(
        String reservationKey,
        int reservationId,
        PeopleReservation contactReservation,
        List<PeopleReservation> accompagningReservations,
        List<String> hostelRemarks
) implements Row {
    public static RoomReservation fromInputRow(int index, InputRow inputRow) {
        final int skipping93to99Index = index >= 93? index + 7 : index;
        return new RoomReservation(
                inputRow.computeReservationKey(),
                skipping93to99Index,
                PeopleReservation.fromInputRow(0, inputRow),
                new ArrayList<>(),
                computeHostelRemarks(inputRow)
        );
    }

    private static List<String> computeHostelRemarks(InputRow inputRow) {
        if (inputRow.computeIsCancelledInBatch_1()) {
            return List.of("***Annulé au 07.11.2023***");
        } else if (inputRow.computeIsCancelledInBatch_2()) {
            return List.of("***Annulé au 14.11.2023***");
        } else {
            return Stream.of(
                            inputRow.computeIsNewInBatch_1()? Optional.of("***nouvelle inscription au 07.11.2023***") : Optional.<String>empty(),
                            inputRow.computeIsNewInBatch_2()? Optional.of("***nouvelle inscription au 14.11.2023***") : Optional.<String>empty(),
                            Optional.ofNullable(inputRow.hostelRemark()),
                            Optional.ofNullable(inputRow.babyphone())
                    ).filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        }
    }

    public ComparisonKey getComparisonKey() {
        return contactReservation.getComparisonKey();
    }

    public RoomReservation replaceContactReservationIfCancelled() {
        if (contactReservation.isCancelled()) {
            List<PeopleReservation> activeReservations = accompagningReservations.stream()
                    .filter(peopleReservation -> !peopleReservation.isCancelled())
                    .toList();
            List<PeopleReservation> cancelledReservations =  accompagningReservations.stream()
                    .filter(PeopleReservation::isCancelled)
                    .toList();
            if (activeReservations.isEmpty()) {
                return this;
            } else {
                PeopleReservation newContactReservation = activeReservations.get(0);
                List<PeopleReservation> remainingActiveReservations = activeReservations.subList(1, activeReservations.size());
                List<PeopleReservation> newAccompagningReservations = new ArrayList<>();
                newAccompagningReservations.addAll(remainingActiveReservations);
                newAccompagningReservations.add(contactReservation);
                newAccompagningReservations.addAll(cancelledReservations);
                return new RoomReservation(
                        reservationKey,
                        reservationId,
                        newContactReservation,
                        newAccompagningReservations,
                        Stream.of(Optional.ofNullable(newContactReservation.computeHostelRemark())).filter(Optional::isPresent).map(Optional::get).toList()
                );
            }
        } else {
            return this;
        }
    }

    public List<OutputRow> toOutputRows() {
        return Stream.<OutputRow>concat(
                Stream.of(toOutputRow()),
                accompagningReservations.stream().map(peopleReservation -> peopleReservation.toOutputRow(this))
        ).toList();
    }

    private OutputRow toOutputRow() {
        return new OutputRow(
                "'" + reservationId,
                contactReservation.lastName(),
                contactReservation.firstName(),
                contactReservation.email(),
                contactReservation.getArrivalDateString(),
                contactReservation.getDepartureDateString(),
                accompagningReservations().stream().filter(PeopleReservation::isAdult).filter(PeopleReservation::isActive).count() + 1,
                accompagningReservations().stream().filter(PeopleReservation::isChild05).filter(PeopleReservation::isActive).count(),
                accompagningReservations().stream().filter(PeopleReservation::isChild69).filter(PeopleReservation::isActive).count(),
                accompagningReservations().stream().filter(PeopleReservation::isChild1015).filter(PeopleReservation::isActive).count(),
                Stream.concat(
                        hostelRemarks.stream(),
                        contactReservation.foodRequest() != null ?
                                Stream.of(contactReservation.getFoodRequestString()) :
                                Stream.empty()
                ).collect(Collectors.joining("  ----  "))

        );
    }

    public int getCurrentReservationIndex() {
        return accompagningReservations().size();
    }
}
