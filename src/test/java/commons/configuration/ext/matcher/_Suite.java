package commons.configuration.ext.matcher;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ FileExistsHostMapperTest.class, LocalHostMatcherTest.class, MachineHostMatcherTest.class,
        MachinePatternHostMatcherTest.class, MachineUtilsTest.class })
public class _Suite {}
