package commons.configuration.ext;

import java.io.Reader;

import org.apache.commons.configuration.ConfigurationException;

/**
 * Contract for implementations that will parse configurations from an external resource.
 * 
 * @author Timothy Storm
 */
public interface ConfigurationParser {
    /**
     * Parses properties from a configuration source.
     * 
     * @param reader
     *            to pull configuration resource from
     * @throws ConfigurationException
     *             for any parse errors
     */
    abstract void parse(Reader reader) throws ConfigurationException;
}
