package commons.configuration.ext;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import commons.configuration.ext.MachineUtils;

public class MachineUtilsTest {
    /**
     * Test that everything returns an answer
     */
    @Test
    public void presence() throws Exception {
        assertNotNull(MachineUtils.hostAddress());
        assertNotNull(MachineUtils.hostName());
        assertNotNull(MachineUtils.localHost());
    }
}
