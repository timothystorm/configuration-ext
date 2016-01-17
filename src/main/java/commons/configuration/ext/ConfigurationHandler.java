package commons.configuration.ext;

import java.io.Reader;
import java.io.Writer;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

public interface ConfigurationHandler {
    void load(Reader source, Configuration configuration) throws ConfigurationException;

    void save(Writer destination, Configuration configuration) throws ConfigurationException;
}
