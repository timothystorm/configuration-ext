package commons.configuration.ext.matcher;

/**
 * Matches hosts if they are assigned the computer name or IP of the runtime environment. Example: 192.168.56.1,
 * Xenon-company.com
 * 
 * @author Timothy Storm
 */
public class MachineHostMatcher implements HostMatcher {
    private static volatile HostMatcher SINGLETON;

    private MachineHostMatcher() {}

    public static HostMatcher instance() {
        if (SINGLETON == null) {
            synchronized (MachineHostMatcher.class) {
                if (SINGLETON == null) SINGLETON = new MachineHostMatcher() {
                    @Override
                    protected Object clone() throws CloneNotSupportedException {
                        throw new CloneNotSupportedException();
                    }
                };
            }
        }
        return SINGLETON;
    }

    @Override
    public boolean matches(String host) {
        if (host == null || host.isEmpty()) return false;
        if (MachineUtils.hostName().equalsIgnoreCase(host)) return true;
        if (MachineUtils.hostAddress().equalsIgnoreCase(host)) return true;
        return false;
    }
}
