package com.stardata.observ.javaagent.instrumentation.common;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class LoggerHelper {

    private static final Logger LOGGER = Logger.getLogger(LoggerHelper.class.getName());
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    public static void info(String message) {
        LOGGER.info(String.format("%d: %s", COUNTER.incrementAndGet(), message));
    }

}
