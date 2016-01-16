package commons.configuration.ext;

import static org.junit.Assert.*;

import org.junit.Test;

public class MachineHostMatcherTest {
    HostMatcher _matcher = MachineHostMatcher.singleton();
    
    @Test
    public void match() throws Exception {
        assertFalse(_matcher.matches(null));
        assertFalse(_matcher.matches(""));
        assertFalse(_matcher.matches("home"));
        assertFalse(_matcher.matches("localhost"));
        assertFalse(_matcher.matches("LOCALHOST"));
        assertFalse(_matcher.matches("127.0.0.1"));
        assertFalse(_matcher.matches("::1"));
        assertTrue(_matcher.matches(MachineUtils.hostAddress()));
        assertTrue(_matcher.matches(MachineUtils.hostName()));
    }
}
