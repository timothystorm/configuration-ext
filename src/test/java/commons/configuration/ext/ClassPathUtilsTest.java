package commons.configuration.ext;

import static org.junit.Assert.assertNotNull;

import java.io.Reader;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ClassPathUtilsTest {
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();
    
    @Test
    public void getDefaultClassLoader() throws Exception{
        assertNotNull(ClassPathUtils.getDefaultClassLoader());
    }
    
    @Test
    public void loadResource() throws Exception{
        assertNotNull(ClassPathUtils.loadResource("version.properties"));
    }
    
    @Test
    public void readResource() throws Exception{
        try(Reader reader = ClassPathUtils.readResource("version.properties")){
            assertNotNull(reader);
        }
    }
}
