package commons.configuration.ext;

/**
 * Matches a host from a configuration based on criteria specific to an implementation. An affirmative answer is used to
 * determine the property level for the runtime system. For example, {@link LocalHostMatcher} can match hosts with a
 * value of
 * "localhost", "127.0.0.1" or "::1".
 * 
 * @author Timothy Storm
 */
interface HostMatcher {
    /**
     * Determines if the host matches the implementation criteria.
     * 
     * @param host
     *            to match against can be null
     * @return true if the host matches the criteria, false otherwise.
     */
    boolean matches(String host);
}
