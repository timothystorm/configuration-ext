package commons.configuration.ext.resolver;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import commons.configuration.ext.EnvironmentResolver;

public class MachinePatternHostMatcherTest {
    EnvironmentResolver _resolver = MachinePatternHostResolver.instance();

    @Test
    public void host_not_found() throws Exception {
        assertFalse(_resolver.resolves("ENVIRONMENT", Arrays.asList("/my\\.machine\\.name/")));
    }
    
    @Test
    public void not_a_pattern() throws Exception {
        // leave off the regex prefix/suffix '/'
        assertFalse(_resolver.resolves("ENVIRONMENT", Arrays.asList("invalid_pattern")));
    }

    /**
     * DON'T EVER USE THIS!!! THIS IS ONLY MEANT TO TEST
     * 
     * @throws Exception
     */
    @Test
    public void glob_pattern() throws Exception {
        assertTrue(_resolver.resolves("ENVIRONMENT", Arrays.asList("/.*/")));
    }

    @Test
    public void perfect_match() throws Exception {
        assertTrue(_resolver.resolves("ENVIRONMENT", Arrays.asList("/" + MachineUtils.hostName() + "/")));
        assertTrue(_resolver.resolves("ENVIRONMENT", Arrays.asList("/" + MachineUtils.hostAddress() + "/")));
    }
}
