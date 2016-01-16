package commons.configuration.ext;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
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
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.NoOpLog;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses and composes a runtime configuration.
 * 
 * @author Darrent Bruxvoort
 * @author Timothy Storm
 * @see runtime-configuration-1.0.0.xsd
 */
class RuntimeConfigurationHandler extends DefaultHandler implements ConfigurationParser, ConfigurationComposer {

    /** Stores the logger. */
    private Log _log;

    /**
     * Allows to set the logger to be used by this handler. This
     * method makes it possible for clients to exactly control logging behavior.
     * Per default a logger is set that will ignore all log messages. Derived
     * classes that want to enable logging should call this method during their
     * initialization with the logger to be used.
     *
     * @param log
     *            the new logger
     * @since 1.4
     */
    public void setLogger(Log log) {
        _log = (log != null) ? log : new NoOpLog();
    }

    /**
     * Returns the logger used by this configuration object.
     *
     * @return the logger
     * @since 1.4
     */
    public Log getLogger() {
        return _log;
    }

    /**
     * Attributes of configuration
     */
    private static class Attr {
        static final String ENV = "env";
        static final String KEY = "key";
    }

    /**
     * Elements of the configuration
     */
    private static class Elem {
        static final String CONFIGURATION = "configuration";
        static final String CONTEXT       = "context";
        static final String HOST          = "host";
        static final String HOSTS         = "hosts";
        static final String PROPERTY      = "property";
        static final String VALUE         = "value";
    }

    /** schema filename used for schema validation */
    private static String _configurationSchema = "runtime-configuration-1.0.0.xsd";

    private static final String ENCODING = "UTF-8";

    /** schema namespace */
    private static final String NAMESPACE = "http://commons.apache.org/schema/runtime-configuration-1.0.0";

    /** configuration callback */
    private final Configuration _config;

    private String _hostEnvironmentState, _propertyKeyState, _propertyEnvironmentState;

    /** matches the runtime environment with the configured host(s) */
    private final HostMatcher _hostMatcher;

    /** <env,[hosts]> */
    Map<String, List<String>> _hosts = new HashMap<>();

    /** <key, <env, value>> */
    Map<String, Map<String, String>> _properties = new HashMap<>();

    /** FSM parse stack */
    Stack<String> _state;

    /** holds the current state of the value */
    StringBuilder _valueState;

    public RuntimeConfigurationHandler(Configuration config) {
        _config = config;
        _hostMatcher = new CompoundHostMatcher(LocalHostMatcher.singleton(), MachineHostMatcher.singleton(),
                MachinePatternHostMatcher.singleton());
        setLogger(null);
    }

    private void assignHost(String host) throws SAXException {
        if (host == null) throw new SAXException("hosts/host element required!");

        String escape = StringEscapeUtils.escapeXml(host);
        _hosts.get(_hostEnvironmentState).add(StringUtils.trim(StringUtils.trim(escape)));
    }

    /**
     * Sets the current host environment being parsed
     * 
     * @param env
     * @throws SAXException
     * @see #assignHost(String)
     */
    private void assignHostEnvironment(String env) throws SAXException {
        if (env == null) throw new SAXException("hots[@env] attribute required!");

        String escape = StringEscapeUtils.escapeXml(env);
        _hosts.put((_hostEnvironmentState = StringUtils.trim(escape)), new ArrayList<String>());
    }

    private void assignProperty(String property) throws SAXException {
        if (property == null) throw new SAXException("hosts:host element required!");
        _properties.get(_propertyKeyState).put(_propertyEnvironmentState, StringUtils.trim(property));
    }

    private void assignPropertyKey(String key) throws SAXException {
        if (key == null) throw new SAXException("property[@key] required!");

        String escape = StringEscapeUtils.escapeXml(key);
        _properties.put((_propertyKeyState = StringUtils.trim(escape)), new HashMap<String, String>());
    }

    private void assignValueEnvironment(String valueEnv) throws SAXException {
        if (valueEnv == null) throw new SAXException("property/value[@env] required!");

        String escape = StringEscapeUtils.escapeXml(valueEnv);
        _propertyEnvironmentState = StringUtils.trim(escape);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        _valueState.append(ch, start, length);
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
    public void compose(final Writer writer) throws ConfigurationException {
        try {
            Log log = getLogger();
            if (log.isDebugEnabled()) log.debug("configuration composition start...");

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
            hosts.setAttribute(Attr.ENV, "0");
            context.appendChild(hosts);

            // host
            Element host = doc.createElement(Elem.HOST);
            host.setTextContent("localhost");
            hosts.appendChild(host);

            // property/values
            for (Iterator<String> keys = _config.getKeys(); keys.hasNext();) {
                String key = keys.next();
                Object value = _config.getProperty(key);

                if (value != null) {
                    // prepare the value
                    String v = StringEscapeUtils.escapeXml(String.valueOf(value));
                    v = StringUtils.replace(v, String.valueOf(','), "\\" + ',');

                    // property
                    Element property = doc.createElement(Elem.PROPERTY);
                    property.setAttribute(Attr.KEY, StringEscapeUtils.escapeXml(key));
                    configuration.appendChild(property);

                    // value
                    Element val = doc.createElement(Elem.VALUE);
                    val.setAttribute(Attr.ENV, "0");
                    val.setTextContent(v);
                    property.appendChild(val);
                }
            }

            // output the results
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.ENCODING, ENCODING);

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);

            if (log.isDebugEnabled()) log.debug("configuration composition end...");
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        for (Entry<Object, Object> entry : getProperties().entrySet()) {
            _config.addProperty((String) entry.getKey(), StringUtils.trim((String) entry.getValue()));
        }
    }

    /**
     * Pops the element state and assigns the element value(s)
     */
    @Override
    public void endElement(String uri, String name, String qName) throws SAXException {
        String elementState = _state.pop();
        if (Elem.HOST.equals(elementState)) assignHost(_valueState.toString());
        if (Elem.VALUE.equals(elementState)) assignProperty(_valueState.toString());

        // setup for the next element
        _valueState.setLength(0);
    }

    /**
     * @return the host env by mapping the config hosts to the host matcher strategy
     * @throws SAXException
     *             if the host env cannot be determined.
     */
    private String getHostEnvironment() throws SAXException {
        StringBuilder hostsTried = new StringBuilder();

        for (Entry<String, List<String>> entry : _hosts.entrySet()) {
            for (String host : entry.getValue()) {
                if (_hostMatcher.matches(host)) return entry.getKey();
                hostsTried.append(host).append(" ");
            }
        }

        throw new SAXException(String.format("No host env found for [" + hostsTried + "]"));
    }

    private Properties getProperties() throws SAXException {
        Properties environmentProperties = new Properties();
        String environment = getHostEnvironment();

        // iterate properties and pull out the appropriate environment value
        for (Entry<String, Map<String, String>> properties : _properties.entrySet()) {
            String key = properties.getKey();
            String environmentValue = properties.getValue().get(environment);
            if (environmentValue != null) environmentProperties.put(key, environmentValue);
        }

        return environmentProperties;
    }

    @Override
    public void parse(Reader reader) throws ConfigurationException {
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

            Log log = getLogger();
            if (log.isDebugEnabled()) getLogger().debug("level configuration parse starting...");
            {
                parser.parse(new InputSource(reader), this);
            }
            if (log.isDebugEnabled()) getLogger().debug("level configuration parse end.");
        } catch (ParserConfigurationException | SAXException |

        IOException e)

        {
            throw new ConfigurationException(e);
        }

    }

    @Override
    public void startDocument() throws SAXException {
        _state = new Stack<>();
        _valueState = new StringBuilder();
    }

    /**
     * Push elements onto the state stack and extracts the attributes.
     */
    @Override
    public void startElement(String uri, String name, String qName, Attributes attr) throws SAXException {
        String elementState = _state.push(StringUtils.lowerCase(name));
        if (Elem.HOSTS.equals(elementState)) assignHostEnvironment(attr.getValue(StringUtils.EMPTY, Attr.ENV));
        if (Elem.PROPERTY.equals(elementState)) assignPropertyKey(attr.getValue(StringUtils.EMPTY, Attr.KEY));
        if (Elem.VALUE.equals(elementState)) assignValueEnvironment(attr.getValue(StringUtils.EMPTY, Attr.ENV));
    }
}
