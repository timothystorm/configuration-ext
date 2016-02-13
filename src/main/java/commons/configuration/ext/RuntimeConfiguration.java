package commons.configuration.ext;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * A {@link Configuration} that can determine what properties to serve based on the runtime environment.
 * <p>
 * <em>Note:</em>Configuration objects of this type can be read concurrently by multiple threads.
 * 
 * @author Darren Bruxvoort
 * @author Timothy Storm
 * @see RuntimeConfigurationHandler
 */
public class RuntimeConfiguration extends PropertiesConfiguration {

    public RuntimeConfiguration() {
        super();
    }

    public RuntimeConfiguration(File file) throws ConfigurationException {
        super(file);
    }

    public RuntimeConfiguration(String fileName) throws ConfigurationException {
        super(fileName);
    }

    public RuntimeConfiguration(URL url) throws ConfigurationException {
        super(url);
    }

    protected ConfigurationHandler getConfigurationHandler() {
        return new RuntimeConfigurationHandler();
    }

    @Override
    public synchronized void load(Reader reader) throws ConfigurationException {
        getConfigurationHandler().load(reader, this);
    }

    @Override
    public void save(Writer writer) throws ConfigurationException {
        getConfigurationHandler().save(this, writer);
    }
}
