package commons.configuration.ext;

import java.io.Reader;
import java.io.Writer;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handles XML configuration formats. {@link Configuration} association is handled by the implementation. The xml that
 * can be (un)marshalled is implementation specific.
 * 
 * @author Timothy Storm
 */
public abstract class ConfigHandler extends DefaultHandler {
    /**
     * Writes an associated {@link Configuration}'s properties out.  
     * 
     * @param
     * @throws ConfigurationException
     */
    abstract void writeTo(Writer writer) throws ConfigurationException;

    /**
     * Reads xml properties into the associated {@link Configuration}.
     * 
     * @param reader
     * @throws ConfigurationException
     */
    abstract void readFrom(Reader reader) throws ConfigurationException;
}
