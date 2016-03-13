package commons.configuration.ext.resolver;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import commons.configuration.ext.EnvironmentResolver;

/**
 * Resolves the environment based on the contents of a filesystem file.
 * <p>
 * Example (all defaults): Using a file located at /opt/app/current/cfg/l3.properties with the contents...
 * 
 * <pre>
 * #-----properties---------
 * environment=L3
 * 
 * #-----configurations.xml--------
 * &lt;context&gt;
 *   &lt;hosts env="L2"&gt;
 *     &lt;host&gt;file:/path/to/file/l2.properties&lt;/host&gt;
 *   &lt;/hosts&gt;
 *   &lt;hosts env="L3"&gt;
 *     &lt;host&gt;file:/path/to/file/l3.properties&lt;/host&gt;
 *   &lt;/hosts&gt;
 * &lt;/context&gt;
 * </pre>
 * <p>
 * Example (customized): Using a file located at /opt/app/current/cfg/levels.properties with the contents...
 * 
 * <pre>
 * #-----properties---------
 * unit=L1
 * int=L1
 * sys=L1
 * vol=L1
 * exp=L1
 * test=L1
 * prod=L1
 * 
 * #-----configurations.xml--------
 * &lt;context&gt;
 *   &lt;hosts env="L2"&gt;
 *     &lt;host&gt;file:/path/to/file/l2.properties&lt;/host&gt;
 *   &lt;/hosts&gt;
 *   &lt;hosts env="L3"&gt;
 *     &lt;host&gt;file:/path/to/file/l3.properties&lt;/host&gt;
 *   &lt;/hosts&gt;
 * &lt;/context&gt;
 * </pre>
 * 
 * @author Timothy Storm
 */
public class FileEnvironmentResolver extends AbstractEnvironmentResolver {
    private static volatile EnvironmentResolver SINGLETON;
    private static final String                 FILE_RESOURCE_PREFIX    = "file:";
    private static final String                 DEFAULT_ENVIRONMENT_KEY = "environment";
    private static final String                 ENV_ATTR_SUFFIX         = "@";

    private FileEnvironmentResolver() {}

    public static EnvironmentResolver instance() {
        if (SINGLETON == null) {
            synchronized (FileEnvironmentResolver.class) {
                if (SINGLETON == null) SINGLETON = new FileEnvironmentResolver() {
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
    public boolean resolves(final String env, final String host) {
        if (env == null || env.isEmpty()) return false;
        if (host == null || host.isEmpty()) return false;

        if (host.startsWith(FILE_RESOURCE_PREFIX)) {
            // extract the resource from the host configuration
            String resource = host.substring(FILE_RESOURCE_PREFIX.length());
            if (resource.isEmpty()) return false;

            // break apart resource details
            String path = StringUtils.substringBefore(resource, ENV_ATTR_SUFFIX);
            String key = StringUtils.substringAfter(resource, ENV_ATTR_SUFFIX);
            key = StringUtils.defaultIfBlank(key, DEFAULT_ENVIRONMENT_KEY);

            try {
                // extract properties and locate the environment value
                Path resourcePath = Paths.get(path);
                Properties props = new Properties();
                props.load(Files.newBufferedReader(resourcePath, Charset.forName("UTF-8")));
                String propertyEnv = props.getProperty(key);

                return env.equalsIgnoreCase(propertyEnv);
            } catch (IOException ioe) {
                getLogger().error(ioe);
            }
        }
        return false;
    }
}
