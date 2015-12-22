package commons.configuration.ext;

import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * Version properties calculated from Maven build resources.
 * 
 * @author Timothy Storm
 */
public class Version {

    private static final String LF = System.getProperty("line.separator");

    /** cache application properties so they are not reloaded every time they are accessed */
    private static volatile Properties _propsCache;

    /** cache svuid (serialization ID) so it isn't recalculated every time it is accessed */
    private static volatile Long _svuidCache;

    /**
     * <pre>
     * [0-9]{1,3} : [major] 1 to * digits (required) 
     * [0-9]{1,3} : [minor] 1 to * digits (required) 
     * [0-9]{1,3} : [maint] 1 to * digits (required)
     * \\w+       : [build] any number of word characters (optional)
     * </pre>
     */
    private static final Pattern VERSION_PATTERN = Pattern.compile("([0-9]+)\\.([0-9]+)\\.([0-9]+)\\-?(\\w+)?$");

    public static String buildVersion() {
        return versionPart(4);
    }

    /**
     * @return project build datetime
     */
    public static String builtAt() {
        return getProperty("application.created");
    }

    public static String builtBy() {
        return getProperty("application.builtBy");
    }

    /**
     * @return {@link ClassLoader} to use for resource loading
     */
    protected static ClassLoader getDefaultClassLoader() {
        ClassLoader loader = null;
        try {
            loader = Thread.currentThread().getContextClassLoader();
        } catch (Throwable threadclassloader_notfound) {}

        if (loader == null) {
            loader = Version.class.getClassLoader();
            if (loader == null) {
                try {
                    loader = ClassLoader.getSystemClassLoader();
                } catch (Throwable giveup) {}
            }
        }
        return loader;
    }

    /**
     * @return project.artifactId from pom
     */
    public static String getName() {
        return getProperty("application.name");
    }

    /**
     * @param key
     *            of the property
     * @return property mapped to the key, or null if not found
     */
    protected static String getProperty(final String key) {
        if (_propsCache == null) {
            synchronized (Version.class) {
                if (_propsCache == null) {
                    ClassLoader loader = getDefaultClassLoader();
                    try {
                        InputStream is = loader.getResourceAsStream("version.properties");
                        Properties local = new Properties();
                        local.load(is);
                        _propsCache = local;
                    } catch (Throwable ex) {
                        _propsCache = null;
                    }
                }
            }
        }
        return _propsCache == null ? null : _propsCache.getProperty(key);
    }

    /**
     * @return project.version from pom
     */
    public static String getVersion() {
        return getProperty("application.version");
    }

    /**
     * Prints out the version information for this module to the Syste print stream
     * 
     * @param args
     *            - ignored
     */
    public static void main(String[] args) {
        System.out.println(manifest());
    }

    public static String maintenanceVersion() {
        return versionPart(3);
    }

    public static long maintenanceVersionAsLong() {
        return Long.parseLong(maintenanceVersion());
    }

    public static String majorVersion() {
        return versionPart(1);
    }

    public static long majorVersionAsLong() {
        return Long.parseLong(majorVersion());
    }

    /**
     * Prints a manifest (MANIFEST.MF) style block
     * 
     * @return manifest info
     */
    public static String manifest() {
        StringBuilder manifest = new StringBuilder();
        manifest.append("Specification-Title: ").append(Version.getName()).append(LF);
        manifest.append("Specification-Version: ").append(Version.majorVersion()).append(LF);
        manifest.append("Implementation-Version: ").append(Version.getVersion()).append(LF);
        manifest.append("Built-At: ").append(Version.builtAt()).append(LF);
        manifest.append("Built-By: ").append(Version.builtBy());
        return manifest.toString();
    }

    public static String minorVersion() {
        return versionPart(2);
    }

    public static long minorVersionAsLong() {
        return Long.parseLong(minorVersion());
    }

    /**
     * Hashing of this modules major + minor versions, defaults to 1 if the hashing fails.
     * 
     * @return byte hashing of major + minor versions
     */
    public static long svuid() {
        return svuid(1L);
    }

    /**
     * Hashing of this modules major + minor versions. If the version cannot be determined then the defaultSvuid
     * is used instead.
     * 
     * @param defaultSvuid
     *            - used if the svuid cannot be calculated from the {@link #getVersion()}
     * @return byte hashing of major + minor versions
     */
    public static long svuid(final long defaultSvuid) {
        if (_svuidCache == null) {
            synchronized (Version.class) {
                if (_svuidCache == null) {
                    try {
                        String version = Version.majorVersion() + Version.minorVersion();
                        if (StringUtils.isEmpty(version)) _svuidCache = defaultSvuid;
                        else _svuidCache = new BigInteger(version.getBytes(StandardCharsets.UTF_8)).longValue();
                    } catch (Exception e) {
                        _svuidCache = defaultSvuid;
                    }
                }
            }
        }
        return _svuidCache;
    }

    /**
     * Extracts the version parts and returns the requested part (1:major, 2:minor, 3:maintenance, 4:build). If the part
     * if less than 0 then the full version is returned.
     * 
     * @param part
     *            of the version required
     * @return version part or empty string if not found
     */
    protected static String versionPart(int part) {
        int p = Math.max(0, part);

        Matcher matcher = VERSION_PATTERN.matcher(getVersion());
        if (matcher.find() && matcher.groupCount() >= p) return matcher.group(p);

        // something wonky
        return StringUtils.EMPTY;
    }

    /**
     * Enforces singleton
     */
    private Version() {}

    /**
     * Enforces singleton
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}
