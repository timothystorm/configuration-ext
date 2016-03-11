package commons.configuration.ext.resolver;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import commons.configuration.ext.EnvironmentResolver;

public class FileEnvironmentMatcherTest {
    EnvironmentResolver _resolver = FileEnvironmentResolver.instance();

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void default_environment_attribute() throws Exception {
        // setup environment file
        File tmp = tmpFolder.newFile();
        Files.write(tmp.toPath(), "environment=L2".getBytes());

        // run resolver
        boolean resolves = _resolver.resolves("L2",
                Arrays.asList(new String[] { "file:" + tmp.getAbsolutePath() }));
        assertTrue(resolves);
    }

    @Test
    public void file_not_exist() throws Exception {
        boolean resolves = _resolver.resolves("ENVIRONMENT",
                Arrays.asList(new String[] { "file:/not/exit/noop.properties" }));
        assertFalse(resolves);
    }

    @Test
    public void no_environment_attribute() throws Exception {
        // setup environment file
        File tmp = tmpFolder.newFile();

        // run resolver
        boolean resolves = _resolver.resolves("ENVIRONMENT",
                Arrays.asList(new String[] { "file:" + tmp.getAbsolutePath() }));
        assertFalse(resolves);
    }
}
