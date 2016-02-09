package commons.configuration.ext.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.FactoryBean;

/**
 * <p>
 * Factory that aids in the creation of a spring injected {@link Configuration}s
 * </p>
 * 
 * <pre>
 * &lt;bean id="AppConfig" class="commons.configuration.ext.spring.ConfigurationFactory"&gt;
 *   &lt;property name="configurations"&gt;
 *    &lt;list&gt;
 *      &lt;value&gt;&lt;bean class="MyConfiguration" /&gt;&lt;/value&gt;
 *      &lt;value&gt;
 *        &lt;bean class="commons.configuration.ext.RuntimeConfiguration"&gt;
 *            &lt;constructor-arg value="path/to/config/file" /&gt;
 *        &lt;/bean&gt;
 *      &lt;/value&gt;
 *    &lt;/list&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 * 
 * &lt;bean id="MyClassThatNeedsConfigs" class="a.b.c.MyClass"&gt;
 *   &lt;property name="configuration" ref="AppConfig" /&gt;
 * &lt;/bean&gt;
 * </pre>
 * 
 * @author Timothy Storm
 */
public class ConfigurationFactory implements FactoryBean<Configuration> {

    /**
     * configuration to be added into a single {@link CompositeConfiguration}. Do not access directly use
     * {@link #getConfigurationsInternal()} instead to avoid NPE.
     */
    private Collection<Configuration> _configs;

    public ConfigurationFactory() {}

    /**
     * Create a {@link Configuration} factory of the provided {@link Configuration}s
     * 
     * @param configs
     *            to be added to the build {@link Configuration}
     */
    public ConfigurationFactory(Configuration... configs) {
        setConfigurations(configs);
    }

    /**
     * Create a {@link Configuration} factory of the provided {@link Configuration}s
     * 
     * @param configs
     *            to be added to the build {@link Configuration}
     */
    public ConfigurationFactory(Collection<Configuration> configs) {
        setConfigurations(configs);
    }

    /**
     * Sets the {@link Configuration}s to be used to built {@link Configuration}
     * 
     * @param configs
     *            to be added to the built {@link Configuration}
     * @see #getObject()
     */
    public void setConfigurations(Configuration... configs) {
        if (configs == null) throw new NullPointerException("configs required!");
        setConfigurations(Arrays.asList(configs));
    }

    /**
     * Sets the {@link Configuration}s to be used to built {@link Configuration}
     * 
     * @param configs
     *            to be added to the built {@link Configuration}
     * @see #getObject()
     */
    public void setConfigurations(Collection<Configuration> configs) {
        if (configs == null) throw new NullPointerException("configs required!");
        Collection<Configuration> internalConfigs = getConfigurationsInternal();
        internalConfigs.clear();
        internalConfigs.addAll(configs);
    }

    protected Collection<Configuration> getConfigurationsInternal() {
        if (_configs == null) _configs = new ArrayList<>();
        return _configs;
    }

    /**
     * {@inheritDoc}
     * Builds a {@link CompositeConfiguration} that includes all of the provided {@link Configuration}s
     */
    @Override
    public Configuration getObject() throws Exception {
        return new CompositeConfiguration(getConfigurationsInternal());
    }

    @Override
    public Class<?> getObjectType() {
        return Configuration.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
