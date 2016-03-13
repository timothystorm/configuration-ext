package commons.configuration.ext.resolver;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

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
        boolean resolves = _resolver.resolves("L2", collect("file:" + tmp.getAbsolutePath()));
        assertTrue(resolves);
    }

    @Test
    public void custom_environment_attribute() throws Exception {
        // setup environment file
        File tmp = tmpFolder.newFile();
        Files.write(tmp.toPath(), "int=L2\n".getBytes(), StandardOpenOption.APPEND);

        boolean l2 = _resolver.resolves("L2", collect("file:" + tmp.getAbsolutePath() + "@int"));
        assertTrue("Expected to resolve to L2", l2);

        boolean l7 = _resolver.resolves("L7", collect("file:" + tmp.getAbsolutePath() + "@int"));
        assertFalse("Expected to resolve to L2 not L7", l7);
    }

    @Test
    public void file_not_exist() throws Exception {
        boolean resolves = _resolver.resolves("ENVIRONMENT", collect("file:/not/exit/noop.properties"));
        assertFalse(resolves);
    }

    @Test
    public void no_environment_attribute() throws Exception {
        // setup environment file
        File tmp = tmpFolder.newFile();

        // run resolver
        boolean resolves = _resolver.resolves("ENVIRONMENT", collect("file:" + tmp.getAbsolutePath()));
        assertFalse(resolves);
    }

    private Collection<String> collect(String... args) {
        if (args == null) return Collections.emptyList();
        return Arrays.asList(args);
    }
}
