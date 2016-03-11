package commons.configuration.ext.resolver;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ AbstractEnvironmentReslolverTest.class, FileEnvironmentMatcherTest.class,
        MachineHostMatcherTest.class, MachinePatternHostMatcherTest.class, MachineUtilsTest.class })
public class _Suite {}
