package com.engineersbox.httpproxy.configuration;

import com.engineersbox.httpproxy.configuration.domain.Target;
import com.engineersbox.httpproxy.configuration.domain.policies.Policies;
import com.engineersbox.httpproxy.configuration.domain.policies.RuleSet;
import com.engineersbox.httpproxy.configuration.domain.policies.Replacement;
import com.engineersbox.httpproxy.configuration.domain.servlet.Servlet;
import com.google.gson.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

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
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(
                        Pattern.class,
                        (JsonDeserializer<Pattern>) (json, _t, _c) -> Pattern.compile(json.getAsString())
                ).create();
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
        logger.info("---- [CONFIG] ----");
        logger.info(String.format(
            "[CONFIG: Servlet > Threading] Pool sizes: [ACCEPTOR: %d] [HANDLER: %d] [CLASS MATCHER %d]",
            this.servlet.threading.acceptorPoolSize,
            this.servlet.threading.handlerPoolSize,
            this.servlet.threading.classMatcherPoolSize
        ));
        logger.info(String.format(
            "[CONFIG: Servlet > Threading] Scheduling Policy: %s",
            this.servlet.threading.schedulingPolicy
        ));
        logger.info(String.format(
            "[CONFIG: Servlet > Connections] Queue Sizes: [ACCEPTOR: %d] [HANDLER: %d]",
            this.servlet.connections.acceptorQueueSize,
            this.servlet.connections.handlerQueueSize
        ));
        logger.info(String.format(
            "[CONFIG: Servlet > Connections] Buffer Size: [SEND: %d] [RECEIVE: %d]",
            this.servlet.connections.writeBufferSize,
            this.servlet.connections.readerBufferSize
        ));
        logger.info(String.format(
            "[CONFIG: Servlet > Connections] Drop After: %d",
            this.servlet.connections.dropAfter
        ));
        logger.info(String.format(
            "[CONFIG: Servlet > Messages] Max Body Size: %d",
            this.servlet.messages.maxBodySize
        ));
        logger.info(String.format(
            "[CONFIG: Servlet > Binding] Local Host [HOST: %s] [PORT: %d]",
            this.servlet.binding.host,
            this.servlet.binding.port
        ));
        logger.info(String.format(
            "[CONFIG: Policy > Enforcement] Behaviour: [IP: %s] [URL: %s]",
            this.policies.enforcement.whitelistBehaviour.ip,
            this.policies.enforcement.whitelistBehaviour.url
        ));
        logger.info(String.format(
            "[CONFIG: Policy > Enforcement] Allow Redirects: %s",
            this.policies.enforcement.allowRedirects
        ));
        logger.info(String.format(
            "[CONFIG: Policy > Rulesets] Imported %d rule set%s",
            this.policies.rulesets.size(),
            this.policies.rulesets.size() > 1 ? "s" : ""
        ));
        for (int i = 0; i < this.policies.rulesets.size(); i++) {
            final RuleSet ruleSet = this.policies.rulesets.get(i);
            logger.info(String.format(
                "[CONFIG: Policy > Rulesets] Ruleset %d: [TYPE: %s] [WILDCARD: %s] [PATTERN: %s]",
                i,
                ruleSet.type,
                ruleSet.isWildcard,
                ruleSet.pattern
            ));
        }
        logger.info(String.format(
            "[CONFIG: Policy > Text Replacements] Imported %d text replacement%s",
            this.policies.textReplacements.size(),
            this.policies.textReplacements.size() > 1 ? "s" : ""
        ));
        for (int i = 0; i < this.policies.textReplacements.size(); i++) {
            final Replacement replacement = this.policies.textReplacements.get(i);
            logger.info(String.format(
                    "[CONFIG: Policy > Text Replacements] Text replacement %d: [FROM: /%s/] [TO: %s]",
                    i,
                    replacement.from.pattern(),
                    replacement.to
            ));
        }
        logger.info(String.format(
            "[CONFIG: Policy > Link Replacements] Imported %d link replacement%s",
            this.policies.linkReplacements.size(),
            this.policies.linkReplacements.size() > 1 ? "s" : ""
        ));
        for (int i = 0; i < this.policies.linkReplacements.size(); i++) {
            final Replacement replacement = this.policies.linkReplacements.get(i);
            logger.info(String.format(
                    "[CONFIG: Policy > Link Replacements] Link replacement %d: [FROM: /%s/] [TO: %s]",
                    i,
                    replacement.from.pattern(),
                    replacement.to
            ));
        }
        logger.info(String.format(
            "[CONFIG: Target] Server Host: [HOST: %s] [PORT: %d]",
            this.target.host,
            this.target.port
        ));
        logger.info("---- [CONFIG] ----");
    }
}
