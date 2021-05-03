package com.engineersbox.httpproxy.resolver.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes the type of handler a given annotated class is.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Handler {

    /**
     * Designates the kind of handlers the class has. This can any one the elements of {@link HandlerType}.
     *
     * <br/><br/>
     *
     * Using the {@link HandlerType#CONTENT} type will mean only methods annotated with {@link MediaType} will be
     * matched when resolving a method for an media type handler. Similarly, only classes annotated with {@link HandlerType#EXCEPTION}
     * will be used to match a handler method for exceptions. Specifically methods annotated with {@link ExceptionHandler}.
     *
     * <br/><br/>
     *
     * @return An element of {@link HandlerType} enum
     */
    HandlerType value();
}
