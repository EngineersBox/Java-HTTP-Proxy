package com.engineersbox.httpproxy.configuration;

import com.engineersbox.httpproxy.configuration.domain.policies.Policies;
import com.engineersbox.httpproxy.configuration.domain.servlet.Servlet;
import com.google.gson.Gson;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Config {

    public final Policies policies;
    public final Servlet servlet;
    public final String targetBaseUrl;

    public Config(final Policies policies, final Servlet servlet, final String targetBaseUrl) {
        this.policies = policies;
        this.servlet = servlet;
        this.targetBaseUrl = targetBaseUrl;
    }

    public static Config fromFile(final String path) throws IOException {
        final BufferedReader reader = Files.newBufferedReader(Paths.get(path));
        final Gson gson = new Gson();
        return gson.fromJson(reader, Config.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Config config = (Config) o;

        return new EqualsBuilder()
                .append(policies, config.policies)
                .append(servlet, config.servlet)
                .isEquals();
    }
}
