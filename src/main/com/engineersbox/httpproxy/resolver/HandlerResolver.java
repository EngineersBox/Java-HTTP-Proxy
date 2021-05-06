package com.engineersbox.httpproxy.resolver;

import com.engineersbox.httpproxy.Proxy;
import com.engineersbox.httpproxy.configuration.ConfigModule;
import com.engineersbox.httpproxy.connection.ConnectionModule;
import com.engineersbox.httpproxy.exceptions.resolver.ResourceEndpointMatcherException;
import com.engineersbox.httpproxy.formatting.FormattingModule;
import com.engineersbox.httpproxy.formatting.http.common.*;
import com.engineersbox.httpproxy.formatting.http.response.HTTPResponseStartLine;
import com.engineersbox.httpproxy.formatting.http.response.StandardResponses;
import com.engineersbox.httpproxy.resolver.annotation.MediaType;
import com.engineersbox.httpproxy.resolver.annotation.ExceptionHandler;
import com.engineersbox.httpproxy.resolver.annotation.Handler;
import com.engineersbox.httpproxy.resolver.annotation.HandlerType;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Resource handler for {@link MediaType} driven resource delimitation
 */
public class HandlerResolver implements ResourceResolver {

    private final Logger logger = LogManager.getLogger(HandlerResolver.class);

    private static final Set<Module> injectables = ImmutableSet.of(
            new ConfigModule(),
            new FormattingModule(),
            new ConnectionModule()
    );

    final Set<URL> contentResourcesURLs;
    final Set<URL> exceptionResourcesURLs;

    public HandlerResolver() {
        final Set<Class<?>> contentResources = resolveResourcesWithType(HandlerType.CONTENT);
        logger.trace("Retrieved " + contentResources.size() + " content resources");
        Set<Class<?>> exceptionResources = resolveResourcesWithType(HandlerType.EXCEPTION);
        logger.trace("Retrieved " + exceptionResources.size() + " exception resources");
        this.contentResourcesURLs = findResourceURLs(contentResources);
        this.exceptionResourcesURLs = findResourceURLs(exceptionResources);
    }

    /**
     * Find classes annotated with {@link Handler} for a given {@link HandlerType} within the
     * project package {@code com.engineersbox.httpproxy.*}
     *
     * <br/><br/>
     *
     * @param type {@link HandlerType} to find classes for
     * @return A {@link Set} of classes matching the given {@link HandlerType}. This is an empty set if none are found
     */
    private Set<Class<?>> resolveResourcesWithType(final HandlerType type) {
        final Reflections reflections = new Reflections(Proxy.class.getPackage().getName());
        return reflections.getTypesAnnotatedWith(Handler.class).stream()
                .filter(h -> h.getAnnotation(Handler.class).value() == type)
                .collect(Collectors.toSet());
    }

    /**
     * Resolves the class path URLs ({@link URL}) for a given {@link Set} of resource classes
     *
     * <br/><br/>
     *
     * @param resources {@link Set} of resource classes
     * @return A {@link Set} of {@link URL} for the class paths each resource
     */
    private Set<URL> findResourceURLs(final Set<Class<?>> resources) {
        return resources.stream()
                .map(r -> ClasspathHelper.forClass(r, r.getClassLoader()))
                .collect(Collectors.toSet());
    }

    /**
     * Creates an instantiation of a given resource via an {@link Injector} using modules within {@link HandlerResolver#injectables}
     *
     * <br/><br/>
     *
     * @param resource Class of resource
     * @param <T> Type of resource
     * @return Instance of {@code T}
     */
    public <T>  T instantiateResource(final Class<T> resource) {
        final Injector injector = Guice.createInjector(injectables);
        logger.trace("Instantiated injector with modules: " + injectables.stream().map(i -> i.getClass().getName()));
        logger.trace("Retrieved instance of " + resource.getName() + " from injector");
        return injector.getInstance(resource);
    }

    /**
     * Retrieves the {@code Content-Type} header from a given {@link Map} of valid <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2" target="_top">RFC 2616 Section 4.2</a>
     * headers. If the header does not exist, this throws {@link ResourceEndpointMatcherException}
     *
     * <br/> <br/>
     *
     * @param headers {@link Map} of valid <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2" target="_top">RFC 2616 Section 4.2</a> headers
     * @return {@link String} containing the media type
     * @throws ResourceEndpointMatcherException If there was no {@code Content-Type} header in the {@link Map}
     */
    private String getContentTypeHeader(final Map<String, String> headers) throws ResourceEndpointMatcherException {
        final Optional<Map.Entry<String, String>> header = headers.entrySet()
                .stream()
                .filter(e -> Pattern.compile(HTTPSymbols.CONTENT_TYPE_HEADER_REGEX).matcher(e.getKey()).find())
                .findFirst();
        if (!header.isPresent()) {
            throw new ResourceEndpointMatcherException("Could not find " + HTTPSymbols.CONTENT_TYPE_HEADER_REGEX + " when trying to resolve resource endpoint");
        }
        final String contentTypeHeader = header.get().getValue();
        String typeValue = contentTypeHeader;
        if (contentTypeHeader.contains(HTTPSymbols.CONTENT_TYPE_CHARSET_KEY)) {
            final String contentTypeSplit = contentTypeHeader.split(HTTPSymbols.CONTENT_TYPE_CHARSET_KEY)[0];
            typeValue = contentTypeSplit.replace(HTTPSymbols.HEADER_VALUE_LIST_DELIMITER, "");
        }
        return typeValue;
    }

    /**
     * Matches a given header against a {@link List} of valid Java regexes, returning the first match. If no matches are
     * found an empty {@link Optional} is returned
     *
     * <br/><br/>
     *
     * @param contentTypeHeader A valid <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2" target="_top">RFC 2616 Section 4.2</a> header
     * @param patterns {@link List} of valid Java regexes
     * @return {@link Optional} containing the matching pattern or an empty {@link Optional} if there was no match
     */
    private Optional<String> matchHeaderToPatterns(final String contentTypeHeader, final List<String> patterns) {
        return patterns.stream()
                .filter(p -> Pattern.compile(p).matcher(contentTypeHeader).matches())
                .findFirst();
    }

    /**
     * Finds a method from a {@link Set} of resource {@link URL}s based on a {@link Predicate} to match against method signatures
     * of the resources.
     *
     * <br/><br/>
     *
     * The methods beings matched against are methods within the {@link Set} of resource {@link URL}s that are annotated
     * with the {@link MediaType} annotation.
     *
     * <br/><br/>
     *
     * If no matching methods are found {@link ResourceEndpointMatcherException} is thrown
     *
     * <br/><br/>
     *
     * @param urls A {@link Set} of resource {@link URL}s
     * @param predicate A {@link Predicate} to match against method signatures
     * @return A matching method
     * @throws ResourceEndpointMatcherException No methods matching the {@link Predicate} were found
     */
    private Method findMethod(final Set<URL> urls, final Predicate<Method> predicate) throws ResourceEndpointMatcherException {
        final Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(urls)
                .setScanners(new MethodAnnotationsScanner())
        );
        Set<Method> resources = reflections.getMethodsAnnotatedWith(MediaType.class);
        final Optional<Method> potentialMethod = resources.stream().filter(predicate).findFirst();
        if (!potentialMethod.isPresent()) {
            throw new ResourceEndpointMatcherException("Could not find matching method");
        }
        logger.trace("Found matching method for resource handling: " + potentialMethod.get().getName());
        return potentialMethod.get();
    }

    /**
     * Matches the given {@link HTTPMessage} to resources based on the {@link MediaType} contained in the
     * {@code Content-Type} header.
     *
     * <br/><br/>
     *
     * @param message {@link HTTPMessage} to handle within a given resource
     * @return {@link HTTPMessage} handled by a matching {@link MediaType} method
     */
    @SuppressWarnings("unchecked")
    @Override
    public HTTPMessage<HTTPResponseStartLine> match(final HTTPMessage<HTTPResponseStartLine> message) {
        try {
            final String contentTypeHeader = getContentTypeHeader(message.headers);
            final Method method = findMethod(this.contentResourcesURLs, m -> {
                if (!m.getReturnType().isAssignableFrom(HTTPMessage.class)) {
                    return false;
                }
                final List<String> patterns = Arrays.asList(m.getAnnotation(MediaType.class).value());
                return matchHeaderToPatterns(contentTypeHeader.trim(), patterns).isPresent();
            });
            logger.trace(String.format(
                    "Invoking method [%s] for message: %s",
                    method.getName(),
                    message
            ));
            return (HTTPMessage<HTTPResponseStartLine>) method.invoke(
                    instantiateResource(method.getDeclaringClass()),
                    message
            );
        } catch (final ResourceEndpointMatcherException e) {
            logger.trace(e.getMessage());
            return message;
        }  catch (final InvocationTargetException e) {
            try {
                return handleResourceException(e, e.getTargetException());
            } catch (final Exception ee) {
                return handleInternalException(ee);
            }
        } catch (final Exception e) {
            return handleInternalException(e);
        }
    }

    /**
     * Handles any exceptions thrown during the invocation of a matching resource method. It will match the {@code targetException}
     * to a handler method defined within a resource annotated as {@link HandlerType#EXCEPTION}.
     *
     * <br/><br/>
     *
     * A matching handler method will except an instance of {@code targetException} or any superclass of it.
     *
     * <br/><br/>
     *
     * @param exception {@link Exception} used to catch {@link Throwable} from method invocation
     * @param targetException {@link Throwable} generated during method invocation
     * @return {@link HTTPMessage} formatted relative to the exception thrown
     * @throws Exception If an error occurs during the handler invocation
     */
    @SuppressWarnings("unchecked")
    public HTTPMessage<HTTPResponseStartLine> handleResourceException(final Exception exception, final Throwable targetException) throws Exception {
        logger.error(exception.getMessage(), exception);
        final Method method = findMethod(this.exceptionResourcesURLs, m -> {
            if (!m.getReturnType().isAssignableFrom(HTTPMessage.class)) {
                return false;
            }
            return Arrays.asList(m.getAnnotation(ExceptionHandler.class).value()).contains(targetException.getClass());
        });
        logger.trace(String.format(
                "Invoking method [%s] for resource exception: %s",
                method.getName(),
                targetException.getClass().getName()
        ));
        return (HTTPMessage<HTTPResponseStartLine>) method.invoke(
                instantiateResource(method.getDeclaringClass()),
                targetException
        );
    }

    /**
     * Handles any errors that occur out of the scope of the a resource method.
     *
     * <br/><br/>
     *
     * @param exception Instance of {@link Exception} thrown out of scope
     * @return An instance of {@link StandardResponses#_500()}
     */
    public HTTPMessage<HTTPResponseStartLine> handleInternalException(final Exception exception) {
        logger.error(exception.getMessage(), exception);
        return StandardResponses._500();
    }
}
