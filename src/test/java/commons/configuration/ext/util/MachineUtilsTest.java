package commons.configuration.ext.util;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import commons.configuration.ext.util.MachineUtils;

public class MachineUtilsTest {
    /**
     * Test that everything returns an answer
     */
    @Test
    public void presence() throws Exception {
        assertNotNull(MachineUtils.hostAddress());
        assertNotNull(MachineUtils.hostName());
        assertNotNull(MachineUtils.fqdn());
    }
}
