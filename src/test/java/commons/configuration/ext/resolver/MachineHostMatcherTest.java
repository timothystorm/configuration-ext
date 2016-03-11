package commons.configuration.ext.resolver;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import commons.configuration.ext.EnvironmentResolver;

public class MachineHostMatcherTest {
    EnvironmentResolver _resolver = MachineHostResolver.instance();

    @Test
    public void resolves() throws Exception {
        assertFalse(_resolver.resolves("ENVIRONMENT", Arrays.asList("localhost")));
        assertFalse(_resolver.resolves("ENVIRONMENT", Arrays.asList("127.00.1")));
        assertFalse(_resolver.resolves("ENVIRONMENT", Arrays.asList("::1")));
        assertTrue(_resolver.resolves("ENVIRONMENT", Arrays.asList(MachineUtils.hostName())));
        assertTrue(_resolver.resolves("ENVIRONMENT", Arrays.asList(MachineUtils.hostAddress())));
    }
}
