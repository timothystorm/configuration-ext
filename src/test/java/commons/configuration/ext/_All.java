package commons.configuration.ext;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ commons.configuration.ext._Suite.class, commons.configuration.ext.matcher._Suite.class,
        commons.configuration.ext.spring._Suite.class })
public class _All {}
