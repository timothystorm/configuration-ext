package commons.configuration.ext;

/**
 * Matches hosts if they are assigned the localhost identity or alias: localhost, 127.0.0.1, ::1.
 * 
 * @author Timothy Storm
 */
public class LocalHostMatcher implements HostMatcher {

    private static volatile HostMatcher SINGLETON;

    static HostMatcher singleton() {
        if (SINGLETON == null) {
            synchronized (LocalHostMatcher.class) {
                if (SINGLETON == null) SINGLETON = new LocalHostMatcher() {
                    protected Object clone() throws CloneNotSupportedException {
                        throw new CloneNotSupportedException();
                    }
                };
            }
        }
        return SINGLETON;
    }

    private LocalHostMatcher() {}

    @Override
    public boolean matches(final String host) {
        if (host == null || host.isEmpty()) return false;
        if ("localhost".equalsIgnoreCase(host)) return true;
        if ("127.0.0.1".equals(host)) return true;
        if ("::1".equals(host)) return true;
        return false;
    }
}
