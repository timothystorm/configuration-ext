package commons.configuration.ext;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import commons.configuration.ext.resolver.MachineUtils;

/**
 * Test behavior of {@link FedExConfiguration}
 */
public class RuntimeConfigurationTest {
    RuntimeConfiguration _config;

    ConfigurationHandler   _handler;
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test(expected = ConfigurationException.class)
    public void ambiguous() throws Exception {
        // write an ambiguous configuration
        File tmp = tmpFolder.newFile();
        Files.write(tmp.toPath(), ambiguousXml().getBytes());

        // expect a failure due to ambiguity
        new RuntimeConfiguration(tmp);
    }

    /**
     * Creates an ambiguous environments
     */
    String ambiguousXml() throws Exception {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        xml.append(
                "<configuration xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\" xs:schemaLocation=\"http://commons.apache.org/schema/runtime-configuration-1.0.0.xsd runtime-configuration-1.0.0.xsd\" xmlns=\"http://commons.apache.org/schema/runtime-configuration-1.0.0\">");
        {
            xml.append("<context>");
            {
                xml.append("<hosts env=\"0\">");
                {
                    // resolves localhost
                    xml.append("<host>" + MachineUtils.hostName() + "</host>");
                    xml.append("<host>localhost</host>");
                }
                xml.append("</hosts>");

                xml.append("<hosts env=\"1\">");
                {
                    // resolves ALL domains
                    xml.append("<host>/.*/</host>");
                }
                xml.append("</hosts>");
            }
            xml.append("</context>");
        }
        xml.append("</configuration>");
        return xml.toString();
    }

    /**
     * Let's eat our own dog food - a configuration written should also be readable
     */
    @Test
    public void dogFood() throws Exception {
        // create new configuration
        RuntimeConfiguration configOut = new RuntimeConfiguration();
        configOut.addProperty("key", "value");

        // write configuration to file
        File tmpFile = tmpFolder.newFile();
        configOut.save(tmpFile);

        // for debugging purposes
        // Files.readAllLines(tmpFile.toPath()).forEach(line -> System.out.println(line));

        // read in configuration
        RuntimeConfiguration configIn = new RuntimeConfiguration(tmpFile);
        assertEquals("value", configIn.getProperty("key"));
    }

    /**
     * Verify the load is called as expected
     */
    @Test
    public void load() throws Exception {
        // setup expectations
        _handler.load(isA(Reader.class), isA(RuntimeConfiguration.class));
        replay(_handler);

        // setup a test file
        File tmp = tmpFolder.newFile();
        Files.write(tmp.toPath(), "mock data".getBytes());
        _config.load(tmp);

        // verify results
        verify(_handler);
    }

    @Test
    public void save() throws Exception {
        _handler.save(isA(Configuration.class), isA(Writer.class));
        replay(_handler);

        // setup a test file
        File tmp = tmpFolder.newFile();
        Files.write(tmp.toPath(), "mock data".getBytes());
        _config.save(tmp);

        // verify results
        verify(_handler);
    }

    @Before
    public void setUp() throws Exception {
        _handler = createMock(ConfigurationHandler.class);
        _config = new RuntimeConfiguration() {
            @Override
            protected ConfigurationHandler getConfigurationHandler() {
                return _handler;
            }
        };
    }
}
