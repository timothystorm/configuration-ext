package commons.configuration.ext;

import static org.junit.Assert.assertEquals;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import commons.configuration.ext.EnvConfiguration;

/**
 * Test behavior of {@link FedExConfiguration}
 */
public class EnvConfigurationTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    /**
     * Test any and all scenarios that can be thought up
     */
    @Test
    public void broadBrush() throws Exception {
        File tmpFile = tmpFolder.newFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile))) {
            writer.write(broadBrushXml());
        }

        EnvConfiguration config = new EnvConfiguration(tmpFile);
        assertEquals("good", config.getProperty("property_key"));
    }

    private String broadBrushXml() {
        StringBuilder xml = new StringBuilder();
        xml.append("<configuration>");
        { // context
            xml.append("<context>");
            { // hosts [env:0]
                xml.append("<hosts env=\"0\">");
                {
                    xml.append("<host>localhost</host>");
                }
                xml.append("</hosts>");
            }
            xml.append("</context>");
        }

        { // property
            xml.append("<property key=\"property_key\">");
            { // value [*:good]
                xml.append("<value env=\"0\">");
                xml.append("good");
                xml.append("</value>");
            }
            xml.append("</property>");
        }
        xml.append("</configuration>");

        return xml.toString();
    }

    /**
     * Let's eat our own dog food
     */
    @Test
    public void dogFood() throws Exception {
        // create new configuration
        EnvConfiguration configOut = new EnvConfiguration();
        configOut.addProperty("myKey", "myValue");

        // write configuration to file
        File tmpFile = tmpFolder.newFile();
        configOut.save(tmpFile);

        // for debugging purposes
        // System.out.println(FileUtils.readFileToString(tmpFile));

        // read in configuration
        EnvConfiguration configIn = new EnvConfiguration(tmpFile);
        assertEquals("myValue", configIn.getProperty("myKey"));
    }
}
