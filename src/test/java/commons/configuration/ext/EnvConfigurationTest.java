package commons.configuration.ext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
    public void anyAndAll() throws Exception {
        File tmpFile = tmpFolder.newFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile))) {
            writer.write(anyAndAllXml());
        }

        EnvConfiguration config = new EnvConfiguration(tmpFile);
        assertNull(config.getString("none_key"));
        assertEquals("", config.getString("empty_key"));
        assertEquals("good", config.getString("txt_key"));
        assertEquals("<custom><xml/></custom>", config.getString("cdata_key"));
    }

    private String anyAndAllXml() {
        StringBuilder xml = new StringBuilder();
        xml.append("<configuration>");
        {
            xml.append("<context>");
            {
                xml.append("<hosts env=\"0\">");
                {
                    xml.append("<host>localhost</host>");
                }
                xml.append("</hosts>");
            }
            xml.append("</context>");
        }

        { // empty property
            xml.append("<property key=\"empty_key\">");
            {
                xml.append("<value env=\"0\" />");
            }
            xml.append("</property>");
        }

        { // text property
            xml.append("<property key=\"txt_key\">");
            {
                xml.append("<value env=\"0\">");
                xml.append("good");
                xml.append("</value>");
            }
            xml.append("</property>");
        }

        {// cdata property
            xml.append("<property key=\"cdata_key\">");
            {
                xml.append("<value env=\"0\">");
                xml.append("<![CDATA[<custom><xml/></custom>]]>");
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
        int bigConfig = 1000;
        
        // create new configuration
        EnvConfiguration configOut = new EnvConfiguration();
        for(int i = 0; i < bigConfig; i++){
            configOut.addProperty("key" + i, "value" + i);
        }

        // write configuration to file
        File tmpFile = tmpFolder.newFile();
        configOut.save(tmpFile);

        // for debugging purposes
        // System.out.println(FileUtils.readFileToString(tmpFile));

        // read in configuration
        EnvConfiguration configIn = new EnvConfiguration(tmpFile);
        for(int i = 0; i < bigConfig; i++){
            assertEquals("value" + i, configIn.getProperty("key" + i));
        }
    }
}
