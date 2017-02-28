package commons.configuration.ext.matcher;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import commons.configuration.ext.util.MachineUtilsTest;

@RunWith(Suite.class)
@SuiteClasses({ LocalHostMatcherTest.class, MachineHostMatcherTest.class, MachinePatternHostMatcherTest.class })
public class _Suite {}
