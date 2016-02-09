package commons.configuration.ext;

import static java.lang.String.format;

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
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import commons.configuration.ext.matcher.CompoundHostMatcher;
import commons.configuration.ext.matcher.HostMatcher;
import commons.configuration.ext.matcher.LocalHostMatcher;
import commons.configuration.ext.matcher.MachineHostMatcher;
import commons.configuration.ext.matcher.MachinePatternHostMatcher;

/**
 * Loads and writes runtime configurations.
 * 
 * @author Darren Bruxvoort
 * @author Timothy Storm
 */
public class RuntimeConfigurationHandler extends DefaultHandler implements ConfigurationHandler {
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

    private static final String GLOB_ENV_KEY = "*";

    /** schema namespace */
    private static final String NAMESPACE = "http://commons.apache.org/schema/runtime-configuration-1.0.0";

    /** schema name */
    private static final String SCHEMA = format("%s.xsd", NAMESPACE.substring(NAMESPACE.lastIndexOf('/') + 1));

    private String _hostEnvironmentState, _propertyKeyState, _propertyEnvironmentState;

    /** matches the runtime environment with the configured host(s) */
    private final HostMatcher _hostMatcher;

    /** <env,[hosts,...]> */
    private Map<String, List<String>> _hosts = new HashMap<>();

    /** <key, <env, value>> */
    private Map<String, Map<String, String>> _runtimeProperties = new HashMap<>();

    /** FSM parse stack */
    Stack<String> _state;

    /** holds the current state of the value */
    StringBuilder _valueState;

    public RuntimeConfigurationHandler() {
        // match specifically to generally
        _hostMatcher = new CompoundHostMatcher(MachineHostMatcher.singleton(), MachinePatternHostMatcher.singleton(),
                LocalHostMatcher.singleton());
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
        if (env == null) throw new SAXException("hosts[@env] attribute required!");

        String escape = StringEscapeUtils.escapeXml(env);
        _hosts.put((_hostEnvironmentState = StringUtils.trim(escape)), new ArrayList<String>());
    }

    private void assignProperty(String property) throws SAXException {
        if (property == null) throw new SAXException("hosts:host element required!");
        _runtimeProperties.get(_propertyKeyState).put(_propertyEnvironmentState, StringUtils.trim(property));
    }

    private void assignPropertyKey(String key) throws SAXException {
        if (key == null) throw new SAXException("property[@key] required!");

        String escape = StringEscapeUtils.escapeXml(key);
        _runtimeProperties.put((_propertyKeyState = StringUtils.trim(escape)), new HashMap<String, String>());
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
    private String getHostEnvironment() throws ConfigurationException {
        StringBuilder hostsTried = new StringBuilder();

        for (Entry<String, List<String>> entry : _hosts.entrySet()) {
            for (String host : entry.getValue()) {
                if (_hostMatcher.matches(host)) return entry.getKey();
                hostsTried.append(host).append(" ");
            }
        }

        throw new ConfigurationException(String.format("No host[@env] found for [" + hostsTried + "]"));
    }

    /**
     * Creates {@link Properties} with only the runtime values
     * 
     * @return runtime {@link Properties}s
     * @throws SAXException
     */
    private Properties getRuntimeProperties() throws ConfigurationException {
        final Properties runtimeProperties = new Properties();
        final String env = getHostEnvironment();

        for (Entry<String, Map<String, String>> properties : _runtimeProperties.entrySet()) {
            String key = properties.getKey();
            String value = null;
            if (value == null /* global */) value = properties.getValue().get(GLOB_ENV_KEY);
            if (value == null /* environment */) value = properties.getValue().get(env);
            if (value != null) runtimeProperties.put(key, value);
        }

        return runtimeProperties;
    }

    /**
     * {@inheritDoc}
     * Loads a runtime xml configuration source into the target {@link Configuration}
     */
    @Override
    public synchronized void load(Reader source, Configuration config) throws ConfigurationException {
        if (config == null) throw new NullPointerException();

        try {
            // setup the schema
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(ClassPathUtils.loadResource(SCHEMA));

            // setup the parser factory
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setSchema(schema);
            factory.setValidating(true);
            factory.setNamespaceAware(true);

            // parse the source
            SAXParser parser = factory.newSAXParser();
            parser.parse(new InputSource(source), this);

            // push the parsed properties into the configuration
            for (Entry<Object, Object> entry : getRuntimeProperties().entrySet()) {
                config.addProperty((String) entry.getKey(), StringUtils.trim((String) entry.getValue()));
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * {@inheritDoc}
     * saves a zero level xml configuration template
     */
    @Override
    public synchronized void save(final Configuration config, final Writer destination) throws ConfigurationException {
        if (config == null) throw new NullPointerException();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder;
            docBuilder = factory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            doc.setXmlVersion("1.0");

            // configuration
            Element configuration = doc.createElementNS(NAMESPACE, Elem.CONFIGURATION);
            configuration.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "xs:schemaLocation",
                    NAMESPACE + " " + SCHEMA);

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
            for (Iterator<String> keys = config.getKeys(); keys.hasNext();) {
                String key = keys.next();
                Object value = config.getProperty(key);

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
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(destination);
            transformer.transform(source, result);
        } catch (ParserConfigurationException | TransformerException e) {
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
