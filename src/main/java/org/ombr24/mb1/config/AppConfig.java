package org.ombr24.mb1.config;

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
        return dataDir + "/1.xlsx";
    }

    public String positions70() {
        return configDir + "/positions70.txt";
    }

    public String positions100() {
        return configDir + "/positions100.txt";
    }

    public String divisionReport(int day) {
        return outputDir + "/" + day + "травня.txt";
    }

    public String divisionJsonReport(int day) {
        return outputDir + "/json/";
    }

    public String commanderPositions() {
        return configDir + "/commanders.txt";
    }

    public String getDataDir() {
        return dataDir;
    }
}
