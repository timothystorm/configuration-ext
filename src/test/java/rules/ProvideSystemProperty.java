package rules;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.rules.ExternalResource;

public class ProvideSystemProperty extends ExternalResource {
    private final Map<String, String> _props = new LinkedHashMap<>();

    public ProvideSystemProperty(String key, String value) {
        addProperty(key, value);
    }

    private void addProperty(String key, String value) {
        _props.put(key, value);
    }

    public ProvideSystemProperty and(String key, String value) {
        addProperty(key, value);
        return this;
    }

    @Override
    protected void before() throws Throwable {
        for (Entry<String, String> property : _props.entrySet()) {
            String key = property.getKey();
            String value = property.getValue();

            if (value == null) System.clearProperty(key);
            else System.setProperty(key, value);
        }
    }

    protected void setProperty(String key, String value) {

    }

    @Override
    protected void after() {
        for (String key : _props.keySet()) {
            System.clearProperty(key);
        }
    }
}
