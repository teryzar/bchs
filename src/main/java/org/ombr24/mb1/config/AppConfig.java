package org.ombr24.mb1.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AppConfig {

    private final String dataDir;
    private final String outputDir;
    private final String configDir;

    public AppConfig(String dataDir, String outputDir, String configDir) {
        this.dataDir = dataDir;
        this.outputDir = outputDir;
        this.configDir = configDir;
    }

    public String inputExcel() {
        return resolvePath(dataDir, "1.xlsx");
    }

    public String positions70() {
        return resolvePath(configDir, "positions70.txt");
    }

    public String positions100() {
        return resolvePath(configDir, "positions100.txt");
    }

    public String divisionReport(int day) {
        return resolvePath(outputDir, day + "травня.txt");
    }

    public String divisionJsonReport(int day) {
        return resolvePath(outputDir, "json");
    }

    public String commanderPositions() {
        return resolvePath(configDir, "commanders.txt");
    }

    public String getDataDir() {
        return dataDir;
    }

    /**
     * Правильно об'єднує базову директорію з відносним шляхом,
     * обробляючи як абсолютні, так і відносні шляхи
     */
    private String resolvePath(String baseDir, String relativePath) {
        Path base = Paths.get(baseDir);
        Path resolved = base.resolve(relativePath);
        return resolved.toString();
    }
}
