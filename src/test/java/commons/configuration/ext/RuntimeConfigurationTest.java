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

    ConfigurationParser    _parser;
    ConfigurationComposer  _composer;
    StrategicConfiguration _config;

    @Before
    public void setUp() throws Exception {
        _parser = createMock(ConfigurationParser.class);
        _composer = createMock(ConfigurationComposer.class);

        _config = new RuntimeConfiguration() {
            @Override
            protected ConfigurationParser getConfigurationParser() {
                return _parser;
            }

            @Override
            protected ConfigurationComposer getConfigurationComposer() {
                return _composer;
            }
        };
    }

    /**
     * Verify the load is called as expected
     */
    @Test
    public void load() throws Exception {
        // setup expectations
        _parser.parse(isA(Reader.class));
        replay(_parser);

        // setup a test file
        File tmp = tmpFolder.newFile();
        Files.write(tmp.toPath(), "mock data".getBytes());
        _config.load(tmp);

        // verify results
        verify(_parser);
    }

    @Test
    public void save() throws Exception {
        _composer.compose(isA(Writer.class));
        replay(_composer);

        // setup a test file
        File tmp = tmpFolder.newFile();
        Files.write(tmp.toPath(), "mock data".getBytes());
        _config.save(tmp);

        // verify results
        verify(_composer);
    }

    /**
     * Let's eat our own dog food - a configuration written should also be readable
     */
    @Test
    public void dogFood() throws Exception {
        // create new configuration
        StrategicConfiguration configOut = new RuntimeConfiguration();
        configOut.addProperty("key", "value");

        // write configuration to file
        File tmpFile = tmpFolder.newFile();
        configOut.save(tmpFile);

        // for debugging purposes
        // Files.readAllLines(tmpFile.toPath()).forEach(line -> System.out.println(line));

        // read in configuration
        StrategicConfiguration configIn = new RuntimeConfiguration(tmpFile);
        assertEquals("value", configIn.getProperty("key"));
    }
}
