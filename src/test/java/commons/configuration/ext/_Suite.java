package commons.configuration.ext;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ClassPathUtilsTest.class, RuntimeConfigurationHandlerTest.class, VersionTest.class,
        RuntimeConfigurationTest.class, })
public class _Suite {}
