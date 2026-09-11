package org.ombr24.mb1;

import org.ombr24.mb1.config.*;
import org.ombr24.mb1.utils.XlsProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Application {
    public static void main(String[] args) throws IOException {
        AppConfig config = new AppConfigLoader().load();
        ReferenceData refData = new ReferenceDataLoader().load(config);

        int startDate = readDate();
        int endDate = readDate();
        for (int dayOfMonth = startDate; dayOfMonth <= endDate; dayOfMonth++) {
            new XlsProcessor(config, refData).process(dayOfMonth);

        }

    }

    private static int readDate() throws IOException {
        System.out.println(System.getProperty("user.dir"));
        System.out.println("За яке число");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        return Integer.parseInt(reader.readLine());
    }
}
