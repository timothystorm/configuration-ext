package commons.configuration.ext.spring;

import static org.junit.Assert.assertEquals;

import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:configuration-factory-test.xml" })
public class ConfigurationFactoryTest {

    @Autowired
    Configurable _configurable;

    @Test
    public void context_configuration() throws Exception {
        Configuration config = _configurable.getConfiguration();

        assertEquals(config.getString("key1"), "value1");
        assertEquals(config.getString("key2"), "value2");
    }

    /**
     * Mocks a real class that would need to have a {@link Configuration} injected
     */
    public static class Configurable {
        private Configuration _config;

        public void setConfiguration(Configuration config) {
            _config = config;
        }

        public Configuration getConfiguration() {
            return _config;
        }
    }
}
