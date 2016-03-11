package commons.configuration.ext.resolver;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

public class AbstractEnvironmentReslolverTest {
    AbstractEnvironmentResolver _resolver = new AbstractEnvironmentResolver() {
        @Override
        protected boolean resolves(String env, String host) {
            return true;
        }
    };

    @Test
    public void resolves_nulls() throws Exception {
        try {
            assertFalse(_resolver.resolves(null, (Collection<String>) null));
            fail();
        } catch (NullPointerException expected) {}

        try {
            assertFalse(_resolver.resolves("", (Collection<String>) null));
            fail();
        } catch (NullPointerException expected) {}

        try {
            assertFalse(_resolver.resolves(null, Arrays.asList(new String[] {})));
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test(expected = IllegalStateException.class)
    public void resolvesTo_without_resolves() throws Exception {
        _resolver.resolvesTo();
    }
}
