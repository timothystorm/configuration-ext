package commons.configuration.ext;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;

/**
 * Loads an xml schema from the META-INF schemas definition
 * 
 * @author Timothy Storm
 */
class RuntimeXmlSchema {
    private final String _namespace, _path;

    private static volatile RuntimeXmlSchema _instance;

    private static final String SCHEMA_DEFINITION = "META-INF/configuration-ext.schemas";

    public static RuntimeXmlSchema instance() throws ConfigurationException {
        if (_instance == null) {
            synchronized (RuntimeXmlSchema.class) {
                if (_instance == null) {
                    try {
                        // read schema definition
                        Properties props = new Properties();
                        props.load(ClassPathUtils.readResource(SCHEMA_DEFINITION));

                        // verify schema definition
                        int size = props.size();
                        if (size <= 0 || size > 1) throw new ConfigurationException(
                                "ambiguous! Are there more than one namespace/schema defition at '" + SCHEMA_DEFINITION
                                        + "'");

                        String namespace = (String) props.keys().nextElement();
                        _instance = new RuntimeXmlSchema(namespace, props.getProperty(namespace));
                    } catch (IOException e) {
                        throw new ConfigurationException("failed to compute schema", e);
                    }
                }
            }
        }
        return _instance;
    }

    private RuntimeXmlSchema(String namespace, String path) {
        _namespace = namespace;
        _path = path;
    }

    public String getNamespace() {
        return _namespace;
    }

    public String getPath() {
        return _path;
    }

    public String getSchema() {
        return _path.substring(_path.lastIndexOf("/") + 1);
    }
}
