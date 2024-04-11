package ch.holygames;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("Hotel-rooming");

        ArrayList<List<InputLine>> inputLines = Files.list(Path.of("input"))
                .map(Path::toFile)
                .map(file -> {
                    try {
                        return Optional.of(new FileReader(file));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<FileReader>empty();
                    }
                })
                .flatMap(Optional::stream)
                .map(BufferedReader::new)
                .map(lineReader -> {
                    try {
                        // skip headers
                        lineReader.readLine();
                        String line = lineReader.readLine();
                        ArrayList<String> lines = new ArrayList<>();
                        while (line != null) {
                            lines.add(line);
                            line = lineReader.readLine();
                        }
                        return lines;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return new ArrayList<String>();
                    }
                }).collect(
                        ArrayList::new,
                        Main::accumulateInputs,
                        Main::combine
                );
        List<InputRow> inputRows = inputLines.stream().flatMap(List::stream)
                .map(InputRow::ofInputLine)
                .filter(inputRow -> !inputRow.computeIsCancelled())
                .toList();
        List<InputRow> orderedInputRows = inputRows.stream()
                .collect(
                        HashMap::new,
                        Main::accumulateOrder,
                        Main::combine)
                .values().stream().flatMap(List::stream).sorted((left, right) -> (int) (left.order() - right.order()))
                .map(OrderedInputRow::inputRow)
                .toList();
        List<RoomReservation> sortedRoomReservations = orderedInputRows.stream()
                .<Map<String, RoomReservation>>collect(
                        HashMap::new,
                        Main::accumulateReservation,
                        Main::combine)
                .values().stream().sorted(Comparator.comparingInt(RoomReservation::reservationId))
                .toList();
        List<RoomReservation> roomReservations = sortedRoomReservations.stream()
                .map(RoomReservation::replaceContactReservationIfCancelled).toList();

        roomReservations.stream()
                .flatMap(reservation -> Stream.of(reservation.contactReservation()))
                .collect(HashMap::new,
                        Main::accumulateDuplicates,
                        Main::combine);

        String processedData = roomReservations.stream()
                .map(RoomReservation::toOutputRows).flatMap(List::stream)
                .map(OutputRow::printRow)
                .collect(Collectors.joining("\n"));
        System.out.println(processedData);

        FileWriter fileWriter = new FileWriter("output.csv");
        fileWriter.write(processedData);
        fileWriter.flush();
    }

    private static void accumulateInputs(List<List<InputLine>> inputLines, List<String> lines) {
        final int offset;
        if (inputLines.size() == 0) {
            offset = 0;
        } else {
            offset = inputLines.get(inputLines.size() - 1).size();
        }
        inputLines.add(lines.stream().map(line -> new InputLine(offset, line)).toList());
    }

    private static void accumulateOrder(Map<String, List<OrderedInputRow>> inputGroupings, InputRow inputRow) {
        long index = inputGroupings.values().stream().flatMap(List::stream).count();

        String neighborhoodKey = inputRow.computeNeighborhoodKey(index);
        List<OrderedInputRow> existingNeighbor = inputGroupings.getOrDefault(neighborhoodKey, new ArrayList<>());
        final long order;
        if (inputRow.babyphone() == null) {
            if (existingNeighbor.isEmpty()) {
                order = index;
            } else {
                order = existingNeighbor.get(0).order();
            }
        } else {
            if (existingNeighbor.isEmpty()) {
                order = -1000;
            } else {
                order = -Math.abs(existingNeighbor.get(0).order());
                existingNeighbor = existingNeighbor.stream()
                        .map(orderedInputRow -> orderedInputRow.withOrder(order))
                        .collect(ArrayList::new, (l, n) -> l.add(n), (l, r) -> l.addAll(r));
            }
        }
        existingNeighbor.add(new OrderedInputRow(order, inputRow));
        inputGroupings.put(neighborhoodKey, existingNeighbor);
    }

    private static void accumulateReservation(Map<String, RoomReservation> roomReservations, InputRow inputRow) {
        RoomReservation existingReservation = roomReservations.get(inputRow.computeReservationKey());
        if (existingReservation == null) {
            RoomReservation roomReservation = RoomReservation.fromInputRow(roomReservations.size() + 1, inputRow);
            roomReservations.put(roomReservation.reservationKey(), roomReservation);
        } else {
            PeopleReservation peopleReservation = PeopleReservation.fromInputRow(existingReservation.accompagningReservations().size() + 1, inputRow);
            existingReservation.accompagningReservations().add(peopleReservation);
        }
    }

    private static void accumulateDuplicates(Map<Row.ComparisonKey, PeopleReservation> peopleReservations, PeopleReservation peopleReservation) {
        Row.ComparisonKey comparisonKey = peopleReservation.getComparisonKey();
        if (peopleReservations.containsKey(comparisonKey)) {
            throw new IllegalStateException("duplicates in generated peopleReservations: " + peopleReservations.get(comparisonKey) + " and " + peopleReservation);
        } else {
            peopleReservations.put(comparisonKey, peopleReservation);
        }
    }

    private static <T, U> void combine(T first, U second) {
        throw new UnsupportedOperationException("Nope, I'm not implementing this. Hopefully combine doesn't get called by collect as no parallel stream is used");
    }
}
