package com.engineersbox.httpproxy.resolver.annotation.method;

import com.engineersbox.httpproxy.formatting.http.common.HTTPMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation indicating the associated method handles TRACE requests
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@MethodAnnotationType(HTTPMethod.TRACE)
public @interface TRACE {
}
