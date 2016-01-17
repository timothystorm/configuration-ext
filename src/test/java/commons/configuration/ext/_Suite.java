package commons.configuration.ext;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ BasicConfigurationBuilderTest.class, ClassPathUtilsTest.class, RuntimeConfigurationHandlerTest.class,
        LocalHostMatcherTest.class, MachineHostMatcherTest.class, MachineUtilsTest.class,
        MachinePatternHostMatcherTest.class, VersionTest.class, RuntimeConfigurationTest.class, })
public class _Suite {}
