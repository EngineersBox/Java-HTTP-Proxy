package com.engineersbox.httpproxy.resolver;

import com.engineersbox.httpproxy.Proxy;
import com.engineersbox.httpproxy.configuration.ConfigModule;
import com.engineersbox.httpproxy.connection.ConnectionModule;
import com.engineersbox.httpproxy.exceptions.ResourceEndpointMatcherException;
import com.engineersbox.httpproxy.formatting.FormattingModule;
import com.engineersbox.httpproxy.formatting.http.common.*;
import com.engineersbox.httpproxy.formatting.http.response.HTTPResponseStartLine;
import com.engineersbox.httpproxy.resolver.annotation.ContentType;
import com.engineersbox.httpproxy.resolver.annotation.ExceptionHandler;
import com.engineersbox.httpproxy.resolver.annotation.Handler;
import com.engineersbox.httpproxy.resolver.annotation.HandlerType;
import com.engineersbox.httpproxy.servlet.ProxyModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HandlerResolver implements ResourceResolver {

    private final Logger logger = LogManager.getLogger(HandlerResolver.class);

    final Set<Class<?>> contentResources;
    final Set<Class<?>> exceptionResources;
    final Set<URL> contentResourcesURLs;
    final Set<URL> exceptionResourcesURLs;

    public HandlerResolver() {
        this.contentResources = resolveResourcesWithType(HandlerType.CONTENT);
        this.exceptionResources = resolveResourcesWithType(HandlerType.EXCEPTION);
        this.contentResourcesURLs = findResourceURLs(this.contentResources);
        this.exceptionResourcesURLs = findResourceURLs(this.exceptionResources);
    }

    private Set<Class<?>> resolveResourcesWithType(final HandlerType type) {
        final Reflections reflections = new Reflections(Proxy.class.getPackage().getName());
        return reflections.getTypesAnnotatedWith(Handler.class).stream()
                .filter(h -> h.getAnnotation(Handler.class).value() == type)
                .collect(Collectors.toSet());
    }

    private Set<URL> findResourceURLs(final Set<Class<?>> resources) {
        return resources.stream()
                .map(r -> ClasspathHelper.forClass(r, r.getClassLoader()))
                .collect(Collectors.toSet());
    }

    public <T>  T instantiateResource(final Class<T> resource) {
        final Injector injector = Guice.createInjector(
                new ConfigModule(),
                new FormattingModule(),
                new ConnectionModule(),
                new ProxyModule()
        );
        return injector.getInstance(resource);
    }

    private String getContentTypeHeader(final Map<String, String> headers) throws ResourceEndpointMatcherException {
        if (!headers.containsKey(HTTPSymbols.CONTENT_TYPE_HEADER)) {
            throw new ResourceEndpointMatcherException("Could not find " + HTTPSymbols.CONTENT_TYPE_HEADER + " when trying to resolve resource endpoint");
        }
        final String contentTypeHeader = headers.get(HTTPSymbols.CONTENT_TYPE_HEADER);
        String typeValue = contentTypeHeader;
        if (contentTypeHeader.contains(HTTPSymbols.CONTENT_TYPE_CHARSET_KEY)) {
            final String contentTypeSplit = contentTypeHeader.split(HTTPSymbols.CONTENT_TYPE_CHARSET_KEY)[0];
            typeValue = contentTypeSplit.replace(HTTPSymbols.HEADER_VALUE_LIST_DELIMITER, "");
        }
        return typeValue;
    }

    private Optional<String> matchHeaderToPatterns(final String contentTypeHeader, final List<String> patterns) {
        return patterns.stream()
                .filter(p -> Pattern.compile(p).matcher(contentTypeHeader).matches())
                .findFirst();
    }

    private Method findMethod(final Set<URL> urls, final Predicate<Method> predicate) throws Exception {
        final Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(urls));
        Set<Method> resources = reflections.getMethodsAnnotatedWith(ContentType.class);
        final Optional<Method> potentialMethod = resources.stream().filter(predicate).findFirst();
        if (!potentialMethod.isPresent()) {
            throw new ResourceEndpointMatcherException("Could not find matching method");
        }
        return potentialMethod.get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public HTTPMessage<HTTPResponseStartLine> match(final HTTPMessage<HTTPResponseStartLine> message) {
        try {
            final String contentTypeHeader = getContentTypeHeader(message.headers);
            final Method method = findMethod(this.contentResourcesURLs, m -> {
                if (!m.getReturnType().isAssignableFrom(HTTPMessage.class)) {
                    return false;
                }
                final List<String> patterns = Arrays.asList(m.getAnnotation(ContentType.class).value());
                return matchHeaderToPatterns(contentTypeHeader, patterns).isPresent();
            });
            return (HTTPMessage<HTTPResponseStartLine>) method.invoke(
                    instantiateResource(method.getDeclaringClass()),
                    message
            );
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

    @SuppressWarnings("unchecked")
    public HTTPMessage<HTTPResponseStartLine> handleResourceException(final Exception exception, final Throwable targetException) throws Exception {
        logger.error(exception.getMessage(), exception);
        final Method method = findMethod(this.exceptionResourcesURLs, m -> {
            if (!m.getReturnType().isAssignableFrom(HTTPMessage.class)) {
                return false;
            }
            return Arrays.asList(m.getAnnotation(ExceptionHandler.class).value()).contains(targetException.getClass());
        });
        return (HTTPMessage<HTTPResponseStartLine>) method.invoke(
                instantiateResource(method.getDeclaringClass()),
                targetException
        );
    }

    public HTTPMessage<HTTPResponseStartLine> handleInternalException(final Exception exception) {
        logger.error(exception.getMessage(), exception);
        final Map<String, String> headers = new HashMap<>();
        headers.put("Connection", "close");
        headers.put("Server", "HTTPProxy");
        return new HTTPMessage<>(
                new HTTPResponseStartLine(
                        HTTPStatusCode._500.code,
                        HTTPStatusCode._500.message,
                        HTTPVersion.HTTP11
                ),
                headers
        );
    }
}