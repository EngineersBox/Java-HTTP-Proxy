package com.engineersbox.httpproxy.resolver.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a path to be handle be a resource or method. For example, with a class annotated with {@link Path}, this will
 * designate a global resource path for all methods, ensuring that any matchable method within that also uses a {@link Path}
 * will be appended to the resource path. In this case, invoking the {@code getAccountName()} method would be done for
 * any path matching the full resolution of {@code /account/{id}/name}.
 * <pre>{@code
 * @Path("/account/{id})
 * public class SomeResource {
 *
 *      private final Accounts;
 *
 *      @Inject
 *      public SomeResource(final Accounts accounts) {
 *          this.accounts = accounts;
 *      }
 *
 *      @Path("/name")
 *      public String getAccountName(@PathParam("id") final String id) {
 *         return this.accounts.find(id).getName();
 *      }
 *
 * }
 * }</pre>
 *
 * Note that when using the {@link Path} annotation at both a {@link ElementType#TYPE} and {@link ElementType#METHOD}
 * level will result in {@link PathParam} annotations retrieving from both the annotations as a single source.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Path {

    /**
     * Path pattern to match requests or responses against
     *
     * @return Path pattern as a {@link String}
     */
    public String value();
}
