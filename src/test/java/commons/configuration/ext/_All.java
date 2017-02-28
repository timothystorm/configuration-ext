package commons.configuration.ext;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import commons.configuration.ext.util.MachineUtilsTest;

@RunWith(Suite.class)
@SuiteClasses({ commons.configuration.ext._Suite.class, commons.configuration.ext.matcher._Suite.class,
        commons.configuration.ext.spring._Suite.class, MachineUtilsTest.class })
public class _All {}
