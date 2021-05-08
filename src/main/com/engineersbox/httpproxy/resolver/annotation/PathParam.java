package com.engineersbox.httpproxy.resolver.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Designates a method parameter to be filled with the matching path parameter from the handled endpoint path
 *
 * <br/><br/>
 *
 * For example, with a path designated by {@code /account/{id}/users}, anything of the form {@code /account/.*\/}
 * will be matched and the {@code {id}} part will be passed as a {@link String} to the constructor of {@code AccID}
 * through injection. The resulting instantiation of {@code AccID} will be passed to an invocation fot the
 * {@code getUsers(...)} method.
 * <pre>{@code
 * public class SomeResource {
 *      private final Accounts accounts;
 *
 *      @Inject
 *      public SomeResource(final Accounts accounts) {
 *          this.accounts = accounts;
 *      }
 *
 *      static class AccID {
 *          public String id;
 *
 *          @Inject
 *          public AccID(final String id) {
 *              this.id = id;
 *          }
 *      }
 *
 *
 *      @Path("/account/{id}/users")
 *      public List<String> getUsers(@PathParam("id") final AccID accountID) {
 *          return this.accounts.find(accountID).getUsers()
 *      }
 * }
 * }</pre>
 *
 * Note that if the annotated parameter is not a custom injectable object like {@code AccID} then it will either be passed
 * as a string literate and attempt auto type coercion if possible, or will throw an error to indicate it is not the
 * correct type. Generally speaking, path parameters should be used on {@link String} parameters, since usage is on
 * a per-handler basis. For example:
 * <pre>{@code
 *      @Path("/account/{id}/users")
 *      public List<String> getUsers(@PathParam("id") final String accountID) {
 *          return this.accounts.find(accountID).getUsers()
 *      }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface PathParam {

    /**
     * Path parameter in capture syntax (E.g. {@code {id}}), to be used to fill a designated parameter
     *
     * <br/><br/>
     *
     * @return Path parameter as a {@link String}
     */
    public String value();
}
