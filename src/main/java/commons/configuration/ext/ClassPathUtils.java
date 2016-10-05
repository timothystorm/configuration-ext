package commons.configuration.ext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Utility to load resources from the classpath
 * 
 * @author Timothy Storm
 */
class ClassPathUtils {
    /**
     * @return {@link ClassLoader} to use for resource loading
     */
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader loader = null;
        try {
            loader = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // cannot access thread context classloader -- falling back
        }

        if (loader == null) {
            loader = ClassPathUtils.class.getClassLoader();
            if (loader == null) {
                try {
                    loader = ClassLoader.getSystemClassLoader();
                } catch (Throwable giveUpAndReturnNull) {}
            }
        }
        return loader;
    }

    /**
     * Loads a resource on the classpath
     * 
     * @param location
     *            - relative location of resource
     * @return URL of the classpath resource
     */
    public static URL loadResource(String location) {
        ClassLoader loader = getDefaultClassLoader();
        if (loader == null) throw new IllegalStateException("class loader not found");
        URL url = loader.getResource(location);

        if (url == null) throw new IllegalStateException("resource [" + location + "] not found!");
        return url;
    }

    /**
     * Loads a resource on the classpath
     * 
     * @param location
     *            - relative location of resource
     * @return reader for the classpath resource
     * @throws IOException
     *             - if an I/O exception occurs.
     */
    public static Reader readResource(String location) throws IOException {
        return new BufferedReader(new InputStreamReader(loadResource(location).openStream(), StandardCharsets.UTF_8));
    }
}
