package commons.configuration.ext.spring;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;

/**
 * Allows for configuration of individual bean property values from a configuration resource. Useful
 * for custom configuration files targeted at system administrators that override bean properties configured in the
 * application context.
 * 
 * @author Timothy Storm
 */
public abstract class ConfigurationResourceConfigurer implements BeanFactoryPostProcessor, Ordered {
    private Configuration _config;

    private int _order = Integer.MAX_VALUE; // default: same as non-Ordered

    protected final Log log = LogFactory.getLog(getClass());

    @Override
    public int getOrder() {
        return _order;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        processConfiguration(beanFactory, _config);
    }

    /**
     * Apply the given Properties to the bean factory
     * 
     * @param beanFactory
     *            the bean factory used by the application context
     * @param config
     *            the configurations to apply
     * @throws BeansException
     *             in case of errors
     */
    protected abstract void processConfiguration(ConfigurableListableBeanFactory beanFactory, Configuration config)
            throws BeansException;

    public void setConfiguration(Configuration config) {
        _config = config;
    }

    public void setOrder(int order) {
        _order = order;
    }
}
