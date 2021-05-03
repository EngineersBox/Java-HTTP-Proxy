package com.engineersbox.httpproxy.resolver.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes the throwable type to handle within a given annotated method
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ExceptionHandler {

    /**
     * Contains an array of {@link Throwable} to be handled by a the annotated method
     *
     * <br/><br/>
     *
     * @return An array of {@link Throwable}
     */
    Class<? extends Throwable>[] value();
}
