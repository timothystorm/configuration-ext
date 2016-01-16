package commons.configuration.ext;

/**
 * Matches hosts by comparing a host pattern with the runtime environments host name.
 * 
 * @author Timothy Storm
 */
public class MachinePatternHostMatcher implements HostMatcher {
    private static volatile HostMatcher SINGLETON;

    private MachinePatternHostMatcher() {}

    public static HostMatcher singleton() {
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
     * To initiate a match, host patterns and must start/end with a backslash - '/'.
     * <p>
     * <table border="1">
     * <tr>
     * <th>Host Name</th>
     * <th>Host Pattern</th>
     * <th>Matches</th>
     * </tr>
     * <tr>
     * <td>xenon-company1.com</td>
     * <td>/xenon.+/</td>
     * <td>true</td>
     * </tr>
     * </tr>
     * <tr>
     * <td>xenon-company2.com</td>
     * <td>/xenon-company[\\d].+/</td>
     * <td>true</td>
     * </tr>
     * </tr>
     * <tr>
     * <td>xenon-company3.com</td>
     * <td>/xenon-company[1-2].+/</td>
     * <td>false</td>
     * </tr>
     * </table>
     */
    @Override
    public boolean matches(String host) {
        if (host == null || host.isEmpty()) return false;
        if (host.startsWith("/") && host.startsWith("/")) {
            String pattern = host.substring(1, host.length() - 1);
            return MachineUtils.hostName().matches(pattern);
        }
        return false;
    }
}
