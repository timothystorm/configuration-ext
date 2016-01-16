package commons.configuration.ext;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;

/**
 * A {@link Configuration} that can use different {@link ConfigurationParser}'s or {@link ConfigurationComposer}'s. This
 * allows for custom property formats to be used, instead of the standard set provided by commons configuration.
 * <p>
 * <em>Note:</em>Configuration objects of this type can be read concurrently by multiple threads.
 * 
 * @author Darren Bruxvoort
 * @author Timothy Storm
 */
public abstract class StrategicConfiguration extends PropertiesConfiguration {

    /**
     * Creates an empty {@link Configuration} object which can be used to synthesize a new Properties file by adding
     * values
     * and then saving().
     */
    public StrategicConfiguration() {
        super();
    }

    /**
     * Creates and loads the properties from the specified file. The specified file can contain "include" properties
     * which then are loaded and merged into the properties.
     *
     * @param file
     *            The properties file to load.
     * @throws ConfigurationException
     *             Error while loading the properties file
     */
    public StrategicConfiguration(File file) throws ConfigurationException {
        super(file);
    }

    /**
     * Creates and loads the properties from the specified file. The specified file can contain "include" properties
     * which then are loaded and merged into the properties.
     *
     * @param fileName
     *            The name of the properties file to load.
     * @throws ConfigurationException
     *             Error while loading the properties file
     */
    public StrategicConfiguration(String fileName) throws ConfigurationException {
        super(fileName);
    }

    /**
     * Creates and loads the properties from the specified URL. The specified file can contain "include" properties
     * which then are loaded and merged into the properties.
     *
     * @param url
     *            The location of the properties file to load.
     * @throws ConfigurationException
     *             Error while loading the properties file
     */
    public StrategicConfiguration(URL url) throws ConfigurationException {
        super(url);
    }

    /**
     * @return handler that reads/writes the configuration resource
     */
    abstract protected ConfigurationParser getConfigurationParser();

    abstract protected ConfigurationComposer getConfigurationComposer();

    @Override
    public synchronized void load(Reader reader) throws ConfigurationException {
        Log log = getLogger();

        if (log.isDebugEnabled()) log.debug("reading configuration resource start...");
        getConfigurationParser().parse(reader);
        if (log.isDebugEnabled()) log.debug("reading configuration resource end.");
    }

    @Override
    public synchronized void save(Writer writer) throws ConfigurationException {
        Log log = getLogger();

        if (log.isDebugEnabled()) log.debug("saving configuration resource start...");
        getConfigurationComposer().compose(writer);
        if (log.isDebugEnabled()) log.debug("saving configuration resource end.");
    }
}
