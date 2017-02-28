package commons.configuration.ext.matcher;

import static org.junit.Assert.*;

import org.junit.Test;

import commons.configuration.ext.matcher.HostMatcher;
import commons.configuration.ext.matcher.MachineHostMatcher;
import commons.configuration.ext.util.MachineUtils;

public class MachineHostMatcherTest {
    HostMatcher _matcher = MachineHostMatcher.instance();
    
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
