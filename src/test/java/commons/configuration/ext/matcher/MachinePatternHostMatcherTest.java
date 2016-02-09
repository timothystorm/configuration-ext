package commons.configuration.ext.matcher;

import static org.junit.Assert.*;

import org.junit.Test;

import commons.configuration.ext.matcher.HostMatcher;
import commons.configuration.ext.matcher.MachinePatternHostMatcher;

public class MachinePatternHostMatcherTest {
    HostMatcher _matcher = MachinePatternHostMatcher.singleton();
    
    @Test
    public void match() throws Exception {
        assertFalse(_matcher.matches(null));
        assertFalse(_matcher.matches(""));
        assertFalse(_matcher.matches("localhost"));
        assertFalse(_matcher.matches("127.0.0.1"));
        assertFalse(_matcher.matches("::1"));
        
        // ip pattern
        assertFalse(_matcher.matches("/[0-9.]{1,3}[0-9.]{1,3}[0-9.]{1,3}[0-9]{1,3}/"));
        
        // computer name pattern
        assertTrue(_matcher.matches("/[a-zA-Z0-9\\-.]+/"));
    }
}
