package com.engineersbox.httpproxy.resolver;

import com.engineersbox.httpproxy.formatting.http.request.HTTPRequestStartLine;
import com.engineersbox.httpproxy.resolver.annotation.Path;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A path formatter, supporting key based indexing into a path on a per component basis
 */
public class MatchableParameterisedPath {

    private static final String PATH_COMPONENT_DELIMITER = "/";
    private static final Pattern RETRIEVABLE_PARAM_REGEX = Pattern.compile("\\{\\w*\\}");
    private static final String REMOVE_ALL_BRACES_REGEX = "\\{|\\}";

    private Pattern matchablePath;
    private String path = "";
    private List<String> splitPath;
    private final Map<String, Integer> retrievableParams = new HashMap<>();

    public MatchableParameterisedPath(final String path) {
        addToPath(path);
    }

    /**
     * Formats the provided {@code newPath} to ensure it retains formatting requirements around component delimitation.
     *
     * @param newPath HTTP target path as a {@link String}
     * @return Formatted path
     */
    private String formatPath(final String newPath) {
        String currentPath = path;
        if (StringUtils.endsWith(path, PATH_COMPONENT_DELIMITER) && StringUtils.startsWith(newPath, PATH_COMPONENT_DELIMITER)) {
            currentPath = StringUtils.removeEnd(currentPath, PATH_COMPONENT_DELIMITER);
        }
        return currentPath + newPath;
    }

    /**
     * Processes the {@link MatchableParameterisedPath#splitPath}, creating a mapping between matchable path components
     * of the format {@code {<KEY>}} and the index in the path
     */
    private void storeRetrievableParams() {
        if (this.splitPath == null || this.splitPath.size() < 1) {
            return;
        }
        for (int i = 0; i < this.splitPath.size(); i++) {
            final String component = this.splitPath.get(i);
            if (RETRIEVABLE_PARAM_REGEX.matcher(component).matches()) {
                this.retrievableParams.put(component.replaceAll(REMOVE_ALL_BRACES_REGEX, ""), i);
            }
        }
    }

    /**
     * Adds a path portion to the end of the current path, retaining formatting.
     *
     * @param morePath Path portion to add
     */
    public void addToPath(final String morePath) {
        this.path = formatPath(morePath);
        this.splitPath = Arrays.asList(this.path.split(PATH_COMPONENT_DELIMITER));
        this.matchablePath = Pattern.compile(
                this.splitPath.stream()
                        .map(p -> RETRIEVABLE_PARAM_REGEX.matcher(p).matches() ? "\\w*" : p)
                        .collect(Collectors.joining("/"))
        );
        storeRetrievableParams();
    }

    /**
     * Retrieves the path component specified by the key used in a {@link Path} annotation. If the key has no mapping match
     * or the index is outside of the retrievable params list range, it will return {@code null}
     *
     * @param paramName A {@link String} key to retrieve a path component for
     * @param target A HTTP target from an instance of {@link HTTPRequestStartLine}
     * @return Path component matchting the {@code paramName} key, or {@code null if it does not exist}
     */
    public String getPathParam(final String paramName, final String target) {
        final Integer idx = this.retrievableParams.get(paramName);
        if (idx == null) {
            return null;
        }
        if (idx >= this.splitPath.size()) {
            return null;
        }
        return target.split(PATH_COMPONENT_DELIMITER)[idx];
    }

    /**
     * Checks if the stored path matches against a given {@code target} path
     *
     * @param target Path to match with
     * @return {@code true} if it matches {@code false} otherwise
     */
    public boolean matchPathToTarget(final String target) {
        return this.matchablePath.matcher(target).find();
    }

}
