package commons.configuration.ext;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ BasicConfigurationBuilderTest.class, ClassPathUtilsTest.class, EnvConfigurationTest.class,
        MachineUtilsTest.class, VersionTest.class })
public class _Suite {}
