package com;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

public class DecodeQuery {

    private static final String FILE_OUT = "src/main/java/resources/output.txt";
    private static final String FILE_IN = "src/main/java/resources/input.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("d.MM.uuuu");

    public void decodeQuery() throws IOException {
        List<String[]> waitingTimeList = new ArrayList<>();
        List<String[]> queryList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(FILE_IN));
        while (reader.ready()) {
            String currentLine = reader.readLine();
            if (currentLine.startsWith("C")) {
                waitingTimeList.add(currentLine.substring(2).split(" "));
            } else if (currentLine.startsWith("D")) {
                queryList.add(currentLine.substring(2).split("[-\\s]"));
            }
        }
        List<OptionalDouble> outputList = decodeQueryHelper(waitingTimeList, queryList);
        BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_OUT));
        for (OptionalDouble optionalDouble : outputList) {
            if (optionalDouble.isPresent()) {
                writer.append(String.valueOf((int) optionalDouble.getAsDouble()));
                writer.append("\n");
            } else {
                writer.append("-\n");
            }
        }
    }

    private List<OptionalDouble> decodeQueryHelper(List<String[]> waitingTimeList, List<String[]> queryList) {
        return queryList.stream()
                .map(query -> waitingTimeList.stream()
                        // filter by response type
                        .filter(waitingTime -> waitingTime[2].equals(query[2]))
                        // filter by date
                        .filter(waitingTime -> LocalDate.parse(waitingTime[3], FORMATTER)
                                .isAfter(LocalDate.parse(query[3], FORMATTER)))
                        .filter(waitingTime -> LocalDate.parse(waitingTime[3], FORMATTER)
                                .isBefore(LocalDate.parse((query.length > 4 ? query[4] : query[3]), FORMATTER)))
                        // filter by service
                        .filter(waitingTime -> query[0].equals("*") || waitingTime[0].startsWith(query[0]))
                        // filter by question
                        .filter(waitingTime -> query[1].equals("*") || waitingTime[1].startsWith(query[1]))
                        .mapToInt(waitingTime -> Integer.parseInt(waitingTime[4]))
                        .average())
                .collect(Collectors.toList());
    }
}
