package commons.configuration.ext;

class MachineHostMatcher implements HostMatcher {
    private static volatile HostMatcher SINGLETON;

    private MachineHostMatcher() {}

    public static HostMatcher singleton() {
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
        if(host == null || host.isEmpty()) return false;
        if (MachineUtils.hostName().equalsIgnoreCase(host)) return true;
        if (MachineUtils.hostAddress().equalsIgnoreCase(host)) return true;
        return false;
    }
}
