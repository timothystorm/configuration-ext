package commons.configuration.ext;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import commons.configuration.ext.EnvConfiguration;
import commons.configuration.ext.BasicConfigurationBuilder;
import commons.configuration.ext.ResourceUtils;

public class BasicConfigurationBuilderTest {
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void getConfiguration() throws Exception {
        BasicConfigurationBuilder builder = new BasicConfigurationBuilder();
        builder.setConfigurations(mockPropertiesConfig(), mockEnvConfig());
        Configuration config = builder.getConfiguration();

        assertNotNull(config.getString("application.version"));
        assertEquals(100, config.getInt("env_key"));
    }

    Configuration mockPropertiesConfig() {
        try {
            File versionFile = ResourceUtils.getFile("classpath:version.properties");
            return new PropertiesConfiguration(versionFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Configuration mockEnvConfig() {
        try {
            EnvConfiguration config = new EnvConfiguration();
            config.addProperty("env_key", "100");
            return config;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
