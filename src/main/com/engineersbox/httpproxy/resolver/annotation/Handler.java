package com.engineersbox.httpproxy.resolver.annotation;

import com.engineersbox.httpproxy.resolver.annotation.method.*;

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
     * Using the {@link HandlerType#RESPONSE_CONTENT} type will mean only response methods annotated with {@link MediaType} will be
     * matched when resolving a method for an media type handler. Using the {@link HandlerType#REQUEST_CONTENT} value
     * indicates that only request methods with {@link Path} annotations and/or HTTP method annotation are used.
     *
     * <br/><br/>
     *
     * The HTTP method annotations being matched are:
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
     * Similarly, only classes annotated with {@link HandlerType#EXCEPTION}
     * will be used to match a handler method for exceptions. Specifically methods annotated with {@link ExceptionHandler}.
     *
     * @return An element of {@link HandlerType} enum
     */
    HandlerType value();
}
