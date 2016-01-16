package commons.configuration.ext;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * This {@link Configuration} implements a customized configuration format. The reading and writing are handled by
 * providing a {@link ConfigHandler}.
 * <p>
 * <em>Note:</em>Configuration objects of this type can be read concurrently by multiple threads.
 * 
 * @author Darren Bruxvoort
 * @author Timothy Storm
 */
public class XmlConfiguration extends PropertiesConfiguration {

    /**
     * Creates an empty {@link Configuration} object which can be used to synthesize a new Properties file by adding values
     * and then saving().
     */
    public XmlConfiguration() {
        super();
    }

    /**
     * Creates and loads the xml properties from the specified file. The specified file can contain "include" properties
     * which then are loaded and merged into the properties.
     *
     * @param file
     *            The properties file to load.
     * @throws ConfigurationException
     *             Error while loading the properties file
     */
    public XmlConfiguration(File file) throws ConfigurationException {
        super(file);
    }

    /**
     * Creates and loads the xml properties from the specified file. The specified file can contain "include" properties
     * which then are loaded and merged into the properties.
     *
     * @param fileName
     *            The name of the properties file to load.
     * @throws ConfigurationException
     *             Error while loading the properties file
     */
    public XmlConfiguration(String fileName) throws ConfigurationException {
        super(fileName);
    }

    /**
     * Creates and loads the xml properties from the specified URL. The specified file can contain "include" properties
     * which then are loaded and merged into the properties.
     *
     * @param url
     *            The location of the properties file to load.
     * @throws ConfigurationException
     *             Error while loading the properties file
     */
    public XmlConfiguration(URL url) throws ConfigurationException {
        super(url);
    }

    /**
     * @return handler that reads/writes the configuration resource
     */
    protected ConfigHandler getConfiugrationHandler() {
        return new ConfigHandler_1_0_0(this);
    }

    @Override
    public synchronized void load(Reader reader) throws ConfigurationException {
        getConfiugrationHandler().readFrom(reader);
    }

    @Override
    public void save(Writer writer) throws ConfigurationException {
        getConfiugrationHandler().writeTo(writer);
    }
}
