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
public class XmlConfigurationTest {
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    ConfigHandler    _configHandler;
    XmlConfiguration _config;

    @Before
    public void setUp() throws Exception {
        _configHandler = createMock(ConfigHandler.class);
        _config = new XmlConfiguration() {
            @Override
            protected ConfigHandler getConfiugrationHandler() {
                return _configHandler;
            }
        };
    }

    /**
     * Verify the load is called as expected
     */
    @Test
    public void load() throws Exception {
        // setup expectations
        _configHandler.readFrom(isA(Reader.class));
        replay(_configHandler);

        // setup a test file
        File tmp = tmpFolder.newFile();
        Files.write(tmp.toPath(), "mock data".getBytes());
        _config.load(tmp);

        // verify results
        verify(_configHandler);
    }

    @Test
    public void save() throws Exception {
        _configHandler.writeTo(isA(Writer.class));
        replay(_configHandler);

        // setup a test file
        File tmp = tmpFolder.newFile();
        Files.write(tmp.toPath(), "mock data".getBytes());
        _config.save(tmp);

        // verify results
        verify(_configHandler);
    }

    /**
     * Let's eat our own dog food - a configuration written should also be readable
     */
    @Test
    public void dogFood() throws Exception {
        int bigConfig = 1000;

        // create new configuration
        XmlConfiguration configOut = new XmlConfiguration();
        for (int i = 0; i < bigConfig; i++) {
            configOut.addProperty("key" + i, "value" + i);
        }

        // write configuration to file
        File tmpFile = tmpFolder.newFile();
        configOut.save(tmpFile);

        // for debugging purposes
        // Files.readAllLines(tmpFile.toPath()).forEach(line -> System.out.println(line));

        // read in configuration
        XmlConfiguration configIn = new XmlConfiguration(tmpFile);
        for (int i = 0; i < bigConfig; i++) {
            assertEquals("value" + i, configIn.getProperty("key" + i));
        }
    }
}
