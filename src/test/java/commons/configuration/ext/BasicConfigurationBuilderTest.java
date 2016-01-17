package commons.configuration.ext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.nio.file.Files;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import rules.ProvideSystemProperty;

public class BasicConfigurationBuilderTest {
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Rule
    public final ProvideSystemProperty sysProp = new ProvideSystemProperty("systemTestKey", "systemTestValue");

    @Test
    public void happy_path() throws Exception {
        BasicConfigurationBuilder builder = new BasicConfigurationBuilder();
        builder.setConfigurations(mockPropertiesConfig(), mockEnvConfig());
        Configuration config = builder.getConfiguration();

        assertNotNull(config.getString("application.version"));
        assertEquals(100, config.getInt("env_key"));
    }

    Configuration mockEnvConfig() {
        try {
            RuntimeConfiguration config = new RuntimeConfiguration();
            config.addProperty("env_key", "100");
            return config;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Configuration mockPropertiesConfig() {
        try {
            File versionFile = ResourceUtils.getFile("classpath:version.properties");
            return new PropertiesConfiguration(versionFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void properties_sys_configuration() throws Exception {
        // setup configuration builder
        BasicConfigurationBuilder builder = new BasicConfigurationBuilder();
        builder.useSystemProperties();

        // get configuraitons
        Configuration config = builder.getConfiguration();
        assertEquals("systemTestValue", config.getString("systemTestKey"));
    }

    @Test
    public void properties_file_configuration() throws Exception {
        // setup properties file
        File file = tmpFolder.newFile("test.properties");
        Files.write(file.toPath(), "prop.key=prop.value".getBytes());

        // setup configuration builder
        BasicConfigurationBuilder builder = new BasicConfigurationBuilder();
        builder.setPropertiesConfiguration("file:" + file.getAbsolutePath());

        // get configuraitons
        Configuration config = builder.getConfiguration();
        assertEquals("prop.value", config.getString("prop.key"));
    }
}
