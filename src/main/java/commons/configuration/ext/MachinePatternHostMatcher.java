package commons.configuration.ext;

class MachinePatternHostMatcher implements HostMatcher {
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
