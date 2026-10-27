package com.buuz135.missedtime.config;

import com.buuz135.missedtime.MissedTime;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class MissedTimeConfig {
    private static final Path PATH = Path.of("config", "missedtime.properties");
    private static int acceleratedTicksPerOfflineMinute = 20;
    private static int acceleratedTicksPerSecond = 60;
    private static int minimumOfflineMinutes = 1;
    private static int maximumOfflineHours = 168;
    private static int rampUpSeconds = 10;
    private static int spectatorRadius = 32;
    private static boolean recoveryParticipantEnabled = true;

    private MissedTimeConfig() {
    }

    public static void load() {
        Properties properties = defaults();
        try {
            Files.createDirectories(PATH.getParent());
            if (Files.exists(PATH)) {
                Properties loaded = new Properties();
                try (Reader reader = Files.newBufferedReader(PATH)) {
                    loaded.load(reader);
                }
                properties.putAll(loaded);
                if (!loaded.containsKey("acceleratedTicksPerSecond") && loaded.containsKey("maximumAcceleratedTicksPerSecond")) {
                    properties.setProperty("acceleratedTicksPerSecond", loaded.getProperty("maximumAcceleratedTicksPerSecond"));
                }
            }
            properties.remove("maximumAcceleratedTicksPerSecond");
            properties.remove("rampDownSeconds");
            acceleratedTicksPerOfflineMinute = positive(properties, "acceleratedTicksPerOfflineMinute", 20);
            acceleratedTicksPerSecond = positive(properties, "acceleratedTicksPerSecond", 60);
            minimumOfflineMinutes = nonNegative(properties, "minimumOfflineMinutes", 1);
            maximumOfflineHours = positive(properties, "maximumOfflineHours", 168);
            rampUpSeconds = positive(properties, "rampUpSeconds", 10);
            spectatorRadius = positive(properties, "spectatorRadius", 32);
            recoveryParticipantEnabled = Boolean.parseBoolean(properties.getProperty("recoveryParticipantEnabled", "true"));
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                properties.store(writer, "MissedTime configuration");
            }
        } catch (IOException | NumberFormatException exception) {
            MissedTime.LOGGER.error("Unable to load {}", PATH, exception);
        }
    }

    private static Properties defaults() {
        Properties properties = new Properties();
        properties.setProperty("acceleratedTicksPerOfflineMinute", "20");
        properties.setProperty("acceleratedTicksPerSecond", "60");
        properties.setProperty("minimumOfflineMinutes", "1");
        properties.setProperty("maximumOfflineHours", "168");
        properties.setProperty("rampUpSeconds", "10");
        properties.setProperty("spectatorRadius", "32");
        properties.setProperty("recoveryParticipantEnabled", "true");
        return properties;
    }

    private static int positive(Properties properties, String key, int fallback) {
        return Math.max(1, Integer.parseInt(properties.getProperty(key, Integer.toString(fallback))));
    }

    private static int nonNegative(Properties properties, String key, int fallback) {
        return Math.max(0, Integer.parseInt(properties.getProperty(key, Integer.toString(fallback))));
    }

    public static int acceleratedTicksPerOfflineMinute() {
        return acceleratedTicksPerOfflineMinute;
    }

    public static int acceleratedTicksPerSecond() {
        return acceleratedTicksPerSecond;
    }

    public static int minimumOfflineMinutes() {
        return minimumOfflineMinutes;
    }

    public static int maximumOfflineHours() {
        return maximumOfflineHours;
    }

    public static int rampUpSeconds() {
        return rampUpSeconds;
    }

    public static int spectatorRadius() {
        return spectatorRadius;
    }

    public static boolean recoveryParticipantEnabled() {
        return recoveryParticipantEnabled;
    }
}
