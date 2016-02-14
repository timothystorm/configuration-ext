package commons.configuration.ext.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public void basic_property_configurer() throws Exception {
        assertEquals("basicValue", _configuredClass.basicValue);
    }
    
    @Test
    public void index_property_configurer() throws Exception {
        assertEquals("indexValue", _configuredClass.indexValue);
    }

    @Test
    public void nested_property_configurer() throws Exception {
        assertEquals("outerValue[innerValue]", _configuredClass.nestedValue);
    }

    @Test
    public void list_property_configurer() throws Exception {
        List<String> list = _configuredClass.listValue;
        assertNotNull(list);
        assertTrue(list.contains("listValue"));
    }

    @Test
    public void set_property_configurer() throws Exception {
        Set<String> set = _configuredClass.setValue;
        assertNotNull(set);
        assertTrue(set.contains("setValue"));
    }

    @Test
    public void map_property_configurer() throws Exception {
        Map<String, String> map = _configuredClass.mapValue;
        assertNotNull(map);
        assertTrue(map.containsKey("mapKey"));
        assertEquals("mapValue", map.get("mapKey"));
    }

    public static class ConfiguredClass {
        String              indexValue, basicValue, nestedValue;
        List<String>        listValue;
        Set<String>         setValue;
        Map<String, String> mapValue;
        
        public ConfiguredClass(String indexValue){
            this.indexValue = indexValue;
        }

        public void setSetValue(Set<String> setValue) {
            this.setValue = setValue;
        }

        public void setMapValue(Map<String, String> mapValue) {
            this.mapValue = mapValue;
        }

        public void setListValue(List<String> listValue) {
            this.listValue = listValue;
        }

        public void setBasicValue(String basicValue) {
            this.basicValue = basicValue;
        }

        public void setNestedValue(String nestedValue) {
            this.nestedValue = nestedValue;
        }
    }
}
