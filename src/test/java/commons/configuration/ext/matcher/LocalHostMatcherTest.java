package commons.configuration.ext.matcher;

import static org.junit.Assert.*;

import org.junit.Test;

import commons.configuration.ext.matcher.HostMatcher;
import commons.configuration.ext.matcher.LocalHostMatcher;

public class LocalHostMatcherTest {
    HostMatcher _matcher = LocalHostMatcher.instance();
    
    @Test
    public void match() throws Exception {
        assertFalse(_matcher.matches(null));
        assertFalse(_matcher.matches(""));
        assertFalse(_matcher.matches("home"));
        assertTrue(_matcher.matches("localhost"));
        assertTrue(_matcher.matches("LOCALHOST"));
        assertTrue(_matcher.matches("127.0.0.1"));
        assertTrue(_matcher.matches("::1"));
    }
}
