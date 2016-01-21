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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test behavior of {@link FedExConfiguration}
 */
public class RuntimeConfigurationTest {
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    ConfigurationHandler _handler;
    RuntimeConfiguration _config;

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
}
