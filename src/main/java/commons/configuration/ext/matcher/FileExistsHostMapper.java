package commons.configuration.ext.matcher;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.configuration.ConfigurationException;

/**
 * Matches the host based on the presence of a file on the file system.
 * 
 * <pre>
 * &lt;context&gt;
 *   &lt;hosts env="TEST"&gt;
 *     &lt;host&gt;file:/path/to/file/test.env&lt;/host&gt;
 *   &lt;/hosts&gt;
 *   &lt;hosts env="PROD"&gt;
 *     &lt;host&gt;file:/path/to/file/prod.env&lt;/host&gt;
 *   &lt;/hosts&gt;
 * &lt;/context&gt;
 * </pre>
 * 
 * @author Timothy Storm
 */
public class FileExistsHostMapper implements HostMatcher {
    private static volatile HostMatcher SINGLETON;
    private static final String         FILE_RESOURCE_PREFIX = "file:";

    private FileExistsHostMapper() {}

    public static HostMatcher instance() {
        if (SINGLETON == null) {
            synchronized (FileExistsHostMapper.class) {
                if (SINGLETON == null) SINGLETON = new FileExistsHostMapper() {
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
    public boolean matches(String host) throws ConfigurationException {
        if (host == null || host.isEmpty()) return false;
        if (host.startsWith(FILE_RESOURCE_PREFIX)) {
            String resourcePath = host.substring(FILE_RESOURCE_PREFIX.length());
            return Files.exists(Paths.get(resourcePath));
        }
        return false;
    }
}
