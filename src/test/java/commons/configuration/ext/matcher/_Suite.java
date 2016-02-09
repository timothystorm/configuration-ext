package commons.configuration.ext.matcher;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ LocalHostMatcherTest.class, MachineHostMatcherTest.class, MachinePatternHostMatcherTest.class,
        MachineUtils.class })
public class _Suite {}
