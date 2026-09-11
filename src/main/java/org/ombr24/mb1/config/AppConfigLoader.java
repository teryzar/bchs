package org.ombr24.mb1.config;

import java.io.InputStream;
import java.util.Properties;

public class AppConfigLoader {

    public AppConfig load() {
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (is == null) {
                throw new IllegalStateException("application.properties not found");
            }

            Properties p = new Properties();
            p.load(is);

            return new AppConfig(
                    p.getProperty("data.dir"),
                    p.getProperty("output.dir"),
                    p.getProperty("config.dir")
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load config", e);
        }
    }
}
