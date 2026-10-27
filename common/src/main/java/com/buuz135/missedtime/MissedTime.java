package com.buuz135.missedtime;

import com.buuz135.missedtime.config.MissedTimeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MissedTime {
    public static final String MOD_ID = "missedtime";
    public static final String MOD_NAME = "MissedTime";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private MissedTime() {
    }

    public static void init() {
        MissedTimeConfig.load();
        LOGGER.info("Loaded {}", MOD_NAME);
    }
}
