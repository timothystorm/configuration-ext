package commons.configuration.ext.matcher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FileExistsHostMapperTest {
    HostMatcher _matcher = FileExistsHostMapper.instance();

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void file_exists() throws Exception {
        // setup environment file
        File tmp = tmpFolder.newFile();
        assertTrue(_matcher.matches("file:" + tmp.getAbsolutePath()));
    }

    @Test
    public void file_not_exists() throws Exception {
        assertFalse(_matcher.matches("file:" + "/not/exists.env"));
    }
}
