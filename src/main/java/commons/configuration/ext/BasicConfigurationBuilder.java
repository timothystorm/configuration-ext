package commons.configuration.ext;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationBuilder;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;

/**
 * <p>
 * Utility to easily combine multiple {@link Configuration}s. This can be used in several ways but the result of
 * {@link #getConfiguration()} should be static or cached to prevent continuously reloading.
 * <p>
 * POJO
 * 
 * <pre>
 * public class MyClass {
 *   private static final Configuration _config;
 *   
 *   public MyClass(){
 *     BasicConfigurationBuilder builder = new BasicConfigurationBuilder();
 *     builder.setConfigurations(new SystemConfiguration());
 *     builder.setConfiguration(new PropertiesConfiguration("path/to/props.properties"));
 *     builder.setPropertiesConfiguration("file:/opt/resource/some.properties");
 *     builder.setStrategicConfiguration("classpath:envconfig.xml");
 *     _config = builder.getConfiguration();
 *   }
 *   
 *   public void myMethod(){
 *      _config.getString("propertyKey");
 *      ...
 *   }
 * }
 * </pre>
 * <p>
 * Spring
 * 
 * <pre>
 * &lt;bean id="Configuration" class="commons.configuration.ext.BasicConfigurationBuilder" init-method="getConfiguration"&gt;
 *  &lt;constructor-arg&gt;
 *      &lt;bean class="org.apache.commons.configuration.SystemConfiguration" /&gt;
 *  &lt;/constructor-arg&gt;
 *  &lt;property name="configurations"&gt;
 *    &lt;array&gt;
 *      &lt;bean class="org.apache.commons.configuration.PropertiesConfiguration"&gt;
 *        &lt;constructor-arg value="path/to/props.properties" /&gt;
 *      &lt;/bean&gt;
 *    &lt;/array&gt;
 *  &lt;/property&gt;
 *  &lt;property name="envConfiguration" value="classpath:envconfig.xml" /&gt;
 * &lt;/bean&gt;
 * 
 * &lt;bean id="MyClass"&gt;
 *   &lt;property name="config" ref="Configuration" /&gt;
 * &lt;/bean&gt;
 * 
 * public class MyClass {
 *   private Configuration _config;
 *   
 *   public void setConfig(Configuration config){
 *     _config = config;
 *   }
 *   
 *   public void myMethod(){
 *     _config.getString("propertyKey");
 *     ...
 *   }
 * }
 * </pre>
 * 
 * @author Timothy Storm
 */
public class BasicConfigurationBuilder implements ConfigurationBuilder {

    private Collection<Configuration> _configs;

    /**
     * Instances a builder without any pre-built {@link Configuration}s
     */
    public BasicConfigurationBuilder() {}

    /**
     * Instances a builder with pre-built{@link Configuration}s
     * 
     * @param configs
     *            to be added to the final {@link Configuration}
     * @see #getConfiguration()
     */
    public BasicConfigurationBuilder(Collection<Configuration> configs) {
        setConfigurations(configs);
    }

    /**
     * Instances a builder with pre-built {@link Configuration}s
     * 
     * @param configs
     *            to be added to the final {@link Configuration}
     * @see #getConfiguration()
     */
    public BasicConfigurationBuilder(Configuration... configs) {
        setConfigurations(configs);
    }

    /**
     * {@inheritDoc}
     */
    public Configuration getConfiguration() {
        return new CompositeConfiguration(getConfigurationsInternal());
    }

    /**
     * @return the internally managed {@link Configuration} collection
     */
    private Collection<Configuration> getConfigurationsInternal() {
        if (_configs == null) _configs = Collections.synchronizedCollection(new HashSet<>());
        return _configs;
    }

    /**
     * Sets additional pre-built {@link Configuration}s to be added to the final {@link Configuration}
     * configs to be added to the final {@link Configuration}
     * 
     * @param configs
     *            to be added tothe built {@link Configuration}
     * @return this builder for further setup
     * @see #getConfiguration()
     */
    public BasicConfigurationBuilder setConfigurations(Collection<Configuration> configs) {
        if (configs == null || configs.isEmpty()) return this;
        for (Configuration c : configs) {
            if (c != null) getConfigurationsInternal().add(c);
        }
        return this;
    }

    /**
     * Sets additional pre-built {@link Configuration}s to be added to the final {@link Configuration}
     * configs to be added to the final {@link Configuration}
     * 
     * @param configs
     *            to be added tothe built {@link Configuration}
     * @return this builder for further setup
     * @see #getConfiguration()
     */
    public BasicConfigurationBuilder setConfigurations(Configuration... configs) {
        if (configs == null || configs.length <= 0) return this;
        return setConfigurations(Arrays.asList(configs));
    }

    /**
     * utility method for adding a {@link StrategicConfiguration} using resource resolution similar to spring.
     * 
     * <pre>
     * builder.setPropertiesConfiguration("envconfig.xml");
     * builder.setPropertiesConfiguration("path/to/envconfig.xml");
     * builder.setPropertiesConfiguration("classpath:envconfig.xml");
     * builder.setPropertiesConfiguration("file:/path/to/envconfig.xml");
     * </pre>
     * 
     * @param configLocation
     *            location of the environment configuration file
     * @return this builder for further setup
     * @see #getConfiguration()
     */
    public BasicConfigurationBuilder setStrategicConfiguration(String configLocation) {
        try {
            File envFile = ResourceUtils.getFile(configLocation);
            return setConfigurations(new RuntimeConfiguration(envFile));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * utility method for adding a {@link PropertiesConfiguration} using resource resolution similar to spring.
     * 
     * <pre>
     * builder.setPropertiesConfiguration("fileName.properties");
     * builder.setPropertiesConfiguration("path/to/fileName.properties");
     * builder.setPropertiesConfiguration("classpath:fileName.properties");
     * builder.setPropertiesConfiguration("file:/path/to/fileName.properties");
     * </pre>
     * 
     * @param propertiesLocation
     *            location of the properties file
     * @return this builder for further setup
     * @see #getConfiguration()
     */
    public BasicConfigurationBuilder setPropertiesConfiguration(String propertiesLocation) {
        try {
            File propFile = ResourceUtils.getFile(propertiesLocation);
            return setConfigurations(new PropertiesConfiguration(propFile));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public BasicConfigurationBuilder useSystemProperties(){
        return setConfigurations(new SystemConfiguration());
    }
}
