package com.engineersbox.httpproxy.resolver.annotation.method;

import com.engineersbox.httpproxy.formatting.http.common.HTTPMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation indicating the type of Method a given HTTP method annotation will handle
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface MethodAnnotationType {
    /**
     * HTTP method to handle
     *
     * @return An instance of {@link HTTPMethod}
     */
    HTTPMethod value();
}
