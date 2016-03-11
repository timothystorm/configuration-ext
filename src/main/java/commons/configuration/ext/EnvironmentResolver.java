package commons.configuration.ext;

import java.util.Collection;

/**
 * Resolves components to a resolution result.
 * provide.
 * 
 * @author Timothy Storm
 */
public interface EnvironmentResolver {
    /**
     * Attempts to resolve the component against a criteria.
     * 
     * @param component
     *            to resolve, can be null
     * @return true if components passes implementation criteria
     */
    boolean resolves(String env, Collection<String> hosts);

    /**
     * Returns the resolution of the component
     * </p>
     * 
     * @return resolved component
     * @throws IllegalStateException
     *             if {@link #resolves(String...))} has not been attempted or the previous
     *             {@link #resolves(String...)} results in false.
     */
    String resolvesTo();
}
