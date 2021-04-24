package com.engineersbox.httpproxy;

import com.engineersbox.httpproxy.configuration.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Proxy {

    protected static final Logger logger = LogManager.getLogger(Proxy.class);

    private static Config config;
    private static final String CONFIG_PATH_ARG_NAME = "config.path";
    private static final String DEFAULT_CONFIG_PATH = "config.json";

    public static void main(String[] args) {
        String configFilePath = System.getProperty(CONFIG_PATH_ARG_NAME);
        if (configFilePath == null) {
            configFilePath = DEFAULT_CONFIG_PATH;
        }
        logger.info(configFilePath);
        try {
            config = Config.fromFile(configFilePath);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
