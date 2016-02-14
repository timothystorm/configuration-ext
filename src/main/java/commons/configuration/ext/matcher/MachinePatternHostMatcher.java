package commons.configuration.ext.matcher;

/**
 * Matches hosts by comparing a host pattern with the runtime environments host name.
 * 
 * @author Timothy Storm
 */
public class MachinePatternHostMatcher implements HostMatcher {
    private static volatile HostMatcher SINGLETON;

    private MachinePatternHostMatcher() {}

    public static HostMatcher instance() {
        if (SINGLETON == null) {
            synchronized (MachineHostMatcher.class) {
                if (SINGLETON == null) SINGLETON = new MachinePatternHostMatcher() {
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
    public boolean matches(String host) {
        if (host == null || host.isEmpty()) return false;
        if (host.startsWith("/") && host.endsWith("/")) {
            String pattern = host.substring(1, host.length() - 1);
            return MachineUtils.hostName().matches(pattern);
        }
        return false;
    }
}
