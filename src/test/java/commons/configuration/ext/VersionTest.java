package commons.configuration.ext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class VersionTest {
    static final String APP_NAME = "configuration-ext";

    @Test
    public void buildVersion() throws Exception {
        String bv = Version.buildVersion();

        // build version is optional so only check provided in pom.xml
        if (bv != null && bv.length() > 0) assertTrue(bv.matches("[a-zA-Z]+"));
    }

    @Test
    public void builtAt() throws Exception {
        assertNotNull(Version.builtAt());
    }

    @Test
    public void builtBy() throws Exception {
        assertNotNull(Version.builtBy());
    }

    @Test
    public void maintenanceVersion() throws Exception {
        assertTrue(Version.maintenanceVersion().matches("[0-9]+"));
    }

    @Test
    public void majorVersion() throws Exception {
        assertTrue(Version.majorVersion().matches("[0-9]+"));
    }

    @Test
    public void minorVersion() throws Exception {
        assertTrue(Version.minorVersion().matches("[0-9]+"));
    }

    @Test
    public void name() throws Exception {
        assertEquals(APP_NAME, Version.getName());
    }

    @Test
    public void svuid() throws Exception {
        long svuid = Version.svuid();
        assertTrue(svuid > 0);
    }

    @Test
    public void svuid_defaultSvuid() throws Exception {
        long svuid = Version.svuid(12345L);
        assertTrue(svuid > 0);
    }

    @Test
    public void version() throws Exception {
        assertNotNull(Version.getVersion());
    }
}
