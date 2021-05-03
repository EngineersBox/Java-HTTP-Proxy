package com.engineersbox.httpproxy;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.configuration.ConfigModule;
import com.engineersbox.httpproxy.connection.ConnectionModule;
import com.engineersbox.httpproxy.formatting.FormattingModule;
import com.engineersbox.httpproxy.servlet.ProxyServlet;
import com.engineersbox.httpproxy.connection.threading.PoolManager;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Proxy initialiser and configurator
 */
public class Proxy {

    private static final Logger logger = LogManager.getLogger(Proxy.class);

    private static final String CONFIG_PATH_ARG_NAME = "config.path";
    private static final String CONFIG_FILE_PATH = System.getProperty(CONFIG_PATH_ARG_NAME);
    private static final String DEFAULT_CONFIG_PATH = "config.json";

    public static Config config;
    public static PoolManager poolManager;

    /**
     * Main proxy program, using {@link Injector} to instantiate {@link PoolManager} and {@link ProxyServlet} instances
     *
     * <br/><br/>
     *
     * @param args Array of {@link String} program arguments
     */
    public static void main(String[] args) {
        try {
            final String cfgPath = CONFIG_FILE_PATH != null ? CONFIG_FILE_PATH : DEFAULT_CONFIG_PATH;
            logger.info("Importing config from: " + cfgPath);
            config = Config.fromFile(cfgPath);
            config.logConfig();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        Injector injector = Guice.createInjector(
                new ConfigModule()
        );
        poolManager = injector.getInstance(PoolManager.class);

        injector = Guice.createInjector(
                new ConfigModule(),
                new FormattingModule(),
                new ConnectionModule()
        );
        final ProxyServlet proxyServlet = injector.getInstance(ProxyServlet.class);
        proxyServlet.init();
        proxyServlet.serve();
    }

}
