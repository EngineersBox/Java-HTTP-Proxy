package com.engineersbox.httpproxy.resolver.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes the <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.7" target="_top">RFC 2616 Section 3.7</a>
 * compliant media type to handle in a given method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface MediaType {

    /**
     * Contains an array of Java regexes to match for media types to be handled within
     * the annotated method.
     *
     * @return An array of Java regexes
     */
    String[] value();
}
