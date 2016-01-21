package commons.configuration.ext;

import java.io.Reader;
import java.io.Writer;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

/**
 * Handles the loading and saving of a configuration resource to/from an implementation specific format.
 * 
 * @author Timothy Storm
 */
public interface ConfigurationHandler {
    /**
     * Loads configurations read from the source into the {@link Configuration}
     * 
     * @param source
     *            of the configuration properties
     * @param configuration
     *            to load the read properties into
     * @throws ConfigurationException
     *             if anything goes wrong
     */
    void load(Reader source, Configuration configuration) throws ConfigurationException;

    /**
     * Saves the properties from the {@link Configuration} to the writer target
     * 
     * @param destination
     *            to write the properties into
     * @param configuration
     *            to read the properties from
     * @throws ConfigurationException
     *             if anything goes wrong
     */
    void save(Configuration configuration, Writer destination) throws ConfigurationException;
}
