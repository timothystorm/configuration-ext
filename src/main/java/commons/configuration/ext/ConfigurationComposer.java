package commons.configuration.ext;

import java.io.Writer;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

/**
 * Contract for implementations that will compose configurations to an external resource.
 * 
 * @author Timothy Storm
 */
public interface ConfigurationComposer {
    /**
     * Writes an associated {@link Configuration}'s properties out.
     * 
     * @param writer
     *            to compose the configuration to.
     * @throws ConfigurationException
     *             on any errors
     */
    void compose(Writer writer) throws ConfigurationException;
}
