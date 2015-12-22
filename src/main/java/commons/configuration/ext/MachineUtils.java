package commons.configuration.ext;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Machine (physical and JVM) utilities to identify attributes of this runtime
 * machine.
 * 
 * @author Timothy Storm
 */
class MachineUtils {
    /**
     * Cache localhost because it <b>should not</b> change while the JVM is
     * running.
     */
    private static volatile InetAddress LOCAL_HOST;

    private static final long MB = 1024 * 1024;

    /**
     * @return total amount of memory being used by the JVM
     */
    public static long committedMemory() {
        return totalMemory() - freeMemory();
    }

    /**
     * @return amount of free memory in the JVM in MB
     */
    public static long freeMemory() {
        return Runtime.getRuntime().freeMemory() / MB;
    }

    /**
     * @return this machines IP address string in textual presentation
     */
    public static String hostAddress() {
        return localHost().getHostAddress();
    }

    /**
     * @return host name for this machine.
     */
    public static String hostName() {
        return localHost().getHostName();
    }

    /**
     * Returns an <code>InetAddress</code> object encapsulating what is most
     * likely the machine's LAN IP address.
     * <p/>
     * This method is intended for use as a replacement of JDK method
     * <code>InetAddress.getLocalHost</code>, because that method is ambiguous
     * on Linux systems. Linux systems enumerate the loopback network interface
     * the same way as regular LAN network interfaces, but the JDK
     * <code>InetAddress.getLocalHost</code> method does not specify the
     * algorithm used to select the address returned under such circumstances,
     * and will often return the loopback address, which is not valid for
     * network communication. Details
     * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037">here
     * </a>.
     * <p/>
     * This method will scan all IP addresses on all network interfaces on the
     * host machine to determine the IP address most likely to be the machine's
     * LAN address. If the machine has multiple IP addresses, this method will
     * prefer a site-local IP address (e.g. 192.168.x.x or 10.10.x.x, usually
     * IPv4) if the machine has one (and will return the first site-local
     * address if the machine has more than one), but if the machine does not
     * hold a site-local address, this method will return simply the first
     * non-loopback address found (IPv4 or IPv6).
     * <p/>
     * If this method cannot find a non-loopback address using this selection
     * algorithm, it will fall back to calling and returning the result of JDK
     * method <code>InetAddress.getLocalHost</code>.
     * <p/>
     * 
     * @return localhost or null if any failures determining this machines
     *         network address.
     */
    public static InetAddress localHost() {
        if (LOCAL_HOST == null) {
            synchronized (MachineUtils.class) {
                if (LOCAL_HOST == null) {
                    try {
                        InetAddress candidate = null;

                        // Iterate all NICs
                        for (Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces(); nets
                                .hasMoreElements();) {
                            NetworkInterface n = (NetworkInterface) nets.nextElement();

                            // Iterate all IP addresses assigned to each card...
                            for (Enumeration<InetAddress> inets = n.getInetAddresses(); inets.hasMoreElements();) {
                                InetAddress i = (InetAddress) inets.nextElement();
                                if (!i.isLoopbackAddress()) {
                                    // short circuit because a site local
                                    // address was found
                                    if (i.isSiteLocalAddress()) return (LOCAL_HOST = i);
                                    else if (candidate == null) candidate = i;
                                }
                            }
                        }

                        if (LOCAL_HOST == null) LOCAL_HOST = candidate;
                        if (LOCAL_HOST == null) LOCAL_HOST = InetAddress.getLocalHost();
                    } catch (Exception e) {
                        return null;
                    }
                }
            }
        }
        return LOCAL_HOST;
    }

    /**
     * Determines hardware address of the interface if it has one and if it can
     * be accessed given the current privileges.
     * 
     * @return MAC address or null on failure
     */
    public static String macAddress() {
        NetworkInterface network;
        try {
            network = NetworkInterface.getByInetAddress(localHost());
            byte[] mac = network.getHardwareAddress();

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            return sb.toString();
        } catch (SocketException e) {
            return null;
        }
    }

    /**
     * @return this machines identity which is the merge of the DNS name and IP
     *         ex. my.machine.id:10.4.3.121
     */
    public static String machineId() {
        return String.format("%s:%s", hostName(), hostAddress());
    }

    /**
     * @return maximum amount of memory that the JVM will attempt to use in MB
     */
    public static long maxMemory() {
        return Runtime.getRuntime().maxMemory() / MB;
    }

    /**
     * @return total amount of memory in the JVM in MB
     */
    public static long totalMemory() {
        return Runtime.getRuntime().totalMemory() / MB;
    }
}
