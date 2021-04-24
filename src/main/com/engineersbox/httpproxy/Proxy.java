package com.engineersbox.httpproxy;

import com.engineersbox.httpproxy.configuration.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Proxy {

    protected static final Logger logger = LogManager.getLogger();

    private static Config config;

    public static void main(String[] args) {
        try {
            config = Config.fromFile("resources/config.json");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
