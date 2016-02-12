package commons.configuration.ext.spring;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:configuration-placeholder-configurer-test.xml" })
public class ConfigurationPlaceholderConfigurerTest {

    @Autowired
    ConfiguredClass _configuredClass;

    @Test
    public void context_configuration() throws Exception {
        assertEquals("value1", _configuredClass._configValue);
    }

    public static class ConfiguredClass {
        String _configValue;

        public void setConfigValue(String configValue) {
            _configValue = configValue;
        }
    }
}
