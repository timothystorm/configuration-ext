package commons.configuration.ext;

import java.io.File;
import java.net.URL;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;

/**
 * A {@link Configuration} that can determine what properties to serve based on the runtime environment.
 * <p>
 * <em>Note:</em>Configuration objects of this type can be read concurrently by multiple threads.
 * 
 * @author Darren Bruxvoort
 * @author Timothy Storm
 * 
 * @see RuntimeConfigurationHandler
 */
public class RuntimeConfiguration extends StrategicConfiguration {

    private volatile RuntimeConfigurationHandler _instance;

    public RuntimeConfiguration() {
        super();
    }

    public RuntimeConfiguration(File file) throws ConfigurationException {
        super(file);
    }

    public RuntimeConfiguration(String fileName) throws ConfigurationException {
        super(fileName);
    }

    @Override
    public void addProperty(String key, Object value) {
        super.addProperty(key, value);
    }

    public RuntimeConfiguration(URL url) throws ConfigurationException {
        super(url);
    }

    private RuntimeConfigurationHandler getHandler() {
        if (_instance == null) {
            synchronized (RuntimeConfiguration.class) {
                if (_instance == null) {
                    Log log = getLogger();
                    if (log.isDebugEnabled()) log.debug("creating " + RuntimeConfigurationHandler.class);
                    _instance = new RuntimeConfigurationHandler(this);
                }
            }
        }
        return _instance;
    }

    @Override
    protected ConfigurationParser getConfigurationParser() {
        return getHandler();
    }

    @Override
    protected ConfigurationComposer getConfigurationComposer() {
        return getHandler();
    }
}
