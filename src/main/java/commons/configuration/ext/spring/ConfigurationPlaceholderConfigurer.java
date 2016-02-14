package commons.configuration.ext.spring;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;

/**
 * <p>
 * A configuration resource configurer that resolves placeholders in bean property values of context definitions. It
 * pulls
 * values from a configuration into bean definitions.
 * The default placeholder syntax follows the JSF style:
 * #{...}
 * <p>
 * Example XML context definition:
 * 
 * <pre>
 * &lt;!-- Prepare configuration --&gt;
 * &lt;bean id="AppConfig" class="commons.configuration.ext.spring.ConfigurationFactory"&gt;
 *      &lt;property name="configurations"&gt;
 *          &lt;list&gt;
 *              &lt;bean class="commons.configuration.ext.RuntimeConfiguration"&gt;
 *                  &lt;constructor-arg
 *                      value="classpath:application-config.xml"
 *                      type="java.io.File" /&gt;
 *              &lt;/bean&gt;
 *          &lt;/list&gt;
 *      &lt;/property&gt;
 *  &lt;/bean&gt;
 *
 *  &lt;!-- Prepare configuration placeholder --&gt;
 *  &lt;bean
 *      class="commons.configuration.ext.spring.ConfigurationPlaceholderConfigurer"&gt;
 *      &lt;property name="configuration" ref="AppConfig" /&gt;
 *  &lt;/bean&gt;
 *  
 *  &lt;!-- Use configuration placeholder(s) --&gt;
 *  &lt;bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource"&gt;
 *    &lt;property name="driverClassName"&gt;&lt;value&gt;#{driver}&lt;/value&gt;&lt;/property&gt;
 *    &lt;property name="url"&gt;&lt;value&gt;jdbc:#{dbname}&lt;/value&gt;&lt;/property&gt;
 *  &lt;/bean&gt;
 * </pre>
 * 
 * @author Timothy Storm
 */
public class ConfigurationPlaceholderConfigurer extends ConfigurationResourceConfigurer {
    public static final String DEFAULT_PLACEHODER_PREFIX = "#{";
    public static final String DEFAULT_PLACEHODER_SUFFIX = "}";

    private boolean _ignoreUnresolvablePlaceholders = false;
    private String  _placeholderPrefix              = DEFAULT_PLACEHODER_PREFIX;
    private String  _placeholderSuffix              = DEFAULT_PLACEHODER_SUFFIX;

    private boolean nullSafeEquals(Object o1, Object o2) {
        return (o1 == o2 || (o1 != null && o1.equals(o2)));
    }

    protected void parseBeanDefinition(Configuration config, BeanDefinition definition) {
        MutablePropertyValues mpv = definition.getPropertyValues();
        if (mpv != null) parseConfigurationValue(config, mpv);

        ConstructorArgumentValues cav = definition.getConstructorArgumentValues();
        if (cav != null) {
            parseIndexedArgValues(config, cav.getIndexedArgumentValues());
            parseGenericArgValues(config, cav.getGenericArgumentValues());
        }
    }

    protected void parseConfigurationValue(Configuration config, MutablePropertyValues mpv) {
        for (PropertyValue pv : mpv.getPropertyValues()) {
            Object newValue = parseValue(config, pv.getValue());
            if (!nullSafeEquals(newValue, pv.getValue())) mpv.addPropertyValue(pv.getName(), newValue);
        }
    }

    protected void parseDefinition(Configuration config, BeanDefinition definition) {
        MutablePropertyValues mpv = definition.getPropertyValues();
        if (mpv != null) parseConfigurationValue(config, mpv);

        ConstructorArgumentValues cav = definition.getConstructorArgumentValues();
        if (cav != null) {
            parseIndexedArgValues(config, cav.getIndexedArgumentValues());
            parseGenericArgValues(config, cav.getGenericArgumentValues());
        }
    }

    @SuppressWarnings("rawtypes")
    protected void parseGenericArgValues(Configuration config, List list) {
        for (int i = 0; i < list.size(); i++) {
            ConstructorArgumentValues.ValueHolder valueHolder = (ConstructorArgumentValues.ValueHolder) list.get(i);
            Object newValue = parseValue(config, valueHolder.getValue());
            if (!nullSafeEquals(newValue, valueHolder.getValue())) valueHolder.setValue(newValue);
        }
    }

    @SuppressWarnings("rawtypes")
    protected void parseIndexedArgValues(Configuration config, Map map) {
        for (Iterator<?> mapIt = map.keySet().iterator(); mapIt.hasNext();) {
            Integer index = (Integer) mapIt.next();

            ConstructorArgumentValues.ValueHolder valueHolder = (ConstructorArgumentValues.ValueHolder) map.get(index);
            Object newValue = parseValue(config, valueHolder.getValue());
            if (!nullSafeEquals(newValue, valueHolder.getValue())) valueHolder.setValue(newValue);
        }
    }

    /**
     * Parse the given List, exchanging its values if necessary
     * 
     * @param config
     *            configuration resolve placeholders against
     * @param list
     *            to parse
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void parseList(Configuration config, List list) {
        for (int i = 0; i < list.size(); i++) {
            Object elem = list.get(i);
            Object newValue = parseValue(config, elem);
            if (!nullSafeEquals(newValue, elem)) list.set(i, newValue);
        }
    }

    /**
     * Parse the given Map, exchanging its values if necessary
     * 
     * @param config
     *            configuration resolve placeholders against
     * @param map
     *            to parse
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void parseMap(Configuration config, Map map) {
        for (Iterator<?> mapIt = new HashMap(map).keySet().iterator(); mapIt.hasNext();) {
            Object key = mapIt.next();
            Object newKey = parseValue(config, key);
            boolean isNewKey = !nullSafeEquals(key, newKey);
            Object value = map.get(key);
            Object newValue = parseValue(config, value);
            if (isNewKey) map.remove(key);
            if (isNewKey || !nullSafeEquals(newValue, value)) map.put(newKey, newValue);
        }
    }

    /**
     * Parse the given Set, exchanging its values if necessary
     * 
     * @param config
     *            configuration resolve placeholders against
     * @param set
     *            to parse
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void parseSet(Configuration config, Set set) {
        for (Iterator<?> setIt = set.iterator(); setIt.hasNext();) {
            Object elem = setIt.next();
            Object newValue = parseValue(config, elem);
            if (!nullSafeEquals(newValue, elem)) {
                set.remove(elem);
                set.add(newValue);
            }
        }
    }

    /**
     * Parse values recursively to be able to resolve cross-references between placeholder values.
     * 
     * @param config
     *            configuration resolve placeholders against
     * @param value
     *            placeholder
     * @param originalPlaceholder
     *            recursive placeholder value
     * @return parsed string
     */
    protected String parseString(Configuration config, String value, String originalPlaceholder) throws BeansException {
        int startIdx = value.indexOf(_placeholderPrefix);
        while (startIdx != -1) {
            int endIdx = value.indexOf(_placeholderSuffix, startIdx + _placeholderPrefix.length());
            if (endIdx != -1) {
                String placeholder = value.substring(startIdx + _placeholderPrefix.length(), endIdx);
                String originalPlaceholderToUse = null;

                if (originalPlaceholder != null) {
                    originalPlaceholderToUse = originalPlaceholder;
                    if (placeholder.equals(originalPlaceholder)) { throw new BeanDefinitionStoreException(
                            "Circular placeholder reference '" + placeholder + "' in property definition [" + config
                                    + "]"); }
                } else originalPlaceholderToUse = placeholder;

                String configValue = resolvePlaceholder(placeholder, config);
                if (configValue != null) {
                    configValue = parseString(config, configValue, originalPlaceholderToUse);
                    log.debug("Resolving placeholder '" + placeholder + "' to [" + configValue + "]");
                    value = value.substring(0, startIdx) + configValue + value.substring(endIdx + 1);
                    startIdx = value.indexOf(_placeholderPrefix, startIdx + configValue.length());
                } else if (_ignoreUnresolvablePlaceholders) return value;
                else throw new BeanDefinitionStoreException("Could not resolve placeholder '" + placeholder + "'");
            } else startIdx = -1;
        }
        return value;
    }

    @SuppressWarnings("rawtypes")
    protected Object parseValue(Configuration config, Object value) {
        if (value instanceof String) return parseString(config, (String) value, null);
        else if (value instanceof TypedStringValue) {
            return parseString(config, ((TypedStringValue) value).getValue(), null);
        } else if (value instanceof RuntimeBeanReference) {
            RuntimeBeanReference ref = (RuntimeBeanReference) value;
            String newBeanName = parseString(config, ref.getBeanName(), null);
            if (!(newBeanName.equals(ref.getBeanName()))) return new RuntimeBeanReference(newBeanName);
        } else if (value instanceof List) parseList(config, (List) value);
        else if (value instanceof Set) parseSet(config, (Set<?>) value);
        else if (value instanceof Map) parseMap(config, (Map<?, ?>) value);
        else if (value instanceof BeanDefinition) parseBeanDefinition(config, (BeanDefinition) value);
        else if (value instanceof BeanDefinitionHolder) {
            parseBeanDefinition(config, ((BeanDefinitionHolder) value).getBeanDefinition());
        }
        return value;
    }

    @Override
    protected void processConfiguration(ConfigurableListableBeanFactory beanFactory, Configuration config)
            throws BeansException {
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
            try {
                parseDefinition(config, definition);
            } catch (BeanDefinitionStoreException ex) {
                throw new BeanDefinitionStoreException(definition.getResourceDescription(), beanName, ex.getMessage());
            }
        }
    }

    /**
     * Resolve the given placeholder using the given configuration. Default implementation simply checks for a
     * corresponding property key.
     * Subclasses can override this for customized placeholder-to-key mappings or custom resolution strategies, possibly
     * just using the given configurations as fallback.
     * 
     * @param placeholder
     *            that maps to a configuration value
     * @param config
     *            to resolve placeholders against
     * @return configuration mapped to the placeholder
     */
    protected String resolvePlaceholder(String placeholder, Configuration config) {
        return config.getString(placeholder);
    }

    /**
     * Set whether to ignore unresolvable placeholders. Default is false: An exception will be thrown if a placeholder
     * cannot not be resolved
     * 
     * @param ignoreUnresolvablePlaceholders
     *            true = ignore unresolved placeholders
     */
    public void setIgnoreUnresolvablePlaceholder(boolean ignoreUnresolvablePlaceholders) {
        _ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
    }

    /**
     * Set the prefix that a placeholder string starts with. The default is "%{"
     * 
     * @param placeholderPrefix
     *            prefix of the placeholder
     * @see DEFAULT_PLACEHODER_PREFIX
     */
    public void setPlaceholderPrefix(String placeholderPrefix) {
        _placeholderPrefix = placeholderPrefix;
    }

    /**
     * Set the suffix that a placeholder string ends with. The default is "}"
     * 
     * @param placeholderSuffix
     *            suffix of the placeholder
     * @see DEFAULT_PLACEHODER_SUFFIX
     */
    public void setPlaceholderSuffix(String placeholderSuffix) {
        _placeholderSuffix = placeholderSuffix;
    }
}
