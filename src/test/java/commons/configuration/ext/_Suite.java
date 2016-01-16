package commons.configuration.ext;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ BasicConfigurationBuilderTest.class, ClassPathUtilsTest.class, ConfigHandler_1_0_0_Test.class,
        LocalHostMatcherTest.class, MachineHostMatcherTest.class, MachineUtilsTest.class, VersionTest.class,
        XmlConfigurationTest.class, })
public class _Suite {}
