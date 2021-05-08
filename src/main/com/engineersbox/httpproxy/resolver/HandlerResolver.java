package com.engineersbox.httpproxy.resolver;

import com.engineersbox.httpproxy.Proxy;
import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.configuration.ConfigModule;
import com.engineersbox.httpproxy.connection.ConnectionModule;
import com.engineersbox.httpproxy.exceptions.resolver.ResourceEndpointMatcherException;
import com.engineersbox.httpproxy.formatting.FormattingModule;
import com.engineersbox.httpproxy.formatting.http.common.*;
import com.engineersbox.httpproxy.formatting.http.request.HTTPRequestStartLine;
import com.engineersbox.httpproxy.formatting.http.response.HTTPResponseStartLine;
import com.engineersbox.httpproxy.formatting.http.response.StandardResponses;
import com.engineersbox.httpproxy.resolver.annotation.*;
import com.engineersbox.httpproxy.resolver.annotation.method.*;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Resource handler for {@link MediaType} driven resource delimitation
 */
public class HandlerResolver implements ResourceResolver {

    private final Logger logger = LogManager.getLogger(HandlerResolver.class);
    private final Config config;

    private static final Set<Module> injectables = ImmutableSet.of(
            new ConfigModule(),
            new FormattingModule(),
            new ConnectionModule()
    );

    private static final Set<Class<? extends Annotation>> methodAnnotations = ImmutableSet.of(
            GET.class,
            POST.class,
            UPDATE.class,
            PUT.class,
            OPTIONS.class,
            HEAD.class,
            TRACE.class
    );

    final Set<Class<?>> requestResources;
    final Set<Class<?>> responseResources;
    final Set<Class<?>> exceptionResources;

    final Set<URL> requestContentResourceURLS;
    final Set<URL> responseContentResourceURLS;
    final Set<URL> exceptionResourcesURLs;

    @Inject
    public HandlerResolver(final Config config) {
        this.config = config;
        this.requestResources = resolveResourcesWithType(HandlerType.REQUEST_CONTENT);
        this.requestContentResourceURLS = findResourceURLs(this.requestResources);
        logger.debug("Retrieved " + this.requestContentResourceURLS.size() + " request content resources");
        this.responseResources = resolveResourcesWithType(HandlerType.RESPONSE_CONTENT);
        this.responseContentResourceURLS = findResourceURLs(this.responseResources);
        logger.debug("Retrieved " + this.responseContentResourceURLS.size() + " response content resources");
        this.exceptionResources = resolveResourcesWithType(HandlerType.EXCEPTION);
        this.exceptionResourcesURLs = findResourceURLs(this.exceptionResources);
        logger.trace("Retrieved " + this.exceptionResourcesURLs.size() + " exception resources");
    }

    /**
     * Find classes annotated with {@link Handler} for a given {@link HandlerType} within the
     * project package {@code com.engineersbox.httpproxy.*}
     *
     * @param type {@link HandlerType} to find classes for
     * @return A {@link Set} of classes matching the given {@link HandlerType}. This is an empty set if none are found
     */
    private Set<Class<?>> resolveResourcesWithType(final HandlerType type) {
        final Reflections reflections = new Reflections(Proxy.class.getPackage().getName());
        return reflections.getTypesAnnotatedWith(Handler.class).stream()
                .filter(h -> h.getAnnotation(Handler.class).value().equals(type))
                .collect(Collectors.toSet());
    }

    /**
     * Resolves the class path URLs ({@link URL}) for a given {@link Set} of resource classes
     *
     * @param resources {@link Set} of resource classes
     * @return A {@link Set} of {@link URL} for the class paths each resource
     */
    private Set<URL> findResourceURLs(final Set<Class<?>> resources) {
        return resources.stream()
                .flatMap(r -> ClasspathHelper.forPackage(r.getPackage().getName()).stream())
                .collect(Collectors.toSet());
    }

    /**
     * Creates an instantiation of a given resource via an {@link Injector} using modules within {@link HandlerResolver#injectables}
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
     * Determines whether a given method has one of the HTTP method annotations and also if the method matches that of
     * the supplied {@code httpMethod}.
     *
     * <br/><br/>
     *
     * HTTP method annotations being matched:
     * <ul>
     *     <li>{@link GET}</li>
     *     <li>{@link HEAD}</li>
     *     <li>{@link OPTIONS}</li>
     *     <li>{@link POST}</li>
     *     <li>{@link PUT}</li>
     *     <li>{@link TRACE}</li>
     *     <li>{@link UPDATE}</li>
     * </ul>
     *
     * @param method A {@link Method} to check annotations on
     * @param httpMethod A specific {@link HTTPMethod} to check if the method has annotated for
     * @return {@code true} if the method has a method annotation and the method matches {@code httpMethod}, {@code false}
     * otherwise
     */
    private boolean matchHTTPMethodAnnotation(final Method method, final HTTPMethod httpMethod) {
        for (final Annotation ma : method.getAnnotations()) {
            for (final Class<? extends Annotation> hma : methodAnnotations) {
                MethodAnnotationType methodAnnotationType;
                if (hma.isAssignableFrom(ma.annotationType()) && (methodAnnotationType = hma.getAnnotation(MethodAnnotationType.class)) != null) {
                    return methodAnnotationType.value() == httpMethod;
                }
            }
        }
        return false;
    }

    /**
     * Determines whether a given {@link HTTPRequestStartLine} and {@link Method} have a matchable path, and if it matches
     * a given start line.
     *
     *
     * @param startLine An instance of {@link HTTPRequestStartLine} from a request {@link HTTPMessage}
     * @param method The {@link Method} to check handling of {@link Path} annotations on
     * @return {@link Pair} containing a {@link Boolean} and {@link MatchableParameterisedPath} based on whether a given
     * method has a {@link Path} annotated and can match against the given {@link HTTPRequestStartLine}
     */
    private Pair<Boolean, MatchableParameterisedPath> matchPathToPatterns(final HTTPRequestStartLine startLine, final Method method) {
        if (!matchHTTPMethodAnnotation(method, startLine.method)) {
            return ImmutablePair.of(false, null);
        }
        Path pathAnnotation = method.getDeclaringClass().getAnnotation(Path.class);
        final MatchableParameterisedPath matchableParameterisedPath = new MatchableParameterisedPath(pathAnnotation != null ? pathAnnotation.value() : "/");
        if (!matchableParameterisedPath.matchPathToTarget(startLine.target)) {
            return ImmutablePair.of(false, null);
        }
        if ((pathAnnotation = method.getAnnotation(Path.class)) != null) {
            matchableParameterisedPath.addToPath(pathAnnotation.value());
            if (!matchableParameterisedPath.matchPathToTarget(startLine.target)) {
                return ImmutablePair.of(false, null);
            }
        }
        return ImmutablePair.of(true, matchableParameterisedPath);
    }

    /**
     * Determine whether a {@link Set} of {@link Class} has a particular {@link Class} contained within. This operates
     * on the canonical names of {@link Class} to ensure that package-level differences are captured.
     *
     * @param res A particular {@link Class} canonical name to check if should be excluded
     * @param toFilter A {@link Set} of {@link Class} to base exclusion on
     * @return {@code true} if {@code toFiler} contains the class, {@code false} otherwise
     */
    final boolean excludeResources(final String res, final Set<Class<?>> toFilter) {
        return toFilter.stream()
                .noneMatch(clazz -> res.replaceAll("[\\\\/]", ".").contains(clazz.getCanonicalName()));
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
     * @param urls A {@link Set} of resource {@link URL}s
     * @param predicate A {@link Predicate} to match against method signatures
     * @return A matching method
     * @throws ResourceEndpointMatcherException No methods matching the {@link Predicate} were found
     */
    private Method findMethod(final Set<URL> urls, final Set<Class<?>> exclude, final Predicate<Method> predicate) throws ResourceEndpointMatcherException {
        final Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(urls)
                .filterInputsBy(res -> excludeResources(res, exclude))
                .setScanners(new MethodAnnotationsScanner())
                .setExecutorService(Executors.newFixedThreadPool(this.config.servlet.threading.classMatcherPoolSize))
        );
        final Set<Method> resources = reflections.getMethodsAnnotatedWith(MediaType.class);
        resources.addAll(reflections.getMethodsAnnotatedWith(Path.class));
        final Optional<Method> potentialMethod = resources.stream().filter(predicate).findFirst();
        if (!potentialMethod.isPresent()) {
            throw new ResourceEndpointMatcherException("Could not find matching handler method, returning message as-is");
        }
        logger.trace("Found matching method for resource handling: " + potentialMethod.get().getName());
        return potentialMethod.get();
    }

    /**
     * Retrieves parameters for a given {@link Method} and instantiates them via default constructors. This will also
     * handling filling {@link PathParam} annotated parameters, by retrieving them from from the supplied
     * {@link MatchableParameterisedPath}.
     *
     * <br/><br/>
     *
     * Note that this will <strong>NOT</strong> use injection, since the parameters should be within the content of an
     * {@link HTTPMessage} only.
     *
     * @param method A {@link Method} to instantiate all required parameters for
     * @param message Instance of {@link HTTPStartLine} to use in parameters
     * @param matchableParameterisedPath Instance of {@link MatchableParameterisedPath} used to retrieve path parameters from
     * @return An array of {@link Object} containing the instantiated parameters
     * @throws InstantiationException If a parameter could not be instantiated, or a default constructor did not exist.
     * @throws IllegalAccessException If the instance of not accessible or has locked down access
     * @throws NoSuchMethodException If there is no constructor for the path parameter, only if the path parameter is
     *  not an instance of {@link String} but potentially takes one as an argument.
     * @throws InvocationTargetException If a given parameter could not be instantiated, particularly during constructor
     * invocation in this context
     */
    private Object[] instantiateMethodParams(final Method method, final HTTPMessage<HTTPRequestStartLine> message, final MatchableParameterisedPath matchableParameterisedPath) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final Class<?>[] params =  method.getParameterTypes();
        final Annotation[][] paramAnnotations = method.getParameterAnnotations();

        final List<Object> instantiatedParams = new ArrayList<>();
        for (int i = 0; i < params.length; i++) {
            final Class<?> param = params[i];
            if (message.getClass().isAssignableFrom(param)) {
                instantiatedParams.add(message);
                continue;
            }
            final Annotation[] annotations = paramAnnotations[i];
            if (annotations.length < 1) {
                instantiatedParams.add(param.newInstance());
            }
            PathParam pathParam = null;
            for (final Annotation annotation : annotations) {
                if (annotation.annotationType().isAssignableFrom(PathParam.class)) {
                    pathParam = (PathParam) annotation;
                    break;
                }
            }
            if (pathParam != null) {
                final Constructor<?> constructor = param.getConstructor(String.class);
                instantiatedParams.add(
                        constructor.newInstance(matchableParameterisedPath.getPathParam(pathParam.value(), message.startLine.target))
                );
                continue;
            }
            instantiatedParams.add(param.newInstance());
        }
        return instantiatedParams.toArray();
    }

    /**
     * Matches the given {@link HTTPMessage} to resources based on the {@link MediaType} contained in the
     * {@code Content-Type} header.
     *
     * @param message {@link HTTPMessage} to handle within a given resource
     * @return {@link HTTPMessage} handled by a matching {@link MediaType} method
     */
    @SuppressWarnings("unchecked")
    public HTTPMessage<HTTPRequestStartLine> matchRequest(final HTTPMessage<HTTPRequestStartLine> message) {
        try {
            AtomicReference<MatchableParameterisedPath> matchableParameterisedPath = new AtomicReference<>();
            final Method method = findMethod(
                    this.requestContentResourceURLS,
                    Stream.concat(this.exceptionResources.stream(), this.responseResources.stream()).collect(Collectors.toSet()),
                    m -> {
                if (!m.getReturnType().isAssignableFrom(HTTPMessage.class)) {
                    return false;
                }
                final Pair<Boolean, MatchableParameterisedPath> matched = matchPathToPatterns(message.startLine, m);
                matchableParameterisedPath.set(matched.getRight());
                return matched.getLeft();
            });
            logger.trace(String.format(
                    "Invoking method [%s] for request message: %s",
                    method.getName(),
                    message
            ));
            return (HTTPMessage<HTTPRequestStartLine>) method.invoke(
                    instantiateResource(method.getDeclaringClass()),
                    instantiateMethodParams(
                            method,
                            message,
                            matchableParameterisedPath.get()
                    )
            );
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return message;
        }
    }

    /**
     * Matches the given {@link HTTPMessage} to resources based on the {@link MediaType} contained in the
     * {@code Content-Type} header.
     *
     * @param message {@link HTTPMessage} to handle within a given resource
     * @return {@link HTTPMessage} handled by a matching {@link MediaType} method
     */
    @SuppressWarnings("unchecked")
    @Override
    public HTTPMessage<HTTPResponseStartLine> matchResponse(final HTTPMessage<HTTPResponseStartLine> message) {
        try {
            final String contentTypeHeader = getContentTypeHeader(message.headers);
            final Method method = findMethod(
                    this.responseContentResourceURLS,
                    Stream.concat(this.exceptionResources.stream(), this.requestResources.stream()).collect(Collectors.toSet()),
                    m -> {
                if (!m.getReturnType().isAssignableFrom(HTTPMessage.class)) {
                    return false;
                }
                final List<String> patterns = Arrays.asList(m.getAnnotation(MediaType.class).value());
                return matchHeaderToPatterns(contentTypeHeader.trim(), patterns).isPresent();
            });
            logger.trace(String.format(
                    "Invoking method [%s] for response message: %s",
                    method.getName(),
                    message
            ));
            return (HTTPMessage<HTTPResponseStartLine>) method.invoke(
                    instantiateResource(method.getDeclaringClass()),
                    message
            );
        } catch (final ResourceEndpointMatcherException e) {
            logger.debug(e.getMessage());
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
     * @param exception {@link Exception} used to catch {@link Throwable} from method invocation
     * @param targetException {@link Throwable} generated during method invocation
     * @return {@link HTTPMessage} formatted relative to the exception thrown
     * @throws Exception If an error occurs during the handler invocation
     */
    @SuppressWarnings("unchecked")
    public HTTPMessage<HTTPResponseStartLine> handleResourceException(final Exception exception, final Throwable targetException) throws Exception {
        logger.error(exception.getMessage(), exception);
        final Method method = findMethod(
                this.exceptionResourcesURLs,
                Stream.concat(this.requestResources.stream(), this.responseResources.stream()).collect(Collectors.toSet()),
                m -> {
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
     * @param exception Instance of {@link Exception} thrown out of scope
     * @return An instance of {@link StandardResponses#_500()}
     */
    public HTTPMessage<HTTPResponseStartLine> handleInternalException(final Exception exception) {
        logger.error(exception.getMessage(), exception);
        return StandardResponses._500();
    }
}
