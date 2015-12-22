package commons.configuration.ext;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Stack;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This {@link Configuration} implements a specialized configuration xml format that permits property selection based on
 * the deployment environment. A configuration file looks like this:
 * 
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 * &lt;conf:configuration xmlns:conf="http://commons.apache.org/schema/configuration"
 *     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *     xsi:schemaLocation="http://commons.apache.org/schema/configuration configuration-1.0.0.xsd "&gt;
 *     &lt;conf:context&gt;
 *         &lt;!-- List the hosts names and/or IPs that are part of the env -- /&gt;
 *         &lt;conf:hosts env="L0"&gt;
 *            &lt;conf:host&gt;localhost&lt;/conf:host&gt;
 *         &lt;/conf:hosts&gt;
 *         &lt;conf:hosts env="L1"&gt;
 *            &lt;conf:host&gt;uje70831.idev.env.com&lt;/conf:host&gt;
 *            &lt;conf:host&gt;10.255.61.171&lt;/conf:host&gt;
 *            &lt;conf:host&gt;dje70831.idev.env.com&lt;/conf:host&gt;
 *            &lt;conf:host&gt;10.255.61.170&lt;/conf:host&gt;
 *         &lt;/conf:hosts&gt;
 *         &lt;conf:hosts env="L2"&gt;
 *            &lt;conf:host&gt;ije70831.idev.env.com&lt;/conf:host&gt;
 *            &lt;conf:host&gt;10.255.61.172&lt;/conf:host&gt;
 *            &lt;conf:host&gt;ije70832.idev.env.com&lt;/conf:host&gt;
 *            &lt;conf:host&gt;10.255.61.173&lt;/conf:host&gt;
 *         &lt;/conf:hosts&gt;
 *     &lt;/conf:context&gt;
 *         
 *     &lt;!-- Environment properties -- /&gt;
 *     &lt;conf:property key="return-address"&gt;
 *        &lt;conf:value env="L0"&gt;dead.letter@env.com&lt;/conf:value&gt;
 *        &lt;conf:value env="L1"&gt;dev.team@env.com&lt;/conf:value&gt;
 *        &lt;conf:value env="L2"&gt;qa.team@env.com&lt;/conf:value&gt;
 *     &lt;/conf:property&gt;
 *     
 *     &lt;!-- Global property -- /&gt;
 *     &lt;conf:property key="key"&gt;
 *        &lt;conf:value env="*"&gt;global-value&lt;/conf:value&gt;
 *     &lt;/conf:property&gt;
 *         
 * &lt;/conf:configuration&gt;
 * </pre>
 * 
 * <em>Note:</em>Configuration objects of this type can be read concurrently by multiple threads.
 * <p>
 * TODO: make the schema reader strategic and injectable
 * 
 * @author Darren Bruxvoort
 * @author Timothy Storm
 * @see #save(Writer)
 */
public class EnvConfiguration extends PropertiesConfiguration {
    /**
     * Attributes of env-configuration
     */
    private static class Attr {
        static final String ENV = "env";
        static final String KEY = "key";
    }

    /**
     * Container for env-config.xml resources.
     */
    private static class Config_1_0_0 {
        private String _hostEnv, _propKey, _propEnv;

        /** <env,[hosts]> */
        Map<String, List<String>> _hosts = new HashMap<>();

        /** <key, <env, value>> */
        Map<String, Map<String, String>> _props = new HashMap<>();

        void addHost(String host) throws SAXException {
            if (host == null) throw new SAXException("hosts:host element required!");

            String escape = StringEscapeUtils.escapeXml(host);
            _hosts.get(_hostEnv).add(StringUtils.trim(StringUtils.trim(escape)));
        }

        void addProperty(String property) throws SAXException {
            if (property == null) throw new SAXException("hosts:host element required!");
            _props.get(_propKey).put(_propEnv, StringUtils.trim(property));
        }

        /**
         * @return the host env by mapping the config hosts to the current host name/address
         * @throws SAXException
         *             if the host env cannot be determined.
         */
        private String getHostEnv() throws SAXException {
            for (Entry<String, List<String>> entry : _hosts.entrySet()) {
                for (String host : entry.getValue()) {
                    if ("localhost".equalsIgnoreCase(host)) return entry.getKey();
                    if ("127.0.0.1".equalsIgnoreCase(host)) return entry.getKey();
                    if ("::1".equalsIgnoreCase(host)) return entry.getKey();
                    if (MachineUtils.hostName().equalsIgnoreCase(host)) return entry.getKey();
                    if (MachineUtils.hostAddress().equalsIgnoreCase(host)) return entry.getKey();
                }
            }

            throw new SAXException(String.format("No host env found for [localhost|%s|%s]", MachineUtils.hostName(),
                    MachineUtils.hostAddress()));
        }

        private Properties getProperties() throws SAXException {
            Properties props = new Properties();
            String env = getHostEnv();

            // iterate properties and pull out the appropriate env'ed value
            for (Entry<String, Map<String, String>> properties : _props.entrySet()) {
                String key = properties.getKey();
                String value = properties.getValue().get(env);
                if (value != null) props.put(key, value);
            }

            return props;
        }

        /**
         * Sets the current host environment being parsed
         * 
         * @param env
         * @throws SAXException
         * @see #addHost(String)
         */
        void setHostEnv(String env) throws SAXException {
            if (env == null) throw new SAXException("hosts:env attribute required!");

            String escape = StringEscapeUtils.escapeXml(env);
            _hosts.put((_hostEnv = StringUtils.trim(escape)), new ArrayList<String>());
        }

        void setPropertyEnv(String env) throws SAXException {
            if (env == null) throw new SAXException("prop:env attribute required!");

            String escape = StringEscapeUtils.escapeXml(env);
            _propEnv = StringUtils.trim(escape);
        }

        void setPropertyKey(String key) throws SAXException {
            if (key == null) throw new SAXException("properties:key attribute required!");

            String escape = StringEscapeUtils.escapeXml(key);
            _props.put((_propKey = StringUtils.trim(escape)), new HashMap<String, String>());
        }
    }

    /**
     * Elements of the env-configuration
     */
    private static class Elem {
        static final String CONFIGURATION = "configuration";
        static final String CONTEXT       = "context";
        static final String HOST          = "host";
        static final String HOSTS         = "hosts";
        static final String PROPERTY      = "property";
        static final String VALUE         = "value";
    }

    /**
     * XML parse handler for env-config.xml resources [finite state machine]
     */
    private class FedExConfigurationHandler extends DefaultHandler {
        Config_1_0_0  _config;
        Stack<String> _state;
        StringBuilder _str;

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            _str.append(ch, start, length);
        }

        @Override
        public void endDocument() throws SAXException {
            for (Entry<Object, Object> entry : _config.getProperties().entrySet()) {
                addProperty((String) entry.getKey(), StringUtils.trim((String) entry.getValue()));
            }
        }

        @Override
        public void endElement(String uri, String name, String qName) throws SAXException {
            String element = _state.peek();
            if (Elem.HOST.equals(element)) _config.addHost(_str.toString());
            if (Elem.VALUE.equals(element)) _config.addProperty(_str.toString());

            // setup for the nexte element
            _state.pop();
            _str.setLength(0);
        }

        @Override
        public void startDocument() throws SAXException {
            _state = new Stack<>();
            _config = new Config_1_0_0();
            _str = new StringBuilder();
        }

        @Override
        public void startElement(String uri, String name, String qName, Attributes attr) throws SAXException {
            _state.push(StringUtils.lowerCase(name));

            String element = _state.peek();
            if (Elem.HOSTS.equals(element)) _config.setHostEnv(attr.getValue(StringUtils.EMPTY, Attr.ENV));
            if (Elem.PROPERTY.equals(element)) _config.setPropertyKey(attr.getValue(StringUtils.EMPTY, Attr.KEY));
            if (Elem.VALUE.equals(element)) _config.setPropertyEnv(attr.getValue(StringUtils.EMPTY, Attr.ENV));
        }
    }

    /**
     * schema filename used for schema validation
     */
    private static String _configurationSchema = "configuration-1.0.0.xsd";
    static final String   ENCODING             = "UTF-8";

    static final String NAMESPACE = "http://commons.apache.org/schema/configuration";

    // initialization block to set the encoding before loading the file in the constructors
    {
        setEncoding(ENCODING);
    }

    /**
     * Creates an empty FedExConfiguration object which can be used to synthesize a new Properties file by adding values
     * and then saving().
     */
    public EnvConfiguration() {
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
    public EnvConfiguration(File file) throws ConfigurationException {
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
    public EnvConfiguration(String fileName) throws ConfigurationException {
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
    public EnvConfiguration(URL url) throws ConfigurationException {
        super(url);
    }

    @Override
    public synchronized void load(Reader in) throws ConfigurationException {
        try {
            // setup the schema
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(ClassPathUtils.loadResource(_configurationSchema));

            // setup the parser factory
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setSchema(schema);
            factory.setValidating(true);
            factory.setNamespaceAware(true);

            // parse the input
            SAXParser parser = factory.newSAXParser();
            parser.parse(new InputSource(in), new FedExConfigurationHandler());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Writes a template to be enriched with the particulars. This can also be used to create a template for further
     * configurations.
     * <p>
     * Create a new configuration...
     * 
     * <pre>
     * EnvironmentConfiguration config = new EnvironmentConfiguration();
     * config.addProperty("key1", "value1");
     * config.addProperty("key2", "value2");
     * 
     * File configFile = new File("my-config.xml");
     * config.save(configFile);
     * </pre>
     * <p>
     * Generates configuration file...
     * 
     * <pre>
     * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
     * &lt;configuration xmlns:conf="http://commons.apache.org/schema/configuration" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://commons.apache.org/schema/configuration configuration-1.0.0.xsd"&gt;
     *   &lt;context&gt;
     *       &lt;hosts env="0"&gt;
     *         &lt;host&gt;CFSIT111111.corp.ds.env.com&lt;/host&gt;
     *       &lt;/hosts&gt;
     *   &lt;/context&gt;
     *   
     *   &lt;property key="key1"&gt;
     *       &lt;value env="0"&gt;value1&lt;/value&gt;
     *   &lt;/property&gt;
     *   
     *   &lt;property key="key2"&gt;
     *       &lt;value env="0"&gt;value2&lt;/value&gt;
     *   &lt;/property&gt;
     *&lt;/configuration&gt;
     * </pre>
     */
    @Override
    public void save(Writer out) throws ConfigurationException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            doc.setXmlVersion("1.0");

            // configuration
            Element configuration = doc.createElementNS(NAMESPACE, Elem.CONFIGURATION);
            configuration.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "xs:schemaLocation",
                    String.join(" ", NAMESPACE, _configurationSchema));
            doc.appendChild(configuration);

            // context
            Element context = doc.createElement(Elem.CONTEXT);
            configuration.appendChild(context);

            // hosts
            Element hosts = doc.createElement(Elem.HOSTS);
            hosts.setAttribute("env", "0");
            context.appendChild(hosts);

            // host
            Element host = doc.createElement(Elem.HOST);
            host.setTextContent("localhost");
            hosts.appendChild(host);

            // property/values
            for (Iterator<String> keys = getKeys(); keys.hasNext();) {
                String key = keys.next();
                Object value = getProperty(key);

                if (value != null) {
                    // prepare the value
                    String v = StringEscapeUtils.escapeXml(String.valueOf(value));
                    v = StringUtils.replace(v, String.valueOf(getListDelimiter()), "\\" + getListDelimiter());

                    // property
                    Element property = doc.createElement("property");
                    property.setAttribute("key", StringEscapeUtils.escapeXml(key));
                    configuration.appendChild(property);

                    // value
                    Element val = doc.createElement("value");
                    val.setAttribute("env", "0");
                    val.setTextContent(v);
                    property.appendChild(val);
                }
            }

            // output the results
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.ENCODING, getEncoding() != null ? getEncoding() : ENCODING);

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }
}
