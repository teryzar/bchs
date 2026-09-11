package org.ombr24.mb1.config;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ReferenceDataLoader {

    public ReferenceData load(AppConfig appConfig) {


        List<String> positions70 = readFileToList(appConfig.positions70());
        List<String> positions100 = readFileToList(appConfig.positions100());
        Map<String, String> commanderPositions = readFileToMap(appConfig.commanderPositions());

        return new ReferenceData(
                positions70,
                positions100,
                commanderPositions
        );
    }

    private Map<String, String> readFileToMap(String filePath) {

        Path path = Paths.get(System.getProperty("user.dir") + filePath);
        Map<String, String> mapFromFile = null;
        try {
            mapFromFile = Files.lines(path)
                    .collect(Collectors.toMap(k -> k.split(":")[0], v -> v.split(":")[1]));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return mapFromFile;
    }

    private List<String> readFileToList (String filePath) {
        Path path = Paths.get(System.getProperty("user.dir") + filePath);
        List<String> list;
        try {
            list = Files.readAllLines(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return list;

    }
}
