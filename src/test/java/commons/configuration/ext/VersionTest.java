package commons.configuration.ext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.junit.Test;

import commons.configuration.ext.Version;

public class VersionTest {
    static final String APP_NAME = "configuration-ext";

    @Test
    public void buildVersion() throws Exception {
        String bv = Version.buildVersion();

        // build version is optional so only check provided in pom.xml
        if (StringUtils.isNotBlank(bv)) assertTrue(StringUtils.isAlpha(bv));
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
        assertTrue(NumberUtils.isDigits(Version.maintenanceVersion()));
    }

    @Test
    public void majorVersion() throws Exception {
        assertTrue(NumberUtils.isDigits(Version.majorVersion()));
    }

    @Test
    public void minorVersion() throws Exception {
        assertTrue(NumberUtils.isDigits(Version.minorVersion()));
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
