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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
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
 * @author Timothy Storm
 */
class ConfigHandler_1_0_0 extends ConfigHandler {

    /**
     * Attributes of env-configuration
     */
    private static class Attr {
        static final String ENV = "env";
        static final String KEY = "key";
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
     * schema filename used for schema validation
     */
    private static String _configurationSchema = "env-configuration-1.0.0.xsd";

    private static final String ENCODING = "UTF-8";

    private static final String NAMESPACE = "http://commons.apache.org/schema/env-configuration-1.0.0";

    /** configuration callback */
    private final Configuration _config;
    private String              _hostEnv, _propKey, _propEnv;

    private final HostMatcher _hostMatcher;

    /** <env,[hosts]> */
    Map<String, List<String>>        _hosts = new HashMap<>();
    /** <key, <env, value>> */
    Map<String, Map<String, String>> _props = new HashMap<>();

    Stack<String> _state;

    StringBuilder _str;

    public ConfigHandler_1_0_0(Configuration config) {
        _config = config;
        _hostMatcher = new CompoundHostMatcher(LocalHostMatcher.singleton(), MachineHostMatcher.singleton(),
                MachinePatternHostMatcher.singleton());
    }

    void addHost(String host) throws SAXException {
        if (host == null) throw new SAXException("hosts:host element required!");

        String escape = StringEscapeUtils.escapeXml(host);
        _hosts.get(_hostEnv).add(StringUtils.trim(StringUtils.trim(escape)));
    }

    void addProp(String property) throws SAXException {
        if (property == null) throw new SAXException("hosts:host element required!");
        _props.get(_propKey).put(_propEnv, StringUtils.trim(property));
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        _str.append(ch, start, length);
    }

    @Override
    public void endDocument() throws SAXException {
        for (Entry<Object, Object> entry : getProperties().entrySet()) {
            _config.addProperty((String) entry.getKey(), StringUtils.trim((String) entry.getValue()));
        }
    }

    @Override
    public void endElement(String uri, String name, String qName) throws SAXException {
        String element = _state.peek();
        if (Elem.HOST.equals(element)) addHost(_str.toString());
        if (Elem.VALUE.equals(element)) addProp(_str.toString());

        // setup for the nexte element
        _state.pop();
        _str.setLength(0);
    }

    /**
     * @return the host env by mapping the config hosts to the host matcher strategy
     * @throws SAXException
     *             if the host env cannot be determined.
     */
    protected String getHostEnv() throws SAXException {
        StringBuilder attempts = new StringBuilder();

        for (Entry<String, List<String>> entry : _hosts.entrySet()) {
            for (String host : entry.getValue()) {
                if (_hostMatcher.matches(host)) return entry.getKey();
                attempts.append(host).append(" ");
            }
        }

        throw new SAXException(String.format("No host env found for [" + attempts + "]"));
    }

    Properties getProperties() throws SAXException {
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

    @Override
    void readFrom(Reader reader) throws ConfigurationException {
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
            parser.parse(new InputSource(reader), this);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new ConfigurationException(e);
        }
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

    @Override
    public void startDocument() throws SAXException {
        _state = new Stack<>();
        _str = new StringBuilder();
    }

    @Override
    public void startElement(String uri, String name, String qName, Attributes attr) throws SAXException {
        _state.push(StringUtils.lowerCase(name));

        String element = _state.peek();
        if (Elem.HOSTS.equals(element)) setHostEnv(attr.getValue(StringUtils.EMPTY, Attr.ENV));
        if (Elem.PROPERTY.equals(element)) setPropertyKey(attr.getValue(StringUtils.EMPTY, Attr.KEY));
        if (Elem.VALUE.equals(element)) setPropertyEnv(attr.getValue(StringUtils.EMPTY, Attr.ENV));
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
    void writeTo(final Writer writer) throws ConfigurationException {
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
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }
}
