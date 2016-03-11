package commons.configuration.ext.resolver;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

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
