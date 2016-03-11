package commons.configuration.ext.resolver;

import commons.configuration.ext.EnvironmentResolver;

/**
 * Matches hosts if they are assigned the computer name or IP of the runtime environment. Example: 192.168.56.1,
 * Xenon-company.com
 * 
 * @author Timothy Storm
 */
public class MachineHostResolver extends AbstractEnvironmentResolver {
    private static volatile EnvironmentResolver SINGLETON;

    private MachineHostResolver() {
        super();
    }

    public static EnvironmentResolver instance() {
        if (SINGLETON == null) {
            synchronized (MachineHostResolver.class) {
                if (SINGLETON == null) SINGLETON = new MachineHostResolver() {
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
    public boolean resolves(String env, String host) {
        if (host == null || host.isEmpty()) return false;
        if (MachineUtils.hostName().equalsIgnoreCase(host)) return true;
        if (MachineUtils.hostAddress().equalsIgnoreCase(host)) return true;
        return false;
    }
}
