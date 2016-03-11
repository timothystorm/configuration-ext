package commons.configuration.ext.resolver;

import commons.configuration.ext.EnvironmentResolver;

/**
 * Matches hosts by comparing a host pattern with the runtime environments host name.
 * 
 * @author Timothy Storm
 */
public class MachinePatternHostResolver extends AbstractEnvironmentResolver {
    private static volatile EnvironmentResolver SINGLETON;

    private MachinePatternHostResolver() {
        super();
    }

    public static EnvironmentResolver instance() {
        if (SINGLETON == null) {
            synchronized (MachineHostResolver.class) {
                if (SINGLETON == null) SINGLETON = new MachinePatternHostResolver() {
                    @Override
                    protected Object clone() throws CloneNotSupportedException {
                        throw new CloneNotSupportedException();
                    }
                };
            }
        }
        return SINGLETON;
    }

    /**
     * {@inheritDoc}
     * <p>
     * To initiate a pattern match, host patterns must start/end with a backslash - '/'.
     * </p>
     * <ul>
     * <li>
     * Host [xenon-company1.com]
     * <ul>
     * <li>/xenon.+/ = true</li>
     * <li>/xenon-company[\\d].+/ = true</li>
     * <li>/xenon-company[2].+/ = false</li>
     * </ul>
     * </li>
     * </ul>
     */
    @Override
    public boolean resolves(final String env, final String host) {
        if (host == null || host.isEmpty()) return false;
        if (host.startsWith("/") && host.startsWith("/")) {
            String pattern = host.substring(1, host.length() - 1);
            if (MachineUtils.hostName().matches(pattern)) return true;
            if (MachineUtils.hostAddress().matches(pattern)) return true;
        }
        return false;
    }
}
