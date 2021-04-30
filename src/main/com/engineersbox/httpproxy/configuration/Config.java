package com.engineersbox.httpproxy.configuration;

import com.engineersbox.httpproxy.configuration.domain.Target;
import com.engineersbox.httpproxy.configuration.domain.policies.Policies;
import com.engineersbox.httpproxy.configuration.domain.policies.RuleSet;
import com.engineersbox.httpproxy.configuration.domain.servlet.Servlet;
import com.google.gson.Gson;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Config {

    private final Logger logger = LogManager.getLogger(Config.class);

    public final Policies policies;
    public final Servlet servlet;
    public final Target target;

    public Config(final Policies policies, final Servlet servlet, final Target target) {
        this.policies = policies;
        this.servlet = servlet;
        this.target = target;
    }

    public static Config fromFile(final String path) throws IOException {
        final BufferedReader reader = Files.newBufferedReader(Paths.get(path));
        final Gson gson = new Gson();
        final Config cfg =  gson.fromJson(reader, Config.class);
        return new Config(cfg.policies, cfg.servlet, cfg.target);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final Config config = (Config) o;

        return new EqualsBuilder()
                .append(policies, config.policies)
                .append(servlet, config.servlet)
                .append(target, config.target)
                .isEquals();
    }

    public void logConfig() {
        logger.debug(String.format(
            "[CONFIG: Servlet > Threading] Pool sizes: [ACCEPTOR: %d] [HANDLER: %d]",
            this.servlet.threading.acceptorPoolSize,
            this.servlet.threading.handlerPoolSize
        ));
        logger.debug(String.format(
            "[CONFIG: Servlet > Threading] Scheduling Policy: %s",
            this.servlet.threading.schedulingPolicy
        ));
        logger.debug(String.format(
            "[CONFIG: Servlet > Connections] Queue Sizes: [ACCEPTOR: %d] [HANDLER: %d]",
            this.servlet.connections.acceptorQueueSize,
            this.servlet.connections.handlerQueueSize
        ));
        logger.debug(String.format(
            "[CONFIG: Servlet > Connections] Connection Dropping: [AFTER DURATION (ms): %d] [FAILED DNS RESOLUTION: %s]",
            this.servlet.connections.dropAfter,
            this.servlet.connections.dropOnFailedDNSLookup
        ));
        logger.debug(String.format(
            "[CONFIG: Servlet > Connections] Buffer Size: [SOCKET READER: %d]",
            this.servlet.connections.readerBufferSize
        ));
        logger.debug(String.format(
            "[CONFIG: Servlet > Messages] Message Behaviour [MAX BODY SIZE: %d] [DROP ON MALFORMED: %s]",
            this.servlet.messages.maxBodySize,
            this.servlet.messages.dropOnMalformed
        ));
        logger.debug(String.format(
            "[CONFIG: Servlet > Binding] Local Host [HOST: %s] [PORT: %d]",
            this.servlet.binding.host,
            this.servlet.binding.port
        ));
        logger.debug(String.format(
            "[CONFIG: Servlet] Cache [SIZE: %d]",
            this.servlet.cacheSize
        ));
        logger.debug(String.format(
            "[CONFIG: Policy > Enforcement] Behaviour: [IP: %s] [URL: %s]",
            this.policies.enforcement.whitelistBehaviour.ip,
                this.policies.enforcement.whitelistBehaviour.url
        ));
        logger.debug(String.format(
            "[CONFIG: Policy > Rulesets] Imported %d rule sets",
            this.policies.rulesets.size()
        ));
        for (int i = 0; i < this.policies.rulesets.size(); i++) {
            final RuleSet ruleSet = this.policies.rulesets.get(i);
            logger.debug(String.format(
                "[CONFIG: Policy > Rulesets] Ruleset %d: [TYPE: %s] [WILDCARD: %s] [PATTERN: %s]",
                i,
                ruleSet.type,
                ruleSet.isWildcard,
                ruleSet.pattern
            ));
        }
        logger.debug(String.format(
            "[CONFIG: Target] Server Host: [HOST: %s] [PORT: %d]",
            this.target.host,
            this.target.port
        ));
    }
}
